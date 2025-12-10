package com.example.myapplication_firebase;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Commentaire> commentairesList;
    private Context context;
    private FirebaseFirestore db;

    public CommentAdapter(List<Commentaire> commentairesList) {
        this.commentairesList = commentairesList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setCommentaires(List<Commentaire> newCommentairesList) {
        this.commentairesList = newCommentairesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Commentaire commentaire = commentairesList.get(position);

        String userId = commentaire.getUserId();

        // Définir un nom temporaire en attendant la réponse asynchrone
        holder.textUserId.setText("Chargement...");

        // Récupérer le nom d'utilisateur depuis la collection 'utilisateurs'
        if (userId != null) {
            db.collection("utilisateurs")
                    .document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // CORRECTION: Utilisation du champ "nom"
                        String displayName = documentSnapshot.getString("nom");

                        if (displayName != null && !displayName.isEmpty()) {
                            holder.textUserId.setText(displayName);
                        } else {
                            // Nom de secours si le champ 'nom' n'est pas rempli
                            String shortId = userId.substring(0, Math.min(userId.length(), 8)) + "...";
                            holder.textUserId.setText("Utilisateur: " + shortId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // En cas d'échec, afficher l'ID tronqué
                        String shortId = userId.substring(0, Math.min(userId.length(), 8)) + "...";
                        holder.textUserId.setText("Utilisateur: " + shortId + " (Erreur)");
                        Log.e("CommentAdapter", "Erreur de chargement du profil pour ID: " + userId, e);
                    });
        } else {
            holder.textUserId.setText("Utilisateur Inconnu");
        }


        // Affichage du texte
        holder.textCommentText.setText(commentaire.getTexte());

        // Affichage de la note
        holder.commentRatingBar.setRating(commentaire.getNote());

        // Affichage de la date
        if (commentaire.getTimestamp() != null) {
            String date = DateFormat.format("dd MMM yyyy", commentaire.getTimestamp().toDate()).toString();
            holder.textCommentDate.setText(date);
        } else {
            holder.textCommentDate.setText("Date indisponible");
        }
    }

    @Override
    public int getItemCount() {
        return commentairesList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView textUserId;
        public RatingBar commentRatingBar;
        public TextView textCommentText;
        public TextView textCommentDate;

        public CommentViewHolder(View itemView) {
            super(itemView);
            textUserId = itemView.findViewById(R.id.text_user_id);
            commentRatingBar = itemView.findViewById(R.id.comment_rating_bar);
            textCommentText = itemView.findViewById(R.id.text_comment_text);
            textCommentDate = itemView.findViewById(R.id.text_comment_date);
        }
    }
}