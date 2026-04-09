package com.flightsearch.app.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.flightsearch.app.R;

public final class NotificationHelper {

    public static final String CHANNEL_FLIGHT_REMINDERS = "flight_reminders";

    private NotificationHelper() {}

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_FLIGHT_REMINDERS,
                context.getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription(context.getString(R.string.notif_channel_reminders_desc));
        nm.createNotificationChannel(ch);
    }
}
