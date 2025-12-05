package com.example.myapplication_firebase;

import com.google.firebase.firestore.GeoPoint;
import java.util.Date;
import java.util.List;

public class Annonce {

    private String adresse;
    private Date datePublication;
    private String description;
    private String imageurl;
    private String pieces;
    private String superficie;
    private String uid;
    private boolean parking;
    private List<String> equipements;
    private GeoPoint location;

    private float noteMoyenne;
    private String documentId;

    public Annonce() {
    }

    // Favoris et ID
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Notation
    public float getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(float noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }

    // Propriétés de l'Annonce
    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Date getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Date datePublication) {
        this.datePublication = datePublication;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEquipements() {
        return equipements;
    }

    public void setEquipements(List<String> equipements) {
        this.equipements = equipements;
    }

    public String getImageUrl() {
        return imageurl;
    }

    public void setImageUrl(String imageurl) {
        this.imageurl = imageurl;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getPieces() {
        return pieces;
    }

    public void setPieces(String pieces) {
        this.pieces = pieces;
    }

    public boolean isParking() {
        return parking;
    }

    public void setParking(boolean parking) {
        this.parking = parking;
    }

    public String getSuperficie() {
        return superficie;
    }

    public void setSuperficie(String superficie) {
        this.superficie = superficie;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}