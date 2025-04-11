package com.application.modo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class Login extends AppCompatActivity {

    private EditText etUsername1, etPassword1;
    private Button btnContinue1;
    private TextView tvForgotPassword, tvSignUpLnk;
    private ProgressBar progressBar;
    private RelativeLayout loadingOverlay;
    private ImageView checkmarkView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final int MAX_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUsername1 = findViewById(R.id.etUsername1);
        etPassword1 = findViewById(R.id.etPassword1);
        btnContinue1 = findViewById(R.id.btnContinue1);
        tvForgotPassword = findViewById(R.id.tvForgotPassword1);
        tvSignUpLnk = findViewById(R.id.tvSignUpLnk);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        checkmarkView = findViewById(R.id.checkmarkView);

        setupPasswordToggle(etPassword1);

        btnContinue1.setOnClickListener(view -> attemptLogin());
        tvForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(this, ForgotPassword.class));
            overridePendingTransition(0, 0);
            finish();
        });
        tvSignUpLnk.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUp1st.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void attemptLogin() {
        String username = etUsername1.getText().toString().trim();
        String password = etPassword1.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        checkmarkView.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot userDoc = query.getDocuments().get(0);
                        String email = userDoc.getString("email");
                        String uid = userDoc.getId();

                        boolean mustReset = Boolean.TRUE.equals(userDoc.getBoolean("mustResetPassword"));

                        // 🔒 Check if user must reset password
                        if (mustReset) {
                            loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(this, "Your account is locked. Please reset your password.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, ForgotPassword.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return;
                        }

                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // ✅ Reset failed attempts & unlock
                                        db.collection("users").document(uid)
                                                .update("failedAttempts", 0, "mustResetPassword", false);

                                        progressBar.setVisibility(View.GONE);
                                        checkmarkView.setVisibility(View.VISIBLE);

                                        checkmarkView.postDelayed(() -> {
                                            loadingOverlay.setVisibility(View.GONE);
                                            startActivity(new Intent(this, Home.class));
                                            overridePendingTransition(0, 0);
                                            finish();
                                        }, 1000);
                                    } else {
                                        // ❌ Incorrect password
                                        final long[] attempts = {userDoc.get("failedAttempts") instanceof Number
                                                ? ((Number) userDoc.get("failedAttempts")).longValue()
                                                : 0};

                                        attempts[0]++;
                                        boolean shouldLock = attempts[0] >= MAX_ATTEMPTS;

                                        db.collection("users").document(uid)
                                                .update("failedAttempts", attempts[0], "mustResetPassword", shouldLock);

                                        loadingOverlay.setVisibility(View.GONE);

                                        if (shouldLock) {
                                            Toast.makeText(this, "3 failed attempts. Please reset your password.", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(this, ForgotPassword.class));
                                            overridePendingTransition(0, 0);
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Login failed! Attempts left: " + (MAX_ATTEMPTS - attempts[0]), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        loadingOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupPasswordToggle(EditText editText) {
        final boolean[] isVisible = {false};

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    isVisible[0] = !isVisible[0];
                    if (isVisible[0]) {
                        editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_show, 0);
                    } else {
                        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_hide, 0);
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}