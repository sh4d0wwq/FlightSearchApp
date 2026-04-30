package com.flightsearch.app.remote;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.flightsearch.app.models.FlightSearch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RemoteFlightRepository {

    private static final String USERS = "users";
    private static final String SAVED_FLIGHTS = "saved_flights";
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

    @Nullable
    public static String currentUserId() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u != null ? u.getUid() : null;
    }

    private static void collectionSet(Context context, FlightSearch flight, boolean mergePatch) {
        if (flight == null) return;
        if (!isFirebaseReady(context)) return;
        String uid = currentUserId();
        if (uid == null) return;
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
                if (mergePatch) {
                    db.collection(USERS).document(uid).collection(SAVED_FLIGHTS)
                            .document(docId).set(map, SetOptions.merge());
                } else {
                    db.collection(USERS).document(uid).collection(SAVED_FLIGHTS)
                            .document(docId).set(map);
                }
            } catch (Exception ignored) { }
        });
    }

    public static void syncSavedFlight(Context context, FlightSearch flight) {
        collectionSet(context, flight, false);
    }

    public static void deleteRemote(Context context, long localId) {
        if (!isFirebaseReady(context)) return;
        String uid = currentUserId();
        if (uid == null) return;
        EXEC.execute(() -> {
            try {
                FirebaseFirestore.getInstance()
                        .collection(USERS).document(uid).collection(SAVED_FLIGHTS)
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
        String uid = currentUserId();
        if (uid == null) {
            if (onUrl != null) onUrl.onReady(null);
            return;
        }
        EXEC.execute(() -> {
            String url = null;
            try {
                StorageReference ref = FirebaseStorage.getInstance().getReference()
                        .child("flight_images")
                        .child(uid)
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
        String uid = currentUserId();
        if (uid == null) return;
        EXEC.execute(() -> {
            try {
                Map<String, Object> m = new HashMap<>();
                m.put("imageUrl", imageUrl);
                FirebaseFirestore.getInstance()
                        .collection(USERS).document(uid).collection(SAVED_FLIGHTS)
                        .document(String.valueOf(localId))
                        .set(m, SetOptions.merge());
            } catch (Exception ignored) { }
        });
    }

    public interface SavedFlightsSnapshotListener {
        void onSavedFlightsUpdated(List<FlightSearch> flights);
    }

    public static FlightSearch flightFromFirestoreDoc(DocumentSnapshot d) {
        if (d == null || !d.exists()) return null;
        FlightSearch fs = new FlightSearch();
        Long lid = d.getLong("localId");
        if (lid != null) fs.setId(lid);
        fs.setFromCity(str(d.getString("fromCity")));
        fs.setToCity(str(d.getString("toCity")));
        fs.setDepartureDate(str(d.getString("departureDate")));
        fs.setPrice(str(d.getString("price")));
        fs.setAirline(str(d.getString("airline")));
        fs.setAirlineName(str(d.getString("airlineName")));
        fs.setFlightNumber(str(d.getString("flightNumber")));
        fs.setDepartureTime(str(d.getString("departureTime")));
        fs.setArrivalTime(str(d.getString("arrivalTime")));
        fs.setDuration(str(d.getString("duration")));
        Long stops = d.getLong("stops");
        fs.setStops(stops != null ? stops.intValue() : 0);
        fs.setTripCategory(str(d.getString("tripCategory")));
        fs.setImageUrl(str(d.getString("imageUrl")));
        return fs;
    }

    private static String str(String s) {
        return s != null ? s : "";
    }

    /**
     * Real-time updates: reflects remote changes in the UI without restarting the activity.
     */
    public static ListenerRegistration listenUserSavedFlights(Context context, String uid,
                                                               SavedFlightsSnapshotListener listener) {
        if (!isFirebaseReady(context) || uid == null || uid.isEmpty() || listener == null) {
            return null;
        }
        return FirebaseFirestore.getInstance()
                .collection(USERS).document(uid).collection(SAVED_FLIGHTS)
                .addSnapshotListener((QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
                    if (e != null || snapshots == null) {
                        listener.onSavedFlightsUpdated(new ArrayList<>());
                        return;
                    }
                    List<FlightSearch> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        FlightSearch fs = flightFromFirestoreDoc(doc);
                        if (fs != null && fs.getId() > 0) list.add(fs);
                    }
                    listener.onSavedFlightsUpdated(list);
                });
    }
}
