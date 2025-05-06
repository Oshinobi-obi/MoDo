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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsMonthlyFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private TextView tvMonthlyCompletedTask1, tvMonthlyMissedTask1;
    private TextView tvMonthlyCurrentCompletionRate1, tvMonthlyPreviousCompletionRate1, tvMonthlyProductivityScore1;
    private TextView tvMostProductiveWeek1, tvLeastProductiveWeek1;

    private final SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy h:mm a", Locale.ENGLISH);

    public AnalyticsMonthlyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_analytics_monthly, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        pieChart = view.findViewById(R.id.monthlyPieChart);
        barChart = view.findViewById(R.id.monthlyBarChart);
        lineChart = view.findViewById(R.id.monthlyLineChart);
        tvMonthlyCompletedTask1 = view.findViewById(R.id.tvMonthlyCompletedTask1);
        tvMonthlyMissedTask1 = view.findViewById(R.id.tvMonthlyMissedTask1);
        tvMonthlyCurrentCompletionRate1 = view.findViewById(R.id.tvMonthlyCurrentCompletionRate1);
        tvMonthlyPreviousCompletionRate1 = view.findViewById(R.id.tvMonthlyPreviousCompletionRate1);
        tvMonthlyProductivityScore1 = view.findViewById(R.id.tvMonthlyProductivityScore1);
        tvMostProductiveWeek1 = view.findViewById(R.id.tvMostProductiveWeek1);
        tvLeastProductiveWeek1 = view.findViewById(R.id.tvLeastProductiveWeek1);

        fetchTaskStatistics();

        return view;
    }

    private List<String> generateWeeksOffset(int offsetDays) {
        List<String> weekRanges = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, offsetDays);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        for (int i = 0; i < 4; i++) {
            Date end = calendar.getTime();
            calendar.add(Calendar.DATE, -6);
            Date start = calendar.getTime();

            String formatted = new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(start) + " - " +
                    new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(end);
            weekRanges.add(0, formatted);

            calendar.add(Calendar.DATE, -1);
        }
        return weekRanges;
    }

    private void fetchTaskStatistics() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> currentWeeks = generateWeeks();
                    List<String> previousWeeks = generateWeeksOffset(-28);
                    int weekCount = currentWeeks.size();

                    int[] completedPerWeek = new int[weekCount];
                    int[] missedPerWeek = new int[weekCount];
                    int[] highCompleted = new int[weekCount];
                    int[] mediumCompleted = new int[weekCount];
                    int[] lowCompleted = new int[weekCount];
                    int[] highMissed = new int[weekCount];
                    int[] mediumMissed = new int[weekCount];
                    int[] lowMissed = new int[weekCount];

                    int totalCompleted = 0, totalMissed = 0;
                    int high = 0, medium = 0, low = 0;

                    int previousCompleted = 0, previousMissed = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String deadlineStr = doc.getString("deadline");
                        String status = doc.getString("status");
                        String priority = doc.getString("priority");

                        if (deadlineStr != null && status != null) {
                            try {
                                String[] parts = deadlineStr.split(" PHT");
                                Date date = sdf.parse(parts[0]);

                                for (int i = 0; i < weekCount; i++) {
                                    String[] range = currentWeeks.get(i).split(" - ");
                                    Date start = new SimpleDateFormat("MMM dd", Locale.ENGLISH).parse(range[0]);
                                    Date end = new SimpleDateFormat("MMM dd", Locale.ENGLISH).parse(range[1]);

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(start);
                                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                                    start = cal.getTime();

                                    cal.setTime(end);
                                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                                    end = cal.getTime();

                                    if (!date.before(start) && !date.after(end)) {
                                        if (status.equalsIgnoreCase("Completed")) {
                                            completedPerWeek[i]++;
                                            if ("High".equalsIgnoreCase(priority)) highCompleted[i]++;
                                            else if ("Medium".equalsIgnoreCase(priority)) mediumCompleted[i]++;
                                            else if ("Low".equalsIgnoreCase(priority)) lowCompleted[i]++;
                                        } else if (status.equalsIgnoreCase("Missed")) {
                                            missedPerWeek[i]++;
                                            if ("High".equalsIgnoreCase(priority)) highMissed[i]++;
                                            else if ("Medium".equalsIgnoreCase(priority)) mediumMissed[i]++;
                                            else if ("Low".equalsIgnoreCase(priority)) lowMissed[i]++;
                                        }
                                        if (priority != null) {
                                            switch (priority) {
                                                case "High": high++; break;
                                                case "Medium": medium++; break;
                                                case "Low": low++; break;
                                            }
                                        }
                                        break;
                                    }
                                }

                                for (String range : previousWeeks) {
                                    String[] prev = range.split(" - ");
                                    Date start = new SimpleDateFormat("MMM dd", Locale.ENGLISH).parse(prev[0]);
                                    Date end = new SimpleDateFormat("MMM dd", Locale.ENGLISH).parse(prev[1]);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(start);
                                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                                    start = cal.getTime();
                                    cal.setTime(end);
                                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                                    end = cal.getTime();
                                    if (!date.before(start) && !date.after(end)) {
                                        if (status.equalsIgnoreCase("Completed")) previousCompleted++;
                                        else if (status.equalsIgnoreCase("Missed")) previousMissed++;
                                        break;
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    for (int val : completedPerWeek) totalCompleted += val;
                    for (int val : missedPerWeek) totalMissed += val;

                    int totalDone = totalCompleted + totalMissed;
                    tvMonthlyCompletedTask1.setText(String.valueOf(totalCompleted));
                    tvMonthlyMissedTask1.setText(String.valueOf(totalMissed));
                    float currentRate = totalDone > 0 ? (totalCompleted * 100f) / totalDone : 0f;
                    tvMonthlyCurrentCompletionRate1.setText(String.format(Locale.ENGLISH, "%.0f%%", currentRate));

                    float previousTotal = previousCompleted + previousMissed;
                    float previousRate = previousTotal > 0 ? (previousCompleted * 100f / previousTotal) : 0f;
                    tvMonthlyPreviousCompletionRate1.setText(String.format(Locale.ENGLISH, "%.0f%%", previousRate));

                    float diff = currentRate - previousRate;
                    String direction = diff >= 0 ? "Up" : "Down";
                    float displayDiff = Math.abs(diff);

                    TextView tvMonthlyCompletionComparison1 = requireView().findViewById(R.id.tvMonthlyCompletionComparison1);
                    tvMonthlyCompletionComparison1.setText(String.format(Locale.ENGLISH,
                            "%s by %.1f%% from last month, Keep up the Good Work!", direction, displayDiff));

                    setupPieChart(high, medium, low);
                    setupBarChart(completedPerWeek, missedPerWeek, currentWeeks);
                    setupLineChart(highCompleted, mediumCompleted, lowCompleted, highMissed, mediumMissed, lowMissed, currentWeeks);
                });
    }

    private void setupLineChart(int[] highCompleted, int[] mediumCompleted, int[] lowCompleted,
                                int[] highMissed, int[] mediumMissed, int[] lowMissed,
                                List<String> weekLabels) {

        List<Entry> productivityEntries = new ArrayList<>();
        float[] scores = new float[weekLabels.size()];
        for (int i = 0; i < weekLabels.size(); i++) {
            float score = (highCompleted[i] * 1.2f + mediumCompleted[i] * 1.1f + lowCompleted[i] * 1.1f)
                    - (highMissed[i] * 1.2f + mediumMissed[i] * 1.1f + lowMissed[i] * 1.0f);
            productivityEntries.add(new Entry(i, score));
            scores[i] = score;
        }

        LineDataSet dataSet = new LineDataSet(productivityEntries, "");
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
        xAxis.setAxisMaximum(weekLabels.size() - 0.5f);
        xAxis.setValueFormatter(new ValueFormatter() {
            final String[] weekLabelsFixed = {"Week 4", "Week 3", "Week 2", "Week 1"};

            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < weekLabelsFixed.length ? weekLabelsFixed[index] : "";
            }
        });

        lineChart.animateY(1000);
        lineChart.invalidate();

        // Compute most/least productive weeks
        float max = scores[0], min = scores[0];
        List<Integer> maxIndices = new ArrayList<>();
        List<Integer> minIndices = new ArrayList<>();

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > max) {
                max = scores[i];
                maxIndices.clear();
                maxIndices.add(i);
            } else if (scores[i] == max) {
                maxIndices.add(i);
            }

            if (scores[i] < min) {
                min = scores[i];
                minIndices.clear();
                minIndices.add(i);
            } else if (scores[i] == min) {
                minIndices.add(i);
            }
        }

        List<String> weekFixedLabels = Arrays.asList("Week 4", "Week 3", "Week 2", "Last Week");
        String most = maxIndices.size() == weekLabels.size() ? "Balanced" :
                String.join(", ", maxIndices.stream().map(weekFixedLabels::get).toArray(String[]::new));
        String least = minIndices.size() == weekLabels.size() ? "Balanced" :
                String.join(", ", minIndices.stream().map(weekFixedLabels::get).toArray(String[]::new));

        tvMostProductiveWeek1.setText(most);
        tvLeastProductiveWeek1.setText(least);

        float totalScore = 0f;
        for (float score : scores) totalScore += score;
        float avg = totalScore / scores.length;
        tvMonthlyProductivityScore1.setText(String.format(Locale.ENGLISH, "Your overall monthly productivity score is %.1f!", avg));
    }

    private List<String> generateWeeks() {
        List<String> weekRanges = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        for (int i = 0; i < 4; i++) {
            Date end = calendar.getTime();
            calendar.add(Calendar.DATE, -6);
            Date start = calendar.getTime();
            String formatted = new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(start) + " - " +
                    new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(end);
            weekRanges.add(0, formatted);
            calendar.add(Calendar.DATE, -1);
        }
        return weekRanges;
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

    private void setupBarChart(int[] completed, int[] missed, List<String> labels) {
        List<BarEntry> completedEntries = new ArrayList<>();
        List<BarEntry> missedEntries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
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

        XAxis xAxis = barChart.getXAxis();
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(barData.getGroupWidth(0.3f, 0.05f) * labels.size());
        barChart.groupBars(0f, 0.3f, 0.05f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < labels.size() ? labels.get(index) : "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisRight().setEnabled(false);
        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(40f);
        barChart.setTouchEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDescription(null);
        barChart.setExtraOffsets(10, 10, 10, 10);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupPieChart(int high, int medium, int low) {
        List<PieEntry> entries = new ArrayList<>();
        if (high > 0) entries.add(new PieEntry(high, "High"));
        if (medium > 0) entries.add(new PieEntry(medium, "Medium"));
        if (low > 0) entries.add(new PieEntry(low, "Low"));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.rgb(49, 48, 55), Color.rgb(147, 144, 174), Color.rgb(194, 191, 221));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentValueFormatter());
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
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
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setXEntrySpace(20f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}