package com.flightsearch.app.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodingResponse {

    @SerializedName("results")
    private List<GeoResult> results;

    public List<GeoResult> getResults() { return results; }

    public static class GeoResult {
        private String name;
        private double latitude;
        private double longitude;
        private String country;

        @SerializedName("admin1")
        private String region;

        public String getName() { return name; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getCountry() { return country; }
        public String getRegion() { return region; }
    }
}
