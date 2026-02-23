package com.flightsearch.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flightsearch.app.database.DatabaseHelper;
import com.flightsearch.app.models.FlightSearch;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FlightDetailsActivity extends BaseActivity {

    private TextView textViewFrom;
    private TextView textViewTo;
    private TextView textViewDate;
    private TextView textViewPrice;
    private TextView textViewRoute;
    private MaterialButton buttonSave;
    private MaterialButton buttonEdit;
    private MaterialButton buttonDelete;

    private DatabaseHelper databaseHelper;
    private FlightSearch currentSearch;
    private boolean isFromSaved;
    private long searchId;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.flight_details);
        }

        initializeViews();
        setupDatabase();
        loadIntentData();
        setupListeners();
    }

    private void initializeViews() {
        textViewRoute = findViewById(R.id.textViewRoute);
        textViewFrom = findViewById(R.id.textViewFrom);
        textViewTo = findViewById(R.id.textViewTo);
        textViewDate = findViewById(R.id.textViewDate);
        textViewPrice = findViewById(R.id.textViewPrice);
        buttonSave = findViewById(R.id.buttonSave);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    private void setupDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        isFromSaved = intent.getBooleanExtra("FROM_SAVED", false);

        if (isFromSaved) {
            searchId = intent.getLongExtra("SEARCH_ID", -1);
            if (searchId != -1) {
                currentSearch = databaseHelper.getSearch(searchId);
                if (currentSearch != null) {
                    displayFlightDetails(currentSearch);
                    buttonSave.setVisibility(View.GONE);
                    buttonEdit.setVisibility(View.VISIBLE);
                    buttonDelete.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Error loading search", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            String from = intent.getStringExtra("FROM_CITY");
            String to = intent.getStringExtra("TO_CITY");
            String date = intent.getStringExtra("DEPARTURE_DATE");
            String price = intent.getStringExtra("PRICE");

            currentSearch = new FlightSearch(from, to, date, price);
            displayFlightDetails(currentSearch);
            buttonSave.setVisibility(View.VISIBLE);
            buttonEdit.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
        }
    }

    private void displayFlightDetails(FlightSearch search) {
        String route = search.getFromCity() + " → " + search.getToCity();
        if (textViewRoute != null) {
            textViewRoute.setText(route);
        }
        textViewFrom.setText(search.getFromCity());
        textViewTo.setText(search.getToCity());
        textViewDate.setText(search.getDepartureDate());

        String price = search.getPrice();
        if (price != null && !price.startsWith("$")) {
            price = "$" + price;
        }
        textViewPrice.setText(price);
    }

    private void setupListeners() {
        buttonSave.setOnClickListener(v -> saveSearch());
        buttonEdit.setOnClickListener(v -> showEditDialog());
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void saveSearch() {
        long id = databaseHelper.addSearch(currentSearch);
        if (id > 0) {
            currentSearch.setId(id);
            Toast.makeText(this, R.string.search_saved, Toast.LENGTH_SHORT).show();
            buttonSave.setVisibility(View.GONE);
            buttonEdit.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Error saving search", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_search);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_search, null);
        TextInputEditText editFrom = dialogView.findViewById(R.id.editDialogFrom);
        TextInputEditText editTo = dialogView.findViewById(R.id.editDialogTo);
        TextInputEditText editDate = dialogView.findViewById(R.id.editDialogDate);
        TextInputEditText editPrice = dialogView.findViewById(R.id.editDialogPrice);

        editFrom.setText(currentSearch.getFromCity());
        editTo.setText(currentSearch.getToCity());
        editDate.setText(currentSearch.getDepartureDate());

        String price = currentSearch.getPrice();
        if (price != null && price.startsWith("$")) {
            price = price.substring(1);
        }
        editPrice.setText(price);

        editDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        editDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String from = editFrom.getText().toString().trim();
            String to = editTo.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            String priceStr = editPrice.getText().toString().trim();

            if (!from.isEmpty() && !to.isEmpty() && !date.isEmpty()) {
                if (!priceStr.startsWith("$") && !priceStr.isEmpty()) {
                    priceStr = "$" + priceStr;
                }

                currentSearch.setFromCity(from);
                currentSearch.setToCity(to);
                currentSearch.setDepartureDate(date);
                currentSearch.setPrice(priceStr);

                int updated = databaseHelper.updateSearch(currentSearch);
                if (updated > 0) {
                    displayFlightDetails(currentSearch);
                    Toast.makeText(this, R.string.search_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error updating search", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_search)
                .setMessage("Are you sure you want to delete this search?")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    databaseHelper.deleteSearch(currentSearch.getId());
                    Toast.makeText(this, R.string.search_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}