package com.example.myapplication_firebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // NOUVEAU
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast; // NOUVEAU
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // NÉCESSAIRE pour les images
import com.google.firebase.auth.FirebaseAuth; // NÉCESSAIRE pour l'UID utilisateur
import java.util.List;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private List<Annonce> annonces;
    private Context context;
    private FavoriteDbHelper dbHelper; // 🚨 NOUVEAU : POUR FAVORIS HORS LIGNE (SQLite)
    private FirebaseAuth mAuth; // 🚨 NOUVEAU : POUR RÉCUPÉRER L'UTILISATEUR

    public AnnonceAdapter(List<Annonce> annonces, Context context) {
        this.annonces = annonces;
        this.context = context;
        this.dbHelper = new FavoriteDbHelper(context); // Initialisation de SQLite
        this.mAuth = FirebaseAuth.getInstance(); // Initialisation de Firebase Auth
    }

    public static class AnnonceViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageAnnonce;
        public TextView textAdresse;
        public TextView textDescription;
        public RatingBar ratingBar;
        public TextView textSuperficie;
        public TextView textPieces;
        public ImageButton favoriteBtn; // Gardé pour l'ID, mais masqué dans le XML
        public Button rateBtn; // 🚨 NOUVEAU : Utilisé comme bouton "Favoris"

        public AnnonceViewHolder(View itemView) {
            super(itemView);
            imageAnnonce = itemView.findViewById(R.id.image_annonce);
            textAdresse = itemView.findViewById(R.id.text_adresse);
            textDescription = itemView.findViewById(R.id.text_description);
            ratingBar = itemView.findViewById(R.id.rating_bar_moyenne);
            textSuperficie = itemView.findViewById(R.id.text_superficie);
            textPieces = itemView.findViewById(R.id.text_pieces);
            favoriteBtn = itemView.findViewById(R.id.favorite_btn);
            rateBtn = itemView.findViewById(R.id.rate_btn); // NOUVEAU
        }
    }

    @NonNull
    @Override
    public AnnonceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_annonce, parent, false);
        return new AnnonceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnonceViewHolder holder, int position) {
        final Annonce annonce = annonces.get(position);
        final String annonceId = annonce.getDocumentId();

        holder.textAdresse.setText(annonce.getAdresse());
        holder.textDescription.setText(annonce.getDescription());
        holder.textSuperficie.setText(annonce.getSuperficie() + " m²");
        holder.textPieces.setText(annonce.getPieces() + " pièces");
        holder.ratingBar.setRating(annonce.getNoteMoyenne());

        // 🖼️ Chargement de l'image (maintenant activé)
        if (annonce.getImageUrl() != null && !annonce.getImageUrl().isEmpty()) {
            Glide.with(context).load(annonce.getImageUrl()).into(holder.imageAnnonce);
        } // Pas de 'else' pour ne pas écraser l'image si elle n'existe pas

        // ❤️ GESTION DES FAVORIS AVEC LE BOUTON TEXTE (rate_btn)
        boolean isFavorite = dbHelper.isFavorite(annonceId);

        if (isFavorite) {
            holder.rateBtn.setText("Retirer des Favoris");
        } else {
            // Le bouton affichera "Favoris" par défaut, ou on peut le rendre plus explicite:
            holder.rateBtn.setText("Ajouter aux Favoris");
        }

        // Le bouton rate_btn gère désormais la logique des favoris.
        holder.rateBtn.setOnClickListener(v -> {
            // Note: On passe le bouton texte au lieu de l'ImageButton
            toggleFavoriteStatus(annonceId, holder.rateBtn);
        });

        // Clic sur l'élément complet pour la vue détaillée (inchangé)
        holder.itemView.setOnClickListener(v -> {
            // Logique pour ouvrir la vue détaillée de l'annonce
        });
    }

    // Méthode modifiée pour accepter un 'Button' et changer son texte.
    private void toggleFavoriteStatus(String annonceId, Button button) {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(context, "Veuillez vous connecter pour gérer les favoris.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isFavorite(annonceId)) {
            // Retirer des favoris
            dbHelper.removeFavorite(annonceId);
            button.setText("Ajouter aux Favoris"); // 🚨 Mise à jour du texte
            Toast.makeText(context, "Retiré des favoris.", Toast.LENGTH_SHORT).show();
        } else {
            // Ajouter aux favoris
            boolean success = dbHelper.addFavorite(annonceId, currentUserId);
            if (success) {
                button.setText("Retirer des Favoris"); // 🚨 Mise à jour du texte
                Toast.makeText(context, "Ajouté aux favoris (Hors Ligne).", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Erreur lors de l'ajout aux favoris.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return annonces.size();
    }
}