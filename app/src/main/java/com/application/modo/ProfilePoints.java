package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfilePoints extends Fragment {

    private RecyclerView rvProfilePoints;
    private ProfilePointsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvCurrentPoints;  // renamed to reflect "current"

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_points, container, false);
        rvProfilePoints = view.findViewById(R.id.rvProfilePoints);
        rvProfilePoints.setLayoutManager(new LinearLayoutManager(getContext()));

        tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        List<ProfilePointsItem> pointList = new ArrayList<>();
        adapter = new ProfilePointsAdapter(pointList);
        rvProfilePoints.setAdapter(adapter);

        // Fetch points and display
        db.collection("users").document(uid).collection("profile_points")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    pointList.clear();
                    int totalPoints = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title") != null ? doc.getString("title") : "Unknown Task";
                        String date = doc.getString("date") != null ? doc.getString("date") : "Unknown Date";
                        String pointsStr = doc.getString("points") != null ? doc.getString("points") : "0";

                        int points = extractPoints(pointsStr); // "+10 points" → 10
                        totalPoints += points;

                        pointList.add(new ProfilePointsItem(title, date, "+" + points + " points"));
                    }

                    // Display in the tvCurrentPoints
                    tvCurrentPoints.setText("Current Points: " + totalPoints + " pts");
                    adapter.notifyDataSetChanged();
                    checkForRewards(totalPoints, uid);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load points.", Toast.LENGTH_SHORT).show());

        return view;
    }

    private int extractPoints(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void checkForRewards(int totalPoints, String uid) {
        List<ProfileRewardsItem> rewardsToAdd = new ArrayList<>();

        if (totalPoints >= 5)
            rewardsToAdd.add(new ProfileRewardsItem("Bear", "Strong and steady fighter", "(Milestone: 5 pts)"));
        if (totalPoints >= 10)
            rewardsToAdd.add(new ProfileRewardsItem("Cat", "Graceful, calm, always curious", "(Milestone: 10 pts)"));
        if (totalPoints >= 20)
            rewardsToAdd.add(new ProfileRewardsItem("Chicken", "Small but brave spirit", "(Milestone: 20 pts)"));
        if (totalPoints >= 40)
            rewardsToAdd.add(new ProfileRewardsItem("Dog", "Loyal and fearless friend", "(Milestone: 40 pts)"));
        if (totalPoints >= 80)
            rewardsToAdd.add(new ProfileRewardsItem("Gorilla", "Strong leader, kind heart", "(Milestone: 80 pts)"));
        if (totalPoints >= 160)
            rewardsToAdd.add(new ProfileRewardsItem("Owl", "Wise eyes see everything", "(Milestone: 160 pts)"));
        if (totalPoints >= 320)
            rewardsToAdd.add(new ProfileRewardsItem("Panda", "Gentle soul, peaceful warrior", "(Milestone: 320 pts)"));
        if (totalPoints >= 640)
            rewardsToAdd.add(new ProfileRewardsItem("Rabbit", "Fast and clever jumper", "(Milestone: 640 pts)"));
        if (totalPoints >= 1280)
            rewardsToAdd.add(new ProfileRewardsItem("Sealion", "Playful, bold sea explorer", "(Milestone: 1280 pts)"));

        for (ProfileRewardsItem reward : rewardsToAdd) {
            db.collection("users").document(uid)
                    .collection("profile_rewards")
                    .whereEqualTo("name", reward.getName())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            db.collection("users").document(uid)
                                    .collection("profile_rewards")
                                    .add(reward);
                        }
                    });
        }
    }
}