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

public class ProfileRewards extends Fragment {

    private RecyclerView rvProfileRewards;
    private ProfileRewardsAdapter adapter;

    public ProfileRewards() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_rewards, container, false);

        rvProfileRewards = view.findViewById(R.id.rvProfileRewards);
        rvProfileRewards.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ProfileRewardsItem> rewardList = new ArrayList<>();
        rewardList.add(new ProfileRewardsItem("Bear", "Strong and steady fighter", "(Milestone: 200 pts)"));
        rewardList.add(new ProfileRewardsItem("Cat", "Graceful, calm, always curious", "(Milestone: 250 pts)"));
        rewardList.add(new ProfileRewardsItem("Chicken", "Small but brave spirit", "(Milestone: 300 pts)"));
        rewardList.add(new ProfileRewardsItem("Dog", "Loyal and fearless friend", "(Milestone: 350 pts)"));
        rewardList.add(new ProfileRewardsItem("Gorilla", "Strong leader, kind heart", "(Milestone: 400 pts)"));
        rewardList.add(new ProfileRewardsItem("Owl", "Wise eyes see everything", "(Milestone: 450 pts)"));
        rewardList.add(new ProfileRewardsItem("Panda", "Gentle soul, peaceful warrior", "(Milestone: 500 pts)"));
        rewardList.add(new ProfileRewardsItem("Rabbit", "Fast and clever jumper", "(Milestone: 550 pts)"));
        rewardList.add(new ProfileRewardsItem("Sealion", "Playful, bold sea explorer", "(Milestone: 600 pts)"));

        adapter = new ProfileRewardsAdapter(rewardList);
        rvProfileRewards.setAdapter(adapter);

        return view;
    }
}
