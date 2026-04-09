package com.flightsearch.app;

import android.app.Application;

import com.flightsearch.app.notifications.NotificationHelper;

public class FlightApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannels(this);
    }
}
