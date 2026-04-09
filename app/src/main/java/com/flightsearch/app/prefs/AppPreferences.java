package com.flightsearch.app.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {

    private static final String PREFS = "flight_app_prefs";
    private static final String KEY_REMINDERS = "reminders_enabled";
    private static final String KEY_REMINDER_HOURS = "reminder_hours_before";

    private AppPreferences() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isRemindersEnabled(Context c) {
        return sp(c).getBoolean(KEY_REMINDERS, true);
    }

    public static void setRemindersEnabled(Context c, boolean v) {
        sp(c).edit().putBoolean(KEY_REMINDERS, v).apply();
    }

    public static int getReminderHoursBefore(Context c) {
        return sp(c).getInt(KEY_REMINDER_HOURS, 24);
    }

    public static void setReminderHoursBefore(Context c, int hours) {
        sp(c).edit().putInt(KEY_REMINDER_HOURS, Math.max(1, Math.min(168, hours))).apply();
    }
}
