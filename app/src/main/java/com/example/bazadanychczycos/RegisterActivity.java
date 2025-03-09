package com.example.bazadanychczycos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private DataBase.DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DataBase.DatabaseHelper(this);

        Button registerButton = findViewById(R.id.button_register);
        registerButton.setOnClickListener(v -> registerUser());

        // Dodaj obsługę kliknięcia dla przycisku logowania
        Button loginButton = findViewById(R.id.button_login);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        EditText usernameInput = findViewById(R.id.edit_text_username);
        EditText passwordInput = findViewById(R.id.edit_text_password);

        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdź czy użytkownik już istnieje
        if (dbHelper.userExists(username)) {
            Toast.makeText(this, getString(R.string.username_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        // Dodaj użytkownika do bazy danych
        long result = dbHelper.addUser(username, password);

        if (result != -1) {
            // Zapisz dane do SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();

            Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.registration_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}