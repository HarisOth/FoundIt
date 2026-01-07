package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private MaterialButton signUpButton;
    private CheckBox emailAlertsCheckbox;
    private TextView signInText;

    private FirebaseAuth mAuth;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        setupClickListeners();
        setupTextWatchers();
        setupBackPressHandler();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.editText);
        passwordEditText = findViewById(R.id.editText2);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        signUpButton = findViewById(R.id.signUpButton);
        emailAlertsCheckbox = findViewById(R.id.emailAlertsCheckbox);
        signInText = findViewById(R.id.signInText);
    }

    private void setupClickListeners() {
        // Sign Up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        // Sign in text (navigate to login)
        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void setupTextWatchers() {
        // Email validation
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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

                updateSignUpButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password validation
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();

                if (password.isEmpty()) {
                    passwordInputLayout.setError("Password cannot be empty");
                    isPasswordValid = false;
                } else if (password.length() < 6) {
                    passwordInputLayout.setError("Minimum 6 characters");
                    isPasswordValid = false;
                } else {
                    passwordInputLayout.setError(null);
                    isPasswordValid = true;
                }

                updateSignUpButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBackPressHandler() {
        // Handle back press with animation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back with animation
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void updateSignUpButtonState() {
        signUpButton.setEnabled(isEmailValid && isPasswordValid);
        signUpButton.setAlpha(isEmailValid && isPasswordValid ? 1.0f : 0.5f);
    }

    private void createUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Final validation
        if (!isEmailValid || !isPasswordValid) {
            Toast.makeText(this, "Please fix errors before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        signUpButton.setText("Creating Account...");
        signUpButton.setEnabled(false);

        // Firebase create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Reset button state
                        signUpButton.setText("Sign Up");
                        signUpButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Check if user wants email alerts
                            boolean wantsAlerts = emailAlertsCheckbox.isChecked();

                            // You can save this preference to Firebase Firestore here
                            // Example: saveEmailPreference(wantsAlerts);

                            Toast.makeText(RegisterActivity.this,
                                    "Account created successfully!", Toast.LENGTH_SHORT).show();

                            // Navigate to main activity
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        } else {
                            // Handle specific errors
                            String errorMessage = "Registration failed";
                            if (task.getException() != null) {
                                String error = task.getException().getMessage();
                                if (error.contains("already in use")) {
                                    errorMessage = "Email already registered";
                                } else if (error.contains("weak password")) {
                                    errorMessage = "Password is too weak";
                                } else if (error.contains("network")) {
                                    errorMessage = "Network error. Please check your connection";
                                }
                            }
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Optional: If you want to keep onBackPressed for older devices
    // @Override
    // public void onBackPressed() {
    //     super.onBackPressed();
    //     overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    // }
}