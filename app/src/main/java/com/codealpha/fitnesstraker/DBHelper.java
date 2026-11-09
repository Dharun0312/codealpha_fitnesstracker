package com.codealpha.fitnesstraker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "Fitness.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE activities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, " +
                "minutes INTEGER, " +
                "calories INTEGER, " +
                "steps INTEGER, " +
                "date TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS activities");
        onCreate(db);
    }

    public boolean addActivity(String type,
                               int minutes,
                               int calories,
                               int steps,
                               String date) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("type", type);
        values.put("minutes", minutes);
        values.put("calories", calories);
        values.put("steps", steps);
        values.put("date", date);

        long result = db.insert("activities", null, values);

        return result != -1;
    }
}