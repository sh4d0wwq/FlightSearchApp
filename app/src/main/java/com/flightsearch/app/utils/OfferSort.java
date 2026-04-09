package com.flightsearch.app.utils;

import com.flightsearch.app.models.FlightOffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class OfferSort {

    public enum OfferSortOption {
        PRICE_ASC,
        PRICE_DESC,
        DURATION_ASC,
        STOPS_ASC
    }

    private OfferSort() {}

    public static List<FlightOffer> sort(List<FlightOffer> offers, OfferSortOption opt) {
        if (offers == null) return new ArrayList<>();
        List<FlightOffer> copy = new ArrayList<>(offers);
        Comparator<FlightOffer> c;
        if (opt == null) opt = OfferSortOption.PRICE_ASC;
        switch (opt) {
            case PRICE_DESC:
                c = (a, b) -> Double.compare(parsePrice(b.getPrice()), parsePrice(a.getPrice()));
                break;
            case DURATION_ASC:
                c = Comparator.comparingInt(OfferSort::durationMinutes);
                break;
            case STOPS_ASC:
                c = Comparator.comparingInt(FlightOffer::getStops);
                break;
            case PRICE_ASC:
            default:
                c = (a, b) -> Double.compare(parsePrice(a.getPrice()), parsePrice(b.getPrice()));
                break;
        }
        Collections.sort(copy, c);
        return copy;
    }

    private static double parsePrice(String p) {
        if (p == null) return Double.MAX_VALUE;
        String d = p.replaceAll("[^0-9.]", "");
        try {
            return d.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(d);
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    private static int durationMinutes(FlightOffer o) {
        String dur = o.getDuration();
        if (dur == null) return Integer.MAX_VALUE;
        int h = 0, m = 0;
        try {
            if (dur.contains("h")) {
                String[] parts = dur.split("h");
                h = Integer.parseInt(parts[0].trim().replaceAll("[^0-9]", ""));
                if (parts.length > 1) {
                    m = Integer.parseInt(parts[1].replaceAll("[^0-9]", "").trim());
                }
            } else if (dur.endsWith("m")) {
                m = Integer.parseInt(dur.replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) { }
        return h * 60 + m;
    }
}
