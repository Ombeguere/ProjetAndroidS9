package com.example.myapplication_firebase;

public class Avis {

    private String userId;
    private float note;
    private String annonceId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getNote() {
        return note;
    }

    public void setNote(float note) {
        this.note = note;
    }

    public String getAnnonceId() {
        return annonceId;
    }

    public void setAnnonceId(String annonceId) {
        this.annonceId = annonceId;
    }
}
