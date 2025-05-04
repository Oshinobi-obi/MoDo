package com.application.modo;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class QAgent {
    private Map<String, Double> qTable = new HashMap<>();
    private double alpha = 0.1;
    private double gamma = 0.9;

    public double getQValue(String state, String action) {
        return qTable.getOrDefault(state + "|" + action, 0.0);
    }

    public void updateQValue(String state, String action, double reward, String nextState) {
        double oldQ = getQValue(state, action);
        double maxNextQ = getMaxQValue(nextState);
        double newQ = oldQ + alpha * (reward + gamma * maxNextQ - oldQ);
        qTable.put(state + "|" + action, newQ);
        saveQTableToFirestore(); // Save after update
    }

    public double getMaxQValue(String state) {
        double maxQ = Double.NEGATIVE_INFINITY;
        for (String key : qTable.keySet()) {
            if (key.startsWith(state + "|")) {
                maxQ = Math.max(maxQ, qTable.get(key));
            }
        }
        return maxQ == Double.NEGATIVE_INFINITY ? 0.0 : maxQ;
    }

    public String chooseBestAction(String state) {
        double bestValue = Double.NEGATIVE_INFINITY;
        String bestAction = "";
        for (String key : qTable.keySet()) {
            if (key.startsWith(state + "|")) {
                double value = qTable.get(key);
                if (value > bestValue) {
                    bestValue = value;
                    bestAction = key.split("\\|")[1];
                }
            }
        }
        return bestAction.isEmpty() ? "Mark as Complete" : bestAction;
    }

    public void saveQTableToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("qTable", qTable)
                .addOnSuccessListener(aVoid -> Log.d("QAgent", "Q-table saved"))
                .addOnFailureListener(e -> Log.e("QAgent", "Failed to save Q-table", e));
    }

    public void loadQTableFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.contains("qTable")) {
                        Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("qTable");
                        qTable.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                qTable.put(key, ((Number) value).doubleValue());
                            }
                        }
                        Log.d("QAgent", "Q-table loaded");
                    }
                })
                .addOnFailureListener(e -> Log.e("QAgent", "Failed to load Q-table", e));
    }
}