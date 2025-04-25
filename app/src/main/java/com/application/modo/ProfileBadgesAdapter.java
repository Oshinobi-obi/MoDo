package com.application.modo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfileBadgesAdapter extends RecyclerView.Adapter<ProfileBadgesAdapter.ProfileBadgesViewHolder> {

    private List<ProfileBadgesItem> badgeList;

    public ProfileBadgesAdapter(List<ProfileBadgesItem> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public ProfileBadgesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_badges, parent, false);
        return new ProfileBadgesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileBadgesViewHolder holder, int position) {
        ProfileBadgesItem currentBadge = badgeList.get(position);

        holder.tvStatusTitle.setText(currentBadge.getStatusTitle());
        holder.tvStatusDescription.setText(currentBadge.getStatusDescription());
        holder.tvStatusRequirements.setText(currentBadge.getStatusRequirements());
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public static class ProfileBadgesViewHolder extends RecyclerView.ViewHolder {

        public TextView tvStatusTitle;
        public TextView tvStatusDescription;
        public TextView tvStatusRequirements;

        public ProfileBadgesViewHolder(View itemView) {
            super(itemView);
            tvStatusTitle = itemView.findViewById(R.id.tvStatusTitle);
            tvStatusDescription = itemView.findViewById(R.id.tvStatusDescription);
            tvStatusRequirements = itemView.findViewById(R.id.tvStatusRequirements);
        }
    }
}