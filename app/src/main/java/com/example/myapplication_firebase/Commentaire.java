package com.example.myapplication_firebase;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Commentaire {

    private String userId;
    private float note;
    private String texte;
    private Timestamp timestamp;

    public Commentaire() {
    }

    public Commentaire(String userId, float note, String texte, Timestamp timestamp) {
        this.userId = userId;
        this.note = note;
        this.texte = texte;
        this.timestamp = timestamp;
    }

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