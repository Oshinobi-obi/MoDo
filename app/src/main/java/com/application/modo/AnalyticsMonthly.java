package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import androidx.fragment.app.Fragment;
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

public class AnalyticsMonthly extends Fragment {

    public AnalyticsMonthly() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_analytics_monthly, container, false);

        setupBarChart(view);
        setupPieChart(view);
        setupLineChart(view);

        return view;
    }

    private void setupBarChart(View view) {
        BarChart barChart = view.findViewById(R.id.monthlyBarChart);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        List<BarEntry> completeTaskEntries = List.of(
                new BarEntry(0f, 10f), new BarEntry(1f, 7f),
                new BarEntry(2f, 12f), new BarEntry(3f, 8f)
        );

        List<BarEntry> incompleteTaskEntries = List.of(
                new BarEntry(0f, 5f), new BarEntry(1f, 4f),
                new BarEntry(2f, 3f), new BarEntry(3f, 6f)
        );

        BarDataSet completeTaskDataSet = new BarDataSet(completeTaskEntries, "Complete Task");
        completeTaskDataSet.setColor(Color.rgb(49, 48, 55));
        completeTaskDataSet.setValueTextColor(Color.BLACK);
        completeTaskDataSet.setValueTextSize(16f);
        completeTaskDataSet.setValueFormatter(new IntValueFormatter());

        BarDataSet incompleteTaskDataSet = new BarDataSet(incompleteTaskEntries, "Incomplete Task");
        incompleteTaskDataSet.setColor(Color.rgb(147, 144, 174));
        incompleteTaskDataSet.setValueTextColor(Color.BLACK);
        incompleteTaskDataSet.setValueTextSize(16f);
        incompleteTaskDataSet.setValueFormatter(new IntValueFormatter());

        float groupSpace = 0.2f, barSpace = 0.05f, barWidth = 0.35f;
        BarData data = new BarData(completeTaskDataSet, incompleteTaskDataSet);
        data.setBarWidth(barWidth);

        barChart.setData(data);
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(16f);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * 4);
        xAxis.setValueFormatter(new WeekValueFormatter());

        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(16f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        barChart.animateY(1000);
    }

    private void setupPieChart(View view) {
        PieChart pieChart = view.findViewById(R.id.monthlyPieChart);

        List<PieEntry> pieEntries = List.of(
                new PieEntry(30f, "High"),
                new PieEntry(33f, "Medium"),
                new PieEntry(37f, "Low")
        );

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
        pieChart.animateY(1000);
        pieChart.invalidate();

        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(16f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setXEntrySpace(20f);
    }

    private void setupLineChart(View view) {
        LineChart lineChart = view.findViewById(R.id.monthlyLineChart);

        List<Entry> lineEntries = List.of(
                new Entry(0f, 2f), new Entry(1f, 4f),
                new Entry(2f, 3f), new Entry(3f, 6f)
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
        xAxis.setAxisMaximum(3.5f);
        xAxis.setValueFormatter(new WeekValueFormatter());

        lineChart.animateY(1000);
        lineChart.invalidate();
    }

    // Helper formatters
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

    private static class WeekValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            switch ((int) value) {
                case 0: return "Week 1";
                case 1: return "Week 2";
                case 2: return "Week 3";
                case 3: return "Week 4";
                default: return "";
            }
        }
    }
}