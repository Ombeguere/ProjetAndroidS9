package com.example.myapplication_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.json.JSONArray;


public class DetailAnnonceActivity extends AppCompatActivity {

    private static final String TAG = "DetailAnnonceActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FirebaseFirestore db;
    private String currentAnnonceId;

    private TextView detailTitre;
    private TextView detailPrix;
    private TextView detailAdresse;
    private TextView detailDescription;
    private TextView detailSuperficie;
    private TextView detailPieces;
    private TextView detailEquipements;
    private ImageView detailImage;

    private TextView textDistance;
    private TextView textDureeTrajet;
    private FusedLocationProviderClient fusedLocationClient;
    private double annonceLat;
    private double annonceLon;

    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Commentaire> commentairesList;
    private Button addCommentButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_annonce);

        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        detailTitre = findViewById(R.id.detail_titre);
        detailPrix = findViewById(R.id.detail_prix);
        detailAdresse = findViewById(R.id.detail_adresse);
        detailDescription = findViewById(R.id.detail_description);
        detailSuperficie = findViewById(R.id.detail_superficie);
        detailPieces = findViewById(R.id.detail_pieces);
        detailEquipements = findViewById(R.id.detail_equipements);
        detailImage = findViewById(R.id.detail_image);

        textDistance = findViewById(R.id.text_distance);
        textDureeTrajet = findViewById(R.id.text_duree_trajet);

        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        addCommentButton = findViewById(R.id.add_comment_button);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentairesList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentairesList);
        commentsRecyclerView.setAdapter(commentAdapter);


        currentAnnonceId = getIntent().getStringExtra("ANNONCE_ID");

        if (currentAnnonceId != null) {
            chargerDetailsAnnonce(currentAnnonceId);
            chargerCommentaires(currentAnnonceId);

            addCommentButton.setOnClickListener(v -> {
                String adresse = detailAdresse.getText().toString();

                Intent intent = new Intent(DetailAnnonceActivity.this, RatingActivity.class);
                intent.putExtra("ANNONCE_ID", currentAnnonceId);
                intent.putExtra("ANNONCE_ADRESSE", adresse);
                startActivity(intent);
            });

        } else {
            Toast.makeText(this, "Erreur: ID de l'annonce manquant.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentAnnonceId != null) {
            chargerCommentaires(currentAnnonceId);
        }
        if (annonceLat != 0 && currentAnnonceId != null) {
            obtenirPositionUtilisateur();
        }
    }


    private void obtenirPositionUtilisateur() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double userLat = location.getLatitude();
                    double userLon = location.getLongitude();

                    calculerTrajet(userLat, userLon, annonceLat, annonceLon);
                } else {
                    textDistance.setText("Position indisponible.");
                    textDureeTrajet.setText("Position indisponible.");
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenirPositionUtilisateur();
            } else {
                textDistance.setText("Permission de localisation refusée.");
                textDureeTrajet.setText("Permission de localisation refusée.");
            }
        }
    }

    private void calculerTrajet(double userLat, double userLon, double annonceLat, double annonceLon) {

        final String API_KEY = "AIzaSyCQ7gh18X5uFjCECYn4oEfiTvoN3vRq2NU"; // Clé API Insérée

        String origins = userLat + "," + userLon;
        String destinations = annonceLat + "," + annonceLon;

        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&key=%s",
                origins, destinations, API_KEY);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray rows = response.getJSONArray("rows");
                if (rows.length() > 0) {
                    JSONObject row = rows.getJSONObject(0);
                    JSONArray elements = row.getJSONArray("elements");
                    if (elements.length() > 0) {
                        JSONObject element = elements.getJSONObject(0);

                        if (element.getString("status").equals("OK")) {
                            String distanceText = element.getJSONObject("distance").getString("text");
                            String durationText = element.getJSONObject("duration").getString("text");

                            textDistance.setText(distanceText);
                            textDureeTrajet.setText(durationText);
                        } else {
                            textDistance.setText("Trajet indisponible");
                            textDureeTrajet.setText("Trajet indisponible");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur de parsing JSON", e);
                textDistance.setText("Erreur calcul");
                textDureeTrajet.setText("Erreur calcul");
            }
        }, error -> {
            Log.e(TAG, "Erreur réseau (Volley): " + error.toString());
            textDistance.setText("Erreur réseau");
            textDureeTrajet.setText("Erreur réseau");
        });

        queue.add(jsonObjectRequest);
    }

    private void chargerDetailsAnnonce(String annonceId) {
        db.collection("annonce").document(annonceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            afficherDetails(document);
                            obtenirPositionUtilisateur();
                        } else {
                            Toast.makeText(this, "Annonce introuvable.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Document not found for ID: " + annonceId);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Échec du chargement des détails.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching document: ", task.getException());
                        finish();
                    }
                });
    }

    private void afficherDetails(DocumentSnapshot document) {
        String titre = document.getString("titre");
        String adresse = document.getString("adresse");
        String description = document.getString("description");
        String imageUrl = document.getString("imageUrl");

        GeoPoint location = document.getGeoPoint("location");
        if (location != null) {
            annonceLat = location.getLatitude();
            annonceLon = location.getLongitude();
        } else {
            annonceLat = 0;
            annonceLon = 0;
            textDistance.setText("Localisation non définie");
            textDureeTrajet.setText("Localisation non définie");
        }


        String superficie = document.getString("superficie") != null ? document.getString("superficie") + " m²" : "N/A";
        String pieces = document.getString("pieces") != null ? document.getString("pieces") + " pièces" : "N/A";

        Object prixObj = document.get("prix");
        String prixStr = "Prix Inconnu";
        if (prixObj instanceof Double) {
            prixStr = String.format(Locale.FRANCE, "%.2f € / mois", (Double) prixObj);
        } else if (prixObj instanceof Long) {
            prixStr = ((Long) prixObj).toString() + " € / mois";
        }

        List<String> equipementsList = (List<String>) document.get("equipements");
        String equipementsStr = (equipementsList != null && !equipementsList.isEmpty())
                ? String.join(", ", equipementsList)
                : "Aucun équipement additionnel spécifié.";

        detailTitre.setText(titre != null ? titre : "Annonce sans titre");
        detailPrix.setText(prixStr);
        detailAdresse.setText(adresse != null ? adresse : "Adresse non fournie");
        detailDescription.setText(description != null ? description : "Description non disponible.");
        detailSuperficie.setText(superficie);
        detailPieces.setText(pieces);
        detailEquipements.setText(equipementsStr);

        /*
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                 .load(imageUrl)
                 .into(detailImage);
        } else {
            detailImage.setImageResource(R.drawable.placeholder_image);
        }
        */
    }


    private void chargerCommentaires(String annonceId) {
        db.collection("annonce").document(annonceId)
                .collection("avis")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentairesList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Commentaire commentaire = document.toObject(Commentaire.class);
                        commentairesList.add(commentaire);
                    }

                    commentAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Impossible de charger les avis.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erreur chargement avis", e);
                });
    }
}