package com.example.pokedexlite.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pokedex.db";
    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE favorites (id INTEGER PRIMARY KEY, name TEXT, image_url TEXT)");
        db.execSQL("CREATE TABLE team (id INTEGER PRIMARY KEY AUTOINCREMENT, pokemon_id INTEGER, name TEXT, image_url TEXT, types TEXT, note TEXT)");
        db.execSQL("CREATE TABLE api_cache (cache_key TEXT PRIMARY KEY, json TEXT)");
        db.execSQL("CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT, search_query TEXT, pokemon_id INTEGER, name TEXT, created_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS team");
        db.execSQL("DROP TABLE IF EXISTS api_cache");
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    public Cursor getAllFavorites() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM favorites", null);
    }

    public boolean isFavorite(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM favorites WHERE id = ?", new String[]{String.valueOf(id)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void addFavorite(int id, String name, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("image_url", imageUrl);
        db.insert("favorites", null, values);
    }

    public void removeFavorite(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("favorites", "id = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getTeam() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM team", null);
    }

    public int getTeamCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM team", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    public void addToTeam(int pokemonId, String name, String imageUrl, String types) {
        if (getTeamCount() >= 6) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pokemon_id", pokemonId);
        values.put("name", name);
        values.put("image_url", imageUrl);
        values.put("types", types);
        values.put("note", "");
        db.insert("team", null, values);
    }

    public void updateTeamNote(int id, String newNote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("note", newNote);
        db.update("team", values, "id = ?", new String[]{String.valueOf(id)});
    }
    public void removeFromTeam(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("team", "id = ?", new String[]{String.valueOf(id)});
    }
    public Cursor getHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM history ORDER BY created_at DESC", null);
    }

    public void addHistory(String query, int pokemonId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("search_query", query);
        values.put("pokemon_id", pokemonId);
        values.put("name", name);
        values.put("created_at", System.currentTimeMillis());
        db.insert("history", null, values);
    }
    public void saveCache(String key, String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cache_key", key);
        values.put("json", json);
        db.replace("api_cache", null, values);
    }
    public String getCache(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT json FROM api_cache WHERE cache_key = ?", new String[]{key});
        String result = null;
        if (cursor.moveToFirst()) result = cursor.getString(0);
        cursor.close();
        return result;
    }
}