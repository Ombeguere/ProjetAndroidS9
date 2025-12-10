package com.example.myapplication_firebase;

import android.content.Intent; // Import nécessaire pour Intent
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// 1. IMPLÉMENTER l'interface AnnonceAdapter.OnItemClickListener
public class FavoritesActivity extends AppCompatActivity implements AnnonceAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private AnnonceAdapter annonceAdapter;
    private List<Annonce> annoncesList;
    private FirebaseFirestore db;
    private FavoriteDbHelper dbHelper;
    private FirebaseAuth mAuth;
    private static final String TAG = "FavoritesActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        dbHelper = new FavoriteDbHelper(this);
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view_annonces); // Réutiliser l'ID du RecyclerView de ListActivity
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        annoncesList = new ArrayList<>();

        // 2. CORRECTION DU CONSTRUCTEUR : Ajouter 'this' comme troisième argument (le listener)
        annonceAdapter = new AnnonceAdapter(annoncesList, this, this);
        recyclerView.setAdapter(annonceAdapter);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Veuillez vous connecter pour voir vos favoris.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            fetchFavoriteAnnounces();
        }
    }

    private void fetchFavoriteAnnounces() {
        // 1. Récupérer les IDs des favoris depuis SQLite
        List<String> favoriteIds = dbHelper.getAllFavoriteIds();

        if (favoriteIds.isEmpty()) {
            Toast.makeText(this, "Vous n'avez aucun favori.", Toast.LENGTH_SHORT).show();
            annoncesList.clear();
            annonceAdapter.notifyDataSetChanged();
            return;
        }

        // 2. Interroger Firestore avec les IDs récupérés
        // Attention: whereIn est limité à 10 éléments par défaut.
        db.collection("annonce")
                // FieldPath.documentId() permet de filtrer par l'ID du document
                .whereIn(FieldPath.documentId(), favoriteIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        annoncesList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Annonce annonce = document.toObject(Annonce.class);
                            annonce.setDocumentId(document.getId());
                            annoncesList.add(annonce);
                        }

                        annonceAdapter.notifyDataSetChanged();

                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des annonces favorites.", task.getException());
                        Toast.makeText(this, "Échec du chargement. Si hors ligne, les données pourraient ne pas avoir été mises en cache.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // 3. Implémentation de la méthode de l'interface AnnonceAdapter.OnItemClickListener
    @Override
    public void onItemClick(String annonceId) {
        Log.d(TAG, "Clic sur l'annonce favorite ID: " + annonceId);

        // Lancer l'activité de détail en passant l'ID de l'annonce
        Intent intent = new Intent(FavoritesActivity.this, DetailAnnonceActivity.class);
        intent.putExtra("ANNONCE_ID", annonceId);
        startActivity(intent);
    }
}