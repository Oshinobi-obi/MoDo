package com.application.modo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUp2nd extends AppCompatActivity {

    private EditText etPassword2, etReEnterPassword1;
    private CheckBox checkBox;
    private Button btnSignUp;
    private TextView tvRequirement1, tvRequirement2, tvRequirement3, tvRequirement4, tvPasswordMismatch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String email, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2nd);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etPassword2 = findViewById(R.id.etPassword2);
        etReEnterPassword1 = findViewById(R.id.etReEnterPassword1);
        checkBox = findViewById(R.id.checkBox);
        btnSignUp = findViewById(R.id.btnSignUp);

        tvRequirement1 = findViewById(R.id.tvRequirement1);
        tvRequirement2 = findViewById(R.id.tvRequirement2);
        tvRequirement3 = findViewById(R.id.tvRequirement3);
        tvRequirement4 = findViewById(R.id.tvRequirement4);
        tvPasswordMismatch = findViewById(R.id.tvPasswordMismatch);

        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username");

        // ✨ Animations
        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        etPassword2.startAnimation(fade);
        etReEnterPassword1.startAnimation(fade);
        tvRequirement1.startAnimation(slideUp);
        tvRequirement2.startAnimation(slideUp);
        tvRequirement3.startAnimation(slideUp);
        tvRequirement4.startAnimation(slideUp);
        tvPasswordMismatch.startAnimation(fade);
        checkBox.startAnimation(fade);
        btnSignUp.startAnimation(bounce);

        etPassword2.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });

        etReEnterPassword1.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordMatch();
            }
            public void afterTextChanged(Editable s) {}
        });

        setupPasswordToggle(etPassword2);
        setupPasswordToggle(etReEnterPassword1);

        btnSignUp.setOnClickListener(view -> {
            String password = etPassword2.getText().toString().trim();
            String confirmPassword = etReEnterPassword1.getText().toString().trim();

            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!checkBox.isChecked()) {
                Toast.makeText(this, "You must agree to the terms", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(this, "Password does not meet requirements!", Toast.LENGTH_SHORT).show();
                return;
            }

            createAccount(email, password);
        });
    }

    private void checkPasswordMatch() {
        String pass = etPassword2.getText().toString();
        String confirm = etReEnterPassword1.getText().toString();

        if (!confirm.equals(pass)) {
            tvPasswordMismatch.setVisibility(View.VISIBLE);
            tvPasswordMismatch.setText("❌ Passwords do not match");
            tvPasswordMismatch.setTextColor(Color.RED);
        } else {
            tvPasswordMismatch.setVisibility(View.GONE);
        }
    }

    private void validatePassword(String password) {
        boolean hasUppercase = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLowercase = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()\\-_+=\\|\\[\\]{};:/?.>,<]").matcher(password).find();

        updateRequirement(tvRequirement1, hasUppercase);
        updateRequirement(tvRequirement2, hasLowercase);
        updateRequirement(tvRequirement3, hasDigit);
        updateRequirement(tvRequirement4, hasSpecial);
    }

    private void updateRequirement(TextView textView, boolean isValid) {
        if (isValid) {
            textView.setTextColor(Color.GREEN);
            textView.setText("✔ " + textView.getText().toString().substring(2));
        } else {
            textView.setTextColor(Color.RED);
            textView.setText("❌ " + textView.getText().toString().substring(2));
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[a-z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find() &&
                Pattern.compile("[!@#$%^&*()\\-_+=\\|\\[\\]{};:/?.>,<]").matcher(password).find();
    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user.getUid(), username, email);
                        }
                    } else {
                        Toast.makeText(this, "This email is already in use. Please use a different one.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String username, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("profile", "");
        user.put("firstname", "");
        user.put("middlename", "");
        user.put("lastname", "");
        user.put("suffix", "");
        user.put("birthdate", "");
        user.put("failedAttempts", 0);
        user.put("age", "");
        user.put("mustResetPassword", false);
        user.put("taskreminder", "");
        user.put("breakreminder", "");

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUp2nd.this, Login.class));
                    overridePendingTransition(0, 0);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving user", Toast.LENGTH_SHORT).show();
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
