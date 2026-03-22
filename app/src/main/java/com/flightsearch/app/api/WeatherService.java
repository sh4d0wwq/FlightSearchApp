package com.flightsearch.app.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {

    @GET("v1/forecast")
    Call<WeatherResponse> getForecast(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("daily") String dailyVariables,
            @Query("timezone") String timezone,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}
