package com.flightsearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            goMain();
            return;
        }

        editEmail = findViewById(R.id.editAuthEmail);
        editPassword = findViewById(R.id.editAuthPassword);
        MaterialButton buttonSignIn = findViewById(R.id.buttonSignIn);
        MaterialButton buttonRegister = findViewById(R.id.buttonRegister);

        buttonSignIn.setOnClickListener(v -> signIn());
        buttonRegister.setOnClickListener(v -> register());
    }

    private String email() {
        return text(editEmail);
    }

    private String password() {
        return text(editPassword);
    }

    private static String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void signIn() {
        String e = email();
        String p = password();
        if (e.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, R.string.auth_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goMain();
                    } else {
                        Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void register() {
        String e = email();
        String p = password();
        if (e.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, R.string.auth_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (p.length() < 6) {
            Toast.makeText(this, R.string.auth_password_length, Toast.LENGTH_SHORT).show();
            return;
        }
        auth.createUserWithEmailAndPassword(e, p)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.auth_registered, Toast.LENGTH_SHORT).show();
                        goMain();
                    } else {
                        Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
