package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
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

public class AnalyticsOverallFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvTotalTaskAmount1;
    private TextView tvOverallCompletedTask1;
    private PieChart pieChart;

    public AnalyticsOverallFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_analytics_overall, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTotalTaskAmount1 = view.findViewById(R.id.tvTotalTaskAmount1);
        tvOverallCompletedTask1 = view.findViewById(R.id.tvOverallCompletedTask1);
        pieChart = view.findViewById(R.id.overallPieChart);

        setupBarChart(view);
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
                    int highCount = 0, mediumCount = 0, lowCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        totalTasks++;

                        String status = doc.getString("status");
                        if (status != null && status.equalsIgnoreCase("Completed")) {
                            completedTasks++;
                        }

                        String priority = doc.getString("priority");
                        if (priority != null) {
                            switch (priority) {
                                case "High": highCount++; break;
                                case "Medium": mediumCount++; break;
                                case "Low": lowCount++; break;
                            }
                        }
                    }

                    tvTotalTaskAmount1.setText(String.valueOf(totalTasks));
                    tvOverallCompletedTask1.setText(String.valueOf(completedTasks));

                    setupPieChart(highCount, mediumCount, lowCount);
                })
                .addOnFailureListener(e -> {
                    tvTotalTaskAmount1.setText("0");
                    tvOverallCompletedTask1.setText("0");
                    setupPieChart(0, 0, 0);
                });
    }

    private void setupBarChart(View view) {
        BarChart barChart = view.findViewById(R.id.overallBarChart);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        List<BarEntry> completeTaskEntries = List.of(
                new BarEntry(0f, 10f), new BarEntry(1f, 7f),
                new BarEntry(2f, 12f), new BarEntry(3f, 8f),
                new BarEntry(4f, 11f), new BarEntry(5f, 9f),
                new BarEntry(6f, 13f)
        );

        List<BarEntry> incompleteTaskEntries = List.of(
                new BarEntry(0f, 5f), new BarEntry(1f, 4f),
                new BarEntry(2f, 3f), new BarEntry(3f, 6f),
                new BarEntry(4f, 2f), new BarEntry(5f, 7f),
                new BarEntry(6f, 4f)
        );

        BarDataSet completeTaskDataSet = new BarDataSet(completeTaskEntries, "Complete Task");
        completeTaskDataSet.setColor(Color.rgb(49, 48, 55));
        completeTaskDataSet.setValueTextColor(Color.BLACK);
        completeTaskDataSet.setValueTextSize(12f);
        completeTaskDataSet.setValueFormatter(new IntValueFormatter());

        BarDataSet incompleteTaskDataSet = new BarDataSet(incompleteTaskEntries, "Incomplete Task");
        incompleteTaskDataSet.setColor(Color.rgb(147, 144, 174));
        incompleteTaskDataSet.setValueTextColor(Color.BLACK);
        incompleteTaskDataSet.setValueTextSize(12f);
        incompleteTaskDataSet.setValueFormatter(new IntValueFormatter());

        float groupSpace = 0.25f, barSpace = 0.05f, barWidth = 0.35f;
        BarData data = new BarData(completeTaskDataSet, incompleteTaskDataSet);
        data.setBarWidth(barWidth);

        barChart.setData(data);
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * 7);
        barChart.groupBars(0f, groupSpace, barSpace);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new DayValueFormatter());

        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        barChart.getDescription().setEnabled(false);
        barChart.setExtraOffsets(10, 10, 10, 10);
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