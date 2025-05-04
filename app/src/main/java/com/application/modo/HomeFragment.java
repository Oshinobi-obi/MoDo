package com.application.modo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

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
    private Handler countdownHandler = new Handler();
    private Runnable countdownRunnable;
    private boolean isTaskDueDialogShown = false;

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

    private void addPointsAndCheckRewards(String uid, String taskTitle, String dateString, int pointsEarned) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> newPoint = new HashMap<>();
        newPoint.put("title", taskTitle);
        newPoint.put("date", dateString);
        newPoint.put("points", "+" + pointsEarned + " points");

        db.collection("users").document(uid)
                .collection("profile_points")
                .add(newPoint)
                .addOnSuccessListener(docRef -> {
                    db.collection("users").document(uid)
                            .collection("profile_points")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                int totalPoints = 0;
                                for (QueryDocumentSnapshot doc : snapshot) {
                                    String pointsStr = doc.getString("points") != null ? doc.getString("points") : "0";
                                    totalPoints += extractPoints(pointsStr);
                                }
                                unlockRewardsIfNeeded(uid, totalPoints);
                            });
                });
    }

    private int extractPoints(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void unlockRewardsIfNeeded(String uid, int totalPoints) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ProfileRewardsItem> rewards = new ArrayList<>();

        if (totalPoints >= 200)
            rewards.add(new ProfileRewardsItem("Bear", "Strong and steady fighter", "(Milestone: 200 pts)"));
        if (totalPoints >= 250)
            rewards.add(new ProfileRewardsItem("Cat", "Graceful, calm, always curious", "(Milestone: 250 pts)"));
        if (totalPoints >= 300)
            rewards.add(new ProfileRewardsItem("Chicken", "Small but brave spirit", "(Milestone: 300 pts)"));
        if (totalPoints >= 350)
            rewards.add(new ProfileRewardsItem("Dog", "Loyal and fearless friend", "(Milestone: 350 pts)"));
        if (totalPoints >= 400)
            rewards.add(new ProfileRewardsItem("Gorilla", "Strong leader, kind heart", "(Milestone: 400 pts)"));
        if (totalPoints >= 450)
            rewards.add(new ProfileRewardsItem("Owl", "Wise eyes see everything", "(Milestone: 450 pts)"));
        if (totalPoints >= 500)
            rewards.add(new ProfileRewardsItem("Panda", "Gentle soul, peaceful warrior", "(Milestone: 500 pts)"));
        if (totalPoints >= 550)
            rewards.add(new ProfileRewardsItem("Rabbit", "Fast and clever jumper", "(Milestone: 550 pts)"));
        if (totalPoints >= 600)
            rewards.add(new ProfileRewardsItem("Sealion", "Playful, bold sea explorer", "(Milestone: 600 pts)"));

        for (ProfileRewardsItem reward : rewards) {
            db.collection("users").document(uid)
                    .collection("profile_rewards")
                    .whereEqualTo("name", reward.getName())
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap.isEmpty()) {
                            db.collection("users").document(uid)
                                    .update("unlockedRewards", com.google.firebase.firestore.FieldValue.arrayUnion(reward.getName()));
                            db.collection("users").document(uid)
                                    .collection("profile_rewards")
                                    .add(reward);
                        }
                    });
        }
    }

    private void startCountdownTimer(TextView tvTaskDuration, long endTime, Runnable onFinish) {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long millisLeft = endTime - System.currentTimeMillis();

                if (millisLeft > 0) {
                    long hours = (millisLeft / (1000 * 60 * 60)) % 24;
                    long minutes = (millisLeft / (1000 * 60)) % 60;
                    long seconds = (millisLeft / 1000) % 60;

                    tvTaskDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    countdownHandler.postDelayed(this, 1000);
                } else {
                    tvTaskDuration.setText("00:00:00");
                    countdownHandler.removeCallbacks(this);
                    onFinish.run();
                }
            }
        };

        countdownHandler.post(countdownRunnable);
    }

    private void showTaskDueDialog(String uid, String taskId, long currentEndTime, MediaPlayer mediaPlayer) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_taskdue, null);
        AlertDialog dueDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        if (dueDialog.getWindow() != null)
            dueDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnExtend = view.findViewById(R.id.btnExtend);
        Button btnMarkComplete = view.findViewById(R.id.btnMarkComplete);
        TextView tvRecommendedAction = view.findViewById(R.id.tvRecommendedAction);

        QAgent qAgent = new QAgent();
        qAgent.loadQTableFromFirestore();

        String priority = currentTask.getPriority();
        String currentState = "Due|" + priority;

        String suggestedAction = qAgent.chooseBestAction(currentState);
        tvRecommendedAction.setText("Recommended Action: " + suggestedAction);

        View.OnClickListener stopAlarmAndDismiss = v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            dueDialog.dismiss();
        };

        btnExtend.setOnClickListener(v -> {
            String action = "Extend";
            double reward = 5.0;
            String nextState = "Ongoing|" + priority;

            long newEndTime = System.currentTimeMillis() + (15 * 60 * 1000);
            db.collection("users").document(uid).collection("tasks").document(taskId)
                    .update("endTime", newEndTime)
                    .addOnSuccessListener(aVoid -> {
                        qAgent.updateQValue(currentState, action, reward, nextState);

                        Toast.makeText(getContext(), "Extended 15 minutes", Toast.LENGTH_SHORT).show();
                        stopAlarmAndDismiss.onClick(v);
                        fetchCurrentTask(uid);

                        TextView tvTaskDuration = ongoingTaskDialog.findViewById(R.id.tvTaskDuration);
                        if (tvTaskDuration != null) {
                            startCountdownTimer(tvTaskDuration, newEndTime, () -> {
                                if (!isTaskDueDialogShown) {
                                    isTaskDueDialogShown = true;
                                    MediaPlayer mp = MediaPlayer.create(requireContext(), R.raw.alarm);
                                    mp.setLooping(true);
                                    mp.start();
                                    showTaskDueDialog(uid, taskId, newEndTime, mp);
                                }
                            });
                        }
                    });
        });

        btnMarkComplete.setOnClickListener(v -> {
            String action = "Complete";
            double reward = 10.0;
            String nextState = "Idle";

            countdownHandler.removeCallbacks(countdownRunnable);
            long now = System.currentTimeMillis();
            long remaining = Math.max(currentEndTime - now, 0);
            String formatted = formatMillisToDuration(remaining);

            db.collection("users").document(uid).collection("tasks").document(taskId)
                    .update("status", "Completed", "endTime", now, "duration", formatted)
                    .addOnSuccessListener(aVoid -> {
                        qAgent.updateQValue(currentState, action, reward, nextState);

                        Toast.makeText(getContext(), "Task marked as completed!", Toast.LENGTH_SHORT).show();
                        stopAlarmAndDismiss.onClick(v);
                        if (ongoingTaskDialog != null) ongoingTaskDialog.dismiss();
                        fetchCompletedTask(uid);
                        fetchCurrentTask(uid);
                    });
        });

        dueDialog.show();
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

                long durationInMillis = parseDuration(upcomingTask.getDuration()); // parse "HH:mm:ss"
                long endTime = System.currentTimeMillis() + durationInMillis;

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
                                        .update(
                                                "status", "Ongoing",
                                                "endTime", endTime
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Task started!", Toast.LENGTH_SHORT).show();
                                            upcomingTaskDialog.dismiss();
                                            fetchUpcomingTask(uid);
                                            fetchCurrentTask(uid);
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(), "Failed to update task.", Toast.LENGTH_SHORT).show());
                            }
                        });
            }
        });

        upcomingTaskDialog.show();
    }

    private long parseDuration(String durationStr) {
        try {
            String[] parts = durationStr.split(":");
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);
            return (hours * 3600 + minutes * 60 + seconds) * 1000;
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMillisToDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
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
        TextView tvTaskDuration = view.findViewById(R.id.tvTaskDuration);
        TextView tvRecommendedAction = view.findViewById(R.id.tvRecommendedAction);
        Button btnTaskSettings = view.findViewById(R.id.btnTaskSettings);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvCTTaskTitle.setText(currentTask.getTitle());
        tvCTTaskDescription.setText(currentTask.getDescription());
        tvCTDeadlineDuration.setText("Deadline: " + currentTask.getDeadline());
        tvCTLabel.setText("Label: " + currentTask.getLabel());

        String uid = mAuth.getCurrentUser().getUid();
        Long endTimeObj = currentTask.getEndTime();
        if (endTimeObj == null) {
            Toast.makeText(getContext(), "Task end time is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        final long finalEndTime = endTimeObj;
        isTaskDueDialogShown = false;

        QAgent qAgent = new QAgent();
        qAgent.loadQTableFromFirestore();
        String currentState = "Ongoing|" + currentTask.getPriority();

        String suggestedAction = qAgent.chooseBestAction(currentState);
        tvRecommendedAction.setText("Recommended Action: " + suggestedAction);

        startCountdownTimer(tvTaskDuration, finalEndTime, () -> {
            if (!isTaskDueDialogShown) {
                isTaskDueDialogShown = true;
                MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();

                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", currentTask.getTitle())
                        .whereEqualTo("description", currentTask.getDescription())
                        .whereEqualTo("deadline", currentTask.getDeadline())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();
                                showTaskDueDialog(uid, docId, finalEndTime, mediaPlayer);
                            }
                        });
            }
        });

        btnDone.setOnClickListener(v -> {
            countdownHandler.removeCallbacks(countdownRunnable);

            db.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("title", currentTask.getTitle())
                    .whereEqualTo("description", currentTask.getDescription())
                    .whereEqualTo("deadline", currentTask.getDeadline())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String docId = querySnapshot.getDocuments().get(0).getId();
                            long now = System.currentTimeMillis();
                            long remaining = Math.max(finalEndTime - now, 0);
                            String formatted = formatMillisToDuration(remaining);

                            db.collection("users").document(uid).collection("tasks")
                                    .document(docId)
                                    .update("status", "Completed", "endTime", now, "duration", formatted)
                                    .addOnSuccessListener(aVoid -> {
                                        qAgent.updateQValue(currentState, "Complete", 10.0, "Idle");

                                        int rewardPoints = currentTask.getPriority().equals("High") ? 10 :
                                                currentTask.getPriority().equals("Medium") ? 7 : 5;

                                        String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
                                        addPointsAndCheckRewards(uid, currentTask.getTitle(), date, rewardPoints);

                                        Toast.makeText(getContext(), "Task marked as completed!", Toast.LENGTH_SHORT).show();
                                        ongoingTaskDialog.dismiss();
                                        fetchCurrentTask(uid);
                                        fetchCompletedTask(uid);
                                        checkAndUpdateBadges(uid);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to update task.", Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        btnTaskSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings clicked!", Toast.LENGTH_SHORT).show();
            ongoingTaskDialog.dismiss();
        });

        ongoingTaskDialog.show();
    }

    private void checkAndUpdateBadges(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int completedCount = snapshot.size();

                    List<String> earned = new ArrayList<>();
                    if (completedCount >= 5) earned.add("Beginner");
                    if (completedCount >= 10) earned.add("Apprentice");
                    if (completedCount >= 20) earned.add("Achiever");
                    if (completedCount >= 30) earned.add("Bookworm");
                    if (completedCount >= 50) earned.add("Go-Getter");
                    if (completedCount >= 75) earned.add("Honor Student");
                    if (completedCount >= 100) earned.add("Overachiever");
                    if (completedCount >= 150) earned.add("Cum Laude");
                    if (completedCount >= 200) earned.add("Valedictorian");

                    db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener(userSnap -> {
                                List<String> existing = (List<String>) userSnap.get("profile_badges");
                                if (existing == null) existing = new ArrayList<>();

                                Set<String> newSet = new HashSet<>(existing);
                                newSet.addAll(earned); // Avoid duplicates

                                db.collection("users").document(uid)
                                        .update("profile_badges", new ArrayList<>(newSet));
                            });
                });
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