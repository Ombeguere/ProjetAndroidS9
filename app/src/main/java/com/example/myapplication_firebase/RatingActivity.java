package com.example.myapplication_firebase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {

    private String annonceId;
    private String annonceAdresse;
    private RatingBar ratingInput;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Connexion requise pour noter.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 1. Récupération des données de l'Intent
        annonceId = getIntent().getStringExtra("ANNONCE_ID");
        annonceAdresse = getIntent().getStringExtra("ANNONCE_ADRESSE");

        // 2. Initialisation des vues
        TextView textAdresse = findViewById(R.id.text_annonce_adresse);
        ratingInput = findViewById(R.id.rating_input);
        Button btnSubmit = findViewById(R.id.btn_submit_rating);

        textAdresse.setText(annonceAdresse);

        // 3. Logique de soumission
        btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        if (ratingInput.getRating() == 0) {
            Toast.makeText(this, "Veuillez donner une note.", Toast.LENGTH_SHORT).show();
            return;
        }

        float note = ratingInput.getRating();
        String userId = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Stocker l'avis dans une sous-collection de l'annonce
        db.collection("annonces")
                .document(annonceId)
                .collection("avis")
                .document(userId) // Utiliser l'UID comme ID de document pour écraser l'ancienne note
                .set(createAvisMap(userId, note, annonceId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Note soumise. Mise à jour de la moyenne en cours...", Toast.LENGTH_LONG).show();
                    // 🚨 Étape suivante: La note moyenne doit être recalculée (voir point 3 ci-dessous)
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la soumission.", Toast.LENGTH_SHORT).show();
                });
    }

    // Utiliser une Map pour la compatibilité Firestore
    private Map<String, Object> createAvisMap(String userId, float note, String annonceId) {
        Map<String, Object> avisMap = new HashMap<>();
        avisMap.put("userId", userId);
        avisMap.put("note", note);
        avisMap.put("annonceId", annonceId);
        return avisMap;
    }
}