package com.application.modo;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QAgent {
    private Map<String, Double> qTable = new HashMap<>();
    private double alpha = 0.1;
    private double gamma = 0.9;
    private double epsilon = 0.1;
    private Random random = new Random();
    private Map<String, Integer> stateVisitCount = new HashMap<>();
    private Map<String, Double> stateSuccessRate = new HashMap<>();
    private Map<String, Integer> transitionCount = new HashMap<>();

    public double getQValue(String state, String action) {
        return qTable.getOrDefault(state + "|" + action, 0.0);
    }

    public void updateQValue(String state, String action, double reward, String nextState) {
        // Track visits and success
        stateVisitCount.put(state, stateVisitCount.getOrDefault(state, 0) + 1);
        double currentSuccessRate = stateSuccessRate.getOrDefault(state, 0.0);
        int visits = stateVisitCount.get(state);
        stateSuccessRate.put(state, (currentSuccessRate * (visits - 1) + reward) / visits);

        // Q-learning update
        double adaptiveAlpha = calculateAdaptiveLearningRate(state);
        double oldQ = getQValue(state, action);
        double maxNextQ = getMaxQValue(nextState);
        double newQ = oldQ + adaptiveAlpha * (reward + gamma * maxNextQ - oldQ);
        qTable.put(state + "|" + action, newQ);

        // Track transition
        String transitionKey = state + "->" + nextState;
        transitionCount.put(transitionKey, transitionCount.getOrDefault(transitionKey, 0) + 1);

        saveQTableToFirestore();
    }

    private double calculateAdaptiveLearningRate(String state) {
        int visits = stateVisitCount.getOrDefault(state, 0);
        return visits == 0 ? alpha : alpha / (1 + Math.log(1 + visits));
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
        if (random.nextDouble() < epsilon) {
            return getRandomAction();
        }

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

        return bestAction.isEmpty() ? getRandomAction() : bestAction;
    }

    private String getRandomAction() {
        String[] actions = {"Mark as Complete", "Extend", "Reschedule"};
        return actions[random.nextInt(actions.length)];
    }

    public double calculateReward(String priority, String status) {
        double reward = 0.0;
        switch (priority) {
            case "High":
                reward = status.equals("Completed") ? 10 : (status.equals("Extended") ? 3 : -10); // ✅ UPDATED
                break;
            case "Medium":
                reward = status.equals("Completed") ? 7 : (status.equals("Extended") ? 2 : -7); // ✅ UPDATED
                break;
            case "Low":
                reward = status.equals("Completed") ? 5 : (status.equals("Extended") ? 1 : -5); // ✅ UPDATED
                break;
        }
        return reward;
    }

    public void saveQTableToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("qTable", qTable);
        data.put("stateVisitCount", stateVisitCount);
        data.put("stateSuccessRate", stateSuccessRate);
        data.put("transitionCount", transitionCount);

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d("QAgent", "Q-table and stats saved"))
                .addOnFailureListener(e -> Log.e("QAgent", "Failed to save Q-table", e));
    }

    public void loadQTableFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.contains("qTable")) {
                        Map<String, Object> map = (Map<String, Object>) doc.get("qTable");
                        qTable.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                qTable.put(key, ((Number) value).doubleValue());
                            }
                        }
                    }

                    if (doc.contains("stateVisitCount")) {
                        Map<String, Object> map = (Map<String, Object>) doc.get("stateVisitCount");
                        stateVisitCount.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                stateVisitCount.put(key, ((Number) value).intValue());
                            }
                        }
                    }

                    if (doc.contains("stateSuccessRate")) {
                        Map<String, Object> map = (Map<String, Object>) doc.get("stateSuccessRate");
                        stateSuccessRate.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                stateSuccessRate.put(key, ((Number) value).doubleValue());
                            }
                        }
                    }

                    if (doc.contains("transitionCount")) {
                        Map<String, Object> map = (Map<String, Object>) doc.get("transitionCount");
                        transitionCount.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                transitionCount.put(key, ((Number) value).intValue());
                            }
                        }
                    }

                    Log.d("QAgent", "Q-table and stats loaded");
                })
                .addOnFailureListener(e -> Log.e("QAgent", "Failed to load Q-table", e));
    }

    public void resetLearning() {
        qTable.clear();
        stateVisitCount.clear();
        stateSuccessRate.clear();
        transitionCount.clear();
    }

    public double getStateSuccessRate(String state) {
        return stateSuccessRate.getOrDefault(state, 0.0);
    }

    public int getStateVisitCount(String state) {
        return stateVisitCount.getOrDefault(state, 0);
    }

    public Map<String, Integer> getTransitionCount() {
        return transitionCount;
    }
}