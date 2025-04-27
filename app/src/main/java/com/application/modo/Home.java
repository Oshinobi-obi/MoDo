package com.application.modo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.*;

public class Home extends AppCompatActivity {

    private TextView tvUsername3, tvCurrentTaskTitle, tvCurrentTaskDescription, tvCurrentTaskDeadline;
    private TextView tvTaskTitle1, tvTaskDescription1, tvTaskDeadline;
    private ImageView imgvPicture1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AddTask currentTask;
    private AddTask completedTask;
    private AlertDialog ongoingTaskDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvUsername3 = findViewById(R.id.tvUsername3);
        imgvPicture1 = findViewById(R.id.imgvPicture1);
        FloatingActionButton fabAddTask1 = findViewById(R.id.fabAddTask1);
        tvCurrentTaskTitle = findViewById(R.id.tvCurrentTaskTitle1);
        tvCurrentTaskDescription = findViewById(R.id.tvCurrentTaskDescription1);
        tvCurrentTaskDeadline = findViewById(R.id.tvCurrentTaskDeadline1);
        tvTaskTitle1 = findViewById(R.id.tvTaskTitle1);
        tvTaskDescription1 = findViewById(R.id.tvTaskDescription1);
        tvTaskDeadline = findViewById(R.id.tvTaskDeadline);

        ConstraintLayout clCurrentTask1 = findViewById(R.id.clCurrentTask1);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String username = document.getString("username");
                            tvUsername3.setText(getString(R.string.welcome_user, username));
                            String avatarName = document.getString("profile");
                            if (avatarName == null || avatarName.isEmpty()) avatarName = "default_avatar";
                            int resId = avatarMap.getOrDefault(avatarName, R.drawable.default_avatar);
                            imgvPicture1.setImageResource(resId);
                            fetchCurrentTask(uid);
                            fetchCompletedTask(uid);
                        } else {
                            tvUsername3.setText(getString(R.string.welcome_default));
                            imgvPicture1.setImageResource(R.drawable.default_avatar);
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUsername3.setText(getString(R.string.welcome_default));
                        imgvPicture1.setImageResource(R.drawable.default_avatar);
                    });
        }

        fabAddTask1.setOnClickListener(v -> showAddTaskDialog());
        ConstraintLayout clTask1 = findViewById(R.id.clTask1);
        clTask1.setOnClickListener(v -> showCompletedTaskDialog());
        clCurrentTask1.setOnClickListener(v -> showOngoingTaskDialog());

        findViewById(R.id.ibtnCalendar1).setOnClickListener(v -> startActivity(new Intent(this, Calendar.class)));
        findViewById(R.id.ibtnAnalytics1).setOnClickListener(v -> startActivity(new Intent(this, Analytics.class)));
        findViewById(R.id.ibtnProfile1).setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        findViewById(R.id.tvCurrentTaskSeeAll1).setOnClickListener(v -> {startActivity(new Intent(this, HomeCurrentTask.class));finish();});
        findViewById(R.id.tvTaskSeeAll1).setOnClickListener(v -> {startActivity(new Intent(this, HomeCompletedTask.class));finish();});
    }

    private void fetchCurrentTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Ongoing")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load current task", Toast.LENGTH_SHORT).show());
    }

    private void fetchCompletedTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Completed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load completed task", Toast.LENGTH_SHORT).show());
    }

    private void showAddTaskDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskadding, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTaskTitle = view.findViewById(R.id.etTaskTitle1);
        EditText etTaskDescription = view.findViewById(R.id.etmlTaskDescription1);
        Spinner spnrPriority = view.findViewById(R.id.spnrPriorityLevel1);
        Spinner spnrLabel = view.findViewById(R.id.spnrLabelName1);
        Button btnDateTime = view.findViewById(R.id.btnDateTime);
        Button btnSave = view.findViewById(R.id.btnDone);

        final String[] selectedDeadline = {"No deadline"};

        spnrPriority.setAdapter(getPriorityAdapter());
        spnrPriority.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        spnrLabel.setAdapter(getLabelAdapter());
        spnrLabel.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDateTime.setOnClickListener(v -> showDateTimePicker(btnDateTime, selectedDeadline));

        btnSave.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            String priority = spnrPriority.getSelectedItem() != null ? spnrPriority.getSelectedItem().toString() : "None";
            String label = spnrLabel.getSelectedItem() != null ? spnrLabel.getSelectedItem().toString() : "None";

            if (title.isEmpty()) {
                etTaskTitle.setError(getString(R.string.title_required));
                return;
            }

            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("title", title);
                taskData.put("description", description);
                taskData.put("priority", priority);
                taskData.put("label", label);
                taskData.put("deadline", selectedDeadline[0]);
                taskData.put("status", "Ongoing");
                taskData.put("timestamp", Timestamp.now());

                db.collection("users").document(uid).collection("tasks").add(taskData)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, getString(R.string.task_added), Toast.LENGTH_SHORT).show();
                            fetchCurrentTask(uid);
                            fetchCompletedTask(uid);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.task_save_error), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void showOngoingTaskDialog() {
        if (currentTask == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ongoingtask_view, null);
        ongoingTaskDialog = new AlertDialog.Builder(this).setView(view).create();
        if (ongoingTaskDialog.getWindow() != null) ongoingTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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

        btnTaskSettings.setOnClickListener(v -> {
            showTaskOptionsDialog();
        });

        btnDone.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null && currentTask != null) {
                String uid = mAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", currentTask.getTitle())
                        .whereEqualTo("description", currentTask.getDescription())
                        .whereEqualTo("deadline", currentTask.getDeadline()) // or timestamp if available
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();

                                db.collection("users").document(uid).collection("tasks")
                                        .document(docId)
                                        .update("status", "Completed")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Task marked as completed!", Toast.LENGTH_SHORT).show();
                                            ongoingTaskDialog.dismiss();
                                            fetchCurrentTask(uid); // 🆕 Refresh current task
                                            fetchCompletedTask(uid); // 🆕 Also refresh the completed task
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error finding task.", Toast.LENGTH_SHORT).show());
            }
        });

        ongoingTaskDialog.show();
    }

    private void showCompletedTaskDialog() {
        if (completedTask == null) { // we need to define completedTask
            Toast.makeText(this, "No completed task to show.", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_completedtask_view, null);
        AlertDialog completedTaskDialog = new AlertDialog.Builder(this).setView(view).create();
        if (completedTaskDialog.getWindow() != null) completedTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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

    private void showTaskOptionsDialog() {
        if (currentTask == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskoptions, null);
        AlertDialog optionsDialog = new AlertDialog.Builder(this).setView(view).create();
        if (optionsDialog.getWindow() != null) optionsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnEditTask = view.findViewById(R.id.btnEditTask);

        btnEditTask.setOnClickListener(v -> {
            optionsDialog.dismiss();
            if (ongoingTaskDialog != null && ongoingTaskDialog.isShowing()) {
                ongoingTaskDialog.dismiss(); // 🆕 Close both
            }
            showEditTaskDialog();
        });

        view.findViewById(R.id.btnEditLabel).setOnClickListener(v -> {
            Toast.makeText(this, "Edit Label clicked!", Toast.LENGTH_SHORT).show();
            optionsDialog.dismiss();
        });

        view.findViewById(R.id.btnRemoveTask).setOnClickListener(v -> {
            Toast.makeText(this, "Remove Task clicked!", Toast.LENGTH_SHORT).show();
            optionsDialog.dismiss();
        });

        optionsDialog.show();
    }

    private void showEditTaskDialog() {
        if (currentTask == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskedit, null);
        AlertDialog editDialog = new AlertDialog.Builder(this).setView(view).create();
        if (editDialog.getWindow() != null) editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitle = view.findViewById(R.id.etTaskTitle1);
        EditText etDescription = view.findViewById(R.id.etmlTaskDescription1);
        Spinner spnrPriority = view.findViewById(R.id.spnrPriorityLevel1);
        Spinner spnrLabel = view.findViewById(R.id.spnrLabelName1);
        Button btnDateTime = view.findViewById(R.id.btnDateTime);
        Button btnDone = view.findViewById(R.id.btnDone);

        etTitle.setText(currentTask.getTitle());
        etDescription.setText(currentTask.getDescription());
        btnDateTime.setText(currentTask.getDeadline());

        spnrPriority.setAdapter(getPriorityAdapter());
        spnrPriority.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        if (currentTask.getPriority() != null) {
            int pos = ((ArrayAdapter<String>) spnrPriority.getAdapter()).getPosition(currentTask.getPriority());
            spnrPriority.setSelection(pos);
        }

        spnrLabel.setAdapter(getLabelAdapter());
        spnrLabel.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        if (currentTask.getLabel() != null) {
            int pos = ((ArrayAdapter<String>) spnrLabel.getAdapter()).getPosition(currentTask.getLabel());
            spnrLabel.setSelection(pos);
        }

        btnDateTime.setOnClickListener(v -> showDateTimePicker(btnDateTime));

        btnDone.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString().trim();
            String updatedDescription = etDescription.getText().toString().trim();
            String updatedPriority = spnrPriority.getSelectedItem() != null ? spnrPriority.getSelectedItem().toString() : "None";
            String updatedLabel = spnrLabel.getSelectedItem() != null ? spnrLabel.getSelectedItem().toString() : "None";
            String updatedDeadline = btnDateTime.getText().toString().replace("Deadline: ", "").trim();

            if (updatedTitle.isEmpty()) {
                etTitle.setError("Title is required!");
                return;
            }

            if (mAuth.getCurrentUser() != null && currentTask != null) {
                String uid = mAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", currentTask.getTitle())
                        .whereEqualTo("description", currentTask.getDescription())
                        .whereEqualTo("timestamp", currentTask.getTimestamp())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String docId = querySnapshot.getDocuments().get(0).getId();

                                Map<String, Object> updatedData = new HashMap<>();
                                updatedData.put("title", updatedTitle);
                                updatedData.put("description", updatedDescription);
                                updatedData.put("priority", updatedPriority);
                                updatedData.put("label", updatedLabel);
                                updatedData.put("deadline", updatedDeadline);

                                db.collection("users").document(uid).collection("tasks").document(docId)
                                        .update(updatedData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show();

                                            // 🆕 Update the currentTask manually
                                            currentTask.setTitle(updatedTitle);
                                            currentTask.setDescription(updatedDescription);
                                            currentTask.setPriority(updatedPriority);
                                            currentTask.setLabel(updatedLabel);
                                            currentTask.setDeadline(updatedDeadline);

                                            editDialog.dismiss();
                                            showOngoingTaskDialog();
                                        })

                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error finding task.", Toast.LENGTH_SHORT).show());
            }
        });

        editDialog.show();
    }

    private void showDateTimePicker(Button btnDateTime, String[] selectedDeadline) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskdatetimepicker, null);
        AlertDialog dateDialog = new AlertDialog.Builder(this).setView(view).create();
        if (dateDialog.getWindow() != null) dateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DatePicker datePicker = view.findViewById(R.id.datePicker1);
        Spinner timePicker = view.findViewById(R.id.spnrTimePicker1);
        Button btnDone = view.findViewById(R.id.btnDone);

        List<String> times = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            int h = hour % 12;
            if (h == 0) h = 12;
            String ampm = hour < 12 ? "AM" : "PM";
            times.add(h + ":00 " + ampm);
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }
        };

        timePicker.setAdapter(timeAdapter);
        timePicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDone.setOnClickListener(v -> {
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();
            int year = datePicker.getYear();
            String date = month + "/" + day + "/" + year;
            String time = timePicker.getSelectedItem() != null ? timePicker.getSelectedItem().toString() : "";
            String finalDeadline = date + " " + time;

            btnDateTime.setText(getString(R.string.deadline, finalDeadline));
            selectedDeadline[0] = finalDeadline;
            dateDialog.dismiss();
        });

        dateDialog.show();
    }

    private void showDateTimePicker(Button btnDateTime) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskdatetimepicker, null);
        AlertDialog dateDialog = new AlertDialog.Builder(this).setView(view).create();
        if (dateDialog.getWindow() != null) dateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DatePicker datePicker = view.findViewById(R.id.datePicker1);
        Spinner timePicker = view.findViewById(R.id.spnrTimePicker1);
        Button btnDone = view.findViewById(R.id.btnDone);

        List<String> times = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            int h = hour % 12;
            if (h == 0) h = 12;
            String ampm = hour < 12 ? "AM" : "PM";
            times.add(h + ":00 " + ampm);
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }
        };

        timePicker.setAdapter(timeAdapter);
        timePicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDone.setOnClickListener(v -> {
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();
            int year = datePicker.getYear();
            String date = month + "/" + day + "/" + year;
            String time = timePicker.getSelectedItem() != null ? timePicker.getSelectedItem().toString() : "";
            btnDateTime.setText(date + " " + time);
            dateDialog.dismiss();
        });

        dateDialog.show();
    }

    private ArrayAdapter<String> getLabelAdapter() {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, List.of(
                "Work", "Personal", "School", "Fitness", "Finance", "Health", "Learning", "Hobby", "Project"
        )) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }
        };
    }


    private ArrayAdapter<String> getPriorityAdapter() {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, List.of("High", "Medium", "Low")) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }
        };
    }
}