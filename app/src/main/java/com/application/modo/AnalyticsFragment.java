package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AnalyticsFragment extends Fragment {

    private Button btnWeekly, btnMonthly, btnOverall;

    public AnalyticsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        btnWeekly = view.findViewById(R.id.btnWeekly);
        btnMonthly = view.findViewById(R.id.btnMonthly);
        btnOverall = view.findViewById(R.id.btnOverall);

        // Default load Weekly Fragment
        loadChildFragment(new AnalyticsWeeklyFragment());
        updateButtonStyles(btnWeekly);

        btnWeekly.setOnClickListener(v -> {
            loadChildFragment(new AnalyticsWeeklyFragment());
            updateButtonStyles(btnWeekly);
        });

        btnMonthly.setOnClickListener(v -> {
            loadChildFragment(new AnalyticsMonthlyFragment());
            updateButtonStyles(btnMonthly);
        });

        btnOverall.setOnClickListener(v -> {
            loadChildFragment(new AnalyticsOverallFragment());
            updateButtonStyles(btnOverall);
        });

        return view;
    }

    private void loadChildFragment(Fragment childFragment) {
        if (childFragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.clAnalysis, childFragment) // Make sure you have @+id/clAnalysis in fragment_analytics.xml
                    .commit();
        }
    }

    private void updateButtonStyles(Button selectedButton) {
        int selectedBg = requireContext().getColor(R.color.selected_button_bg);
        int selectedText = requireContext().getColor(R.color.white);
        int defaultBg = requireContext().getColor(R.color.default_button_bg);
        int defaultText = requireContext().getColor(R.color.default_text_color);

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
