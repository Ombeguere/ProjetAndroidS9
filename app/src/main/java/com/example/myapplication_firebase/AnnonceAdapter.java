package com.example.myapplication_firebase;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private List<Annonce> annonces;
    private Context context;
    private FavoriteDbHelper dbHelper;
    private FirebaseAuth mAuth;

    public AnnonceAdapter(List<Annonce> annonces, Context context) {
        this.annonces = annonces;
        this.context = context;
        this.dbHelper = new FavoriteDbHelper(context);
        this.mAuth = FirebaseAuth.getInstance();
    }

    public static class AnnonceViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageAnnonce;
        public TextView textAdresse, textDescription, textSuperficie, textPieces;
        public RatingBar ratingBar;
        public Button favoriteBtn;

        public AnnonceViewHolder(View itemView) {
            super(itemView);
            imageAnnonce = itemView.findViewById(R.id.image_annonce);
            textAdresse = itemView.findViewById(R.id.text_adresse);
            textDescription = itemView.findViewById(R.id.text_description);
            textSuperficie = itemView.findViewById(R.id.text_superficie);
            textPieces = itemView.findViewById(R.id.text_pieces);
            ratingBar = itemView.findViewById(R.id.rating_bar_moyenne);
            favoriteBtn = itemView.findViewById(R.id.favorite_btn);
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
        Annonce annonce = annonces.get(position);
        String annonceId = annonce.getDocumentId();

        holder.textAdresse.setText(annonce.getAdresse());
        holder.textDescription.setText(annonce.getDescription());
        holder.textSuperficie.setText(annonce.getSuperficie() + " m²");
        holder.textPieces.setText(annonce.getPieces() + " pièces");
        holder.ratingBar.setRating(annonce.getNoteMoyenne());

        // Redirection vers RatingActivity au clic sur la RatingBar
        holder.ratingBar.setOnClickListener(v -> {
            Intent intent = new Intent(context, RatingActivity.class);
            intent.putExtra("ANNONCE_ID", annonceId);
            intent.putExtra("ANNONCE_ADRESSE", annonce.getAdresse());
            context.startActivity(intent);
        });

        // Gestion des favoris
        boolean isFavorite = dbHelper.isFavorite(annonceId);
        holder.favoriteBtn.setText(isFavorite ? "Retirer des Favoris" : "Ajouter aux Favoris");

        holder.favoriteBtn.setOnClickListener(v -> toggleFavoriteStatus(annonceId, holder.favoriteBtn));
    }

    private void toggleFavoriteStatus(String annonceId, Button button) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(context, "Veuillez vous connecter pour gérer les favoris.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.isFavorite(annonceId)) {
            dbHelper.removeFavorite(annonceId);
            button.setText("Ajouter aux Favoris");
            Toast.makeText(context, "Retiré des favoris.", Toast.LENGTH_SHORT).show();
        } else {
            boolean success = dbHelper.addFavorite(annonceId, userId);
            if (success) {
                button.setText("Retirer des Favoris");
                Toast.makeText(context, "Ajouté aux favoris.", Toast.LENGTH_SHORT).show();
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
