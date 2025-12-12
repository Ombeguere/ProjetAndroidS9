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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;


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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;


public class DetailAnnonceActivity extends AppCompatActivity {

    private static final String TAG = "DetailAnnonceActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String API_KEY = "AIzaSyCQ7gh18X5uFjCECYn4oEfiTvoN3vRq2NU";

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

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Button buttonAfficherTrajet;

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

        setupLocationRequest();

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

        buttonAfficherTrajet = findViewById(R.id.button_afficher_trajet);

        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        addCommentButton = findViewById(R.id.add_comment_button);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentairesList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentairesList);
        commentsRecyclerView.setAdapter(commentAdapter);


        currentAnnonceId = getIntent().getStringExtra("ANNONCE_ID");

        if (currentAnnonceId != null) {
            chargerDetailsAnnonce(currentAnnonceId);

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
    }

    private void setupLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setNumUpdates(1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                android.location.Location location = locationResult.getLastLocation();
                if (location != null) {
                    traiterPositionUtilisateur(location);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                } else {
                    Log.w(TAG, "LocationResult reçu, mais location est null.");
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void traiterPositionUtilisateur(android.location.Location location) {
        double userLat = location.getLatitude();
        double userLon = location.getLongitude();

        if (annonceLat != 0.0 && annonceLon != 0.0) {
            calculerTrajet(userLat, userLon, annonceLat, annonceLon);

            buttonAfficherTrajet.setVisibility(View.VISIBLE);
            buttonAfficherTrajet.setOnClickListener(v ->
                    afficherTrajetSurCarte(userLat, userLon, annonceLat, annonceLon)
            );

        } else {
            textDistance.setText("Position de l'annonce inconnue.");
            textDureeTrajet.setText("Position de l'annonce inconnue.");
            buttonAfficherTrajet.setVisibility(View.GONE);
        }
    }


    private void obtenirPositionUtilisateur() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    traiterPositionUtilisateur(location);
                } else {
                    textDistance.setText("Recherche position utilisateur...");
                    textDureeTrajet.setText("Recherche position utilisateur...");
                    buttonAfficherTrajet.setVisibility(View.GONE);

                    try {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } catch (SecurityException e) {
                        Log.e(TAG, "Erreur de sécurité lors de la demande de mise à jour de la localisation", e);
                        textDistance.setText("Permission manquante");
                        textDureeTrajet.setText("Permission manquante");
                    }
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
                textDistance.setText("Localisation refusée.");
                textDureeTrajet.setText("Localisation refusée.");
                buttonAfficherTrajet.setVisibility(View.GONE);
            }
        }
    }

    private void calculerTrajet(double userLat, double userLon, double annonceLat, double annonceLon) {

        String origins = userLat + "," + userLon;
        String destinations = annonceLat + "," + annonceLon;

        Log.d(TAG, "Requête trajet de: " + origins + " vers: " + destinations);

        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&key=%s&mode=driving",
                origins, destinations, API_KEY);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String status = response.getString("status");

                if (!status.equals("OK")) {
                    String detailMessage = "Statut: " + status;
                    if (response.has("error_message")) {
                        String errorMessage = response.getString("error_message");
                        detailMessage += " | Détail: " + errorMessage;
                        Log.e(TAG, "MESSAGE DÉTAILLÉ DE GOOGLE: " + errorMessage);
                    } else {
                        Log.e(TAG, "Statut non-OK sans message d'erreur: " + status);
                    }

                    textDistance.setText("Erreur API: " + detailMessage);
                    textDureeTrajet.setText("Erreur API: " + status);
                    return;
                }

                JSONArray rows = response.getJSONArray("rows");
                if (rows.length() > 0) {
                    JSONObject row = rows.getJSONObject(0);
                    JSONArray elements = row.getJSONArray("elements");
                    if (elements.length() > 0) {
                        JSONObject element = elements.getJSONObject(0);

                        if (element.getString("status").equals("OK")) {
                            String distanceText = element.getJSONObject("distance").getString("text");
                            String durationText = element.getJSONObject("duration").getString("text");

                            textDistance.setText(String.format("Distance : %s", distanceText));
                            textDureeTrajet.setText(String.format("Durée (voiture) : %s", durationText));
                        } else {
                            textDistance.setText("Trajet indisponible: " + element.getString("status"));
                            textDureeTrajet.setText("Trajet indisponible: " + element.getString("status"));
                            Log.w(TAG, "Element status: " + element.getString("status"));
                        }
                    } else {
                        textDistance.setText("Pas d'éléments de trajet.");
                        textDureeTrajet.setText("Pas d'éléments de trajet.");
                        Log.w(TAG, "Empty elements array.");
                    }
                } else {
                    textDistance.setText("Pas de ligne de trajet.");
                    textDureeTrajet.setText("Pas de ligne de trajet.");
                    Log.w(TAG, "Empty rows array.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur de parsing JSON", e);
                textDistance.setText("Erreur calcul (JSON)");
                textDureeTrajet.setText("Erreur calcul (JSON)");
            }
        }, error -> {
            String errorMsg = "Erreur réseau (Volley)";
            if (error.networkResponse != null) {
                errorMsg = "Erreur HTTP " + error.networkResponse.statusCode;
            }
            Log.e(TAG, "Erreur Volley: " + error.toString());
            textDistance.setText(errorMsg);
            textDureeTrajet.setText(errorMsg);
        });

        queue.add(jsonObjectRequest);
    }

    private void afficherTrajetSurCarte(double startLat, double startLon, double endLat, double endLon) {

        String mapUri = String.format(Locale.US,
                "google.navigation:q=%f,%f&mode=d",
                endLat, endLon);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            String webUri = String.format(Locale.US,
                    "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f",
                    startLat, startLon, endLat, endLon);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));

            if (webIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(webIntent);
            } else {
                Toast.makeText(this, "Aucune application de carte ou navigateur n'est installée.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void chargerDetailsAnnonce(String annonceId) {
        db.collection("annonce").document(annonceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            afficherDetails(document);
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

        String imageUrlBase64 = document.getString("imageUrlBase64");

        GeoPoint location = document.getGeoPoint("location");
        if (location != null) {
            annonceLat = location.getLatitude();
            annonceLon = location.getLongitude();
        } else {
            annonceLat = 0.0;
            annonceLon = 0.0;
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

        if (imageUrlBase64 != null && !imageUrlBase64.isEmpty()) {
            Bitmap imageBitmap = decodeBase64ToBitmap(imageUrlBase64);
            if (imageBitmap != null) {
                detailImage.setImageBitmap(imageBitmap);
            } else {
                detailImage.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            detailImage.setImageResource(R.drawable.ic_launcher_background);
        }

        obtenirPositionUtilisateur();
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Chaîne Base64 invalide: " + e.getMessage());
            return null;
        }
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