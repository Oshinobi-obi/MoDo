package com.application.modo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Landing extends AppCompatActivity {

    private Button btnSignUpEmail1;
    private TextView tvLogin1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        btnSignUpEmail1 = findViewById(R.id.btnSignUpEmail1);
        tvLogin1 = findViewById(R.id.tvLogin1);

        // ✨ Load animations
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // ✨ Start animations
        btnSignUpEmail1.startAnimation(bounce);
        tvLogin1.startAnimation(fadeIn);

        // ➤ Redirect to Sign Up
        btnSignUpEmail1.setOnClickListener(view -> {
            Intent intent = new Intent(Landing.this, SignUp1st.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // ➤ Redirect to Login
        tvLogin1.setOnClickListener(view -> {
            Intent intent = new Intent(Landing.this, Login.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ProgressBar progressBar = findViewById(R.id.progressBarLanding);
        TextView tvLoading = findViewById(R.id.tvLoading);

        // Hide UI
        btnSignUpEmail1.setVisibility(View.GONE);
        tvLogin1.setVisibility(View.GONE);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);

        // Modern: Use Handler with Looper to avoid deprecation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(Landing.this, Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            } else {
                progressBar.setVisibility(View.GONE);
                tvLoading.setVisibility(View.GONE);

                btnSignUpEmail1.setVisibility(View.VISIBLE);
                tvLogin1.setVisibility(View.VISIBLE);

                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                btnSignUpEmail1.startAnimation(fadeIn);
                tvLogin1.startAnimation(fadeIn);
            }
        }, 1000);
    }
}