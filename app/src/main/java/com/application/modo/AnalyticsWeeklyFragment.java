package com.application.modo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsWeeklyFragment extends Fragment {

    private BarChart barChart;
    private LineChart lineChart;
    private PieChart pieChart;
    private TextView tvWeeklyCompletedTask1, tvWeeklyMissedTask1, tvWeeklyCurrentCompletionRate1, tvWeeklyPreviousCompletionRate1, tvWeeklyCompletionComparison1;
    private TextView tvMostProductiveDay1, tvLeastProductiveDay1, tvWeeklyProductivityScore1;


    public AnalyticsWeeklyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_analytics_weekly, container, false);

        barChart = view.findViewById(R.id.weeklyBarChart);
        pieChart = view.findViewById(R.id.weeklyPieChart);
        lineChart = view.findViewById(R.id.weeklyLineChart);
        tvWeeklyCompletedTask1 = view.findViewById(R.id.tvWeeklyCompletedTask1);
        tvWeeklyMissedTask1 = view.findViewById(R.id.tvWeeklyMissedTask1);
        tvWeeklyCurrentCompletionRate1 = view.findViewById(R.id.tvWeeklyCurrentCompletionRate1);
        tvWeeklyPreviousCompletionRate1 = view.findViewById(R.id.tvWeeklyPreviousCompletionRate1);
        tvWeeklyCompletionComparison1 = view.findViewById(R.id.tvWeeklyCompletionComparison1);
        tvMostProductiveDay1 = view.findViewById(R.id.tvMostProductiveDay1);
        tvLeastProductiveDay1 = view.findViewById(R.id.tvLeastProductiveDay1);
        tvWeeklyProductivityScore1 = view.findViewById(R.id.tvWeeklyProductivityScore1);


        fetchWeeklyTaskStats(); // ✅ this handles chart setup dynamically

        return view;
    }


    private void fetchWeeklyTaskStats() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        SimpleDateFormat deadlineFormat = new SimpleDateFormat("M/d/yyyy h:mm a", Locale.ENGLISH);
        TimeZone phTimeZone = TimeZone.getTimeZone("Asia/Manila");
        sdf.setTimeZone(phTimeZone);
        deadlineFormat.setTimeZone(phTimeZone);

        // Generate current week (last 7 days ending yesterday)
        List<String> last7Days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(phTimeZone);
        calendar.add(Calendar.DAY_OF_YEAR, -1); // yesterday
        for (int i = 0; i < 7; i++) {
            last7Days.add(0, sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Generate previous 7 days
        List<String> previous7Days = new ArrayList<>();
        Calendar prevCalendar = Calendar.getInstance(phTimeZone);
        prevCalendar.add(Calendar.DAY_OF_YEAR, -8);
        for (int i = 0; i < 7; i++) {
            previous7Days.add(0, sdf.format(prevCalendar.getTime()));
            prevCalendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        int[] completedCounts = new int[7];
        int[] missedCounts = new int[7];
        int[] highCompleted = new int[7];
        int[] mediumCompleted = new int[7];
        int[] lowCompleted = new int[7];
        int[] highMissed = new int[7];
        int[] mediumMissed = new int[7];
        int[] lowMissed = new int[7];
        final int[] high = {0}, medium = {0}, low = {0};

        db.collection("users").document(uid).collection("tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int prevCompleted = 0, prevMissed = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String deadlineStr = doc.getString("deadline");
                        String status = doc.getString("status");
                        String priority = doc.getString("priority");

                        if (deadlineStr != null && status != null) {
                            try {
                                String[] parts = deadlineStr.split(" PHT");
                                Date deadlineDate = deadlineFormat.parse(parts[0]);
                                String day = sdf.format(deadlineDate);

                                // Current week
                                for (int i = 0; i < last7Days.size(); i++) {
                                    if (last7Days.get(i).equals(day)) {
                                        if (status.equalsIgnoreCase("Completed")) {
                                            completedCounts[i]++;
                                            if ("High".equalsIgnoreCase(priority)) highCompleted[i]++;
                                            else if ("Medium".equalsIgnoreCase(priority)) mediumCompleted[i]++;
                                            else if ("Low".equalsIgnoreCase(priority)) lowCompleted[i]++;
                                        } else if (status.equalsIgnoreCase("Missed")) {
                                            missedCounts[i]++;
                                            if ("High".equalsIgnoreCase(priority)) highMissed[i]++;
                                            else if ("Medium".equalsIgnoreCase(priority)) mediumMissed[i]++;
                                            else if ("Low".equalsIgnoreCase(priority)) lowMissed[i]++;
                                        }

                                        if (priority != null) {
                                            switch (priority) {
                                                case "High": high[0]++; break;
                                                case "Medium": medium[0]++; break;
                                                case "Low": low[0]++; break;
                                            }
                                        }
                                        break;
                                    }
                                }

                                // Previous week
                                for (String prevDay : previous7Days) {
                                    if (prevDay.equals(day)) {
                                        if (status.equalsIgnoreCase("Completed")) prevCompleted++;
                                        else if (status.equalsIgnoreCase("Missed")) prevMissed++;
                                        break;
                                    }
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Setup charts
                    setupBarChartWeekly(completedCounts, missedCounts, last7Days);
                    setupPieChart(high[0], medium[0], low[0]);
                    setupLineChartWeekly(highCompleted, mediumCompleted, lowCompleted,
                            highMissed, mediumMissed, lowMissed);

                    // === Completion Stats ===
                    int totalCompleted = 0, totalMissed = 0;
                    for (int val : completedCounts) totalCompleted += val;
                    for (int val : missedCounts) totalMissed += val;

                    int totalTasks = totalCompleted + totalMissed;
                    float currentRate = (totalTasks > 0) ? (totalCompleted * 100f / totalTasks) : 0f;
                    float prevRate = (prevCompleted + prevMissed > 0)
                            ? (prevCompleted * 100f / (prevCompleted + prevMissed)) : 0f;

                    tvWeeklyCompletedTask1.setText(String.valueOf(totalCompleted));
                    tvWeeklyMissedTask1.setText(String.valueOf(totalMissed));
                    tvWeeklyCurrentCompletionRate1.setText(String.format(Locale.ENGLISH, "%.0f%%", currentRate));
                    tvWeeklyPreviousCompletionRate1.setText(String.format(Locale.ENGLISH, "%.0f%%", prevRate));

                    float rateDiff = currentRate - prevRate;
                    String direction = rateDiff >= 0 ? "Up" : "Down";
                    float displayDiff = Math.abs(rateDiff);
                    tvWeeklyCompletionComparison1.setText(String.format(Locale.ENGLISH,
                            "%s by %.1f%% from last week, Keep it up!", direction, displayDiff));

                    // === Productivity Score Analysis ===
                    float[] scores = new float[7];
                    for (int i = 0; i < 7; i++) {
                        scores[i] = (highCompleted[i] * 1.2f + mediumCompleted[i] * 1.1f + lowCompleted[i] * 1.1f)
                                - (highMissed[i] * 1.2f + mediumMissed[i] * 1.1f + lowMissed[i] * 1.0f);
                    }

                    float max = scores[0], min = scores[0];
                    List<String> maxDays = new ArrayList<>(), minDays = new ArrayList<>();

                    for (int i = 0; i < 7; i++) {
                        if (scores[i] > max) {
                            max = scores[i];
                            maxDays.clear();
                            maxDays.add(last7Days.get(i));
                        } else if (scores[i] == max && !maxDays.contains(last7Days.get(i))) {
                            maxDays.add(last7Days.get(i));
                        }

                        if (scores[i] < min) {
                            min = scores[i];
                            minDays.clear();
                            minDays.add(last7Days.get(i));
                        } else if (scores[i] == min && !minDays.contains(last7Days.get(i))) {
                            minDays.add(last7Days.get(i));
                        }
                    }

                    if (max == min) {
                        tvMostProductiveDay1.setText("Balanced");
                        tvLeastProductiveDay1.setText("Balanced");
                    } else {
                        if (maxDays.size() >= 4) {
                            tvMostProductiveDay1.setText("Multiple Days");
                        } else {
                            tvMostProductiveDay1.setText(String.join(", ", maxDays));
                        }

                        if (minDays.size() >= 4) {
                            tvLeastProductiveDay1.setText("Multiple Days");
                        } else {
                            tvLeastProductiveDay1.setText(String.join(", ", minDays));
                        }
                    }

                    // Set average weekly productivity score
                    float totalScore = 0f;
                    for (float score : scores) totalScore += score;
                    float avgScore = totalScore / scores.length;
                    tvWeeklyProductivityScore1.setText(String.format(Locale.ENGLISH,
                            "Your weekly productivity score is %.1f!", avgScore));

                });
    }

    private void setupBarChartWeekly(int[] completed, int[] missed, List<String> dayLabels) {
        List<BarEntry> completedEntries = new ArrayList<>();
        List<BarEntry> missedEntries = new ArrayList<>();

        for (int i = 0; i < dayLabels.size(); i++) {
            completedEntries.add(new BarEntry(i, completed[i]));
            missedEntries.add(new BarEntry(i, missed[i]));
        }

        BarDataSet completedSet = new BarDataSet(completedEntries, "Completed Task");
        completedSet.setColor(Color.rgb(49, 48, 55));
        completedSet.setValueTextColor(Color.BLACK);
        completedSet.setValueTextSize(12f);
        completedSet.setValueFormatter(new IntValueFormatter());

        BarDataSet missedSet = new BarDataSet(missedEntries, "Missed Task");
        missedSet.setColor(Color.rgb(147, 144, 174));
        missedSet.setValueTextColor(Color.BLACK);
        missedSet.setValueTextSize(12f);
        missedSet.setValueFormatter(new IntValueFormatter());

        BarData barData = new BarData(completedSet, missedSet);
        barData.setBarWidth(0.3f);
        barChart.setData(barData);

        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(14f);
        legend.setXEntrySpace(40f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < dayLabels.size() ? dayLabels.get(index) : "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);

        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(barData.getGroupWidth(0.3f, 0.05f) * dayLabels.size());
        barChart.groupBars(0f, 0.3f, 0.05f);

        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(Color.BLACK);
        barChart.getLegend().setTextSize(12f);
        barChart.setTouchEnabled(false);
        barChart.setDescription(null);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupPieChart(int high, int medium, int low) {
        List<PieEntry> pieEntries = new ArrayList<>();
        if (high > 0) pieEntries.add(new PieEntry(high, "High"));
        if (medium > 0) pieEntries.add(new PieEntry(medium, "Medium"));
        if (low > 0) pieEntries.add(new PieEntry(low, "Low"));

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(Color.rgb(49, 48, 55), Color.rgb(147, 144, 174), Color.rgb(194, 191, 221));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(16f);
        dataSet.setValueFormatter(new PercentValueFormatter());

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(16f);
        legend.setXEntrySpace(20f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }


    private void setupLineChartWeekly(int[] highCompleted, int[] mediumCompleted, int[] lowCompleted,
                                      int[] highMissed, int[] mediumMissed, int[] lowMissed) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            float score = 1f
                    + (highCompleted[i] * 1.2f + mediumCompleted[i] * 1.1f + lowCompleted[i] * 1.1f)
                    - (highMissed[i] * 1.2f + mediumMissed[i] * 1.1f + lowMissed[i] * 1.0f);
            entries.add(new Entry(i, score));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.rgb(49, 48, 55));
        dataSet.setCircleColor(Color.rgb(147, 144, 174));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(8f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(Color.BLACK);
        lineChart.getLegend().setTextColor(Color.BLACK);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(6.5f);
        xAxis.setValueFormatter(new Last7DaysFormatter());

        lineChart.animateY(1000);
        lineChart.invalidate();
    }


    private static class IntValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }

    private static class PercentValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%d%%", (int) value);
        }
    }

    private static class Last7DaysFormatter extends ValueFormatter {
        private final String[] last7Days;

        public Last7DaysFormatter() {
            last7Days = generateLast7Days();
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            return index >= 0 && index < last7Days.length ? last7Days[index] : "";
        }

        private String[] generateLast7Days() {
            String[] days = new String[7];
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
            calendar.add(Calendar.DAY_OF_YEAR, -7); // start from 7 days ago to end yesterday

            for (int i = 0; i < 7; i++) {
                days[i] = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return days;
        }
    }
}
