package com.example.myapplication_firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView; // Ajout de l'import pour TextView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // Ajout de l'import Firestore
import com.google.firebase.firestore.DocumentReference;

import android.view.View;

public class ApresConnexion extends AppCompatActivity implements View.OnClickListener {
    private Button logoutButton;
    private Button profilButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Déclaration de Firestore
    private TextView welcomeTextView; // Déclaration du TextView
    private Button addLogementBtn;
    private Button viewLogementsBtn;
    private Button viewFavoritesBtn;
    private Button viewOnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apres_connexion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialisation de Firestore

        logoutButton = findViewById(R.id.logoutBtn);
        profilButton = findViewById(R.id.profilBtn);
        addLogementBtn = findViewById(R.id.addLogementBtn);
        viewLogementsBtn = findViewById(R.id.viewLogementsBtn);
        viewFavoritesBtn = findViewById(R.id.viewFavoritesBtn);
        viewOnMap = findViewById(R.id.viewMapBtn);
        welcomeTextView = findViewById(R.id.welcomeText); // Initialisation du TextView

        logoutButton.setOnClickListener(this);
        profilButton.setOnClickListener(this);
        addLogementBtn.setOnClickListener(this);
        viewLogementsBtn.setOnClickListener(this);
        viewFavoritesBtn.setOnClickListener(this);
        viewOnMap.setOnClickListener(this);

        // Appel de la nouvelle méthode pour charger le nom
        displayWelcomeMessage();
    }

    /**
     * Charge le nom de l'utilisateur depuis Firestore et affiche le message de bienvenue.
     */
    private void displayWelcomeMessage() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            DocumentReference userRef = db.collection("utilisateurs").document(uid);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String prenom = documentSnapshot.getString("prenom");
                    if (prenom != null && !prenom.isEmpty()) {
                        welcomeTextView.setText("Bonjour " + prenom);
                    } else {
                        welcomeTextView.setText("Bonjour !");
                    }
                } else {
                    welcomeTextView.setText("Bienvenue !");
                }
            }).addOnFailureListener(e -> {
                Log.e("ApresConnexion", "Erreur lors du chargement du profil: " + e.getMessage());
                welcomeTextView.setText("Bonjour !");
            });
        } else {
            // Ne devrait pas arriver dans ApresConnexion, mais bonne pratique de gestion d'erreur.
            welcomeTextView.setText("Bienvenue !");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == logoutButton) {
            // Déconnexion
            auth.signOut();
            Intent intent = new Intent(this, Connexion.class);
            startActivity(intent);
            finish();
        } else if (v == profilButton) {
            // Redirection vers la page de profil
            Intent intent = new Intent(this, Profil.class);
            startActivity(intent);
        }
        else if (v == addLogementBtn) {
            // Redirection vers la page de profil
            Intent intent = new Intent(this, AjoutAnnonce.class);
            startActivity(intent);
        }
        else if (v == viewLogementsBtn) {
            // Redirection vers la page des annonces
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }
        else if (v == viewFavoritesBtn) {
            // Redirection vers la page des favoris
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
        }else if (v == viewOnMap) {
            // Redirection vers la page de map
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }
    }
}