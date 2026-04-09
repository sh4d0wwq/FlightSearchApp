package com.flightsearch.app.utils;

import com.flightsearch.app.models.FlightSearch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class SavedFlightsQuery {

    public enum SortOption {
        DATE_ASC,
        DATE_DESC,
        PRICE_ASC,
        PRICE_DESC,
        ROUTE_AZ
    }

    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private SavedFlightsQuery() {}

    public static List<FlightSearch> apply(List<FlightSearch> source,
                                           String fuzzyQuery,
                                           String categoryFilter,
                                           boolean directOnly,
                                           SortOption sort) {
        if (source == null) return new ArrayList<>();
        List<FlightSearch> out = new ArrayList<>();
        for (FlightSearch f : source) {
            if (f == null) continue;
            if (!FuzzySearch.flightMatchesQuery(f, fuzzyQuery, 2)) continue;
            if (categoryFilter != null && !categoryFilter.isEmpty()) {
                String c = f.getTripCategory();
                if (c == null || !categoryFilter.equalsIgnoreCase(c)) continue;
            }
            if (directOnly && f.getStops() > 0) continue;
            out.add(f);
        }
        Comparator<FlightSearch> cmp = comparatorFor(sort);
        if (cmp != null) Collections.sort(out, cmp);
        return out;
    }

    private static Comparator<FlightSearch> comparatorFor(SortOption sort) {
        if (sort == null) sort = SortOption.DATE_DESC;
        switch (sort) {
            case DATE_ASC:
                return (a, b) -> Long.compare(dateMillis(a), dateMillis(b));
            case DATE_DESC:
                return (a, b) -> Long.compare(dateMillis(b), dateMillis(a));
            case PRICE_ASC:
                return Comparator.comparingDouble(SavedFlightsQuery::priceValue);
            case PRICE_DESC:
                return (a, b) -> Double.compare(priceValue(b), priceValue(a));
            case ROUTE_AZ:
                return Comparator.comparing((FlightSearch f) -> routeKey(f), String.CASE_INSENSITIVE_ORDER);
            default:
                return (a, b) -> Long.compare(dateMillis(b), dateMillis(a));
        }
    }

    private static long dateMillis(FlightSearch f) {
        try {
            Date d = ISO.parse(f.getDepartureDate());
            return d != null ? d.getTime() : 0L;
        } catch (ParseException e) {
            return 0L;
        }
    }

    private static double priceValue(FlightSearch f) {
        String p = f.getPrice();
        if (p == null) return Double.MAX_VALUE;
        String digits = p.replaceAll("[^0-9.]", "");
        try {
            if (digits.isEmpty()) return Double.MAX_VALUE;
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    private static String routeKey(FlightSearch f) {
        String a = f.getFromCity() != null ? f.getFromCity() : "";
        String b = f.getToCity() != null ? f.getToCity() : "";
        return a + " → " + b;
    }
}
