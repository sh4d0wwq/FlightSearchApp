package com.flightsearch.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.flightsearch.app.models.FlightSearch;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FlightSearchDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SEARCHES = "saved_searches";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FROM_CITY = "from_city";
    private static final String COLUMN_TO_CITY = "to_city";
    private static final String COLUMN_DEPARTURE_DATE = "departure_date";
    private static final String COLUMN_PRICE = "price";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_SEARCHES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FROM_CITY + " TEXT NOT NULL,"
                + COLUMN_TO_CITY + " TEXT NOT NULL,"
                + COLUMN_DEPARTURE_DATE + " TEXT NOT NULL,"
                + COLUMN_PRICE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCHES);
        onCreate(db);
    }

    public long addSearch(FlightSearch search) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM_CITY, search.getFromCity());
        values.put(COLUMN_TO_CITY, search.getToCity());
        values.put(COLUMN_DEPARTURE_DATE, search.getDepartureDate());
        values.put(COLUMN_PRICE, search.getPrice());

        long id = db.insert(TABLE_SEARCHES, null, values);
        db.close();
        return id;
    }

    public FlightSearch getSearch(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SEARCHES,
                new String[]{COLUMN_ID, COLUMN_FROM_CITY, COLUMN_TO_CITY, COLUMN_DEPARTURE_DATE, COLUMN_PRICE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        FlightSearch search = null;
        if (cursor.moveToFirst()) {
            search = new FlightSearch(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FROM_CITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TO_CITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTURE_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
            );
            cursor.close();
        }
        db.close();
        return search;
    }

    public List<FlightSearch> getAllSearches() {
        List<FlightSearch> searches = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SEARCHES + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    FlightSearch search = new FlightSearch();
                    search.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    search.setFromCity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FROM_CITY)));
                    search.setToCity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TO_CITY)));
                    search.setDepartureDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTURE_DATE)));
                    search.setPrice(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                    searches.add(search);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return searches;
    }

    public int updateSearch(FlightSearch search) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM_CITY, search.getFromCity());
        values.put(COLUMN_TO_CITY, search.getToCity());
        values.put(COLUMN_DEPARTURE_DATE, search.getDepartureDate());
        values.put(COLUMN_PRICE, search.getPrice());

        int rowsAffected = db.update(TABLE_SEARCHES, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(search.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteSearch(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SEARCHES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

}
