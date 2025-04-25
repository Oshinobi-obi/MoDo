package com.application.modo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfilePointsAdapter extends RecyclerView.Adapter<ProfilePointsAdapter.PointViewHolder> {

    private List<ProfilePointsItem> pointList;

    public ProfilePointsAdapter(List<ProfilePointsItem> pointList) {
        this.pointList = pointList;
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_points, parent, false);
        return new PointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        ProfilePointsItem point = pointList.get(position);
        holder.tvTaskTitle.setText(point.getTitle());
        holder.tvTaskDescription.setText(point.getDescription());
        holder.tvPointsCollected.setText(point.getPoints());
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }

    static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskDescription, tvPointsCollected;

        public PointViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskCompletedDate);
            tvPointsCollected = itemView.findViewById(R.id.tvPointsCollected);
        }
    }
}