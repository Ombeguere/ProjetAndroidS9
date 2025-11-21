package com.example.myapplication_firebase;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profil extends AppCompatActivity implements View.OnClickListener{

    private EditText nomEdit, prenomEdit, emailEdit, mdpEdit;
    private ImageView profileImage;
    private Button saveBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    private Uri imageUri;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nomEdit = findViewById(R.id.nomEdit);
        prenomEdit = findViewById(R.id.prenomEdit);
        emailEdit = findViewById(R.id.emailEdit);
        mdpEdit = findViewById(R.id.mdpEdit);
        profileImage = findViewById(R.id.profileImage);
        saveBtn = findViewById(R.id.saveBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        saveBtn.setOnClickListener(this);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Pas connecté, revenir en arrière
            finish();
            return;
        }
        uid = currentUser.getUid();

        // Charger les valeurs actuelles depuis Firestore
        DocumentReference userRef = db.collection("utilisateurs").document(uid);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                nomEdit.setText(documentSnapshot.getString("nom"));
                prenomEdit.setText(documentSnapshot.getString("prenom"));
                emailEdit.setText(documentSnapshot.getString("email"));
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {
            updateProfile();
        }
    }

    public void updateProfile() {
        DocumentReference user = db.collection("utilisateurs").document(uid);

        String nom = nomEdit.getText().toString();
        String prenom = prenomEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String mdp = mdpEdit.getText().toString(); // mot de passe non pré-rempli

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mise à jour Firestore
        user.update("nom", nom, "prenom", prenom, "email", email)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show());

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            // Mise à jour email Auth
            if (!firebaseUser.getEmail().equals(email)) {
                firebaseUser.updateEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email Auth mis à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur email Auth : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            // Mise à jour mot de passe si non vide
            if (!mdp.isEmpty()) {
                firebaseUser.updatePassword(mdp).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Mot de passe mis à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur mot de passe : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        // Upload image si elle a été changée
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("profileImages/" + uid + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                user.update("profileUrl", uri.toString());
                            })
                    ).addOnFailureListener(e -> Toast.makeText(this, "Erreur upload image", Toast.LENGTH_SHORT).show());
        }
    }

}