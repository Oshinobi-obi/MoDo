package com.application.modo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import androidx.work.*;
import android.Manifest;

public class ActivityMain extends AppCompatActivity {

    private FloatingActionButton fabAddTask1;
    private ImageButton ibtnHome1, ibtnCalendar1, ibtnAnalytics1, ibtnProfile1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlertDialog addTaskDialog;
    private final String[] selectedDeadline = {"No deadline"};
    private final String[] selectedDuration = {"00:15:00"};


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

        loadFragment(new HomeFragment());
        setActiveIcon(R.id.ibtnHome1);

        checkForMissedTasks();
        scheduleMissedTaskChecker();

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

        // 🔁 Add logic to handle RLInsightsFragment navigation
        fabAddTask1.setOnLongClickListener(v -> {
            loadFragment(new RLInsightsFragment());
            Toast.makeText(this, "RL Insights loaded", Toast.LENGTH_SHORT).show();
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        fabAddTask1.setOnClickListener(v -> showAddTaskModal());
    }

    private void checkForMissedTasks() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", "Ongoing")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp ts = doc.getTimestamp("deadlineTimestamp");
                        if (ts != null && ts.toDate().before(new Date())) {
                            doc.getReference().update("status", "Missing");
                        }
                    }
                });
    }

    private void scheduleMissedTaskChecker() {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(CheckMissedTaskWorker.class, 12, java.util.concurrent.TimeUnit.HOURS)
                        .addTag("missedTaskChecker")
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "missedTaskChecker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
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
        if (ibtnHome1 == null || ibtnCalendar1 == null || ibtnAnalytics1 == null || ibtnProfile1 == null) return;

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
        spnrLabelName1.setAdapter(getLabelAdapter());

        spnrPriorityLevel1.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        spnrLabelName1.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDateTime.setOnClickListener(v -> showDateTimePicker(btnDateTime));

        btnDone.setOnClickListener(v -> {
            String title = etTaskTitle1.getText().toString().trim();
            String description = etmlTaskDescription1.getText().toString().trim();
            String priority = spnrPriorityLevel1.getSelectedItem() != null ? spnrPriorityLevel1.getSelectedItem().toString() : "None";
            String label = spnrLabelName1.getSelectedItem() != null ? spnrLabelName1.getSelectedItem().toString() : "None";
            String deadlineStr = selectedDeadline[0];
            boolean hasDeadline = !deadlineStr.equals("No deadline");

            if (title.isEmpty()) {
                etTaskTitle1.setError("Title is required!");
                return;
            }

            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                Timestamp now = Timestamp.now();
                Timestamp deadlineTS = null;

                if (hasDeadline) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.ENGLISH);
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                        Date deadlineDate = sdf.parse(deadlineStr);
                        if (deadlineDate != null) {
                            deadlineTS = new Timestamp(deadlineDate);
                            DeadlineNotificationScheduler.schedule(
                                    this, title, deadlineStr, deadlineDate.getTime() - 24 * 60 * 60 * 1000L
                            );
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                Map<String, Object> taskData = new HashMap<>();
                taskData.put("title", title);
                taskData.put("description", description);
                taskData.put("priority", priority);
                taskData.put("label", label);
                taskData.put("deadline", deadlineStr);
                taskData.put("deadlineTimestamp", deadlineTS);
                taskData.put("status", "Upcoming");
                taskData.put("timestamp", now);
                taskData.put("duration", selectedDuration[0]);

                db.collection("users").document(uid).collection("tasks")
                        .add(taskData)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show();
                            addTaskDialog.dismiss();

                            // ✅ Re-load HomeFragment to reflect the newly added task
                            loadFragment(new HomeFragment());
                            setActiveIcon(R.id.ibtnHome1); // highlight Home icon
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save task.", Toast.LENGTH_SHORT).show());
            }
        });

        addTaskDialog.show();
    }

    @SuppressLint("SetTextI18n")
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
        Spinner spnrTaskDuration1 = view.findViewById(R.id.spnrTaskDuration1);
        spnrTaskDuration1.setAdapter(getDurationAdapter());
        spnrTaskDuration1.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);


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

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fullTimes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // black text for selected item
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // black text in dropdown
                return view;
            }
        };
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

            ArrayAdapter<String> newTimeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableTimes) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    ((TextView) view).setTextColor(Color.BLACK);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    ((TextView) view).setTextColor(Color.BLACK);
                    return view;
                }
            };
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

            String duration = spnrTaskDuration1.getSelectedItem() != null
                    ? spnrTaskDuration1.getSelectedItem().toString()
                    : "00:15:00";

            // ✅ Store clean values for saving to Firestore
            selectedDeadline[0] = date + " " + time;
            selectedDuration[0] = duration;

            // ✅ Display formatted deadline and duration to the user
            String displayText = selectedDeadline[0] + " • " + selectedDuration[0];
            btnDateTime.setText(displayText);

            dateDialog.dismiss();
        });

        dateDialog.show();
    }

    private ArrayAdapter<String> getPriorityAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("High", "Medium", "Low")) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // selected view text
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // dropdown items text
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }


    private ArrayAdapter<String> getLabelAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(
                "Work", "Personal", "School", "Fitness", "Finance", "Health", "Learning", "Hobby", "Project"
        )) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private ArrayAdapter<String> getDurationAdapter() {
        List<String> durations = new ArrayList<>();
        for (int mins = 15; mins <= 480; mins += 15) {
            int hours = mins / 60;
            int minutes = mins % 60;
            String formatted = String.format(Locale.getDefault(), "%02d:%02d:00", hours, minutes);
            durations.add(formatted);
        }

        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, durations) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
    }
}