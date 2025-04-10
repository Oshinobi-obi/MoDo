package com.application.modo;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class Analytics extends AppCompatActivity {

    Button btnWeekly, btnMonthly, btnOverall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        btnWeekly = findViewById(R.id.btnWeekly);
        btnMonthly = findViewById(R.id.btnMonthly);
        btnOverall = findViewById(R.id.btnOverall);

        // Load default fragment and style
        loadFragment(new AnalyticsWeekly());
        updateButtonStyles(btnWeekly);

        btnWeekly.setOnClickListener(v -> {
            loadFragment(new AnalyticsWeekly());
            updateButtonStyles(btnWeekly);
        });

        btnMonthly.setOnClickListener(v -> {
            loadFragment(new AnalyticsMonthly());
            updateButtonStyles(btnMonthly);
        });

        btnOverall.setOnClickListener(v -> {
            loadFragment(new AnalyticsOverall());
            updateButtonStyles(btnOverall);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.clAnalysis, fragment)
                .commit();
    }

    private void updateButtonStyles(Button selectedButton) {
        // Define colors
        int selectedBg = getResources().getColor(R.color.selected_button_bg); // #313037
        int selectedText = getResources().getColor(R.color.white); // #FFFFFF
        int defaultBg = getResources().getColor(R.color.default_button_bg); // #DBDAE1
        int defaultText = getResources().getColor(R.color.default_text_color); // #313037

        Button[] buttons = {btnWeekly, btnMonthly, btnOverall};

        for (Button btn : buttons) {
            if (btn == selectedButton) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBg));
                btn.setTextColor(selectedText);
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultBg));
                btn.setTextColor(defaultText);
            }
        }
    }
}