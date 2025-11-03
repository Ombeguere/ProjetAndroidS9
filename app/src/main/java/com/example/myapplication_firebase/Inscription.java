package com.example.myapplication_firebase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    Button inscriptionButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    EditText nom;
    EditText prenom;
    EditText email;
    EditText mdp;
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
    }

    @Override
    public void onClick(View v) {
        if(v == inscriptionButton ) {
            String emailText = email.getText().toString();
            String mdpText = mdp.getText().toString();

            // Création de l'utilisateur Firebase Auth
            auth.createUserWithEmailAndPassword(emailText, mdpText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            String uid = user.getUid();

                            Map<String, Object> newUser = new HashMap<>();
                            newUser.put("nom", nom.getText().toString());
                            newUser.put("prenom", prenom.getText().toString());
                            newUser.put("email", emailText);
                            newUser.put("favoris", new ArrayList<>());  // liste vide

                            db.collection("utilisateurs").document(uid).set(newUser)
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(MainActivity.this, "Utilisateur enregistré", Toast.LENGTH_LONG).show()
                                    )
                                    .addOnFailureListener(e -> Log.e("Projet Ousmane", e.toString()));

                        } else {
                            Toast.makeText(MainActivity.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}