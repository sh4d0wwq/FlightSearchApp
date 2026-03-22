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
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_FLIGHTS = "saved_searches";

    private static final String COL_ID = "id";
    private static final String COL_FROM = "from_city";
    private static final String COL_TO = "to_city";
    private static final String COL_DATE = "departure_date";
    private static final String COL_PRICE = "price";
    private static final String COL_AIRLINE = "airline";
    private static final String COL_AIRLINE_NAME = "airline_name";
    private static final String COL_FLIGHT_NUMBER = "flight_number";
    private static final String COL_DEP_TIME = "departure_time";
    private static final String COL_ARR_TIME = "arrival_time";
    private static final String COL_DURATION = "duration";
    private static final String COL_STOPS = "stops";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FLIGHTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_FROM + " TEXT NOT NULL," +
                COL_TO + " TEXT NOT NULL," +
                COL_DATE + " TEXT NOT NULL," +
                COL_PRICE + " TEXT," +
                COL_AIRLINE + " TEXT," +
                COL_AIRLINE_NAME + " TEXT," +
                COL_FLIGHT_NUMBER + " TEXT," +
                COL_DEP_TIME + " TEXT," +
                COL_ARR_TIME + " TEXT," +
                COL_DURATION + " TEXT," +
                COL_STOPS + " INTEGER DEFAULT 0" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_AIRLINE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_AIRLINE_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_FLIGHT_NUMBER + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_DEP_TIME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_ARR_TIME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_DURATION + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FLIGHTS + " ADD COLUMN " + COL_STOPS + " INTEGER DEFAULT 0");
        }
    }

    public long addSearch(FlightSearch flight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = buildValues(flight);
        long id = db.insert(TABLE_FLIGHTS, null, values);
        db.close();
        return id;
    }

    public FlightSearch getSearch(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_FLIGHTS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        FlightSearch result = null;
        if (c.moveToFirst()) {
            result = fromCursor(c);
        }
        c.close();
        db.close();
        return result;
    }

    public List<FlightSearch> getAllSearches() {
        List<FlightSearch> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + TABLE_FLIGHTS + " ORDER BY " + COL_ID + " DESC", null);
            if (c.moveToFirst()) {
                do { list.add(fromCursor(c)); } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return list;
    }

    public int updateSearch(FlightSearch flight) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.update(TABLE_FLIGHTS, buildValues(flight),
                COL_ID + "=?", new String[]{String.valueOf(flight.getId())});
        db.close();
        return rows;
    }

    public void deleteSearch(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FLIGHTS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private ContentValues buildValues(FlightSearch f) {
        ContentValues v = new ContentValues();
        v.put(COL_FROM, f.getFromCity());
        v.put(COL_TO, f.getToCity());
        v.put(COL_DATE, f.getDepartureDate());
        v.put(COL_PRICE, f.getPrice());
        v.put(COL_AIRLINE, f.getAirline());
        v.put(COL_AIRLINE_NAME, f.getAirlineName());
        v.put(COL_FLIGHT_NUMBER, f.getFlightNumber());
        v.put(COL_DEP_TIME, f.getDepartureTime());
        v.put(COL_ARR_TIME, f.getArrivalTime());
        v.put(COL_DURATION, f.getDuration());
        v.put(COL_STOPS, f.getStops());
        return v;
    }

    private FlightSearch fromCursor(Cursor c) {
        FlightSearch f = new FlightSearch();
        f.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        f.setFromCity(c.getString(c.getColumnIndexOrThrow(COL_FROM)));
        f.setToCity(c.getString(c.getColumnIndexOrThrow(COL_TO)));
        f.setDepartureDate(c.getString(c.getColumnIndexOrThrow(COL_DATE)));
        f.setPrice(c.getString(c.getColumnIndexOrThrow(COL_PRICE)));

        int airlineIdx = c.getColumnIndex(COL_AIRLINE);
        if (airlineIdx >= 0) f.setAirline(c.getString(airlineIdx));

        int airlineNameIdx = c.getColumnIndex(COL_AIRLINE_NAME);
        if (airlineNameIdx >= 0) f.setAirlineName(c.getString(airlineNameIdx));

        int flightNumIdx = c.getColumnIndex(COL_FLIGHT_NUMBER);
        if (flightNumIdx >= 0) f.setFlightNumber(c.getString(flightNumIdx));

        int depTimeIdx = c.getColumnIndex(COL_DEP_TIME);
        if (depTimeIdx >= 0) f.setDepartureTime(c.getString(depTimeIdx));

        int arrTimeIdx = c.getColumnIndex(COL_ARR_TIME);
        if (arrTimeIdx >= 0) f.setArrivalTime(c.getString(arrTimeIdx));

        int durationIdx = c.getColumnIndex(COL_DURATION);
        if (durationIdx >= 0) f.setDuration(c.getString(durationIdx));

        int stopsIdx = c.getColumnIndex(COL_STOPS);
        if (stopsIdx >= 0) f.setStops(c.getInt(stopsIdx));

        return f;
    }
}
