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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileBadges extends Fragment {

    private RecyclerView rvProfileBadges;
    private ProfileBadgesAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvCurrentBadges;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_badges, container, false);
        rvProfileBadges = view.findViewById(R.id.rvProfileBadges);
        rvProfileBadges.setLayoutManager(new LinearLayoutManager(getContext()));

        tvCurrentBadges = view.findViewById(R.id.tvCurrentBadges);
        tvCurrentBadges.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        List<ProfileBadgesItem> badgeList = new ArrayList<>();
        adapter = new ProfileBadgesAdapter(badgeList);
        rvProfileBadges.setAdapter(adapter);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> earned = (List<String>) snapshot.get("profile_badges");
                    if (earned == null || earned.isEmpty()) {
                        // No badges earned yet
                        tvCurrentBadges.setVisibility(View.VISIBLE);
                    } else {
                        // Populate RecyclerView
                        Map<String, String[]> definitions = BadgeDefinitions.getAll();
                        for (String badge : earned) {
                            if (definitions.containsKey(badge)) {
                                String[] info = definitions.get(badge);
                                badgeList.add(new ProfileBadgesItem(
                                        badge,
                                        info[0],
                                        info[1]
                                ));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // On error, show message
                    tvCurrentBadges.setText("Failed to load badges");
                    tvCurrentBadges.setVisibility(View.VISIBLE);
                });

        return view;
    }
}

class BadgeDefinitions {
    public static Map<String, String[]> getAll() {
        Map<String, String[]> map = new HashMap<>();
        map.put("Beginner", new String[]{"Just getting things started", "Complete 5 Tasks"});
        map.put("Apprentice", new String[]{"Learning with steady effort", "Complete 10 Tasks"});
        map.put("Achiever", new String[]{"Goals met with pride", "Complete 20 Tasks"});
        map.put("Bookworm", new String[]{"Always hungry for knowledge", "Complete 30 Tasks"});
        map.put("Go-Getter", new String[]{"Chasing progress with passion", "Complete 50 Tasks"});
        map.put("Honor Student", new String[]{"Reliable, consistent academic achiever", "Complete 75 Tasks"});
        map.put("Overachiever", new String[]{"Exceeds expectations every time", "Complete 100 Tasks"});
        map.put("Cum Laude", new String[]{"Excellence recognized and celebrated", "Complete 150 Tasks"});
        map.put("Valedictorian", new String[]{"Top of the class", "Complete 200 Tasks"});
        return map;
    }
}
