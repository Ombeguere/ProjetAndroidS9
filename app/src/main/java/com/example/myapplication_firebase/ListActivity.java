package com.example.myapplication_firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnnonceAdapter annonceAdapter;
    private List<Annonce> annoncesList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_view_annonces);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        annoncesList = new ArrayList<>();
        annonceAdapter = new AnnonceAdapter(annoncesList, this);
        recyclerView.setAdapter(annonceAdapter);

        fetchAnnounces();
    }

    private void fetchAnnounces() {
        db.collection("annonce")
                .get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            annoncesList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // toObject convertit le document Firestore en objet Annonce
                                Annonce annonce = document.toObject(Annonce.class);

                                // Récupération de l'ID unique du document Firestore
                                annonce.setDocumentId(document.getId());

                                annoncesList.add(annonce);
                            }

                            // Actualise l'affichage du RecyclerView
                            annonceAdapter.notifyDataSetChanged();

                        } else {
                            Log.e("Firestore", "Erreur lors de la récupération des annonces.", task.getException());
                            Toast.makeText(ListActivity.this, "Échec du chargement des annonces.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}