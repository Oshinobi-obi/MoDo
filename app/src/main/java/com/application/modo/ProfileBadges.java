package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileBadges extends Fragment {

    private RecyclerView rvProfileBadges;
    private ProfileBadgesAdapter adapter;

    public ProfileBadges() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile_badges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProfileBadges = view.findViewById(R.id.rvProfileBadges);
        rvProfileBadges.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ProfileBadgesItem> badgeList = new ArrayList<>();
        badgeList.add(new ProfileBadgesItem("Beginner", "Just getting things started", "Complete 5 Tasks"));
        badgeList.add(new ProfileBadgesItem("Apprentice", "Learning with steady effort", "Complete 10 Tasks"));
        badgeList.add(new ProfileBadgesItem("Achiever", "Goals met with pride", "Complete 20 Tasks"));
        badgeList.add(new ProfileBadgesItem("Bookworm", "Always hungry for knowledge", "Complete 30 Tasks"));
        badgeList.add(new ProfileBadgesItem("Go-Getter", "Chasing progress with passion", "Complete 50 Tasks"));
        badgeList.add(new ProfileBadgesItem("Honor Student", "Reliable, consistent academic achiever", "Complete 75 Tasks"));
        badgeList.add(new ProfileBadgesItem("Overachiever", "Exceeds expectations every time", "Complete 100 Tasks"));
        badgeList.add(new ProfileBadgesItem("Cum Laude", "Excellence recognized and celebrated", "Complete 150 Tasks"));
        badgeList.add(new ProfileBadgesItem("Valedictorian", "Top of the class", "Complete 200 Tasks"));

        adapter = new ProfileBadgesAdapter(badgeList);
        rvProfileBadges.setAdapter(adapter);
    }
}
