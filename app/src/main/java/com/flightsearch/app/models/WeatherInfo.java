package com.flightsearch.app.models;

public class WeatherInfo {
    public int weatherCode;
    public double tempMax;
    public double tempMin;
    public int precipitation;

    public WeatherInfo() {}

    public WeatherInfo(int weatherCode, double tempMax, double tempMin, int precipitation) {
        this.weatherCode = weatherCode;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.precipitation = precipitation;
    }
}
