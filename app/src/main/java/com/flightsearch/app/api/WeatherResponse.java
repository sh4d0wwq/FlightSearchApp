package com.flightsearch.app.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    private double latitude;
    private double longitude;
    private String timezone;

    @SerializedName("daily")
    private DailyData daily;

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimezone() { return timezone; }
    public DailyData getDaily() { return daily; }

    public static class DailyData {
        private List<String> time;

        @SerializedName("temperature_2m_max")
        private List<Double> tempMax;

        @SerializedName("temperature_2m_min")
        private List<Double> tempMin;

        @SerializedName("precipitation_probability_max")
        private List<Integer> precipitationProbability;

        @SerializedName("weathercode")
        private List<Integer> weatherCode;

        public List<String> getTime() { return time; }
        public List<Double> getTempMax() { return tempMax; }
        public List<Double> getTempMin() { return tempMin; }
        public List<Integer> getPrecipitationProbability() { return precipitationProbability; }
        public List<Integer> getWeatherCode() { return weatherCode; }
    }
}
