package com.demonized.androidproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mor.hazan on 04-Feb-18.
 */

public class DBOpenHelper extends SQLiteOpenHelper {
    public static final String DATA_BASE = "GallemeDB";
    public static final int version = 1;
    public static final String TABLE_NAME = "gps";
    public static final String KEY_LAT = "Latitude";
    public static final String KEY_LON = "Longitude";
    public static final String KEY_FILE = "Filepath";

    public DBOpenHelper(Context context){
        super(context,DATA_BASE,null,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + TABLE_NAME);
        sb.append("(");
        sb.append("_id INTEGER PRIMARY KEY,");
        sb.append(KEY_FILE+ " TEXT,");
        sb.append(KEY_LAT+ " REAL,");
        sb.append(KEY_LON+ " REAL");
        sb.append(")");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getRowsByLat(double fromLat){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor =db.rawQuery("select * from "+ TABLE_NAME ,null);
        return cursor;
    }

    public void insertLine(LineObject lineObject){
        ContentValues cv = new ContentValues();
        cv.put(KEY_FILE,lineObject.file);
        cv.put(KEY_LAT,lineObject.lat);
        cv.put(KEY_LON,lineObject.lon);
        getWritableDatabase().insert(TABLE_NAME,null,cv);
    }
}
