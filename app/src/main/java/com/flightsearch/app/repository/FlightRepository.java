package com.flightsearch.app.repository;

import android.os.Handler;
import android.os.Looper;

import com.flightsearch.app.models.FlightOffer;
import com.flightsearch.app.utils.DurationParser;
import com.flightsearch.app.utils.IataMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlightRepository {

    public enum DataSource { MOCK, CACHE }

    public interface SearchCallback {
        void onSuccess(List<FlightOffer> offers, DataSource source);
        void onError(String message);
    }

    private static final String[][] AIRLINES = {
            {"AFL", "Aeroflot"},
            {"SBI", "S7 Airlines"},
            {"THY", "Turkish Airlines"},
            {"DLH", "Lufthansa"},
            {"AFR", "Air France"},
            {"BAW", "British Airways"},
            {"UAE", "Emirates"},
            {"QTR", "Qatar Airways"},
            {"KLM", "KLM"},
            {"FDB", "flydubai"},
            {"SVR", "Ural Airlines"},
            {"PDV", "Pobeda"},
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void searchFlights(String fromCity, String toCity, String date,
                              SearchCallback callback) {
        executor.execute(() -> {
            String fromCode = IataMapper.getCode(fromCity);
            String toCode = IataMapper.getCode(toCity);
            List<FlightOffer> offers = generateOffers(fromCity, fromCode, toCity, toCode, date);
            mainHandler.post(() -> callback.onSuccess(offers, DataSource.MOCK));
        });
    }

    private List<FlightOffer> generateOffers(String fromCity, String fromCode,
                                              String toCity, String toCode, String date) {
        List<FlightOffer> list = new ArrayList<>();
        Random rnd = new Random((long) (fromCode + toCode + date).hashCode() * 31L);

        int count = 5 + rnd.nextInt(4);
        for (int i = 0; i < count; i++) {
            String[] al = AIRLINES[rnd.nextInt(AIRLINES.length)];
            int fNum = 100 + rnd.nextInt(8900);
            int depH = 5 + rnd.nextInt(15);
            int depM = rnd.nextInt(4) * 15;
            int hours = 1 + rnd.nextInt(14);
            int minutes = rnd.nextInt(60);
            int arrH = (depH + hours) % 24;
            int arrM = (depM + minutes) % 60;
            int stops = i < 3 ? 0 : rnd.nextInt(2);
            double price = 80.0 + rnd.nextDouble() * 1200.0;

            FlightOffer o = new FlightOffer();
            o.setOfferId("GEN_" + i);
            o.setAirline(al[0]);
            o.setAirlineName(al[1]);
            o.setFlightNumber(al[0] + " " + fNum);
            o.setFromCity(fromCity);
            o.setFromCode(fromCode);
            o.setToCity(toCity);
            o.setToCode(toCode);
            o.setDepartureDate(date);
            o.setDepartureTime(String.format(Locale.US, "%02d:%02d", depH, depM));
            o.setArrivalTime(String.format(Locale.US, "%02d:%02d", arrH, arrM));
            o.setDuration(DurationParser.fromHoursMinutes(hours, minutes));
            o.setStops(stops);
            o.setPrice(String.format(Locale.US, "%.2f", price));
            o.setCurrency("USD");
            list.add(o);
        }

        list.sort((a, b) -> {
            try {
                return Double.compare(
                        Double.parseDouble(a.getPrice()),
                        Double.parseDouble(b.getPrice()));
            } catch (Exception e) {
                return 0;
            }
        });
        return list;
    }
}
