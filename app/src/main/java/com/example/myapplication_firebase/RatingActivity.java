package com.example.myapplication_firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity implements View.OnClickListener {

    private String annonceId;
    private String annonceAdresse;
    private RatingBar ratingInput;
    private EditText commentInput;
    private FirebaseUser user;
    private Button submitButton;
    private Button btnBack;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        db = FirebaseFirestore.getInstance();

        TextView textAdresse = findViewById(R.id.text_annonce_adresse);
        ratingInput = findViewById(R.id.rating_input);
        commentInput = findViewById(R.id.comment_input);
        submitButton = findViewById(R.id.btn_submit_rating);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Connexion requise pour noter.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        annonceId = getIntent().getStringExtra("ANNONCE_ID");
        annonceAdresse = getIntent().getStringExtra("ANNONCE_ADRESSE");

        textAdresse.setText(annonceAdresse);

        submitButton.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        float note = ratingInput.getRating();
        String commentaire = commentInput.getText().toString().trim();

        if (note == 0) {
            Toast.makeText(this, "Veuillez donner une note.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        Map<String, Object> avisMap = new HashMap<>();
        avisMap.put("userID", userId);
        avisMap.put("note", note);
        avisMap.put("commentaire", commentaire);
        avisMap.put("annonceId", annonceId);
        avisMap.put("timestamp", FieldValue.serverTimestamp());

        db.collection("annonce")
                .document(annonceId)
                .collection("avis")
                .add(avisMap) // Utilisation de .add() pour créer un nouveau document et permettre plusieurs avis
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Note et commentaire soumis.", Toast.LENGTH_SHORT).show();
                    recalculeMoyenne();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de la soumission.", Toast.LENGTH_SHORT).show()
                );
    }
    private void recalculeMoyenne() {
        CollectionReference avisRef = db.collection("annonce")
                .document(annonceId)
                .collection("avis");

        avisRef.get().addOnSuccessListener(querySnapshot -> {
            float somme = 0;
            int count = 0;

            // Si vous choisissez de ne compter qu'un seul avis par utilisateur pour la moyenne,
            // vous devriez modifier cette logique pour utiliser un Map<String, Double> pour
            // stocker la dernière note de chaque userID avant de calculer la somme.
            // Pour l'instant, nous conservons la logique qui inclut toutes les notes.

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Double note = document.getDouble("note");
                if (note != null) {
                    somme += note;
                    count++;
                }
            }

            float moyenne = count > 0 ? somme / count : 0;

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("noteMoyenne", moyenne);

            db.collection("annonce")
                    .document(annonceId)
                    .update(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note moyenne mise à jour: " + String.format("%.1f", moyenne), Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur mise à jour moyenne.", Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(this, "Erreur récupération avis.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            finish();
        }
    }
}