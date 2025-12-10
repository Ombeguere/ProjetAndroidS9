package com.example.myapplication_firebase;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName; // NÉCESSAIRE pour le mapping

public class Commentaire {

    private String userId; // Mappe à 'userID'
    private float note;
    private String texte; // Mappe à 'commentaire'
    private Timestamp timestamp; // Sera rempli par FieldValue.serverTimestamp()

    public Commentaire() {
    }

    // Constructeur complet (optionnel)
    public Commentaire(String userId, float note, String texte, Timestamp timestamp) {
        this.userId = userId;
        this.note = note;
        this.texte = texte;
        this.timestamp = timestamp;
    }

    // --- Getters et Setters pour le Mapping Firestore ---

    // Mapping pour le champ 'userID' (si Firestore utilise la majuscule 'ID')
    @PropertyName("userID")
    public String getUserId() {
        return userId;
    }
    @PropertyName("userID")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getNote() {
        return note;
    }
    public void setNote(float note) {
        this.note = note;
    }

    // Mapping pour le champ 'commentaire' de Firestore
    @PropertyName("commentaire")
    public String getTexte() {
        return texte;
    }
    @PropertyName("commentaire")
    public void setTexte(String texte) {
        this.texte = texte;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}