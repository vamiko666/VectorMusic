
package com.vova.musik.databases;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class TopTracksStore extends SQLiteOpenHelper {

    private static final String TAG = TopTracksStore.class.getName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TopTracksDatabase";

    private static final String TABLE_TOP_TRACKS = "songs";
    private static final String KEY_ID = "id";
    private static final String KEY_PLAYED = "played";

    private static final int KEY_ID_IDX = 0;
    private static final int KEY_PLAYED_IDX = 1;

    public TopTracksStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONG_DATABASE =
                "CREATE TABLE " + TABLE_TOP_TRACKS + "(" + KEY_ID + " INTEGER," + KEY_PLAYED + " INTEGER" + ")";
        db.execSQL(CREATE_SONG_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldDb, int newDb) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOP_TRACKS);
        this.onCreate(db);
    }

    public void addSongs(long id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID, id);
        values.put(KEY_PLAYED, 0);
        db.insert(TABLE_TOP_TRACKS, null, values);
        db.close();
    }

    public void incaseSong(long id) {

        TopTracksPair pair = getSongs(id);
        int played;
        if (pair == null) {
            addSongs(id);
            played = 0;
        } else {
            played = pair.getValues();
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID, id);
        values.put(KEY_PLAYED, played+1);
        db.update(TABLE_TOP_TRACKS, values, KEY_ID + " = ?", new String[]{ String.valueOf(id) });
        db.close();
    }

    public TopTracksPair getSongs(long id) {

        // Get table data
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TOP_TRACKS, new String[] { KEY_ID, KEY_PLAYED }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);

        // Search through database
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            TopTracksPair pair = new TopTracksPair(id, cursor.getInt(KEY_PLAYED_IDX));
            cursor.close();
            return pair;
        }

        db.close();
        return null;
    }

    public List<TopTracksPair> getPairsList() {

        List<TopTracksPair> pairList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String dbQuery = "SELECT * FROM " + TABLE_TOP_TRACKS;
        Cursor cursor = db.rawQuery(dbQuery, null);

        if (cursor.moveToFirst()) {
            do {
                TopTracksPair pair = new TopTracksPair(cursor.getLong(KEY_ID_IDX), cursor.getInt(KEY_PLAYED_IDX));
                pairList.add(pair);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return pairList;
    }
}
