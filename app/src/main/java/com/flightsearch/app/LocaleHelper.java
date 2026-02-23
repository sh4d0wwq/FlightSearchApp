package com.flightsearch.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "FlightSearchPrefs";
    private static final String KEY_LANGUAGE = "language";

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_RUSSIAN = "ru";

    public static void setLocale(Context context, String languageCode) {
        saveLanguage(context, languageCode);
        updateResources(context, languageCode);
    }

    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());

        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, getSystemLanguage());
    }

    private static String getSystemLanguage() {
        String systemLang = Locale.getDefault().getLanguage();
        if (systemLang.equals("ru")) {
            return LANGUAGE_RUSSIAN;
        }
        return LANGUAGE_ENGLISH;
    }

    private static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        return updateResources(context, lang);
    }

    public static String getLanguageDisplayName(String languageCode) {
        return switch (languageCode) {
            case LANGUAGE_ENGLISH -> "English";
            case LANGUAGE_RUSSIAN -> "Русский";
            default -> "English";
        };
    }
}