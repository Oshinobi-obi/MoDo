package com.application.modo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class RLInsightsFragment extends Fragment {
    private BarChart qValueChart, successRateChart;

    public RLInsightsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rl_insights, container, false);
        qValueChart = view.findViewById(R.id.qValueChart);
        successRateChart = view.findViewById(R.id.successRateChart);
        loadRLDataFromFirestore();
        return view;
    }

    private void loadRLDataFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> qMap = (Map<String, Object>) doc.get("qTable");
                    Map<String, Object> rateMap = (Map<String, Object>) doc.get("stateSuccessRate");

                    if (qMap == null || rateMap == null) return;

                    Map<String, Float> maxQValues = new HashMap<>();
                    for (String key : qMap.keySet()) {
                        String[] split = key.split("\\|");
                        if (split.length < 2) continue;
                        String state = split[0] + "|" + split[1];
                        float q = ((Number) qMap.get(key)).floatValue();
                        maxQValues.put(state, Math.max(q, maxQValues.getOrDefault(state, -Float.MAX_VALUE)));
                    }

                    Map<String, Float> successRates = new HashMap<>();
                    for (String state : rateMap.keySet()) {
                        float rate = ((Number) rateMap.get(state)).floatValue();
                        successRates.put(state, rate);
                    }

                    showBarChart(qValueChart, maxQValues, "Best Q-Value");
                    showBarChart(successRateChart, successRates, "Success Rate");
                })
                .addOnFailureListener(e -> Log.e("RLInsights", "Failed to load RL data", e));
    }

    private void showBarChart(BarChart chart, Map<String, Float> data, String label) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(Color.rgb(49, 48, 55));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        chart.setData(barData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        chart.getAxisLeft().setTextColor(Color.BLACK);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.BLACK);
        chart.getDescription().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}