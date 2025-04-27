package com.application.modo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CompletedTaskAdapter extends RecyclerView.Adapter<CompletedTaskAdapter.ViewHolder> {

    private List<AddTask> completedTaskList;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public CompletedTaskAdapter(List<AddTask> completedTaskList, Context context) {
        this.completedTaskList = completedTaskList;
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CompletedTaskAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_completedtask, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedTaskAdapter.ViewHolder holder, int position) {
        AddTask task = completedTaskList.get(position);
        holder.tvTaskTitle1.setText(task.getTitle());
        holder.tvTaskDescription1.setText(task.getDescription());
        holder.tvTaskDeadline.setText(task.getDeadline());

        // Short Click: Open Dialog
        holder.itemView.setOnClickListener(v -> showCompletedTaskDialog(task));

        // Long Press: Delete Task
        holder.itemView.setOnLongClickListener(v -> {
            confirmDelete(task, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return completedTaskList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle1, tvTaskDescription1, tvTaskDeadline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle1 = itemView.findViewById(R.id.tvTaskTitle1);
            tvTaskDescription1 = itemView.findViewById(R.id.tvTaskDescription1);
            tvTaskDeadline = itemView.findViewById(R.id.tvTaskDeadline);
        }
    }

    private void showCompletedTaskDialog(AddTask task) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_completedtask_view, null);
        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvCTTaskTitle = view.findViewById(R.id.tvCTTaskTitle);
        TextView tvCTTaskDescription = view.findViewById(R.id.tvCTTaskDescription);
        TextView tvCTDeadlineDuration = view.findViewById(R.id.tvCTDeadlineDuration);
        TextView tvCTLabel = view.findViewById(R.id.tvCTLabel);
        TextView tvCTCompletedDate = view.findViewById(R.id.tvCTCompletedDate);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvCTTaskTitle.setText(task.getTitle());
        tvCTTaskDescription.setText(task.getDescription());
        tvCTDeadlineDuration.setText("Deadline: " + task.getDeadline());
        tvCTLabel.setText("Label: " + task.getLabel());
        tvCTCompletedDate.setText(task.getDeadline());

        btnDone.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void confirmDelete(AddTask task, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_task, null);
        AlertDialog deleteDialog = new AlertDialog.Builder(context).setView(view).create();
        if (deleteDialog.getWindow() != null)
            deleteDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", task.getTitle())
                        .whereEqualTo("description", task.getDescription())
                        .whereEqualTo("deadline", task.getDeadline())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();
                                db.collection("users").document(uid).collection("tasks")
                                        .document(docId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                                            completedTaskList.remove(position);
                                            notifyItemRemoved(position);
                                            deleteDialog.dismiss(); // Close the dialog after deletion
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show());
                            }
                        });
            }
        });

        btnNo.setOnClickListener(v -> deleteDialog.dismiss());

        deleteDialog.show();
    }
}