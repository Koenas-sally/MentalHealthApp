package com.example.mentalhealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        findViewById(R.id.signupButton).setOnClickListener(v -> {
            String fullName = ((EditText) findViewById(R.id.fullNameInput)).getText().toString().trim();
            String email = ((EditText) findViewById(R.id.emailInput)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordInput)).getText().toString();
            String confirmPassword = ((EditText) findViewById(R.id.confirmPasswordInput)).getText().toString();

            if (validateInputs(fullName, email, password, confirmPassword)) {
                createAccount(fullName, email, password);
            }
        });

        TextView loginPrompt = findViewById(R.id.loginPrompt);
        makeLoginClickable(loginPrompt);
    }

    private boolean validateInputs(String fullName, String email, String password, String confirmPassword) {
        if (fullName.isEmpty()) {
            ((EditText) findViewById(R.id.fullNameInput)).setError("Name is required");
            return false;
        }

        if (email.isEmpty()) {
            ((EditText) findViewById(R.id.emailInput)).setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ((EditText) findViewById(R.id.emailInput)).setError("Enter a valid email address");
            return false;
        }

        if (password.isEmpty()) {
            ((EditText) findViewById(R.id.passwordInput)).setError("Password is required");
            return false;
        }

        if (password.length() < 8) {
            ((EditText) findViewById(R.id.passwordInput)).setError("Password must be at least 8 characters");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            ((EditText) findViewById(R.id.passwordInput)).setError("Password must contain at least one lowercase letter");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            ((EditText) findViewById(R.id.passwordInput)).setError("Password must contain at least one uppercase letter");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            ((EditText) findViewById(R.id.passwordInput)).setError("Password must contain at least one number");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            ((EditText) findViewById(R.id.passwordInput)).setError("Password must contain at least one special character");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            ((EditText) findViewById(R.id.confirmPasswordInput)).setError("Passwords don't match");
            return false;
        }

        return true;
    }

    private void createAccount(String fullName, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        Toast.makeText(
                                                SignUpActivity.this,
                                                "Account created successfully.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        sendEmailVerification(user);
                                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(
                                SignUpActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                SignUpActivity.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void makeLoginClickable(TextView textView) {
        String fullText = "Already have an account? Log In";
        SpannableString spannableString = new SpannableString(fullText);

        String clickablePart = "Log In";
        int startIndex = fullText.indexOf(clickablePart);
        int endIndex = startIndex + clickablePart.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#1E88E5"));
                ds.setUnderlineText(false); // Remove underline if you want
            }
        };
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }
}