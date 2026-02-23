package com.flightsearch.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flightsearch.app.adapters.SearchAdapter;
import com.flightsearch.app.database.DatabaseHelper;
import com.flightsearch.app.models.FlightSearch;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

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
    private RecyclerView recyclerViewSavedSearches;
    private MaterialCardView cardViewNoSearches;

    private DatabaseHelper databaseHelper;
    private SearchAdapter searchAdapter;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupDatabase();
        setupDatePicker();
        setupRecyclerView();
        loadSavedSearches();
        setupListeners();
    }

    private void initializeViews() {
        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);
        editTextDepartureDate = findViewById(R.id.editTextDepartureDate);
        buttonSearch = findViewById(R.id.buttonSearch);
        fabSettings = findViewById(R.id.fabSettings);
        recyclerViewSavedSearches = findViewById(R.id.recyclerViewSavedSearches);
        cardViewNoSearches = findViewById(R.id.cardViewNoSearches);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editTextDepartureDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupDatePicker() {
        editTextDepartureDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        editTextDepartureDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void setupRecyclerView() {
        recyclerViewSavedSearches.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadSavedSearches() {
        List<FlightSearch> searches = databaseHelper.getAllSearches();

        if (searches == null || searches.isEmpty()) {
            cardViewNoSearches.setVisibility(View.VISIBLE);
            recyclerViewSavedSearches.setVisibility(View.GONE);
        } else {
            cardViewNoSearches.setVisibility(View.GONE);
            recyclerViewSavedSearches.setVisibility(View.VISIBLE);

            if (searchAdapter == null) {
                searchAdapter = new SearchAdapter(new SearchAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(FlightSearch search) {
                        Intent intent = new Intent(MainActivity.this, FlightDetailsActivity.class);
                        intent.putExtra("SEARCH_ID", search.getId());
                        intent.putExtra("FROM_SAVED", true);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(FlightSearch search) {
                        Toast.makeText(MainActivity.this,
                                search.getFromCity() + " → " + search.getToCity(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
                recyclerViewSavedSearches.setAdapter(searchAdapter);
            }
            searchAdapter.submitList(searches);
        }
    }

    private void setupListeners() {
        buttonSearch.setOnClickListener(v -> performSearch());
        fabSettings.setOnClickListener(v -> openSettings());
    }

    private void performSearch() {
        String from = editTextFrom.getText() != null ? editTextFrom.getText().toString().trim() : "";
        String to = editTextTo.getText() != null ? editTextTo.getText().toString().trim() : "";
        String date = editTextDepartureDate.getText() != null ? editTextDepartureDate.getText().toString().trim() : "";

        if (from.isEmpty() || to.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int randomPrice = 100 + (int)(Math.random() * 900);
        String price = "$" + randomPrice;

        Intent intent = new Intent(MainActivity.this, FlightDetailsActivity.class);
        intent.putExtra("FROM_CITY", from);
        intent.putExtra("TO_CITY", to);
        intent.putExtra("DEPARTURE_DATE", date);
        intent.putExtra("PRICE", price);
        intent.putExtra("FROM_SAVED", false);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedSearches();
    }
}