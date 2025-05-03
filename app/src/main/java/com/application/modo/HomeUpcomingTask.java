package com.application.modo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class HomeUpcomingTask extends AppCompatActivity {

    private RecyclerView rvUpcomingTaskList;
    private UpcomingTaskAdapter adapter;
    private List<AddTask> taskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_upcomingtask);

        rvUpcomingTaskList = findViewById(R.id.rvUpcomingTaskList);
        rvUpcomingTaskList.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        adapter = new UpcomingTaskAdapter(taskList, new UpcomingTaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AddTask task) {
                showUpcomingDialog(task);
            }

            @Override
            public void onItemLongClick(AddTask task) {
                showDeleteDialog(task);
            }
        });
        rvUpcomingTaskList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView ivBack = findViewById(R.id.ivReturnUpcomingTask);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(HomeUpcomingTask.this, ActivityMain.class);
            startActivity(intent);
            finish();
        });

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            fetchUpcomingTasks(uid);
        }
    }

    private void showUpcomingDialog(AddTask task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_task_upcoming_view, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = view.findViewById(R.id.tvUTTaskTitle);
        TextView tvDescription = view.findViewById(R.id.tvUTTaskDescription);
        TextView tvDeadline = view.findViewById(R.id.tvUTDeadlineDuration);
        TextView tvLabel = view.findViewById(R.id.tvUTLabel);
        Button btnSettings = view.findViewById(R.id.btnTaskSettings);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvTitle.setText(task.getTitle());
        tvDescription.setText(task.getDescription());
        tvDeadline.setText("Deadline: " + task.getDeadline());
        tvLabel.setText("Label: " + task.getLabel());

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnDone.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) return;
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("title", task.getTitle())
                    .whereEqualTo("description", task.getDescription())
                    .whereEqualTo("deadline", task.getDeadline())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            String docId = snapshot.getDocuments().get(0).getId();
                            db.collection("users").document(uid).collection("tasks")
                                    .document(docId)
                                    .update("status", "Ongoing")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Task started!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        fetchUpcomingTasks(uid);
                                    });
                        }
                    });
        });

        dialog.show();
    }

    private void showDeleteDialog(AddTask task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_task, null);
        AlertDialog deleteDialog = new AlertDialog.Builder(this).setView(view).create();
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
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                String docId = snapshot.getDocuments().get(0).getId();
                                db.collection("users").document(uid).collection("tasks")
                                        .document(docId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                                            fetchUpcomingTasks(uid);
                                            deleteDialog.dismiss();
                                        });
                            }
                        });
            }
        });

        btnNo.setOnClickListener(v -> deleteDialog.dismiss());

        deleteDialog.show();
    }

    private void fetchUpcomingTasks(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Upcoming")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    taskList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        AddTask task = doc.toObject(AddTask.class);
                        taskList.add(task);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle errors if needed
                });
    }
}