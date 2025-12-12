package com.example.myapplication_firebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
public class Connexion extends AppCompatActivity implements View.OnClickListener {

    private EditText emailEditText, mdpEditText;
    private Button connexionButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connexion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true) // Ceci active le cache local de Firestore
                    .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        } catch (Exception e) {
            Log.e("FirebaseInit", "Erreur lors de l'activation de la persistance Firestore", e);
        }

        emailEditText = findViewById(R.id.emailLogin);
        mdpEditText = findViewById(R.id.mdpLogin);
        connexionButton = findViewById(R.id.connexionBtn);
        connexionButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {

        if(v == connexionButton) {

            String email = emailEditText.getText().toString().trim();
            String mdp = mdpEditText.getText().toString().trim();

            if (email.isEmpty() || mdp.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, mdp).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();


                        Intent intent = new Intent(this, ApresConnexion.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // Vérifie si un utilisateur est déjà connecté
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Redirection automatique si déjà connecté
            Intent intent = new Intent(this, ApresConnexion.class);
            startActivity(intent);
            finish();
        }
    }
}