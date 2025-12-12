package com.example.myapplication_firebase;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

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
        db.collection("annonce")
                .orderBy("datePublication", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        annoncesList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Annonce annonce = document.toObject(Annonce.class);
                            annonce.setDocumentId(document.getId());
                            annoncesList.add(annonce);
                        }

                        adapter = new AnnonceAdapter(annoncesList, this, this);
                        recyclerView.setAdapter(adapter);

                    } else {
                        Toast.makeText(this, "Erreur lors du chargement des annonces.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(String annonceId) {
        Intent intent = new Intent(ListActivity.this, DetailAnnonceActivity.class);
        intent.putExtra("ANNONCE_ID", annonceId);
        startActivity(intent);
    }
}