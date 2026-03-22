package com.flightsearch.app.utils;

import java.util.HashMap;
import java.util.Map;

public class AirlineMapper {

    private static final Map<String, String> MAP = new HashMap<>();

    static {
        put("AFL", "Aeroflot");
        put("SBI", "S7 Airlines");
        put("SVR", "Ural Airlines");
        put("PDV", "Pobeda");
        put("VKT", "UTair");
        put("SDM", "Rossiya Airlines");
        put("SUN", "Nordwind Airlines");
        put("NWS", "NordStar");
        put("AZT", "Azimuth");
        put("SKV", "Sky Express");
        put("BRZ", "Aurora");
        put("LYN", "Yamal Airlines");
        put("OMS", "iFly");
        put("IRM", "Ikar");
        put("ART", "SmartAvia");
        put("IRA", "Mahan Air");
        put("BEE", "BelAvia");
        put("AUI", "Ukraine International");
        put("KZR", "Air Astana");
        put("UZB", "Uzbekistan Airways");
        put("AHY", "Azerbaijan Airlines");
        put("GNT", "Georgian Airways");

        put("DLH", "Lufthansa");
        put("AFR", "Air France");
        put("BAW", "British Airways");
        put("KLM", "KLM");
        put("IBE", "Iberia");
        put("AZA", "ITA Airways");
        put("AUA", "Austrian Airlines");
        put("SWR", "Swiss International");
        put("BEL", "Brussels Airlines");
        put("CSA", "Czech Airlines");
        put("LOT", "LOT Polish Airlines");
        put("MAH", "Malév");
        put("ROT", "TAROM");
        put("EZY", "easyJet");
        put("RYR", "Ryanair");
        put("VLG", "Vueling");
        put("NAX", "Norwegian");
        put("SAS", "Scandinavian Airlines");
        put("FIN", "Finnair");
        put("TRA", "Transavia");
        put("WZZ", "Wizz Air");

        put("THY", "Turkish Airlines");
        put("UAE", "Emirates");
        put("QTR", "Qatar Airways");
        put("ETD", "Etihad Airways");
        put("FDB", "flydubai");
        put("SVA", "Saudia");

        put("CCA", "Air China");
        put("CSN", "China Southern");
        put("CES", "China Eastern");
        put("JAL", "Japan Airlines");
        put("ANA", "All Nippon Airways");
        put("KAL", "Korean Air");
        put("AAR", "Asiana Airlines");
        put("THA", "Thai Airways");
        put("SIA", "Singapore Airlines");
        put("CPA", "Cathay Pacific");
        put("AIC", "Air India");

        put("AAL", "American Airlines");
        put("UAL", "United Airlines");
        put("DAL", "Delta Air Lines");
        put("SWA", "Southwest Airlines");
        put("WJA", "WestJet");
        put("ACA", "Air Canada");
        put("GLO", "Gol");
        put("TAM", "LATAM Airlines");

        put("MSR", "EgyptAir");
        put("RAM", "Royal Air Maroc");
        put("ETH", "Ethiopian Airlines");
        put("SAA", "South African Airways");
    }

    private static void put(String code, String name) {
        MAP.put(code.toUpperCase(), name);
    }

    public static String getName(String icaoCode) {
        if (icaoCode == null || icaoCode.isEmpty()) return "Unknown Airline";
        String name = MAP.get(icaoCode.toUpperCase());
        return name != null ? name : icaoCode.toUpperCase();
    }
}
