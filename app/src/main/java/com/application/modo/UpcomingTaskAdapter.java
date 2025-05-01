package com.application.modo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UpcomingTaskAdapter extends RecyclerView.Adapter<UpcomingTaskAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AddTask task);
        void onItemLongClick(AddTask task);
    }


    private List<AddTask> taskList;
    private OnItemClickListener listener;

    public UpcomingTaskAdapter(List<AddTask> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDeadline;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvUpcomingTaskTitle1);
            tvDescription = itemView.findViewById(R.id.tvUpcomingTaskDescription1);
            tvDeadline = itemView.findViewById(R.id.tvUpcomingTaskDeadline);
        }

        public void bind(AddTask task, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(task));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(task);
                return true;
            });
        }
    }

    @NonNull
    @Override
    public UpcomingTaskAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_upcomingtask, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingTaskAdapter.ViewHolder holder, int position) {
        AddTask task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.tvDeadline.setText(task.getDeadline());
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}