package com.example.myapplication_firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjoutAnnonce extends AppCompatActivity implements View.OnClickListener {

    // NOUVEAUX CHAMPS
    private EditText titre;
    private EditText prix;

    private EditText description;
    private EditText superficie;
    private EditText pieces;
    private CheckBox parkingCheckBox;
    private CheckBox wifiCheckBox;
    private CheckBox climatisationCheckBox;
    private Button selectEquipementsBtn;
    private TextView tvEquipementsSummary;
    private EditText adresse;
    private Button selectImageBtn;
    private ImageView imagePreview;
    private Uri imageUri;
    private Button ajoutAnnonceBtn;

    private final String[] allEquipementsArray = new String[]{"Réfrigérateur", "Four", "Micro-ondes", "Lave-vaisselle", "Télévision", "Balcon"};
    private boolean[] checkedEquipements;
    private List<String> equipementsAdditionnels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajout_annonce);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des NOUVEAUX CHAMPS
        titre = findViewById(R.id.titre);
        prix = findViewById(R.id.prix);

        description = findViewById(R.id.description);
        superficie = findViewById(R.id.superficie);
        pieces = findViewById(R.id.pieces);

        parkingCheckBox = findViewById(R.id.parkingCheckBox);
        adresse = findViewById(R.id.adresse);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        imagePreview = findViewById(R.id.imagePreview);
        ajoutAnnonceBtn = findViewById(R.id.saveAnnonceBtn);

        wifiCheckBox = findViewById(R.id.wifiCheckBox);
        climatisationCheckBox = findViewById(R.id.climatisationCheckBox);
        selectEquipementsBtn = findViewById(R.id.selectEquipementsBtn);
        tvEquipementsSummary = findViewById(R.id.tvEquipementsSummary);

        checkedEquipements = new boolean[allEquipementsArray.length];


        selectImageBtn.setOnClickListener(this);
        ajoutAnnonceBtn.setOnClickListener(this);
        selectEquipementsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == ajoutAnnonceBtn ) {
            ajouterLogement();
        } else if (v == selectImageBtn) {
            choisirImage();
        } else if (v == selectEquipementsBtn) {
            showEquipementsDialog();
        }
    }

    private void showEquipementsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner les équipements additionnels");

        builder.setMultiChoiceItems(allEquipementsArray, checkedEquipements, (dialog, which, isChecked) -> {
            checkedEquipements[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, id) -> {
            equipementsAdditionnels.clear();
            for (int i = 0; i < allEquipementsArray.length; i++) {
                if (checkedEquipements[i]) {
                    equipementsAdditionnels.add(allEquipementsArray[i]);
                }
            }

            if (equipementsAdditionnels.isEmpty()) {
                tvEquipementsSummary.setText("Sélection actuelle : Aucun");
            } else {
                tvEquipementsSummary.setText("Sélection actuelle : " + String.join(", ", equipementsAdditionnels));
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    public void ajouterLogement() {

        // NOUVEAU: Récupération du Titre et Prix
        String titreStr = titre.getText().toString().trim();
        String prixStr = prix.getText().toString().trim();

        String desc = description.getText().toString().trim();
        String sup = superficie.getText().toString().trim();
        String nbPieces = pieces.getText().toString().trim();
        String adr = adresse.getText().toString().trim();
        boolean parking = parkingCheckBox.isChecked();

        // 1. Validation de base
        if (titreStr.isEmpty() || prixStr.isEmpty() || desc.isEmpty() || sup.isEmpty() || nbPieces.isEmpty() || adr.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires (Titre, Prix, etc.) et ajouter une image", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Conversion sécurisée du Prix
        Double prixDouble;
        try {
            prixDouble = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez entrer un prix valide (numérique).", Toast.LENGTH_SHORT).show();
            return;
        }


        // Gestion des équipements (inchangé)
        List<String> finalEquipementsList = new ArrayList<>(equipementsAdditionnels);
        if (wifiCheckBox.isChecked()) {
            finalEquipementsList.add("Wi-Fi");
        }
        if (climatisationCheckBox.isChecked()) {
            finalEquipementsList.add("Climatisation");
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> annonce = new HashMap<>();
        annonce.put("uid", uid);

        // NOUVEAU: Ajout du Titre et Prix dans Firestore
        annonce.put("titre", titreStr);
        annonce.put("prix", prixDouble);

        annonce.put("description", desc);
        annonce.put("superficie", sup);
        annonce.put("pieces", nbPieces);

        annonce.put("equipements", finalEquipementsList);

        annonce.put("adresse", adr);
        annonce.put("parking", parking);
        annonce.put("datePublication", new Date());
        annonce.put("imageUrl", "");

        // Placeholder pour la géolocalisation. Vous devrez la remplacer par une vraie conversion d'adresse
        GeoPoint tempLocation = new GeoPoint(0.0, 0.0);
        annonce.put("location", tempLocation);

        db.collection("annonce").add(annonce)
                .addOnSuccessListener(documentReference -> {

                    String annonceId = documentReference.getId();
                    StorageReference storageRef = FirebaseStorage.getInstance()
                            .getReference("annonceImages/" + annonceId + ".jpg");

                    storageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {

                                        db.collection("annonce").document(annonceId)
                                                .update("imageUrl", uri.toString())
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(AjoutAnnonce.this,
                                                            "Annonce ajoutée avec succès",
                                                            Toast.LENGTH_LONG).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Erreur mise à jour image", Toast.LENGTH_SHORT).show()
                                                );
                                    })
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erreur upload image", Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de la création de l'annonce", Toast.LENGTH_SHORT).show()
                );
    }


    private void choisirImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
        }
    }

}