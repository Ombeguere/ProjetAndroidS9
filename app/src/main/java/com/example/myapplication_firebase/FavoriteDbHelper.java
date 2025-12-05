package com.example.myapplication_firebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Favorites.db";
    private static final int DATABASE_VERSION = 1;

    // Constantes de la table des Favoris
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id"; // Clé primaire auto-incrémentée
    public static final String COLUMN_ANNONCE_ID = "annonce_id"; // ID du document Firestore
    public static final String COLUMN_USER_ID = "user_id"; // UID de l'utilisateur

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_ANNONCE_ID + " TEXT UNIQUE NOT NULL," +
                    COLUMN_USER_ID + " TEXT NOT NULL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_FAVORITES;

    public FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Ajoute une annonce aux favoris.
     */
    public boolean addFavorite(String annonceId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANNONCE_ID, annonceId);
        values.put(COLUMN_USER_ID, userId);

        long newRowId = db.insert(TABLE_FAVORITES, null, values);
        db.close();
        return newRowId != -1;
    }

    /**
     * Supprime une annonce des favoris.
     */
    public void removeFavorite(String annonceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_ANNONCE_ID + " = ?";
        String[] selectionArgs = { annonceId };
        db.delete(TABLE_FAVORITES, selection, selectionArgs);
        db.close();
    }

    /**
     * Vérifie si une annonce est en favori.
     */
    public boolean isFavorite(String annonceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_ANNONCE_ID};
        String selection = COLUMN_ANNONCE_ID + " = ?";
        String[] selectionArgs = { annonceId };

        Cursor cursor = db.query(
                TABLE_FAVORITES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean isFav = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isFav;
    }
}