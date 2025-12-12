package com.example.myapplication_firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String annonceId);
    }

    private List<Annonce> annonces;
    private Context context;
    private OnItemClickListener listener;
    private FavoriteDbHelper dbHelper;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AnnonceAdapter(List<Annonce> annonces, Context context, OnItemClickListener listener) {
        this.annonces = annonces;
        this.context = context;
        this.listener = listener;
        this.dbHelper = new FavoriteDbHelper(context);
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull AnnonceViewHolder holder, int position) {
        Annonce annonce = annonces.get(position);
        final String annonceId = annonce.getDocumentId();

        holder.textAdresse.setText(annonce.getTitre());

        holder.textDescription.setText(annonce.getDescription());
        holder.textSuperficie.setText(annonce.getSuperficie() != null ? annonce.getSuperficie() + " m²" : "N/A");
        holder.textPieces.setText(annonce.getPieces() != null ? annonce.getPieces() + " pièces" : "N/A");
        holder.ratingBar.setRating(annonce.getNoteMoyenne());

        String base64Image = annonce.getImageUrlBase64();
        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap imageBitmap = decodeBase64ToBitmap(base64Image);
            if (imageBitmap != null) {
                holder.imageAnnonce.setImageBitmap(imageBitmap);
            } else {
                holder.imageAnnonce.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.imageAnnonce.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(annonceId);
            }
        });

        holder.ratingBar.setOnTouchListener((v, event) -> {
            Intent intent = new Intent(context, RatingActivity.class);
            intent.putExtra("ANNONCE_ID", annonceId);
            intent.putExtra("ANNONCE_ADRESSE", annonce.getAdresse()); // Conserver l'adresse pour RatingActivity si nécessaire
            context.startActivity(intent);
            return true;
        });

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

        String favoritePath = "utilisateurs/" + userId + "/favorits";

        if (dbHelper.isFavorite(annonceId)) {
            dbHelper.removeFavorite(annonceId);

            db.collection(favoritePath).document(annonceId).delete()
                    .addOnSuccessListener(aVoid -> {
                        button.setText("Ajouter aux Favoris");
                        Toast.makeText(context, "Retiré des favoris (Local et Cloud).", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erreur suppression Cloud.", Toast.LENGTH_SHORT).show();
                    });

        } else {
            boolean success = dbHelper.addFavorite(annonceId, userId);

            if (success) {
                Map<String, Object> favoriteData = new HashMap<>();
                favoriteData.put("annonceId", annonceId);
                favoriteData.put("timestamp", FieldValue.serverTimestamp());

                db.collection(favoritePath).document(annonceId).set(favoriteData)
                        .addOnSuccessListener(aVoid -> {
                            button.setText("Retirer des Favoris");
                            Toast.makeText(context, "Ajouté aux favoris (Local et Cloud).", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Erreur d'ajout Cloud.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(context, "Erreur lors de l'ajout aux favoris localement.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e("Base64Decode", "Chaîne Base64 invalide ou tronquée: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return annonces.size();
    }
}