package com.flightsearch.app.utils;

import com.flightsearch.app.models.FlightSearch;

import java.util.Locale;

public final class FuzzySearch {

    private FuzzySearch() {}

    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    public static int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = ca == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] t = prev;
            prev = cur;
            cur = t;
        }
        return prev[m];
    }

    public static boolean matchesFuzzy(String haystack, String needle, int maxDistance) {
        String h = normalize(haystack);
        String n = normalize(needle);
        if (n.isEmpty()) return true;
        if (h.contains(n)) return true;
        if (h.length() <= 64 && n.length() <= 32 && levenshtein(h, n) <= maxDistance) return true;
        for (String token : h.split("[\\s,·—\\-]+")) {
            if (token.isEmpty()) continue;
            if (token.contains(n) || (token.length() <= 32 && n.length() <= 32
                    && levenshtein(token, n) <= maxDistance)) {
                return true;
            }
        }
        return false;
    }

    public static boolean flightMatchesQuery(FlightSearch f, String query, int maxDistance) {
        if (query == null || normalize(query).isEmpty()) return true;
        String q = normalize(query);
        String blob = normalize(f.getFromCity()) + " "
                + normalize(f.getToCity()) + " "
                + normalize(f.getDepartureDate()) + " "
                + normalize(f.getPrice()) + " "
                + normalize(f.getAirline()) + " "
                + normalize(f.getAirlineName()) + " "
                + normalize(f.getFlightNumber()) + " "
                + normalize(f.getTripCategory());
        return matchesFuzzy(blob, q, maxDistance);
    }
}
