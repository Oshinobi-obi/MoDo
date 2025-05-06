package com.application.modo;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QAgent {
    private Map<String, Double> qTable = new HashMap<>();
    private double alpha = 0.1; // Learning rate
    private double gamma = 0.9; // Discount factor
    private double epsilon = 0.1; // Exploration rate
    private Random random = new Random();
    private Map<String, Integer> stateVisitCount = new HashMap<>();
    private Map<String, Double> stateSuccessRate = new HashMap<>();

    public double getQValue(String state, String action) {
        return qTable.getOrDefault(state + "|" + action, 0.0);
    }

    public void updateQValue(String state, String action, double reward, String nextState) {
        // Update state visit count
        stateVisitCount.put(state, stateVisitCount.getOrDefault(state, 0) + 1);
        
        // Update success rate
        double currentSuccessRate = stateSuccessRate.getOrDefault(state, 0.0);
        int visits = stateVisitCount.get(state);
        stateSuccessRate.put(state, (currentSuccessRate * (visits - 1) + reward) / visits);
        
        // Update Q-value with adaptive learning rate
        double adaptiveAlpha = calculateAdaptiveLearningRate(state);
        double oldQ = getQValue(state, action);
        double maxNextQ = getMaxQValue(nextState);
        double newQ = oldQ + adaptiveAlpha * (reward + gamma * maxNextQ - oldQ);
        qTable.put(state + "|" + action, newQ);
        
        // Save updates to Firestore
        saveQTableToFirestore();
    }

    private double calculateAdaptiveLearningRate(String state) {
        int visits = stateVisitCount.getOrDefault(state, 0);
        if (visits == 0) return alpha;
        
        // Decrease learning rate as we learn more about the state
        return alpha / (1 + Math.log(1 + visits));
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
        // Epsilon-greedy strategy
        if (random.nextDouble() < epsilon) {
            // Explore: choose random action
            return getRandomAction();
        }
        
        // Exploit: choose best known action
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
        String[] actions = {"Mark as Complete", "Extend"};
        return actions[random.nextInt(actions.length)];
    }

    public void saveQTableToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("qTable", qTable);
        data.put("stateVisitCount", stateVisitCount);
        data.put("stateSuccessRate", stateSuccessRate);
        
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d("QAgent", "Q-table and statistics saved"))
                .addOnFailureListener(e -> Log.e("QAgent", "Failed to save Q-table and statistics", e));
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
                    }
                    
                    if (documentSnapshot.contains("stateVisitCount")) {
                        Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("stateVisitCount");
                        stateVisitCount.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                stateVisitCount.put(key, ((Number) value).intValue());
                            }
                        }
                    }
                    
                    if (documentSnapshot.contains("stateSuccessRate")) {
                        Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("stateSuccessRate");
                        stateSuccessRate.clear();
                        for (String key : map.keySet()) {
                            Object value = map.get(key);
                            if (value instanceof Number) {
                                stateSuccessRate.put(key, ((Number) value).doubleValue());
                            }
                        }
                    }
                    
                    Log.d("QAgent", "Q-table and statistics loaded");
                })
                .addOnFailureListener(e -> Log.e("QAgent", "Failed to load Q-table and statistics", e));
    }

    public double getStateSuccessRate(String state) {
        return stateSuccessRate.getOrDefault(state, 0.0);
    }

    public int getStateVisitCount(String state) {
        return stateVisitCount.getOrDefault(state, 0);
    }
}