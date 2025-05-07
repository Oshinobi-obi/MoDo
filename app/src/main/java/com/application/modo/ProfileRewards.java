package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProfileRewards extends Fragment {

    private RecyclerView rvProfileRewards;
    private ProfileRewardsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvCurrentPoints;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_rewards, container, false);
        rvProfileRewards = view.findViewById(R.id.rvProfileRewards);
        rvProfileRewards.setLayoutManager(new LinearLayoutManager(getContext()));

        tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        List<ProfileRewardsItem> rewardList = new ArrayList<>();
        adapter = new ProfileRewardsAdapter(rewardList);
        rvProfileRewards.setAdapter(adapter);

        // Fetch total points and then compute unlocked rewards based on milestones
        db.collection("users").document(uid).collection("profile_points")
                .get()
                .addOnSuccessListener(pointsSnap -> {
                    int totalPoints = 0;
                    for (QueryDocumentSnapshot doc : pointsSnap) {
                        String pointsStr = doc.getString("points");
                        totalPoints += extractPoints(pointsStr);
                    }

                    tvCurrentPoints.setText("Current Points: " + totalPoints + " pts");

                    // Clear and repopulate rewards based on sorted milestones
                    rewardList.clear();
                    Map<String, String[]> definitions = RewardDefinitions.getAll();

                    // Convert definitions to list of entries and sort by threshold ascending
                    List<Map.Entry<String, String[]>> entries = new ArrayList<>(definitions.entrySet());
                    Collections.sort(entries, Comparator.comparingInt(e -> parseThreshold(e.getValue()[1])));

                    // Add rewards in ascending order of threshold
                    for (Map.Entry<String, String[]> entry : entries) {
                        String name = entry.getKey();
                        String description = entry.getValue()[0];
                        String milestoneStr = entry.getValue()[1];
                        int threshold = parseThreshold(milestoneStr);
                        if (totalPoints >= threshold) {
                            rewardList.add(new ProfileRewardsItem(name, description, milestoneStr));
                        }
                    }

                    adapter.notifyDataSetChanged();
                });

        return view;
    }

    private int extractPoints(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseThreshold(String milestoneStr) {
        try {
            return Integer.parseInt(milestoneStr.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}

class RewardDefinitions {
    public static Map<String, String[]> getAll() {
        Map<String, String[]> map = new java.util.HashMap<>();
        map.put("Bear", new String[]{"Strong and steady fighter", "(Milestone: 5 pts)"});
        map.put("Cat", new String[]{"Graceful, calm, always curious", "(Milestone: 10 pts)"});
        map.put("Chicken", new String[]{"Small but brave spirit", "(Milestone: 20 pts)"});
        map.put("Dog", new String[]{"Loyal and fearless friend", "(Milestone: 40 pts)"});
        map.put("Gorilla", new String[]{"Strong leader, kind heart", "(Milestone: 80 pts)"});
        map.put("Owl", new String[]{"Wise eyes see everything", "(Milestone: 160 pts)"});
        map.put("Panda", new String[]{"Gentle soul, peaceful warrior", "(Milestone: 320 pts)"});
        map.put("Rabbit", new String[]{"Fast and clever jumper", "(Milestone: 640 pts)"});
        map.put("Sealion", new String[]{"Playful, bold sea explorer", "(Milestone: 1280 pts)"});
        return map;
    }
}
