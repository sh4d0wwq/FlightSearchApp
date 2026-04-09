package com.flightsearch.app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flightsearch.app.adapters.FlightOfferAdapter;
import com.flightsearch.app.adapters.SearchAdapter;
import com.flightsearch.app.database.DatabaseHelper;
import com.flightsearch.app.models.FlightOffer;
import com.flightsearch.app.models.FlightSearch;
import com.flightsearch.app.repository.FlightRepository;
import com.flightsearch.app.utils.OfferSort;
import com.flightsearch.app.utils.SavedFlightsQuery;
import com.flightsearch.app.viewmodel.FlightViewModel;
import com.flightsearch.app.viewmodel.FlightViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final int REQ_POST_NOTIFICATIONS = 1001;

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
    private Spinner spinnerOfferSort;

    private RecyclerView recyclerViewSavedSearches;
    private MaterialCardView cardViewNoSearches;
    private TextView tvNoSavedMessage;
    private TextInputEditText editFilterSaved;
    private Chip chipDirectOnly;
    private ChipGroup chipGroupCategory;
    private Spinner spinnerSavedSort;

    private FlightViewModel viewModel;
    private FlightOfferAdapter offerAdapter;
    private SearchAdapter savedAdapter;
    private DatabaseHelper databaseHelper;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private List<FlightOffer> lastOffers = new ArrayList<>();
    private List<FlightSearch> allSavedCached = new ArrayList<>();

    private int offerSortIndex;
    private int savedSortIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maybeRequestNotificationPermission();
        initViews();
        setupSavedFilterUi();
        setupDatePicker();
        setupRecyclerViews();
        setupViewModel();
        setupListeners();
        loadSavedFlights();
    }

    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        spinnerOfferSort = findViewById(R.id.spinnerOfferSort);

        recyclerViewSavedSearches = findViewById(R.id.recyclerViewSavedSearches);
        cardViewNoSearches = findViewById(R.id.cardViewNoSearches);
        tvNoSavedMessage = findViewById(R.id.tvNoSavedMessage);
        editFilterSaved = findViewById(R.id.editFilterSaved);
        chipDirectOnly = findViewById(R.id.chipDirectOnly);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        spinnerSavedSort = findViewById(R.id.spinnerSavedSort);

        databaseHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editTextDepartureDate.setText(dateFormat.format(calendar.getTime()));

        ArrayAdapter<CharSequence> offerAd = ArrayAdapter.createFromResource(this,
                R.array.offer_sort_labels, android.R.layout.simple_spinner_dropdown_item);
        spinnerOfferSort.setAdapter(offerAd);
        spinnerOfferSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                offerSortIndex = position;
                applyOfferSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> savedAd = ArrayAdapter.createFromResource(this,
                R.array.saved_sort_labels, android.R.layout.simple_spinner_dropdown_item);
        spinnerSavedSort.setAdapter(savedAd);
        spinnerSavedSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savedSortIndex = position;
                applySavedFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupSavedFilterUi() {
        editFilterSaved.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySavedFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        chipDirectOnly.setOnCheckedChangeListener((b, c) -> applySavedFilters());

        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> applySavedFilters());
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

        viewModel.flightOffers.observe(this, offers -> {
            lastOffers = offers != null ? new ArrayList<>(offers) : new ArrayList<>();
            applyOfferSort();
        });

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

    private void applyOfferSort() {
        OfferSort.OfferSortOption opt;
        switch (offerSortIndex) {
            case 1:
                opt = OfferSort.OfferSortOption.PRICE_DESC;
                break;
            case 2:
                opt = OfferSort.OfferSortOption.DURATION_ASC;
                break;
            case 3:
                opt = OfferSort.OfferSortOption.STOPS_ASC;
                break;
            case 0:
            default:
                opt = OfferSort.OfferSortOption.PRICE_ASC;
                break;
        }
        offerAdapter.submitList(OfferSort.sort(lastOffers, opt));
    }

    private SavedFlightsQuery.SortOption savedSortOption() {
        switch (savedSortIndex) {
            case 1:
                return SavedFlightsQuery.SortOption.DATE_ASC;
            case 2:
                return SavedFlightsQuery.SortOption.PRICE_ASC;
            case 3:
                return SavedFlightsQuery.SortOption.PRICE_DESC;
            case 4:
                return SavedFlightsQuery.SortOption.ROUTE_AZ;
            case 0:
            default:
                return SavedFlightsQuery.SortOption.DATE_DESC;
        }
    }

    private String selectedCategoryFilterKey() {
        int id = chipGroupCategory.getCheckedChipId();
        if (id == R.id.chipCatLeisure) return "leisure";
        if (id == R.id.chipCatBusiness) return "business";
        if (id == R.id.chipCatFamily) return "family";
        return null;
    }

    private void applySavedFilters() {
        String q = editFilterSaved.getText() != null ? editFilterSaved.getText().toString().trim() : "";
        boolean direct = chipDirectOnly.isChecked();
        String cat = selectedCategoryFilterKey();
        List<FlightSearch> filtered = SavedFlightsQuery.apply(
                allSavedCached, q, cat, direct, savedSortOption());

        boolean noData = allSavedCached.isEmpty();
        boolean noMatch = !noData && filtered.isEmpty();
        cardViewNoSearches.setVisibility(noData || noMatch ? View.VISIBLE : View.GONE);
        recyclerViewSavedSearches.setVisibility(noData || noMatch ? View.GONE : View.VISIBLE);
        if (tvNoSavedMessage != null) {
            tvNoSavedMessage.setText(noData
                    ? getString(R.string.no_saved_flights)
                    : getString(R.string.no_filter_results));
        }
        savedAdapter.submitList(noMatch ? new ArrayList<>() : filtered);
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
        allSavedCached = databaseHelper.getAllSearches();
        if (allSavedCached == null) allSavedCached = new ArrayList<>();
        applySavedFilters();
    }

    private void showNote(String text) {
        TextView tv = cardCacheNote.findViewById(R.id.tvCacheNote);
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
