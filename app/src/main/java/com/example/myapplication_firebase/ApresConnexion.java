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

        logoutButton.setOnClickListener(this);
        profilButton.setOnClickListener(this);
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
    }
}