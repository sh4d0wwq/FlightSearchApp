package com.flightsearch.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flightsearch.app.adapters.FlightOfferAdapter;
import com.flightsearch.app.adapters.SearchAdapter;
import com.flightsearch.app.database.DatabaseHelper;
import com.flightsearch.app.models.FlightOffer;
import com.flightsearch.app.models.FlightSearch;
import com.flightsearch.app.repository.FlightRepository;
import com.flightsearch.app.viewmodel.FlightViewModel;
import com.flightsearch.app.viewmodel.FlightViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private TextInputEditText editTextFrom;
    private TextInputEditText editTextTo;
    private TextInputEditText editTextDepartureDate;
    private MaterialButton buttonSearch;
    private FloatingActionButton fabSettings;

    private View layoutLoading;
    private View layoutResultsSection;
    private MaterialCardView cardNoResults;
    private MaterialCardView cardCacheNote;
    private MaterialCardView cardOfflineBanner;
    private View dividerSections;
    private RecyclerView recyclerViewResults;

    private RecyclerView recyclerViewSavedSearches;
    private MaterialCardView cardViewNoSearches;

    private FlightViewModel viewModel;
    private FlightOfferAdapter offerAdapter;
    private SearchAdapter savedAdapter;
    private DatabaseHelper databaseHelper;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupDatePicker();
        setupRecyclerViews();
        setupViewModel();
        setupListeners();
        loadSavedFlights();
    }

    private void initViews() {
        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);
        editTextDepartureDate = findViewById(R.id.editTextDepartureDate);
        buttonSearch = findViewById(R.id.buttonSearch);
        fabSettings = findViewById(R.id.fabSettings);

        layoutLoading = findViewById(R.id.layoutLoading);
        layoutResultsSection = findViewById(R.id.layoutResultsSection);
        cardNoResults = findViewById(R.id.cardNoResults);
        cardCacheNote = findViewById(R.id.cardCacheNote);
        cardOfflineBanner = findViewById(R.id.cardOfflineBanner);
        dividerSections = findViewById(R.id.dividerSections);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);

        recyclerViewSavedSearches = findViewById(R.id.recyclerViewSavedSearches);
        cardViewNoSearches = findViewById(R.id.cardViewNoSearches);

        databaseHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editTextDepartureDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupDatePicker() {
        editTextDepartureDate.setOnClickListener(v -> {
            DatePickerDialog dlg = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        calendar.set(year, month, day);
                        editTextDepartureDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
            dlg.getDatePicker().setMinDate(thirtyDaysAgo);
            dlg.show();
        });
    }

    private void setupRecyclerViews() {
        offerAdapter = new FlightOfferAdapter(this::openFlightDetails);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(offerAdapter);

        savedAdapter = new SearchAdapter(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FlightSearch flight) {
                Intent intent = new Intent(MainActivity.this, FlightDetailsActivity.class);
                intent.putExtra(FlightDetailsActivity.EXTRA_SEARCH_ID, flight.getId());
                intent.putExtra(FlightDetailsActivity.EXTRA_FROM_SAVED, true);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(FlightSearch flight) {
                Toast.makeText(MainActivity.this,
                        flight.getFromCity() + " → " + flight.getToCity(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewSavedSearches.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSavedSearches.setAdapter(savedAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this,
                new FlightViewModelFactory(getApplication()))
                .get(FlightViewModel.class);

        viewModel.isLoading.observe(this, loading -> {
            layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            buttonSearch.setEnabled(!loading);
        });

        viewModel.isOffline.observe(this, offline ->
                cardOfflineBanner.setVisibility(offline ? View.VISIBLE : View.GONE));

        viewModel.flightOffers.observe(this, offers ->
                offerAdapter.submitList(offers));

        viewModel.hasResults.observe(this, has -> {
            layoutResultsSection.setVisibility(has ? View.VISIBLE : View.GONE);
            dividerSections.setVisibility(has ? View.VISIBLE : View.GONE);
            if (has) cardNoResults.setVisibility(View.GONE);
        });

        viewModel.dataSource.observe(this, source -> {
            if (source == null) { cardCacheNote.setVisibility(View.GONE); return; }
            if (source == FlightRepository.DataSource.CACHE) {
                showNote(getString(R.string.from_cache_note));
            } else {
                cardCacheNote.setVisibility(View.GONE);
            }
        });

        viewModel.errorEvent.observe(this, event -> {
            if (event == null) return;
            if ("no_results".equals(event)) {
                layoutResultsSection.setVisibility(View.VISIBLE);
                cardNoResults.setVisibility(View.VISIBLE);
                dividerSections.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupListeners() {
        buttonSearch.setOnClickListener(v -> performSearch());
        fabSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void performSearch() {
        String from = text(editTextFrom);
        String to = text(editTextTo);
        String date = text(editTextDepartureDate);

        if (from.isEmpty() || to.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.checkNetworkStatus();
        viewModel.searchFlights(from, to, date);
    }

    private void openFlightDetails(FlightOffer offer) {
        Intent intent = new Intent(this, FlightDetailsActivity.class);
        intent.putExtra(FlightDetailsActivity.EXTRA_OFFER_JSON, new Gson().toJson(offer));
        intent.putExtra(FlightDetailsActivity.EXTRA_FROM_SAVED, false);
        startActivity(intent);
    }

    private void loadSavedFlights() {
        List<FlightSearch> saved = databaseHelper.getAllSearches();
        boolean empty = saved == null || saved.isEmpty();
        cardViewNoSearches.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerViewSavedSearches.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) savedAdapter.submitList(saved);
    }

    private void showNote(String text) {
        android.widget.TextView tv = cardCacheNote.findViewById(R.id.tvCacheNote);
        if (tv != null) tv.setText(text);
        cardCacheNote.setVisibility(View.VISIBLE);
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.checkNetworkStatus();
        loadSavedFlights();
    }
}
