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
    private TextView tvTotalPoints;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_points, container, false);
        rvProfilePoints = view.findViewById(R.id.rvProfilePoints);
        rvProfilePoints.setLayoutManager(new LinearLayoutManager(getContext()));

        tvTotalPoints = view.findViewById(R.id.tvTotalPoints); // Make sure this TextView exists in the layout

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        List<ProfilePointsItem> pointList = new ArrayList<>();
        adapter = new ProfilePointsAdapter(pointList);
        rvProfilePoints.setAdapter(adapter);

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

                    tvTotalPoints.setText("Total Points: " + totalPoints);
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

        if (totalPoints >= 200)
            rewardsToAdd.add(new ProfileRewardsItem("Bear", "Strong and steady fighter", "(Milestone: 200 pts)"));
        if (totalPoints >= 250)
            rewardsToAdd.add(new ProfileRewardsItem("Cat", "Graceful, calm, always curious", "(Milestone: 250 pts)"));
        if (totalPoints >= 300)
            rewardsToAdd.add(new ProfileRewardsItem("Chicken", "Small but brave spirit", "(Milestone: 300 pts)"));
        if (totalPoints >= 350)
            rewardsToAdd.add(new ProfileRewardsItem("Dog", "Loyal and fearless friend", "(Milestone: 350 pts)"));
        if (totalPoints >= 400)
            rewardsToAdd.add(new ProfileRewardsItem("Gorilla", "Strong leader, kind heart", "(Milestone: 400 pts)"));
        if (totalPoints >= 450)
            rewardsToAdd.add(new ProfileRewardsItem("Owl", "Wise eyes see everything", "(Milestone: 450 pts)"));
        if (totalPoints >= 500)
            rewardsToAdd.add(new ProfileRewardsItem("Panda", "Gentle soul, peaceful warrior", "(Milestone: 500 pts)"));
        if (totalPoints >= 550)
            rewardsToAdd.add(new ProfileRewardsItem("Rabbit", "Fast and clever jumper", "(Milestone: 550 pts)"));
        if (totalPoints >= 600)
            rewardsToAdd.add(new ProfileRewardsItem("Sealion", "Playful, bold sea explorer", "(Milestone: 600 pts)"));

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