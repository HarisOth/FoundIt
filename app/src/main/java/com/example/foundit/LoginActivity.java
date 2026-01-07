package com.example.foundit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private MaterialButton loginButton;
    private TextView forgotPasswordText, signUpText;

    private FirebaseAuth mAuth;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Tukar warna status bar supaya sama macam background splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7")); // warna cerah
        }

        // Tukar icon status bar jadi gelap supaya nampak pada background cerah
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mAuth = FirebaseAuth.getInstance();

        // Check if already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupTextWatchers();
        setupClickListeners();
        setupBackPressHandler();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.editText);
        passwordEditText = findViewById(R.id.editText2);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signUpText = findViewById(R.id.signUpText);

        updateLoginButtonState();
    }

    private void setupTextWatchers() {
        // Email validation
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    emailInputLayout.setError("Email cannot be empty");
                    isEmailValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setError("Invalid email address");
                    isEmailValid = false;
                } else {
                    emailInputLayout.setError(null);
                    isEmailValid = true;
                }
                updateLoginButtonState();
            }
        });

        // Password validation (login-friendly)
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();
                if (password.isEmpty()) {
                    passwordInputLayout.setError("Password cannot be empty");
                    isPasswordValid = false;
                } else {
                    passwordInputLayout.setError(null);
                    isPasswordValid = true;
                }
                updateLoginButtonState();
            }
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> loginUser());

        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity(); // exit app
            }
        });
    }

    private void updateLoginButtonState() {
        loginButton.setEnabled(isEmailValid && isPasswordValid);
        loginButton.setAlpha(isEmailValid && isPasswordValid ? 1f : 0.5f);
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!isEmailValid || !isPasswordValid) {
            Toast.makeText(this, "Please fix errors before logging in", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setText("Logging in...");
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginButton.setText("LOGIN");
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        String errorMessage = "Login failed";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error.contains("no user record") || error.contains("invalid credential")) {
                                errorMessage = "Invalid email or password";
                            } else if (error.contains("network")) {
                                errorMessage = "Network error. Check connection";
                            } else if (error.contains("too many requests")) {
                                errorMessage = "Too many attempts. Try later";
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
