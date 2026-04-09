package com.flightsearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.flightsearch.app.prefs.AppPreferences;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {
    private RadioGroup radioGroupTheme;
    private RadioButton radioLight;
    private RadioButton radioDark;
    private RadioButton radioSystem;

    private RadioGroup radioGroupLanguage;
    private RadioButton radioEnglish;
    private RadioButton radioRussian;

    private SwitchMaterial switchReminders;
    private Spinner spinnerReminderHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }

        initializeViews();
        loadCurrentSettings();
        setupListeners();
    }

    private void initializeViews() {
        radioGroupTheme = findViewById(R.id.radioGroupTheme);
        radioLight = findViewById(R.id.radioLight);
        radioDark = findViewById(R.id.radioDark);
        radioSystem = findViewById(R.id.radioSystem);

        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        radioEnglish = findViewById(R.id.radioEnglish);
        radioRussian = findViewById(R.id.radioRussian);

        switchReminders = findViewById(R.id.switchReminders);
        spinnerReminderHours = findViewById(R.id.spinnerReminderHours);

        ArrayAdapter<CharSequence> h = ArrayAdapter.createFromResource(this,
                R.array.reminder_hours_labels, android.R.layout.simple_spinner_dropdown_item);
        spinnerReminderHours.setAdapter(h);
    }

    private void loadCurrentSettings() {
        int currentTheme = ThemeHelper.getTheme(this);
        switch (currentTheme) {
            case ThemeHelper.THEME_LIGHT:
                radioLight.setChecked(true);
                break;
            case ThemeHelper.THEME_DARK:
                radioDark.setChecked(true);
                break;
            case ThemeHelper.THEME_SYSTEM:
                radioSystem.setChecked(true);
                break;
        }

        String currentLanguage = LocaleHelper.getLanguage(this);
        if (currentLanguage.equals(LocaleHelper.LANGUAGE_RUSSIAN)) {
            radioRussian.setChecked(true);
        } else {
            radioEnglish.setChecked(true);
        }

        switchReminders.setChecked(AppPreferences.isRemindersEnabled(this));
        int hours = AppPreferences.getReminderHoursBefore(this);
        String[] opts = getResources().getStringArray(R.array.reminder_hours_labels);
        for (int i = 0; i < opts.length; i++) {
            try {
                if (Integer.parseInt(opts[i]) == hours) {
                    spinnerReminderHours.setSelection(i);
                    break;
                }
            } catch (NumberFormatException ignored) { }
        }
    }

    private void setupListeners() {
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int theme;
            if (checkedId == R.id.radioLight) {
                theme = ThemeHelper.THEME_LIGHT;
            } else if (checkedId == R.id.radioDark) {
                theme = ThemeHelper.THEME_DARK;
            } else {
                theme = ThemeHelper.THEME_SYSTEM;
            }
            ThemeHelper.saveTheme(this, theme);
            recreate();
        });

        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String languageCode;
            if (checkedId == R.id.radioRussian) {
                languageCode = LocaleHelper.LANGUAGE_RUSSIAN;
            } else {
                languageCode = LocaleHelper.LANGUAGE_ENGLISH;
            }

            String currentLanguage = LocaleHelper.getLanguage(this);
            if (!currentLanguage.equals(languageCode)) {
                LocaleHelper.setLocale(this, languageCode);
                Toast.makeText(this, R.string.restart_app, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        switchReminders.setOnCheckedChangeListener((v, checked) ->
                AppPreferences.setRemindersEnabled(this, checked));

        spinnerReminderHours.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                try {
                    int h = Integer.parseInt(parent.getItemAtPosition(position).toString());
                    AppPreferences.setReminderHoursBefore(SettingsActivity.this, h);
                } catch (NumberFormatException ignored) { }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
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