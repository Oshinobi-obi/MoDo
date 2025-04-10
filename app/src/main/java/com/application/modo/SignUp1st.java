package com.application.modo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp1st extends AppCompatActivity {

    private EditText etUsername2, etEmail1;
    private Button btnContinue2;
    private TextView tvLogin3, tvUsernameStatus, tvEmailStatus;

    private FirebaseFirestore db;
    private boolean isUsernameAvailable = false;
    private boolean isEmailValidAndAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1st);

        etUsername2 = findViewById(R.id.etUsername2);
        etEmail1 = findViewById(R.id.etEmail1);
        btnContinue2 = findViewById(R.id.btnContinue2);
        tvLogin3 = findViewById(R.id.tvLogin3);
        tvUsernameStatus = findViewById(R.id.tvUsernameStatus);
        tvEmailStatus = findViewById(R.id.tvEmailStatus);

        db = FirebaseFirestore.getInstance();

        // ✨ Animations
        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        etUsername2.startAnimation(fade);
        etEmail1.startAnimation(fade);
        btnContinue2.startAnimation(bounce);
        tvLogin3.startAnimation(slideUp);
        tvUsernameStatus.startAnimation(fade);
        tvEmailStatus.startAnimation(fade);

        tvUsernameStatus.setVisibility(View.GONE);
        tvEmailStatus.setVisibility(View.GONE);

        // 🔎 Username live validation
        etUsername2.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable editable) {
                String username = editable.toString().trim();

                if (TextUtils.isEmpty(username)) {
                    tvUsernameStatus.setVisibility(View.GONE);
                    isUsernameAvailable = false;
                    return;
                }

                db.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener(query -> {
                            tvUsernameStatus.setVisibility(View.VISIBLE);
                            if (!query.isEmpty()) {
                                tvUsernameStatus.setText("❌ Username is already taken!");
                                tvUsernameStatus.setTextColor(Color.RED);
                                isUsernameAvailable = false;
                            } else {
                                tvUsernameStatus.setText("✔ This Username is available!");
                                tvUsernameStatus.setTextColor(Color.GREEN);
                                isUsernameAvailable = true;
                            }
                        })
                        .addOnFailureListener(e -> {
                            tvUsernameStatus.setText("⚠ Failed to check username");
                            tvUsernameStatus.setTextColor(Color.RED);
                            isUsernameAvailable = false;
                        });
            }
        });

        // 🔍 Email live validation
        etEmail1.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable editable) {
                String email = editable.toString().trim();

                if (TextUtils.isEmpty(email)) {
                    tvEmailStatus.setVisibility(View.GONE);
                    isEmailValidAndAvailable = false;
                    return;
                }

                if (!isValidEmail(email)) {
                    tvEmailStatus.setText("❌ Invalid email format!");
                    tvEmailStatus.setTextColor(Color.RED);
                    tvEmailStatus.setVisibility(View.VISIBLE);
                    isEmailValidAndAvailable = false;
                    return;
                }

                db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(query -> {
                            tvEmailStatus.setVisibility(View.VISIBLE);
                            if (!query.isEmpty()) {
                                tvEmailStatus.setText("❌ Email is already registered!");
                                tvEmailStatus.setTextColor(Color.RED);
                                isEmailValidAndAvailable = false;
                            } else {
                                tvEmailStatus.setText("✔ Email is valid and available!");
                                tvEmailStatus.setTextColor(Color.GREEN);
                                isEmailValidAndAvailable = true;
                            }
                        })
                        .addOnFailureListener(e -> {
                            tvEmailStatus.setText("⚠ Failed to check email");
                            tvEmailStatus.setTextColor(Color.RED);
                            isEmailValidAndAvailable = false;
                        });
            }
        });

        // ✅ Continue if all fields valid
        btnContinue2.setOnClickListener(view -> {
            String username = etUsername2.getText().toString().trim();
            String email = etEmail1.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isUsernameAvailable) {
                Toast.makeText(this, "Username is not available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isEmailValidAndAvailable) {
                Toast.makeText(this, "Email is not valid or already registered", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(SignUp1st.this, SignUp2nd.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        tvLogin3.setOnClickListener(view -> {
            startActivity(new Intent(SignUp1st.this, Login.class));
            overridePendingTransition(0, 0);
            finish();
        });


    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@(gmail\\.com|yahoo\\.com|yahoo\\.com\\.ph|outlook\\.com)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}