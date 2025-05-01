package com.application.modo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvUsername3, tvCurrentTaskTitle, tvCurrentTaskDescription, tvCurrentTaskDeadline;
    private TextView tvTaskTitle1, tvTaskDescription1, tvTaskDeadline;
    private ImageView imgvPicture1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AddTask currentTask;
    private AddTask completedTask;
    private AlertDialog ongoingTaskDialog;
    private AddTask upcomingTask;
    private ConstraintLayout clUpcomingTask;
    private TextView tvUpcomingTaskTitle, tvUpcomingTaskDescription, tvUpcomingTaskDeadline;
    private AddTask missedTask;
    private ConstraintLayout clMissedTask;
    private TextView tvMissedTaskTitle, tvMissedTaskDescription, tvMissedTaskDeadline;


    private final Map<String, Integer> avatarMap = new HashMap<>() {{
        put("bear", R.drawable.bear);
        put("cat", R.drawable.cat);
        put("chicken", R.drawable.chicken);
        put("dog", R.drawable.dog);
        put("gorilla", R.drawable.gorilla);
        put("owl", R.drawable.owl);
        put("panda", R.drawable.panda);
        put("rabbit", R.drawable.rabbit);
        put("sealion", R.drawable.sealion);
        put("default_avatar", R.drawable.default_avatar);
    }};

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvUsername3 = view.findViewById(R.id.tvUsername3);
        imgvPicture1 = view.findViewById(R.id.imgvPicture1);
        tvCurrentTaskTitle = view.findViewById(R.id.tvCurrentTaskTitle1);
        tvCurrentTaskDescription = view.findViewById(R.id.tvCurrentTaskDescription1);
        tvCurrentTaskDeadline = view.findViewById(R.id.tvCurrentTaskDeadline1);
        tvTaskTitle1 = view.findViewById(R.id.tvTaskTitle1);
        tvTaskDescription1 = view.findViewById(R.id.tvTaskDescription1);
        tvTaskDeadline = view.findViewById(R.id.tvTaskDeadline);
        clUpcomingTask = view.findViewById(R.id.clUpcomingTask1);
        clUpcomingTask.setOnClickListener(v -> showUpcomingTaskDialog());
        tvUpcomingTaskTitle = view.findViewById(R.id.tvUpcomingTaskTitle);
        tvUpcomingTaskDescription = view.findViewById(R.id.tvUpcomingTaskDescription);
        tvUpcomingTaskDeadline = view.findViewById(R.id.tvUpcomingTaskDeadline);
        clMissedTask = view.findViewById(R.id.clMissedTask1);
        clMissedTask.setOnClickListener(v -> showMissedTaskDialog());
        tvMissedTaskTitle = view.findViewById(R.id.tvMissedTaskTitle);
        tvMissedTaskDescription = view.findViewById(R.id.tvMissedTaskDescription);
        tvMissedTaskDeadline = view.findViewById(R.id.tvMissedTaskDeadline);

        ConstraintLayout clCurrentTask1 = view.findViewById(R.id.clCurrentTask1);
        ConstraintLayout clTask1 = view.findViewById(R.id.clTask1);

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (!isAdded()) return; // ✅
                        if (document.exists()) {
                            String username = document.getString("username");
                            tvUsername3.setText(getString(R.string.welcome_user, username));
                            String avatarName = document.getString("profile");
                            if (avatarName == null || avatarName.isEmpty()) avatarName = "default_avatar";
                            int resId = avatarMap.getOrDefault(avatarName, R.drawable.default_avatar);
                            imgvPicture1.setImageResource(resId);
                            fetchCurrentTask(uid);
                            fetchCompletedTask(uid);
                            fetchUpcomingTask(uid);
                            fetchMissedTask(uid);
                        } else {
                            tvUsername3.setText(getString(R.string.welcome_default));
                            imgvPicture1.setImageResource(R.drawable.default_avatar);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return; // ✅
                        tvUsername3.setText(getString(R.string.welcome_default));
                        imgvPicture1.setImageResource(R.drawable.default_avatar);
                    });
        }

        clTask1.setOnClickListener(v -> showCompletedTaskDialog());
        clCurrentTask1.setOnClickListener(v -> showOngoingTaskDialog());

        view.findViewById(R.id.tvCurrentTaskSeeAll1).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HomeCurrentTask.class));
            requireActivity().finish();
        });

        view.findViewById(R.id.tvTaskSeeAll1).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HomeCompletedTask.class));
            requireActivity().finish();
        });

        view.findViewById(R.id.tvUpcomingTaskSeeAll).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HomeUpcomingTask.class));
            requireActivity().finish();
        });

        view.findViewById(R.id.tvMissedTaskSeeAll).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HomeMissedTask.class));
            requireActivity().finish();
        });

        return view;
    }

    private void fetchCurrentTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Ongoing")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return; // ✅
                    if (!queryDocumentSnapshots.isEmpty()) {
                        currentTask = queryDocumentSnapshots.getDocuments().get(0).toObject(AddTask.class);
                        if (currentTask != null) {
                            tvCurrentTaskTitle.setText(currentTask.getTitle());
                            tvCurrentTaskDescription.setText(currentTask.getDescription());
                            tvCurrentTaskDeadline.setText(currentTask.getDeadline());
                        }
                    } else {
                        tvCurrentTaskTitle.setText("No Current Task");
                        tvCurrentTaskDescription.setText("");
                        tvCurrentTaskDeadline.setText("");
                        currentTask = null;
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return; // ✅
                    Toast.makeText(getContext(), "Failed to load current task", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCompletedTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Completed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return; // ✅
                    if (!queryDocumentSnapshots.isEmpty()) {
                        completedTask = queryDocumentSnapshots.getDocuments().get(0).toObject(AddTask.class);
                        if (completedTask != null) {
                            tvTaskTitle1.setText(completedTask.getTitle());
                            tvTaskDescription1.setText(completedTask.getDescription());
                            tvTaskDeadline.setText(completedTask.getDeadline());
                        }
                    } else {
                        tvTaskTitle1.setText("No Completed Task");
                        tvTaskDescription1.setText("");
                        tvTaskDeadline.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return; // ✅
                    Toast.makeText(getContext(), "Failed to load completed task", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUpcomingTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Upcoming")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    if (!querySnapshot.isEmpty()) {
                        upcomingTask = querySnapshot.getDocuments().get(0).toObject(AddTask.class);
                        if (upcomingTask != null) {
                            clUpcomingTask.setVisibility(View.VISIBLE);
                            tvUpcomingTaskTitle.setText(upcomingTask.getTitle());
                            tvUpcomingTaskDescription.setText(upcomingTask.getDescription());
                            tvUpcomingTaskDeadline.setText(upcomingTask.getDeadline());
                        }
                    } else {
                        tvUpcomingTaskTitle.setText("No Upcoming Task");
                        tvUpcomingTaskDescription.setText("");
                        tvUpcomingTaskDeadline.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Failed to load upcoming task", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchMissedTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Missing")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    if (!querySnapshot.isEmpty()) {
                        missedTask = querySnapshot.getDocuments().get(0).toObject(AddTask.class);
                        if (missedTask != null) {
                            clMissedTask.setVisibility(View.VISIBLE);
                            tvMissedTaskTitle.setText(missedTask.getTitle());
                            tvMissedTaskDescription.setText(missedTask.getDescription());
                            tvMissedTaskDeadline.setText(missedTask.getDeadline());
                        }
                    } else {
                        tvMissedTaskTitle.setText("No Missed Task");
                        tvMissedTaskDescription.setText("");
                        tvMissedTaskDeadline.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Failed to load missed task", Toast.LENGTH_SHORT).show();
                });
    }

    private void showMissedTaskDialog() {
        if (missedTask == null) return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_missed_view, null);
        AlertDialog missedDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        if (missedDialog.getWindow() != null)
            missedDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvMTTitle = view.findViewById(R.id.tvMTTaskTitle);
        TextView tvMTDescription = view.findViewById(R.id.tvMTTaskDescription);
        TextView tvMTDeadline = view.findViewById(R.id.tvMTDeadlineDuration);
        TextView tvMTLabel = view.findViewById(R.id.tvMTLabel);
        Button btnMarkAsDone = view.findViewById(R.id.btnDone);

        tvMTTitle.setText(missedTask.getTitle());
        tvMTDescription.setText(missedTask.getDescription());
        tvMTDeadline.setText("Deadline: " + missedTask.getDeadline());
        tvMTLabel.setText("Label: " + missedTask.getLabel());

        btnMarkAsDone.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", missedTask.getTitle())
                        .whereEqualTo("description", missedTask.getDescription())
                        .whereEqualTo("deadline", missedTask.getDeadline())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!isAdded()) return;
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();
                                db.collection("users").document(uid).collection("tasks")
                                        .document(docId)
                                        .update("status", "Completed")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Marked as completed", Toast.LENGTH_SHORT).show();
                                            missedDialog.dismiss();
                                            fetchMissedTask(uid);
                                            fetchCompletedTask(uid);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
                            }
                        });
            }
        });

        missedDialog.show();
    }

    private void showUpcomingTaskDialog() {
        if (upcomingTask == null) return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_upcoming_view, null);
        AlertDialog upcomingTaskDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        if (upcomingTaskDialog.getWindow() != null)
            upcomingTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvUTTaskTitle = view.findViewById(R.id.tvUTTaskTitle);
        TextView tvUTTaskDescription = view.findViewById(R.id.tvUTTaskDescription);
        TextView tvUTDeadlineDuration = view.findViewById(R.id.tvUTDeadlineDuration);
        TextView tvUTLabel = view.findViewById(R.id.tvUTLabel);
        Button btnTaskSettings = view.findViewById(R.id.btnTaskSettings);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvUTTaskTitle.setText(upcomingTask.getTitle());
        tvUTTaskDescription.setText(upcomingTask.getDescription());
        tvUTDeadlineDuration.setText("Deadline: " + upcomingTask.getDeadline());
        tvUTLabel.setText("Label: " + upcomingTask.getLabel());

        btnTaskSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings clicked!", Toast.LENGTH_SHORT).show();
            upcomingTaskDialog.dismiss();
        });

        btnDone.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", upcomingTask.getTitle())
                        .whereEqualTo("description", upcomingTask.getDescription())
                        .whereEqualTo("deadline", upcomingTask.getDeadline())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!isAdded()) return;
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();
                                db.collection("users").document(uid).collection("tasks")
                                        .document(docId)
                                        .update("status", "Ongoing")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Task started!", Toast.LENGTH_SHORT).show();
                                            upcomingTaskDialog.dismiss();
                                            fetchUpcomingTask(uid);
                                            fetchCurrentTask(uid);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update task.", Toast.LENGTH_SHORT).show());
                            }
                        });
            }
        });

        upcomingTaskDialog.show();
    }

    private void showOngoingTaskDialog() {
        if (currentTask == null) return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ongoingtask_view, null);
        ongoingTaskDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        if (ongoingTaskDialog.getWindow() != null)
            ongoingTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvCTTaskTitle = view.findViewById(R.id.tvCTTaskTitle);
        TextView tvCTTaskDescription = view.findViewById(R.id.tvCTTaskDescription);
        TextView tvCTDeadlineDuration = view.findViewById(R.id.tvCTDeadlineDuration);
        TextView tvCTLabel = view.findViewById(R.id.tvCTLabel);
        Button btnTaskSettings = view.findViewById(R.id.btnTaskSettings);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvCTTaskTitle.setText(currentTask.getTitle());
        tvCTTaskDescription.setText(currentTask.getDescription());
        tvCTDeadlineDuration.setText("Deadline: " + currentTask.getDeadline());
        tvCTLabel.setText("Label: " + currentTask.getLabel());

        btnDone.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null && currentTask != null) {
                String uid = mAuth.getCurrentUser().getUid();
                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", currentTask.getTitle())
                        .whereEqualTo("description", currentTask.getDescription())
                        .whereEqualTo("deadline", currentTask.getDeadline())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!isAdded()) return; // ✅
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();
                                db.collection("users").document(uid).collection("tasks")
                                        .document(docId)
                                        .update("status", "Completed")
                                        .addOnSuccessListener(aVoid -> {
                                            if (!isAdded()) return; // ✅
                                            Toast.makeText(getContext(), "Task marked as completed!", Toast.LENGTH_SHORT).show();
                                            ongoingTaskDialog.dismiss();
                                            fetchCurrentTask(uid);
                                            fetchCompletedTask(uid);
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!isAdded()) return; // ✅
                                            Toast.makeText(getContext(), "Failed to update task.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            }
        });

        btnTaskSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings clicked!", Toast.LENGTH_SHORT).show();
            ongoingTaskDialog.dismiss();
        });

        ongoingTaskDialog.show();
    }

    private void showCompletedTaskDialog() {
        if (completedTask == null) {
            Toast.makeText(getContext(), "No completed task to show.", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_completed_view, null);
        AlertDialog completedTaskDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        if (completedTaskDialog.getWindow() != null)
            completedTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvCTTaskTitle = view.findViewById(R.id.tvCTTaskTitle);
        TextView tvCTTaskDescription = view.findViewById(R.id.tvCTTaskDescription);
        TextView tvCTDeadlineDuration = view.findViewById(R.id.tvCTDeadlineDuration);
        TextView tvCTLabel = view.findViewById(R.id.tvCTLabel);
        TextView tvCTCompletedDate = view.findViewById(R.id.tvCTCompletedDate);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvCTTaskTitle.setText(completedTask.getTitle());
        tvCTTaskDescription.setText(completedTask.getDescription());
        tvCTDeadlineDuration.setText("Deadline: " + completedTask.getDeadline());
        tvCTLabel.setText("Label: " + completedTask.getLabel());
        tvCTCompletedDate.setText(completedTask.getDeadline());

        btnDone.setOnClickListener(v -> completedTaskDialog.dismiss());

        completedTaskDialog.show();
    }
}