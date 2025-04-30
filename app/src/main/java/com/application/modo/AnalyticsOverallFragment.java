package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AnalyticsOverallFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvTotalTaskAmount1, tvOverallCompletedTask1, tvOverallMissedTask1, tvOverallOngoingTask1, tvOverallCompletionRate1;
    private PieChart pieChart;
    private BarChart barChart;

    public AnalyticsOverallFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_analytics_overall, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTotalTaskAmount1 = view.findViewById(R.id.tvTotalTaskAmount1);
        tvOverallCompletedTask1 = view.findViewById(R.id.tvOverallCompletedTask1);
        tvOverallMissedTask1 = view.findViewById(R.id.tvOverallMissedTask1);
        tvOverallOngoingTask1 = view.findViewById(R.id.tvOverallOngoingTask1);
        tvOverallCompletionRate1 = view.findViewById(R.id.tvOverallCompletionRate1);
        pieChart = view.findViewById(R.id.overallPieChart);
        barChart = view.findViewById(R.id.overallBarChart);

        setupLineChart(view);
        fetchTaskStatistics();

        return view;
    }

    private void fetchTaskStatistics() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalTasks = 0;
                    int completedTasks = 0;
                    int missedTasks = 0;
                    int ongoingTasks = 0;
                    int highCount = 0, mediumCount = 0, lowCount = 0;

                    int[] completedPerDay = new int[7]; // Sun to Sat
                    int[] missedPerDay = new int[7];    // Sun to Sat

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        totalTasks++;

                        String status = doc.getString("status");
                        if (status != null) {
                            if (status.equalsIgnoreCase("Completed")) {
                                completedTasks++;
                            } else if (status.equalsIgnoreCase("Missed")) {
                                missedTasks++;
                            } else if (status.equalsIgnoreCase("Ongoing")) {
                                ongoingTasks++;
                            }
                        }

                        String priority = doc.getString("priority");
                        if (priority != null) {
                            switch (priority) {
                                case "High": highCount++; break;
                                case "Medium": mediumCount++; break;
                                case "Low": lowCount++; break;
                            }
                        }

                        // Group by weekday
                        String deadlineStr = doc.getString("deadline"); // e.g., "4/30/2025 6:00 PM PHT"
                        if (deadlineStr != null && status != null) {
                            try {
                                // Remove timezone suffix and parse using matching pattern
                                String[] parts = deadlineStr.split(" PHT");
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("M/d/yyyy h:mm a");
                                java.util.Date parsedDate = sdf.parse(parts[0]);

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(parsedDate);
                                int dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday, ..., 6 = Saturday

                                if (status.equalsIgnoreCase("Completed")) {
                                    completedPerDay[dayIndex]++;
                                } else if (status.equalsIgnoreCase("Missed")) {
                                    missedPerDay[dayIndex]++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle bad date format gracefully
                            }
                        }
                    }

                    tvTotalTaskAmount1.setText(String.valueOf(totalTasks));
                    tvOverallCompletedTask1.setText(String.valueOf(completedTasks));
                    tvOverallMissedTask1.setText(String.valueOf(missedTasks));
                    tvOverallOngoingTask1.setText(String.valueOf(ongoingTasks));

                    if (totalTasks > 0) {
                        float completionRate = (completedTasks * 100f) / totalTasks;
                        tvOverallCompletionRate1.setText(String.format("%.0f%%", completionRate));
                    } else {
                        tvOverallCompletionRate1.setText("0%");
                    }

                    setupPieChart(highCount, mediumCount, lowCount);
                    setupBarChart(completedPerDay, missedPerDay);
                })
                .addOnFailureListener(e -> {
                    tvTotalTaskAmount1.setText("0");
                    tvOverallCompletedTask1.setText("0");
                    tvOverallMissedTask1.setText("0");
                    tvOverallOngoingTask1.setText("0");
                    tvOverallCompletionRate1.setText("0%");
                    setupPieChart(0, 0, 0);
                    setupBarChart(new int[7], new int[7]);
                });
    }

    private void setupBarChart(int[] completed, int[] missed) {
        List<BarEntry> completedEntries = new ArrayList<>();
        List<BarEntry> missedEntries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
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

        float barWidth = 0.3f;
        float barSpace = 0.05f;  // Space between bars in same group
        float groupSpace = 0.3f; // Space between groups

        BarData barData = new BarData(completedSet, missedSet);
        barData.setBarWidth(barWidth);
        barChart.setData(barData);

        // 🛠️ This line is CRUCIAL — must call BEFORE groupBars
        XAxis xAxis = barChart.getXAxis();
        xAxis.setAxisMinimum(0f);
        float groupWidth = barData.getGroupWidth(groupSpace, barSpace);
        xAxis.setAxisMaximum(0f + groupWidth * 7); // 7 days = 7 groups
        barChart.groupBars(0f, groupSpace, barSpace);

        // X Axis styling
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new DayValueFormatter());

        // Y Axis styling
        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisRight().setEnabled(false);

        // Legend styling
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(40f);

        // Chart settings
        barChart.getDescription().setEnabled(false);
        barChart.setExtraOffsets(10, 10, 10, 10);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

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
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentValueFormatter());

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setMinOffset(10f);

        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setXEntrySpace(20f);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupLineChart(View view) {
        LineChart lineChart = view.findViewById(R.id.overallLineChart);

        List<Entry> lineEntries = List.of(
                new Entry(0f, 2f), new Entry(1f, 4f),
                new Entry(2f, 3f), new Entry(3f, 6f),
                new Entry(4f, 5f), new Entry(5f, 3f),
                new Entry(6f, 7f)
        );

        LineDataSet dataSet = new LineDataSet(lineEntries, "");
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
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(6.5f);
        xAxis.setValueFormatter(new DayValueFormatter());

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

    private static class DayValueFormatter extends ValueFormatter {
        private final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < days.length) {
                return days[index];
            } else {
                return "";
            }
        }
    }
}
