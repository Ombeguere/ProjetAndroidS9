package com.example.myapplication_firebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import android.view.View;

public class ApresConnexion extends AppCompatActivity implements View.OnClickListener {
    private Button logoutButton;
    private Button profilButton;
    private FirebaseAuth auth;
    private Button addLogementBtn;
    private Button viewLogementsBtn;
    private Button viewFavoritesBtn;

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
        logoutButton = findViewById(R.id.logoutBtn);
        profilButton = findViewById(R.id.profilBtn);
        addLogementBtn = findViewById(R.id.addLogementBtn);
        viewLogementsBtn = findViewById(R.id.viewLogementsBtn);
        viewFavoritesBtn = findViewById(R.id.viewFavoritesBtn);

        logoutButton.setOnClickListener(this);
        profilButton.setOnClickListener(this);
        addLogementBtn.setOnClickListener(this);
        viewLogementsBtn.setOnClickListener(this);
        viewFavoritesBtn.setOnClickListener(this);
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
        }
    }
}