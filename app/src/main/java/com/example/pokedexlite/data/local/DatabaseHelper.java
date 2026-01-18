package com.example.pokedexlite.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Pokedex.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_TEAM = "team";
    private static final String TABLE_CACHE = "api_cache";

    private static final String COL_CACHE_KEY = "cache_key";
    private static final String COL_CACHE_JSON = "json";
    private static final String COL_CACHE_TIME = "fetched_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                "pokemon_id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "image_url TEXT, " +
                "types TEXT, " +
                "saved_at INTEGER)";
        db.execSQL(CREATE_FAVORITES);

        String CREATE_TEAM = "CREATE TABLE " + TABLE_TEAM + " (" +
                "slot INTEGER PRIMARY KEY, " +
                "pokemon_id INTEGER, " +
                "name TEXT, " +
                "image_url TEXT, " +
                "types TEXT, " +
                "note TEXT, " +
                "updated_at INTEGER)";
        db.execSQL(CREATE_TEAM);

        String CREATE_CACHE = "CREATE TABLE " + TABLE_CACHE + " (" +
                COL_CACHE_KEY + " TEXT PRIMARY KEY, " +
                COL_CACHE_JSON + " TEXT, " +
                COL_CACHE_TIME + " INTEGER)";
        db.execSQL(CREATE_CACHE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHE);
        onCreate(db);
    }

    public void saveCache(String key, String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_CACHE_KEY, key);
        values.put(COL_CACHE_JSON, json);
        values.put(COL_CACHE_TIME, System.currentTimeMillis());
        db.replace(TABLE_CACHE, null, values);
    }

    public String getCache(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CACHE,
                new String[]{COL_CACHE_JSON},
                COL_CACHE_KEY + "=?",
                new String[]{key},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String json = cursor.getString(0);
            cursor.close();
            return json;
        }
        return null;
    }

    public Cursor getTeam() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TEAM, null);
    }

    public void addFavorite(int id, String name, String imageUrl, String types) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pokemon_id", id);
        values.put("name", name);
        values.put("image_url", imageUrl);
        values.put("types", types);
        values.put("saved_at", System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeFavorite(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, "pokemon_id=?", new String[]{String.valueOf(id)});
    }

    public boolean isFavorite(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_FAVORITES + " WHERE pokemon_id=?", new String[]{String.valueOf(id)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public Cursor getAllFavorites() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FAVORITES + " ORDER BY saved_at DESC", null);
    }

    // --- TEAM OPERATIONS (Max 6 Slots) ---

    // Mengembalikan slot kosong pertama (1-6), atau -1 jika penuh
    public int getAvailableTeamSlot() {
        SQLiteDatabase db = this.getReadableDatabase();
        for (int i = 1; i <= 6; i++) {
            Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_TEAM + " WHERE slot=?", new String[]{String.valueOf(i)});
            if (cursor.getCount() == 0) {
                cursor.close();
                return i;
            }
            cursor.close();
        }
        return -1; // Tim Penuh
    }

    public void addToTeam(int slot, int id, String name, String imageUrl, String types) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("slot", slot);
        values.put("pokemon_id", id);
        values.put("name", name);
        values.put("image_url", imageUrl);
        values.put("types", types);
        values.put("updated_at", System.currentTimeMillis());

        db.replace(TABLE_TEAM, null, values);
    }

    // Cek apakah pokemon ini sudah ada di tim (agar tidak duplikat)
    public boolean isInTeam(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_TEAM + " WHERE pokemon_id=?", new String[]{String.valueOf(id)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
}