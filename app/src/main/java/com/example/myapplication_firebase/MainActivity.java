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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

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
        String emailText = email.getText().toString();
        String mdpText = mdp.getText().toString();

        if (emailText.isEmpty() || mdpText.isEmpty() || nom.getText().toString().isEmpty() || prenom.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de l'utilisateur Firebase Auth
        auth.createUserWithEmailAndPassword(emailText, mdpText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String uid = user.getUid();

                        // Crée le document utilisateur avec un profileUrl vide
                        Map<String, Object> newUser = new HashMap<>();
                        newUser.put("nom", nom.getText().toString());
                        newUser.put("prenom", prenom.getText().toString());
                        newUser.put("email", emailText);
                        newUser.put("favoris", new ArrayList<>()); // liste vide
                        newUser.put("profileUrl", ""); // toujours présent

                        db.collection("utilisateurs").document(uid)
                                .set(newUser)
                                .addOnSuccessListener(unused -> {
                                    if (imageUri != null) {
                                        // Upload de l'image si elle est choisie
                                        StorageReference storageRef = FirebaseStorage.getInstance()
                                                .getReference("profileImages/" + uid + ".jpg");

                                        storageRef.putFile(imageUri)
                                                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                                                        .addOnSuccessListener(uri -> {
                                                            // Mettre à jour Firestore avec l'URL
                                                            db.collection("utilisateurs").document(uid)
                                                                    .update("profileUrl", uri.toString())
                                                                    .addOnSuccessListener(unused2 -> {
                                                                        Toast.makeText(MainActivity.this,
                                                                                "Inscription réussie", Toast.LENGTH_LONG).show();
                                                                        auth.signOut();
                                                                        startActivity(new Intent(this, Connexion.class));
                                                                        finish();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(this, "Erreur lors de l'update de l'image", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        })
                                                )
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Erreur upload image", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Pas de photo → redirection immédiate
                                        Toast.makeText(this, "Inscription réussie", Toast.LENGTH_LONG).show();
                                        auth.signOut();
                                        startActivity(new Intent(this, Connexion.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Erreur Firestore : " + e.getMessage());
                                    Toast.makeText(this, "Erreur d’enregistrement", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Ouvrir la galerie
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Récupérer l'image sélectionnée
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