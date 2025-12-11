package com.example.myapplication_firebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Profil extends AppCompatActivity implements View.OnClickListener{

    private EditText nomEdit, prenomEdit, mdpEdit;
    private ImageView profileImage;
    private Button saveBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private String currentBase64Image = "";

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
        mdpEdit = findViewById(R.id.mdpEdit);
        profileImage = findViewById(R.id.profileImage);
        saveBtn = findViewById(R.id.saveBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        saveBtn.setOnClickListener(this);
        // Permet de cliquer sur l'image pour la modifier
        profileImage.setOnClickListener(v -> openFileChooser());

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        uid = currentUser.getUid();

        DocumentReference userRef = db.collection("utilisateurs").document(uid);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                nomEdit.setText(documentSnapshot.getString("nom"));
                prenomEdit.setText(documentSnapshot.getString("prenom"));

                // LOGIQUE D'AFFICHAGE DE L'IMAGE
                currentBase64Image = documentSnapshot.getString("profileImageBase64");
                if (currentBase64Image != null && !currentBase64Image.isEmpty()) {
                    Bitmap profileBitmap = decodeBase64ToBitmap(currentBase64Image);
                    if (profileBitmap != null) {
                        profileImage.setImageBitmap(profileBitmap);
                    }
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Erreur de chargement du profil : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {
            updateProfile();
        }
    }

    public void updateProfile() {
        String nom = nomEdit.getText().toString();
        String prenom = prenomEdit.getText().toString();
        String mdp = mdpEdit.getText().toString();

        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les champs obligatoires (Nom, Prénom)", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("prenom", prenom);

        // LOGIQUE DE SAUVEGARDE DE L'IMAGE
        if (imageUri != null) {
            String newBase64Image = encodeImageToBase64(imageUri);
            if (newBase64Image != null && !newBase64Image.isEmpty()) {
                updates.put("profileImageBase64", newBase64Image);
            } else {
                Toast.makeText(this, "Impossible d'encoder la nouvelle image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Conserve l'image existante si aucune nouvelle n'est sélectionnée
            updates.put("profileImageBase64", currentBase64Image != null ? currentBase64Image : "");
        }

        db.collection("utilisateurs").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Profil Firestore mis à jour", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show());

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            if (!mdp.isEmpty()) {
                if (mdp.length() < 6) {
                    Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères.", Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseUser.updatePassword(mdp).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Mot de passe mis à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur mot de passe : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e("Base64", "Chaîne Base64 invalide", e);
            return null;
        }
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Compression de l'image en PNG pour la stocker
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();

            return Base64.encodeToString(b, Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.e("Base64Encode", "Erreur lors de l'encodage: " + e.getMessage());
            return null;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}