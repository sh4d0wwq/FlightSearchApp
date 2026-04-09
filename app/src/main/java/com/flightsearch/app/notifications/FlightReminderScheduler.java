package com.flightsearch.app.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.flightsearch.app.prefs.AppPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class FlightReminderScheduler {

    private static final String ACTION = "com.flightsearch.app.REMINDER";
    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private FlightReminderScheduler() {}

    public static void schedule(Context context, long flightId, String from, String to,
                                String departureDate, String departureTime) {
        if (!AppPreferences.isRemindersEnabled(context)) return;
        if (departureDate == null || departureDate.isEmpty()) return;

        long trigger = computeTriggerMillis(departureDate, departureTime,
                AppPreferences.getReminderHoursBefore(context));
        if (trigger <= System.currentTimeMillis() + 60_000L) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, FlightReminderReceiver.class);
        intent.setAction(ACTION);
        String route = (from != null ? from : "?") + " → " + (to != null ? to : "?");
        intent.putExtra(FlightReminderReceiver.EXTRA_TITLE, context.getString(com.flightsearch.app.R.string.reminder_notif_title));
        intent.putExtra(FlightReminderReceiver.EXTRA_TEXT,
                context.getString(com.flightsearch.app.R.string.reminder_notif_text, route, departureDate));
        intent.putExtra(FlightReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId(flightId));

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode(flightId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi);
        }
    }

    public static void cancel(Context context, long flightId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(context, FlightReminderReceiver.class);
        intent.setAction(ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode(flightId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        pi.cancel();
    }

    private static long computeTriggerMillis(String dateStr, String timeStr, int hoursBefore) {
        try {
            Date day = ISO.parse(dateStr);
            Calendar c = Calendar.getInstance();
            c.setTime(day);
            int h = 9;
            int m = 0;
            if (timeStr != null && timeStr.matches("\\d{1,2}:\\d{2}")) {
                String[] p = timeStr.split(":");
                h = Integer.parseInt(p[0].trim());
                m = Integer.parseInt(p[1].trim());
            }
            c.set(Calendar.HOUR_OF_DAY, h);
            c.set(Calendar.MINUTE, m);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long departureMillis = c.getTimeInMillis();
            return departureMillis - TimeUnit.HOURS.toMillis(hoursBefore);
        } catch (ParseException | NumberFormatException e) {
            return 0L;
        }
    }

    private static int requestCode(long flightId) {
        return (int) (flightId & 0x7FFFFFFF);
    }

    private static int notificationId(long flightId) {
        return requestCode(flightId);
    }
}
