package com.example.myapplication_firebase;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.myapplication_firebase.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private MapView map;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        // On initialise la carte
        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(15.0);
        map.setMultiTouchControls(true); // Permet de zoomer avec les doigts

        // Centrer sur Rouen (ESIGELEC) par défaut
        GeoPoint startPoint = new GeoPoint(49.383430, 1.0773341);
        map.getController().setCenter(startPoint);

        db = FirebaseFirestore.getInstance();
        chargerAnnoncesSurCarte();
    }

    private void chargerAnnoncesSurCarte() {
        db.collection("annonce").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double offset = 0.001;
                        int i = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String titre = document.getString("titre");
                            Double prix = document.getDouble("prix");

                            double lat = 49.383430 + (Math.random() - 0.5) * 0.01;
                            double lon = 1.0773341 + (Math.random() - 0.5) * 0.01;

                            GeoPoint pos = new GeoPoint(lat, lon);

                            // Création du Marqueur
                            Marker marker = new Marker(map);
                            marker.setPosition(pos);
                            marker.setTitle(titre + "\n" + prix + " €");
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            marker.setOnMarkerClickListener((m, mapView) -> {
                                m.showInfoWindow();
                                return true;
                            });

                            map.getOverlays().add(marker);
                        }
                        map.invalidate();
                    } else {
                        Toast.makeText(this, "Erreur chargement", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}