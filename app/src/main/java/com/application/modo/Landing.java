package com.application.modo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;

public class Landing extends AppCompatActivity {

    private final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private Button btnSignUpEmail1;
    private TextView tvLogin1;
    private ProgressBar progressBar;
    private TextView tvLoading;

    private boolean permissionAsked = false; // ✨ to track if permission already requested

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        btnSignUpEmail1 = findViewById(R.id.btnSignUpEmail1);
        tvLogin1 = findViewById(R.id.tvLogin1);
        progressBar = findViewById(R.id.progressBarLanding);
        tvLoading = findViewById(R.id.tvLoading);

        btnSignUpEmail1.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);

        requestNotificationPermission(); // ✨ Ask permission immediately

        btnSignUpEmail1.setOnClickListener(view -> {
            Intent intent = new Intent(Landing.this, SignUp1st.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        tvLogin1.setOnClickListener(view -> {
            Intent intent = new Intent(Landing.this, Login.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        permissionAsked = true; // ✨ mark as asked
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (permissionAsked) {
            // ✨ Wait 2 seconds before checking session after permission popup
            new Handler(Looper.getMainLooper()).postDelayed(this::checkSession, 5000);
        } else {
            // ✨ If no need to ask permission (Android 12 below), proceed normally
            checkSession();
        }
    }

    private void checkSession() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(Landing.this, ActivityMain.class); // ✅ Fixed: launch ActivityMain, not HomeFragment
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else {
            progressBar.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
            btnSignUpEmail1.setVisibility(View.VISIBLE);
        }
    }
}