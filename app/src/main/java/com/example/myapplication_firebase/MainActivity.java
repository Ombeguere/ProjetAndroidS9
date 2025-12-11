package com.example.myapplication_firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button inscriptionButton;
    private TextView dejaInscritButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private EditText nom;
    private EditText prenom;
    private EditText email;
    private EditText mdp;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        email = findViewById(R.id.email);
        mdp = findViewById(R.id.mdp);

        inscriptionButton = findViewById(R.id.inscription);
        inscriptionButton.setOnClickListener(this);

        dejaInscritButton = findViewById(R.id.dejaCompte);
        dejaInscritButton.setOnClickListener(this);

        profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> openFileChooser());
    }

    @Override
    public void onClick(View v) {
        if(v == inscriptionButton ) {
            inscrireUtilisateur();
        } else if (v == dejaInscritButton) {
            Intent intent = new Intent(this, Connexion.class);
            startActivity(intent);
        }
    }

    public void inscrireUtilisateur() {
        String emailText = email.getText().toString().trim();
        String mdpText = mdp.getText().toString().trim();
        String nomText = nom.getText().toString().trim();
        String prenomText = prenom.getText().toString().trim();

        if (emailText.isEmpty() || mdpText.isEmpty() || nomText.isEmpty() || prenomText.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mdpText.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(emailText, mdpText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            handleUserProfileCreation(user, nomText, prenomText, emailText);
                        }
                    } else {
                        Toast.makeText(this, "Erreur d'inscription : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleUserProfileCreation(FirebaseUser user, String nomText, String prenomText, String emailText) {
        String base64ImageString = "";

        if (imageUri != null) {
            base64ImageString = encodeImageToBase64(imageUri);
            if (base64ImageString == null || base64ImageString.isEmpty()) {
                Toast.makeText(this, "Erreur d'encodage de l'image.", Toast.LENGTH_SHORT).show();
            }
        }

        saveUserToFirestore(user, nomText, prenomText, emailText, base64ImageString);
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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

    private void saveUserToFirestore(FirebaseUser user, String nomText, String prenomText, String emailText, String base64Image) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("nom", nomText);
        newUser.put("prenom", prenomText);
        newUser.put("email", emailText);
        newUser.put("profileImageBase64", base64Image);

        db.collection("utilisateurs").document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Inscription réussie et profil créé !", Toast.LENGTH_LONG).show();

                    auth.signOut();
                    startActivity(new Intent(MainActivity.this, Connexion.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Erreur Firestore : " + e.getMessage());
                    Toast.makeText(this, "Erreur d’enregistrement du profil. Veuillez réessayer.", Toast.LENGTH_SHORT).show();
                });
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