package com.flightsearch.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flightsearch.app.api.GeocodingResponse;
import com.flightsearch.app.api.GeocodingService;
import com.flightsearch.app.api.RetrofitClient;
import com.flightsearch.app.api.WeatherResponse;
import com.flightsearch.app.api.WeatherService;
import com.flightsearch.app.database.AppDatabase;
import com.flightsearch.app.database.DatabaseHelper;
import com.flightsearch.app.database.WeatherCacheDao;
import com.flightsearch.app.database.WeatherCacheEntity;
import com.flightsearch.app.models.FlightOffer;
import com.flightsearch.app.models.FlightSearch;
import com.flightsearch.app.models.WeatherInfo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlightDetailsActivity extends BaseActivity {

    public static final String EXTRA_OFFER_JSON = "OFFER_JSON";
    public static final String EXTRA_SEARCH_ID  = "SEARCH_ID";
    public static final String EXTRA_FROM_SAVED = "FROM_SAVED";

    private static final long WEATHER_CACHE_TTL = 6L * 60 * 60 * 1000;

    private TextView textViewRoute;
    private TextView textViewFrom;
    private TextView textViewTo;
    private TextView textViewDate;
    private TextView textViewPrice;
    private TextView textViewAirlineName;
    private TextView textViewFlightNumber;
    private TextView textViewTimes;
    private TextView textViewDurationStops;
    private MaterialButton buttonSave;
    private MaterialButton buttonDelete;

    private MaterialCardView cardWeather;
    private TextView tvWeatherCity;
    private View layoutWeatherLoading;
    private View layoutWeatherData;
    private TextView tvWeatherEmoji;
    private TextView tvWeatherDescription;
    private TextView tvWeatherTemp;
    private TextView tvWeatherPrecip;
    private TextView tvWeatherError;

    private DatabaseHelper databaseHelper;
    private WeatherCacheDao weatherCacheDao;
    private final Gson gson = new Gson();

    private FlightSearch currentSaved;
    private FlightOffer currentOffer;
    private boolean isFromSaved;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.flight_details);
        }

        databaseHelper = new DatabaseHelper(this);
        weatherCacheDao = AppDatabase.getInstance(this).weatherCacheDao();
        initViews();
        loadFlightData();
        setupListeners();
    }

    private void initViews() {
        textViewRoute         = findViewById(R.id.textViewRoute);
        textViewFrom          = findViewById(R.id.textViewFrom);
        textViewTo            = findViewById(R.id.textViewTo);
        textViewDate          = findViewById(R.id.textViewDate);
        textViewPrice         = findViewById(R.id.textViewPrice);
        textViewAirlineName   = findViewById(R.id.textViewAirlineName);
        textViewFlightNumber  = findViewById(R.id.textViewFlightNumber);
        textViewTimes         = findViewById(R.id.textViewTimes);
        textViewDurationStops = findViewById(R.id.textViewDurationStops);
        buttonSave            = findViewById(R.id.buttonSave);
        buttonDelete          = findViewById(R.id.buttonDelete);

        cardWeather           = findViewById(R.id.cardWeather);
        tvWeatherCity         = findViewById(R.id.tvWeatherCity);
        layoutWeatherLoading  = findViewById(R.id.layoutWeatherLoading);
        layoutWeatherData     = findViewById(R.id.layoutWeatherData);
        tvWeatherEmoji        = findViewById(R.id.tvWeatherEmoji);
        tvWeatherDescription  = findViewById(R.id.tvWeatherDescription);
        tvWeatherTemp         = findViewById(R.id.tvWeatherTemp);
        tvWeatherPrecip       = findViewById(R.id.tvWeatherPrecip);
        tvWeatherError        = findViewById(R.id.tvWeatherError);
    }

    private void loadFlightData() {
        Intent intent = getIntent();
        isFromSaved = intent.getBooleanExtra(EXTRA_FROM_SAVED, false);

        if (isFromSaved) {
            long id = intent.getLongExtra(EXTRA_SEARCH_ID, -1);
            if (id < 0) { finish(); return; }
            currentSaved = databaseHelper.getSearch(id);
            if (currentSaved == null) { finish(); return; }
            displayFromSaved(currentSaved);
            buttonSave.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.VISIBLE);
            loadWeather(currentSaved.getToCity(), currentSaved.getDepartureDate());
        } else {
            String json = intent.getStringExtra(EXTRA_OFFER_JSON);
            if (json == null) { finish(); return; }
            currentOffer = gson.fromJson(json, FlightOffer.class);
            if (currentOffer == null) { finish(); return; }
            displayFromOffer(currentOffer);
            buttonSave.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.GONE);
            loadWeather(currentOffer.getToCity(), currentOffer.getDepartureDate());
        }
    }

    private void displayFromOffer(FlightOffer offer) {
        setRoute(offer.getFromCity(), offer.getToCity());
        textViewFrom.setText(formatCity(offer.getFromCity(), offer.getFromCode()));
        textViewTo.setText(formatCity(offer.getToCity(), offer.getToCode()));
        textViewDate.setText(nvl(offer.getDepartureDate(), "—"));
        textViewPrice.setText(offer.getFormattedPrice());
        String airline = offer.getAirlineName() != null ? offer.getAirlineName() : offer.getAirline();
        textViewAirlineName.setText(nvl(airline, "—"));
        textViewFlightNumber.setText(nvl(offer.getFlightNumber(), ""));
        textViewTimes.setText(formatTimes(offer.getDepartureTime(), offer.getArrivalTime()));
        textViewDurationStops.setText(formatDurationStops(offer.getDuration(), offer.getStops()));
    }

    private void displayFromSaved(FlightSearch flight) {
        setRoute(flight.getFromCity(), flight.getToCity());
        textViewFrom.setText(nvl(flight.getFromCity(), "—"));
        textViewTo.setText(nvl(flight.getToCity(), "—"));
        textViewDate.setText(nvl(flight.getDepartureDate(), "—"));
        textViewPrice.setText(nvl(flight.getPrice(), "—"));
        String airline = flight.getAirlineName() != null ? flight.getAirlineName() : flight.getAirline();
        textViewAirlineName.setText(nvl(airline, "—"));
        textViewFlightNumber.setText(nvl(flight.getFlightNumber(), ""));
        textViewTimes.setText(formatTimes(flight.getDepartureTime(), flight.getArrivalTime()));
        textViewDurationStops.setText(formatDurationStops(flight.getDuration(), flight.getStops()));
    }

    private void setupListeners() {
        buttonSave.setOnClickListener(v -> saveFlight());
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void saveFlight() {
        if (currentOffer == null) return;
        long id = databaseHelper.addSearch(currentOffer.toFlightSearch());
        if (id > 0) {
            Toast.makeText(this, R.string.flight_saved, Toast.LENGTH_SHORT).show();
            buttonSave.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_flight)
                .setMessage(R.string.confirm_delete_flight)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (currentSaved != null) {
                        databaseHelper.deleteSearch(currentSaved.getId());
                        Toast.makeText(this, R.string.flight_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadWeather(String city, String date) {
        if (city == null || city.isEmpty() || date == null || date.isEmpty()) {
            cardWeather.setVisibility(View.GONE);
            return;
        }

        tvWeatherCity.setText(city + " · " + date);
        showWeatherState(WeatherState.LOADING);

        String cacheKey = (city + "_" + date).toLowerCase(Locale.US).replaceAll("\\s+", "_");

        executor.execute(() -> {
            long now = System.currentTimeMillis();
            weatherCacheDao.deleteExpired(now - WEATHER_CACHE_TTL);

            WeatherCacheEntity cached = weatherCacheDao.getByKey(cacheKey);
            if (cached != null && (now - cached.cachedAt) < WEATHER_CACHE_TTL) {
                WeatherInfo info = gson.fromJson(cached.weatherJson, WeatherInfo.class);
                mainHandler.post(() -> displayWeather(info));
                return;
            }

            try {
                GeocodingService geocodingService = RetrofitClient.getGeocodingService();
                WeatherService weatherService = RetrofitClient.getWeatherService();

                retrofit2.Response<GeocodingResponse> geoResp = geocodingService
                        .search(city, 1, "en", "json")
                        .execute();

                if (!geoResp.isSuccessful() || geoResp.body() == null
                        || geoResp.body().getResults() == null
                        || geoResp.body().getResults().isEmpty()) {
                    postWeatherError(getString(R.string.weather_city_not_found));
                    return;
                }

                GeocodingResponse.GeoResult geo = geoResp.body().getResults().get(0);

                retrofit2.Response<WeatherResponse> weatherResp = weatherService.getForecast(
                        geo.getLatitude(), geo.getLongitude(),
                        "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode",
                        "auto",
                        date, date
                ).execute();

                if (!weatherResp.isSuccessful() || weatherResp.body() == null
                        || weatherResp.body().getDaily() == null) {
                    postWeatherError(getString(R.string.weather_unavailable));
                    return;
                }

                WeatherResponse.DailyData daily = weatherResp.body().getDaily();
                if (daily.getTime() == null || daily.getTime().isEmpty()) {
                    postWeatherError(getString(R.string.weather_no_data));
                    return;
                }

                WeatherInfo info = new WeatherInfo(
                        safeInt(daily.getWeatherCode(), 0),
                        safeDouble(daily.getTempMax(), 0),
                        safeDouble(daily.getTempMin(), 0),
                        safeInt(daily.getPrecipitationProbability(), 0)
                );

                WeatherCacheEntity entry = new WeatherCacheEntity(cacheKey, gson.toJson(info), now);
                weatherCacheDao.insert(entry);

                mainHandler.post(() -> displayWeather(info));

            } catch (Exception e) {
                postWeatherError(getString(R.string.weather_unavailable));
            }
        });
    }

    private void displayWeather(WeatherInfo info) {
        tvWeatherEmoji.setText(weatherEmoji(info.weatherCode));
        tvWeatherDescription.setText(weatherDescription(info.weatherCode));
        tvWeatherTemp.setText(String.format(Locale.US,
                getString(R.string.weather_temp_format),
                Math.round(info.tempMax), Math.round(info.tempMin)));
        tvWeatherPrecip.setText(String.format(
                getString(R.string.weather_precip_format), info.precipitation));
        showWeatherState(WeatherState.DATA);
    }

    private enum WeatherState { LOADING, DATA, ERROR }

    private void showWeatherState(WeatherState state) {
        layoutWeatherLoading.setVisibility(state == WeatherState.LOADING ? View.VISIBLE : View.GONE);
        layoutWeatherData.setVisibility(state == WeatherState.DATA ? View.VISIBLE : View.GONE);
        tvWeatherError.setVisibility(state == WeatherState.ERROR ? View.VISIBLE : View.GONE);
    }

    private void postWeatherError(String msg) {
        mainHandler.post(() -> {
            tvWeatherError.setText(msg);
            showWeatherState(WeatherState.ERROR);
        });
    }

    private String weatherEmoji(int code) {
        if (code == 0)  return "☀️";
        if (code <= 2)  return "🌤️";
        if (code == 3)  return "☁️";
        if (code <= 48) return "🌫️";
        if (code <= 55) return "🌦️";
        if (code <= 65) return "🌧️";
        if (code <= 75) return "❄️";
        if (code <= 82) return "🌦️";
        if (code <= 86) return "🌨️";
        if (code >= 95) return "⛈️";
        return "🌡️";
    }

    private String weatherDescription(int code) {
        if (code == 0)  return getString(R.string.weather_clear);
        if (code <= 2)  return getString(R.string.weather_partly_cloudy);
        if (code == 3)  return getString(R.string.weather_overcast);
        if (code <= 48) return getString(R.string.weather_foggy);
        if (code <= 55) return getString(R.string.weather_drizzle);
        if (code <= 65) return getString(R.string.weather_rainy);
        if (code <= 75) return getString(R.string.weather_snowy);
        if (code <= 82) return getString(R.string.weather_showers);
        if (code <= 86) return getString(R.string.weather_snow_showers);
        if (code >= 95) return getString(R.string.weather_thunderstorm);
        return getString(R.string.weather_variable);
    }

    private void setRoute(String from, String to) {
        if (textViewRoute != null)
            textViewRoute.setText(nvl(from, "?") + " → " + nvl(to, "?"));
    }

    private String formatCity(String city, String code) {
        if (city == null || city.isEmpty()) return nvl(code, "—");
        if (code != null && !code.isEmpty() && !code.equalsIgnoreCase(city))
            return city + " (" + code + ")";
        return city;
    }

    private String formatTimes(String dep, String arr) {
        if (dep != null && !dep.isEmpty() && arr != null && !arr.isEmpty())
            return dep + " → " + arr;
        return "—";
    }

    private String formatDurationStops(String duration, int stops) {
        String dur = nvl(duration, "");
        String stopsStr = stopsText(stops);
        return dur.isEmpty() ? stopsStr : dur + " · " + stopsStr;
    }

    private String stopsText(int stops) {
        if (stops == 0) return getString(R.string.stops_direct);
        if (stops == 1) return getString(R.string.stops_one);
        return getString(R.string.stops_format, stops);
    }

    private String nvl(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    private double safeDouble(List<Double> list, int idx) {
        return (list != null && idx < list.size() && list.get(idx) != null)
                ? list.get(idx) : 0.0;
    }

    private int safeInt(List<Integer> list, int idx) {
        return (list != null && idx < list.size() && list.get(idx) != null)
                ? list.get(idx) : 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
