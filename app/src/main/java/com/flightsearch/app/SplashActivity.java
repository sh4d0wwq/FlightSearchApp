package com.flightsearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> next = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? MainActivity.class
                    : AuthActivity.class;
            startActivity(new Intent(SplashActivity.this, next));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
