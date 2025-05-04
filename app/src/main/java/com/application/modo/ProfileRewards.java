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

import java.util.*;

public class ProfileRewards extends Fragment {

    private RecyclerView rvProfileRewards;
    private ProfileRewardsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_rewards, container, false);
        rvProfileRewards = view.findViewById(R.id.rvProfileRewards);
        rvProfileRewards.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints); // 🔄 Get TextView for current points

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        List<ProfileRewardsItem> rewardList = new ArrayList<>();
        adapter = new ProfileRewardsAdapter(rewardList);
        rvProfileRewards.setAdapter(adapter);

        // Step 1: Fetch total points
        db.collection("users").document(uid).collection("profile_points")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalPoints = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String pointsStr = doc.getString("points");
                        int points = extractPoints(pointsStr);
                        totalPoints += points;
                    }

                    tvCurrentPoints.setText("Current Points: " + totalPoints + " pts");

                    // Step 2: Load rewards based on unlocked ones
                    db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                List<String> unlocked = (List<String>) snapshot.get("unlockedRewards");
                                if (unlocked != null) {
                                    Map<String, String[]> definitions = RewardDefinitions.getAll();
                                    rewardList.clear();
                                    for (String name : unlocked) {
                                        if (definitions.containsKey(name)) {
                                            String[] info = definitions.get(name);
                                            rewardList.add(new ProfileRewardsItem(name, info[0], info[1]));
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            });
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

}

class RewardDefinitions {
    public static Map<String, String[]> getAll() {
        Map<String, String[]> map = new HashMap<>();
        map.put("Bear", new String[]{"Strong and steady fighter", "(Milestone: 200 pts)"});
        map.put("Cat", new String[]{"Graceful, calm, always curious", "(Milestone: 250 pts)"});
        map.put("Chicken", new String[]{"Small but brave spirit", "(Milestone: 300 pts)"});
        map.put("Dog", new String[]{"Loyal and fearless friend", "(Milestone: 350 pts)"});
        map.put("Gorilla", new String[]{"Strong leader, kind heart", "(Milestone: 400 pts)"});
        map.put("Owl", new String[]{"Wise eyes see everything", "(Milestone: 450 pts)"});
        map.put("Panda", new String[]{"Gentle soul, peaceful warrior", "(Milestone: 500 pts)"});
        map.put("Rabbit", new String[]{"Fast and clever jumper", "(Milestone: 550 pts)"});
        map.put("Sealion", new String[]{"Playful, bold sea explorer", "(Milestone: 600 pts)"});
        return map;
    }
}