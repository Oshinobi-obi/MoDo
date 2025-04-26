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
                            if (avatarName == null || avatarName.isEmpty()) {
                                avatarName = "default_avatar";
                            }

                            int resId = avatarMap.getOrDefault(avatarName, R.drawable.default_avatar);
                            imgvPicture1.setImageResource(resId);

                            fetchCurrentTask(uid);
                            fetchCompletedTask(uid);

                        } else {
                            tvUsername3.setText(getString(R.string.welcome_default));
                            Toast.makeText(Home.this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
                            imgvPicture1.setImageResource(R.drawable.default_avatar);
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUsername3.setText(getString(R.string.welcome_default));
                        Toast.makeText(Home.this, getString(R.string.load_user_failed), Toast.LENGTH_SHORT).show();
                        imgvPicture1.setImageResource(R.drawable.default_avatar);
                    });
        }

        fabAddTask1.setOnClickListener(v -> showAddTaskDialog());

        clCurrentTask1.setOnClickListener(v -> showOngoingTaskDialog()); // 🔥 Added this

        findViewById(R.id.ibtnCalendar1).setOnClickListener(v -> startActivity(new Intent(this, Calendar.class)));
        findViewById(R.id.ibtnAnalytics1).setOnClickListener(v -> startActivity(new Intent(this, Analytics.class)));
        findViewById(R.id.ibtnProfile1).setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
    }

    private void fetchCurrentTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Ongoing")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        AddTask task = queryDocumentSnapshots.getDocuments().get(0).toObject(AddTask.class);
                        if (task != null) {
                            tvCurrentTaskTitle.setText(task.getTitle());
                            tvCurrentTaskDescription.setText(task.getDescription());
                            tvCurrentTaskDeadline.setText(task.getDeadline());
                        }
                    } else {
                        tvCurrentTaskTitle.setText("No Current Task");
                        tvCurrentTaskDescription.setText("");
                        tvCurrentTaskDeadline.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load current task", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCompletedTask(String uid) {
        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Completed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        AddTask task = queryDocumentSnapshots.getDocuments().get(0).toObject(AddTask.class);
                        if (task != null) {
                            tvTaskTitle1.setText(task.getTitle());
                            tvTaskDescription1.setText(task.getDescription());
                            tvTaskDeadline.setText(task.getDeadline());
                        }
                    } else {
                        tvTaskTitle1.setText("No Completed Task");
                        tvTaskDescription1.setText("");
                        tvTaskDeadline.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load completed task", Toast.LENGTH_SHORT).show();
                });
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

        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, List.of("Uncategorized"));
        labelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrLabel.setAdapter(labelAdapter);
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
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ongoingtask_view, null);
        AlertDialog ongoingTaskDialog = new AlertDialog.Builder(this).setView(view).create();
        if (ongoingTaskDialog.getWindow() != null)
            ongoingTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvCTTaskTitle = view.findViewById(R.id.tvCTTaskTitle);
        TextView tvCTTaskDescription = view.findViewById(R.id.tvCTTaskDescription);
        TextView tvCTDeadlineDuration = view.findViewById(R.id.tvCTDeadlineDuration);
        TextView tvCTLabel = view.findViewById(R.id.tvCTLabel);
        Button btnTaskSettings = view.findViewById(R.id.btnTaskSettings);
        Button btnDone = view.findViewById(R.id.btnDone);

        tvCTTaskTitle.setText(tvCurrentTaskTitle.getText());
        tvCTTaskDescription.setText(tvCurrentTaskDescription.getText());
        tvCTDeadlineDuration.setText(tvCurrentTaskDeadline.getText());
        tvCTLabel.setText("Uncategorized"); // Default for now

        btnTaskSettings.setOnClickListener(v -> showTaskOptionsDialog());
        btnDone.setOnClickListener(v -> {
            Toast.makeText(this, "Task marked as completed!", Toast.LENGTH_SHORT).show();
            ongoingTaskDialog.dismiss();
        });

        ongoingTaskDialog.show();
    }

    private void showTaskOptionsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskoptions, null);
        AlertDialog taskOptionsDialog = new AlertDialog.Builder(this).setView(view).create();
        if (taskOptionsDialog.getWindow() != null)
            taskOptionsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnEditTask = view.findViewById(R.id.btnEditTask);
        Button btnEditLabel = view.findViewById(R.id.btnEditLabel);
        Button btnRemoveTask = view.findViewById(R.id.btnRemoveTask);

        btnEditTask.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Task clicked!", Toast.LENGTH_SHORT).show();
            taskOptionsDialog.dismiss();
        });

        btnEditLabel.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Label clicked!", Toast.LENGTH_SHORT).show();
            taskOptionsDialog.dismiss();
        });

        btnRemoveTask.setOnClickListener(v -> {
            Toast.makeText(this, "Remove Task clicked!", Toast.LENGTH_SHORT).show();
            taskOptionsDialog.dismiss();
        });

        taskOptionsDialog.show();
    }

    private void showDateTimePicker(Button btnDateTime, String[] selectedDeadline) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskdatetimepicker, null);
        AlertDialog dateDialog = new AlertDialog.Builder(this).setView(view).create();
        if (dateDialog.getWindow() != null) dateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DatePicker datePicker = view.findViewById(R.id.datePicker1);
        Spinner timePicker = view.findViewById(R.id.spnrTimePicker1);
        Button btnDone = view.findViewById(R.id.btnDone);

        List<String> timeOptions = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            String period = (hour < 12) ? "AM" : "PM";
            timeOptions.add(displayHour + ":00 " + period);
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item_limited, timeOptions);
        timeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_limited);
        timePicker.setAdapter(timeAdapter);
        timePicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDone.setOnClickListener(v -> {
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();
            String pickedDate = (month + 1) + "/" + day + "/" + year;

            String time = timePicker.getSelectedItem() != null ? timePicker.getSelectedItem().toString() : "Unspecified Time";
            selectedDeadline[0] = pickedDate + " " + time;
            btnDateTime.setText(getString(R.string.deadline, selectedDeadline[0]));
            dateDialog.dismiss();
        });

        dateDialog.show();
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