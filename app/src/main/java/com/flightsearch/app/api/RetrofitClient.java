package com.flightsearch.app.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/";
    private static final String WEATHER_BASE_URL   = "https://api.open-meteo.com/";

    private static GeocodingService geocodingService;
    private static WeatherService   weatherService;

    private static OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private static Retrofit buildRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(buildHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized GeocodingService getGeocodingService() {
        if (geocodingService == null) {
            geocodingService = buildRetrofit(GEOCODING_BASE_URL).create(GeocodingService.class);
        }
        return geocodingService;
    }

    public static synchronized WeatherService getWeatherService() {
        if (weatherService == null) {
            weatherService = buildRetrofit(WEATHER_BASE_URL).create(WeatherService.class);
        }
        return weatherService;
    }
}
