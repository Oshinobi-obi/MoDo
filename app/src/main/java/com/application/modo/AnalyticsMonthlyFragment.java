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

    private TextView tvMonthlyCurrentCompletionRate1;


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
                    List<String> weeks = generateWeeks();
                    int[] completedPerWeek = new int[weeks.size()];
                    int[] missedPerWeek = new int[weeks.size()];
                    int high = 0, medium = 0, low = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String deadlineStr = doc.getString("deadline");
                        String status = doc.getString("status");
                        String priority = doc.getString("priority");

                        if (deadlineStr != null && status != null) {
                            try {
                                String[] parts = deadlineStr.split(" PHT");
                                Date date = sdf.parse(parts[0]);

                                for (int i = 0; i < weeks.size(); i++) {
                                    String[] range = weeks.get(i).split(" - ");
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
                                        } else if (status.equalsIgnoreCase("Missed")) {
                                            missedPerWeek[i]++;
                                        }

                                        // ✅ Count priority only for tasks within the 4-week range
                                        if (priority != null) {
                                            switch (priority) {
                                                case "High": high++; break;
                                                case "Medium": medium++; break;
                                                case "Low": low++; break;
                                            }
                                        }

                                        break; // done checking this task
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    setupPieChart(high, medium, low);
                    setupBarChart(completedPerWeek, missedPerWeek, generateWeeks());

                    int totalCompleted = 0;
                    int totalMissed = 0;
                    for (int val : completedPerWeek) totalCompleted += val;
                    for (int val : missedPerWeek) totalMissed += val;

                    tvMonthlyCompletedTask1.setText(String.valueOf(totalCompleted));
                    tvMonthlyMissedTask1.setText(String.valueOf(totalMissed));

                    int totalDone = totalCompleted + totalMissed;
                    if (totalDone > 0) {
                        float rate = (totalCompleted * 100f) / totalDone;
                        tvMonthlyCurrentCompletionRate1.setText(String.format(Locale.ENGLISH, "%.0f%%", rate));
                    } else {
                        tvMonthlyCurrentCompletionRate1.setText("0%");
                    }
                });
    }


    private List<String> generateWeeks() {
        List<String> weekRanges = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Set to end of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        for (int i = 0; i < 4; i++) {
            Date end = calendar.getTime();
            calendar.add(Calendar.DATE, -6);
            Date start = calendar.getTime();

            String formatted = new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(start) + " - " +
                    new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(end);
            weekRanges.add(0, formatted); // add to front to reverse order

            calendar.add(Calendar.DATE, -1); // move to previous week
        }

        return weekRanges;
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
        float barWidth = 0.3f;
        float barSpace = 0.05f;
        float groupSpace = 0.3f;
        barData.setBarWidth(barWidth);

        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(barData.getGroupWidth(groupSpace, barSpace) * labels.size());
        barChart.groupBars(0f, groupSpace, barSpace);

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

    private void setupLineChart(View view) {
        List<Entry> entries = List.of(
                new Entry(0, 2), new Entry(1, 4),
                new Entry(2, 3), new Entry(3, 6),
                new Entry(4, 5), new Entry(5, 3),
                new Entry(6, 7)
        );

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
        xAxis.setValueFormatter(new ValueFormatter() {
            final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < days.length) ? days[index] : "";
            }
        });

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
}