package com.flightsearch.app.remote;

import android.content.Context;
import android.net.Uri;

import com.flightsearch.app.models.FlightSearch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RemoteFlightRepository {

    private static final String COLLECTION = "saved_flights";
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    private RemoteFlightRepository() {}

    public static boolean isFirebaseReady(Context context) {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            return app != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static void syncSavedFlight(Context context, FlightSearch flight) {
        if (flight == null) return;
        if (!isFirebaseReady(context)) return;
        EXEC.execute(() -> {
            try {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String docId = String.valueOf(flight.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("localId", flight.getId());
                map.put("fromCity", flight.getFromCity());
                map.put("toCity", flight.getToCity());
                map.put("departureDate", flight.getDepartureDate());
                map.put("price", flight.getPrice());
                map.put("airline", flight.getAirline());
                map.put("airlineName", flight.getAirlineName());
                map.put("flightNumber", flight.getFlightNumber());
                map.put("departureTime", flight.getDepartureTime());
                map.put("arrivalTime", flight.getArrivalTime());
                map.put("duration", flight.getDuration());
                map.put("stops", flight.getStops());
                map.put("tripCategory", flight.getTripCategory());
                map.put("imageUrl", flight.getImageUrl());
                db.collection(COLLECTION).document(docId).set(map);
            } catch (Exception ignored) { }
        });
    }

    public static void deleteRemote(Context context, long localId) {
        if (!isFirebaseReady(context)) return;
        EXEC.execute(() -> {
            try {
                FirebaseFirestore.getInstance()
                        .collection(COLLECTION)
                        .document(String.valueOf(localId))
                        .delete();
            } catch (Exception ignored) { }
        });
    }

    public interface ImageUrlCallback {
        void onReady(String url);
    }

    public static void uploadFlightImageAsync(Context context, long localId, Uri imageUri,
                                              ImageUrlCallback onUrl) {
        if (!isFirebaseReady(context) || imageUri == null) {
            if (onUrl != null) onUrl.onReady(null);
            return;
        }
        EXEC.execute(() -> {
            String url = null;
            try {
                StorageReference ref = FirebaseStorage.getInstance().getReference()
                        .child("flight_images")
                        .child(localId + "_" + System.currentTimeMillis() + ".jpg");
                com.google.android.gms.tasks.Tasks.await(ref.putFile(imageUri));
                url = com.google.android.gms.tasks.Tasks.await(ref.getDownloadUrl()).toString();
            } catch (Exception ignored) { }
            String finalUrl = url;
            android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
            main.post(() -> { if (onUrl != null) onUrl.onReady(finalUrl); });
        });
    }

    public static void patchImageUrlInFirestore(Context context, long localId, String imageUrl) {
        if (imageUrl == null) return;
        if (!isFirebaseReady(context)) return;
        EXEC.execute(() -> {
            try {
                Map<String, Object> m = new HashMap<>();
                m.put("imageUrl", imageUrl);
                FirebaseFirestore.getInstance()
                        .collection(COLLECTION)
                        .document(String.valueOf(localId))
                        .set(m, SetOptions.merge());
            } catch (Exception ignored) { }
        });
    }
}
