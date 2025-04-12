package com.application.modo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private TextView tvUsername3;
    private ImageView imgvPicture1;
    private FloatingActionButton fabAddTask1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String selectedDeadline = "No deadline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvUsername3 = findViewById(R.id.tvUsername3);
        imgvPicture1 = findViewById(R.id.imgvPicture1);
        fabAddTask1 = findViewById(R.id.fabAddTask1);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Load user info from Firestore
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        tvUsername3.setText(" " + username + "!");

                        String avatarName = document.getString("profile");
                        if (avatarName == null || avatarName.isEmpty()) {
                            avatarName = "default_avatar";
                        }
                        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                        imgvPicture1.setImageResource(resId);
                    } else {
                        tvUsername3.setText("Welcome!");
                        Toast.makeText(Home.this, "User data not found", Toast.LENGTH_SHORT).show();
                        imgvPicture1.setImageResource(R.drawable.default_avatar);
                    }
                })
                .addOnFailureListener(e -> {
                    tvUsername3.setText("Welcome!");
                    Toast.makeText(Home.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                    imgvPicture1.setImageResource(R.drawable.default_avatar);
                });

        // FAB opens task modal
        fabAddTask1.setOnClickListener(v -> showAddTaskDialog());

        // Bottom navigation
        findViewById(R.id.ibtnCalendar1).setOnClickListener(v -> {
            startActivity(new Intent(this, Calendar.class));
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.ibtnAnalytics1).setOnClickListener(v -> {
            startActivity(new Intent(this, Analysis.class));
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.ibtnProfile1).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileBadges.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_taskadding, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etTaskTitle = view.findViewById(R.id.etTaskTitle1);
        EditText etTaskDescription = view.findViewById(R.id.etmlTaskDescription1);
        Spinner spnrPriority = view.findViewById(R.id.spnrPriorityLevel1);
        Spinner spnrLabel = view.findViewById(R.id.spnrLabelName1); // placeholder as requested
        Button btnSave = view.findViewById(R.id.btnDone1);
        Button btnDateTime = view.findViewById(R.id.btnDateTime);

        // Setup Priority Spinner
        List<String> priorityList = new ArrayList<>();
        priorityList.add("High");
        priorityList.add("Medium");
        priorityList.add("Low");

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, priorityList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.BLACK : Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }
        };

        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrPriority.setAdapter(priorityAdapter);
        spnrPriority.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        // Open date picker modal
        btnDateTime.setOnClickListener(v -> showDateTimePicker());

        btnSave.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            String priority = spnrPriority.getSelectedItem() != null ? spnrPriority.getSelectedItem().toString() : "None";
            String timeSlot = spnrLabel.getSelectedItem() != null ? spnrLabel.getSelectedItem().toString() : "None";

            if (title.isEmpty()) {
                etTaskTitle.setError("Title is required");
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();
            AddTask newTask = new AddTask(title, description, priority, timeSlot, selectedDeadline);

            db.collection("users").document(uid)
                    .collection("tasks")
                    .add(newTask)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void showDateTimePicker() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_taskdatetimepicker, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dateDialog = builder.create();

        if (dateDialog.getWindow() != null) {
            dateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        CalendarView calendarView = view.findViewById(R.id.cvDatePicker1);
        Spinner timePicker = view.findViewById(R.id.spnrTimePicker1);
        Button btnDone = view.findViewById(R.id.btnDone1);

        // Style the calendar (requires style override — see note below)
        calendarView.setFocusedMonthDateColor(Color.BLACK);
        calendarView.setUnfocusedMonthDateColor(Color.DKGRAY);
        calendarView.setWeekSeparatorLineColor(Color.GRAY);
        calendarView.setSelectedWeekBackgroundColor(Color.parseColor("#E8E8E8"));

        // Time options: 1 hour to 12 hours
        List<String> timeOptions = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            timeOptions.add(i + (i == 1 ? " hour" : " hours"));
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.BLACK : Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
                tv.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.poppins));
                return tv;
            }
        };
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePicker.setAdapter(timeAdapter);
        timePicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        final String[] pickedDate = {""};
        calendarView.setOnDateChangeListener((viewCalendar, year, month, dayOfMonth) -> {
            pickedDate[0] = (month + 1) + "/" + dayOfMonth + "/" + year;
        });

        btnDone.setOnClickListener(v -> {
            String time = timePicker.getSelectedItem() != null ? timePicker.getSelectedItem().toString() : "Unspecified Time";
            selectedDeadline = pickedDate[0] + " " + time;
            Toast.makeText(this, "Deadline: " + selectedDeadline, Toast.LENGTH_SHORT).show();
            dateDialog.dismiss();
        });

        dateDialog.show();
    }
}