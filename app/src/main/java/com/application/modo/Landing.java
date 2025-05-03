package com.application.modo;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
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

    private boolean permissionAsked = false;
    private boolean wentToExactAlarmSettings = false;
    private boolean delayFinished = false; // ✅ New flag to sync both flows

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

        requestNotificationPermission();
        checkExactAlarmPermission();

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

        permissionAsked = true;

        // 🔄 Add 5s delay only after requesting POST_NOTIFICATIONS
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            delayFinished = true;
            if (!wentToExactAlarmSettings) {
                checkSession();
            }
        }, 5000);
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Enable Exact Alarms")
                        .setMessage("To receive accurate task reminders, please enable 'Exact Alarms' in system settings.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            wentToExactAlarmSettings = true;
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only check session after returning from exact alarm settings and if 5s delay is over
        if (wentToExactAlarmSettings && delayFinished) {
            wentToExactAlarmSettings = false;
            checkSession();
        }
    }

    private void checkSession() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(Landing.this, ActivityMain.class);
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