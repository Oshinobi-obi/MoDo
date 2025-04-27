package com.application.modo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class ActivityMain extends AppCompatActivity {

    private FloatingActionButton fabAddTask1;
    private ImageButton ibtnHome1, ibtnCalendar1, ibtnAnalytics1, ibtnProfile1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlertDialog addTaskDialog;
    private final String[] selectedDeadline = {"No deadline"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAddTask1 = findViewById(R.id.fabAddTask1);
        ibtnHome1 = findViewById(R.id.ibtnHome1);
        ibtnCalendar1 = findViewById(R.id.ibtnCalendar1);
        ibtnAnalytics1 = findViewById(R.id.ibtnAnalytics1);
        ibtnProfile1 = findViewById(R.id.ibtnProfile1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Default load HomeFragment
        loadFragment(new HomeFragment());
        setActiveIcon(R.id.ibtnHome1);

        // Bottom navigation
        ibtnHome1.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            setActiveIcon(R.id.ibtnHome1);
        });

        ibtnCalendar1.setOnClickListener(v -> {
            loadFragment(new CalendarFragment());
            setActiveIcon(R.id.ibtnCalendar1);
        });

        ibtnAnalytics1.setOnClickListener(v -> {
            loadFragment(new AnalyticsFragment());
            setActiveIcon(R.id.ibtnAnalytics1);
        });

        ibtnProfile1.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            setActiveIcon(R.id.ibtnProfile1);
        });

        fabAddTask1.setOnClickListener(v -> showAddTaskModal());
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void setActiveIcon(int selectedButtonId) {
        if (ibtnHome1 == null || ibtnCalendar1 == null || ibtnAnalytics1 == null || ibtnProfile1 == null)
            return;

        ibtnHome1.setImageResource(selectedButtonId == R.id.ibtnHome1 ? R.drawable.homeactive : R.drawable.homeinactive);
        ibtnCalendar1.setImageResource(selectedButtonId == R.id.ibtnCalendar1 ? R.drawable.calendaractive : R.drawable.calendarinactive);
        ibtnAnalytics1.setImageResource(selectedButtonId == R.id.ibtnAnalytics1 ? R.drawable.analyticsactive : R.drawable.analyticsinactive);
        ibtnProfile1.setImageResource(selectedButtonId == R.id.ibtnProfile1 ? R.drawable.profileactive : R.drawable.profileinactive);
    }

    private void showAddTaskModal() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskadding, null);
        addTaskDialog = new AlertDialog.Builder(this).setView(view).create();
        if (addTaskDialog.getWindow() != null) addTaskDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTaskTitle1 = view.findViewById(R.id.etTaskTitle1);
        EditText etmlTaskDescription1 = view.findViewById(R.id.etmlTaskDescription1);
        Spinner spnrPriorityLevel1 = view.findViewById(R.id.spnrPriorityLevel1);
        Spinner spnrLabelName1 = view.findViewById(R.id.spnrLabelName1);
        Button btnDateTime = view.findViewById(R.id.btnDateTime);
        Button btnDone = view.findViewById(R.id.btnDone);

        spnrPriorityLevel1.setAdapter(getPriorityAdapter());
        spnrPriorityLevel1.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        spnrLabelName1.setAdapter(getLabelAdapter());
        spnrLabelName1.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDateTime.setOnClickListener(v -> showDateTimePicker(btnDateTime));

        btnDone.setOnClickListener(v -> {
            String title = etTaskTitle1.getText().toString().trim();
            String description = etmlTaskDescription1.getText().toString().trim();
            String priority = spnrPriorityLevel1.getSelectedItem() != null ? spnrPriorityLevel1.getSelectedItem().toString() : "None";
            String label = spnrLabelName1.getSelectedItem() != null ? spnrLabelName1.getSelectedItem().toString() : "None";

            if (title.isEmpty()) {
                etTaskTitle1.setError("Title is required!");
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
                            Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show();
                            addTaskDialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save task.", Toast.LENGTH_SHORT).show());
            }
        });

        addTaskDialog.show();
    }

    private void showDateTimePicker(Button btnDateTime) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskdatetimepicker, null);
        AlertDialog dateDialog = new AlertDialog.Builder(this).setView(view).create();
        if (dateDialog.getWindow() != null)
            dateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
        Calendar now = Calendar.getInstance(); // PH Time

        DatePicker datePicker = view.findViewById(R.id.datePicker1);
        Spinner timePicker = view.findViewById(R.id.spnrTimePicker1);
        Button btnDone = view.findViewById(R.id.btnDone);

        // 📍 Block selecting dates before today
        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        // Setup full times
        List<String> fullTimes = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            int h = hour % 12;
            if (h == 0) h = 12;
            String ampm = hour < 12 ? "AM" : "PM";
            fullTimes.add(h + ":00 " + ampm);
        }

        // Default full times for now
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(fullTimes));
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePicker.setAdapter(timeAdapter);
        timePicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        // 📍 When opening, check if today has available time
        Calendar todayMidnight = (Calendar) now.clone();
        todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
        todayMidnight.set(Calendar.MINUTE, 0);
        todayMidnight.set(Calendar.SECOND, 0);
        todayMidnight.set(Calendar.MILLISECOND, 0);

        List<String> availableTimesToday = new ArrayList<>();
        for (int hour = now.get(Calendar.HOUR_OF_DAY) + 1; hour < 24; hour++) { // +1 to block past hour
            int h = hour % 12;
            if (h == 0) h = 12;
            String ampm = hour < 12 ? "AM" : "PM";
            availableTimesToday.add(h + ":00 " + ampm);
        }

        if (availableTimesToday.isEmpty()) {
            // 📍 If no more times left today, force move to tomorrow
            todayMidnight.add(Calendar.DATE, 1); // Move 1 day forward
            datePicker.updateDate(todayMidnight.get(Calendar.YEAR), todayMidnight.get(Calendar.MONTH), todayMidnight.get(Calendar.DAY_OF_MONTH));
        }

        // 📍 Listen to date changes
        datePicker.setOnDateChangedListener((view1, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, monthOfYear, dayOfMonth);

            boolean isToday = selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

            List<String> availableTimes = new ArrayList<>();

            if (isToday) {
                for (int hour = now.get(Calendar.HOUR_OF_DAY) + 1; hour < 24; hour++) { // +1
                    int h = hour % 12;
                    if (h == 0) h = 12;
                    String ampm = hour < 12 ? "AM" : "PM";
                    availableTimes.add(h + ":00 " + ampm);
                }
                if (availableTimes.isEmpty()) {
                    Toast.makeText(this, "No available time for today. Please select a future date.", Toast.LENGTH_SHORT).show();
                    todayMidnight.add(Calendar.DATE, 1); // move to next day
                    datePicker.updateDate(todayMidnight.get(Calendar.YEAR), todayMidnight.get(Calendar.MONTH), todayMidnight.get(Calendar.DAY_OF_MONTH));
                    availableTimes.addAll(fullTimes); // reset time choices
                }
            } else {
                availableTimes.addAll(fullTimes); // normal future day
            }

            ArrayAdapter<String> newTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTimes);
            newTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timePicker.setAdapter(newTimeAdapter);
            timePicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        });

        btnDone.setOnClickListener(v -> {
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();
            int year = datePicker.getYear();
            String date = month + "/" + day + "/" + year;
            String time = timePicker.getSelectedItem() != null ? timePicker.getSelectedItem().toString() : "";

            if (time.isEmpty()) {
                Toast.makeText(this, "Please select a valid time.", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDeadline[0] = date + " " + time + " PHT";
            btnDateTime.setText(selectedDeadline[0]);
            dateDialog.dismiss();
        });

        dateDialog.show();
    }

    private ArrayAdapter<String> getPriorityAdapter() {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("High", "Medium", "Low"));
    }

    private ArrayAdapter<String> getLabelAdapter() {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(
                "Work", "Personal", "School", "Fitness", "Finance", "Health", "Learning", "Hobby", "Project"
        ));
    }
}