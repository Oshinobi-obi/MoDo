package com.application.modo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileRewardsAdapter extends RecyclerView.Adapter<ProfileRewardsAdapter.RewardViewHolder> {

    private List<ProfileRewardsItem> rewardList;

    public ProfileRewardsAdapter(List<ProfileRewardsItem> rewardList) {
        this.rewardList = rewardList;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_rewards, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        ProfileRewardsItem reward = rewardList.get(position);
        holder.tvAvatarName.setText(reward.getName());
        holder.tvAvatarDescription.setText(reward.getDescription());
        holder.tvRewardCost.setText(reward.getCost());
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {

        TextView tvAvatarName, tvAvatarDescription, tvRewardCost;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarName = itemView.findViewById(R.id.tvAvatarName);
            tvAvatarDescription = itemView.findViewById(R.id.tvAvatarDescription);
            tvRewardCost = itemView.findViewById(R.id.tvRewardCost);
        }
    }
}
