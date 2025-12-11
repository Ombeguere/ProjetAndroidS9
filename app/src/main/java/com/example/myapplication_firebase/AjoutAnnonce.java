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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.util.Log;

// Imports nécessaires pour le géocodage
import android.location.Address;
import android.location.Geocoder;
import java.util.Locale;


public class AjoutAnnonce extends AppCompatActivity implements View.OnClickListener {

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

        String titreStr = titre.getText().toString().trim();
        String prixStr = prix.getText().toString().trim();

        String desc = description.getText().toString().trim();
        String sup = superficie.getText().toString().trim();
        String nbPieces = pieces.getText().toString().trim();
        String adr = adresse.getText().toString().trim();
        boolean parking = parkingCheckBox.isChecked();

        if (titreStr.isEmpty() || prixStr.isEmpty() || desc.isEmpty() || sup.isEmpty() || nbPieces.isEmpty() || adr.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires (Titre, Prix, etc.) et ajouter une image", Toast.LENGTH_LONG).show();
            return;
        }

        Double prixDouble;
        try {
            prixDouble = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez entrer un prix valide (numérique).", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> finalEquipementsList = new ArrayList<>(equipementsAdditionnels);
        if (wifiCheckBox.isChecked()) {
            finalEquipementsList.add("Wi-Fi");
        }
        if (climatisationCheckBox.isChecked()) {
            finalEquipementsList.add("Climatisation");
        }

        String base64Image = encodeImageToBase64(imageUri);
        if (base64Image == null || base64Image.isEmpty()) {
            Toast.makeText(this, "Erreur lors de l'encodage de l'image.", Toast.LENGTH_LONG).show();
            return;
        }

        // DÉBUT DE LA LOGIQUE DE GÉOCODAGE
        GeoPoint location;
        try {
            location = getLocationFromAddress(adr);
        } catch (IOException e) {
            Toast.makeText(this, "Erreur de géocodage (réseau).", Toast.LENGTH_LONG).show();
            Log.e("GEOCODING", "Erreur réseau lors du géocodage: " + e.getMessage());
            return;
        }

        if (location == null) {
            Toast.makeText(this, "Adresse non valide ou introuvable. Veuillez vérifier l'adresse.", Toast.LENGTH_LONG).show();
            return;
        }
        // FIN DE LA LOGIQUE DE GÉOCODAGE


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> annonce = new HashMap<>();
        annonce.put("uid", uid);
        annonce.put("titre", titreStr);
        annonce.put("prix", prixDouble);
        annonce.put("description", desc);
        annonce.put("superficie", sup);
        annonce.put("pieces", nbPieces);
        annonce.put("equipements", finalEquipementsList);
        annonce.put("adresse", adr);
        annonce.put("parking", parking);
        annonce.put("datePublication", new Date());

        annonce.put("imageUrlBase64", base64Image);

        // Utiliser la location réelle
        annonce.put("location", location); // <--- Correction

        db.collection("annonce").add(annonce)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AjoutAnnonce.this,
                            "Annonce ajoutée avec succès",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur lors de la création de l'annonce", Toast.LENGTH_SHORT).show()
                );
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();

            if (b.length > 950000) {
                Toast.makeText(this, "L'image est trop volumineuse pour être stockée en Base64.", Toast.LENGTH_LONG).show();
                Log.e("Base64Error", "Image trop volumineuse: " + b.length + " bytes");
                return null;
            }

            return Base64.encodeToString(b, Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.e("Base64Encode", "Erreur lors de l'encodage: " + e.getMessage());
            return null;
        }
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

    // NOUVELLE MÉTHODE POUR LE GÉOCODAGE
    private GeoPoint getLocationFromAddress(String strAddress) throws IOException {
        // Utilise la locale par défaut (France/Français dans votre cas)
        Geocoder coder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;
        GeoPoint geoPoint = null;

        // Limite à 1 résultat (le plus pertinent)
        addressList = coder.getFromLocationName(strAddress, 1);

        if (addressList != null && !addressList.isEmpty()) {
            Address location = addressList.get(0);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            geoPoint = new GeoPoint(latitude, longitude);
            Log.d("GEOCODING", "Adresse convertie: " + latitude + ", " + longitude);
        }
        return geoPoint;
    }
}