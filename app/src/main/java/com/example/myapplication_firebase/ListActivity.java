package com.example.myapplication_firebase;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

// L'activité doit IMPLÉMENTER l'interface AnnonceAdapter.OnItemClickListener
public class ListActivity extends AppCompatActivity implements AnnonceAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private AnnonceAdapter adapter;
    private FirebaseFirestore db;
    private List<Annonce> annoncesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_view_annonces);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chargerAnnonces();
    }

    private void chargerAnnonces() {
        db.collection("annonce").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        annoncesList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Supposons que Annonce a un constructeur vide et les getters/setters nécessaires
                            Annonce annonce = document.toObject(Annonce.class);
                            annonce.setDocumentId(document.getId()); // Essentiel : stocker l'ID
                            annoncesList.add(annonce);
                        }

                        // IMPORTANT: Passer 'this' comme écouteur de clic
                        adapter = new AnnonceAdapter(annoncesList, this, this);
                        recyclerView.setAdapter(adapter);

                    } else {
                        Toast.makeText(this, "Erreur lors du chargement des annonces.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // NOUVEAU: Implémentation de la méthode de l'interface AnnonceAdapter.OnItemClickListener
    @Override
    public void onItemClick(String annonceId) {
        // Logique de navigation vers l'écran de détail
        Intent intent = new Intent(ListActivity.this, DetailAnnonceActivity.class);
        intent.putExtra("ANNONCE_ID", annonceId);
        startActivity(intent);
    }
}