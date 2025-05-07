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
import java.util.concurrent.TimeUnit;
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

        fabAddTask1    = findViewById(R.id.fabAddTask1);
        ibtnHome1      = findViewById(R.id.ibtnHome1);
        ibtnCalendar1  = findViewById(R.id.ibtnCalendar1);
        ibtnAnalytics1 = findViewById(R.id.ibtnAnalytics1);
        ibtnProfile1   = findViewById(R.id.ibtnProfile1);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

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

        // Long‐press for RLInsights
        fabAddTask1.setOnLongClickListener(v -> {
            loadFragment(new RLInsightsFragment());
            Toast.makeText(this, "RL Insights loaded", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        1
                );
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
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        Timestamp ts = doc.getTimestamp("deadlineTimestamp");
                        if (ts != null && ts.toDate().before(new Date())) {
                            doc.getReference().update("status", "Missing");
                        }
                    }
                });
    }

    private void scheduleMissedTaskChecker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                CheckMissedTaskWorker.class,
                12, TimeUnit.HOURS
        )
                .addTag("missedTaskChecker")
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
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

    private void setActiveIcon(int selId) {
        if (ibtnHome1==null||ibtnCalendar1==null||ibtnAnalytics1==null||ibtnProfile1==null)
            return;
        ibtnHome1     .setImageResource(selId==R.id.ibtnHome1      ? R.drawable.homeactive      : R.drawable.homeinactive);
        ibtnCalendar1 .setImageResource(selId==R.id.ibtnCalendar1  ? R.drawable.calendaractive  : R.drawable.calendarinactive);
        ibtnAnalytics1.setImageResource(selId==R.id.ibtnAnalytics1 ? R.drawable.analyticsactive : R.drawable.analyticsinactive);
        ibtnProfile1  .setImageResource(selId==R.id.ibtnProfile1   ? R.drawable.profileactive   : R.drawable.profileinactive);
    }

    private void showAddTaskModal() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskadding, null);
        addTaskDialog = new AlertDialog.Builder(this).setView(view).create();
        if (addTaskDialog.getWindow() != null)
            addTaskDialog.getWindow()
                    .setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitle       = view.findViewById(R.id.etTaskTitle1);
        EditText etDescription = view.findViewById(R.id.etmlTaskDescription1);
        Spinner spPriority     = view.findViewById(R.id.spnrPriorityLevel1);
        Spinner spLabel        = view.findViewById(R.id.spnrLabelName1);
        Button  btnDateTime    = view.findViewById(R.id.btnDateTime);
        Button  btnDone        = view.findViewById(R.id.btnDone);

        spPriority.setAdapter(getPriorityAdapter());
        spLabel   .setAdapter(getLabelAdapter());
        spPriority.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        spLabel   .setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        btnDateTime.setOnClickListener(v -> showDateTimePicker(btnDateTime));

        btnDone.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc  = etDescription.getText().toString().trim();
            String prio  = spPriority.getSelectedItem()!=null
                    ? spPriority.getSelectedItem().toString()
                    : "None";
            String label = spLabel   .getSelectedItem()!=null
                    ? spLabel   .getSelectedItem().toString()
                    : "None";
            String deadlineStr = selectedDeadline[0];
            boolean hasDL      = !deadlineStr.equals("No deadline");

            if (title.isEmpty()) {
                etTitle.setError("Title is required!");
                return;
            }

            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                Timestamp nowTS = Timestamp.now();
                Timestamp dlTS  = null;

                if (hasDL) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "MM/dd/yyyy h:mm a",
                                Locale.ENGLISH
                        );
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                        Date dlDate = sdf.parse(deadlineStr);
                        if (dlDate != null) {
                            dlTS = new Timestamp(dlDate);
                            DeadlineNotificationScheduler.schedule(
                                    this,
                                    title,
                                    deadlineStr,
                                    dlDate.getTime() - 24*60*60*1000L
                            );
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                Map<String,Object> data = new HashMap<>();
                data.put("title", title);
                data.put("description", desc);
                data.put("priority", prio);
                data.put("label", label);
                data.put("deadline", deadlineStr);
                data.put("deadlineTimestamp", dlTS);
                data.put("status", "Upcoming");
                data.put("timestamp", nowTS);
                data.put("duration", selectedDuration[0]);

                db.collection("users")
                        .document(uid)
                        .collection("tasks")
                        .add(data)
                        .addOnSuccessListener(r -> {
                            Toast.makeText(this,
                                    "Task added successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();
                            addTaskDialog.dismiss();
                            loadFragment(new HomeFragment());
                            setActiveIcon(R.id.ibtnHome1);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Failed to save task.",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
            }
        });

        addTaskDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showDateTimePicker(Button btnDateTime) {
        // Inflate & transparent bg
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_taskdatetimepicker, null);
        AlertDialog dateDialog = new AlertDialog.Builder(this)
                .setView(view).create();
        if (dateDialog.getWindow() != null)
            dateDialog.getWindow()
                    .setBackgroundDrawableResource(android.R.color.transparent);

        // Manila now + widgets
        TimeZone phTz = TimeZone.getTimeZone("Asia/Manila");
        Calendar now         = Calendar.getInstance(phTz);
        DatePicker datePicker = view.findViewById(R.id.datePicker1);
        Spinner   timePicker = view.findViewById(R.id.spnrTimePicker1);
        Spinner   durPicker  = view.findViewById(R.id.spnrTaskDuration1);
        Button    btnDone    = view.findViewById(R.id.btnDone);

        // Durations
        durPicker.setAdapter(getDurationAdapter());
        durPicker.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);

        // Compute today at midnight PH
        Calendar todayMidnight = Calendar.getInstance(phTz);
        todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
        todayMidnight.set(Calendar.MINUTE,      0);
        todayMidnight.set(Calendar.SECOND,      0);
        todayMidnight.set(Calendar.MILLISECOND, 0);

        // Disable past dates
        datePicker.setMinDate(todayMidnight.getTimeInMillis());
        // Init to today
        datePicker.init(
                todayMidnight.get(Calendar.YEAR),
                todayMidnight.get(Calendar.MONTH),
                todayMidnight.get(Calendar.DAY_OF_MONTH),
                (dp, year, month, day) ->
                        populateTimeSpinnerFor(
                                year==now.get(Calendar.YEAR)
                                        && month==now.get(Calendar.MONTH)
                                        && day==now.get(Calendar.DAY_OF_MONTH),
                                timePicker,
                                now
                        )
        );

        // Pre-fill time spinner
        populateTimeSpinnerFor(true, timePicker, now);

        // Done handler
        btnDone.setOnClickListener(v -> {
            int m = datePicker.getMonth()+1;
            int d = datePicker.getDayOfMonth();
            int y = datePicker.getYear();
            String date = m+"/"+d+"/"+y;
            String time = timePicker.getSelectedItem()!=null
                    ? timePicker.getSelectedItem().toString()
                    : "";
            if (time.isEmpty()) {
                Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
                return;
            }
            String dur = durPicker.getSelectedItem()!=null
                    ? durPicker.getSelectedItem().toString()
                    : "00:15:00";

            selectedDeadline[0] = date+" "+time;
            selectedDuration[0] = dur;
            btnDateTime.setText(selectedDeadline[0]+" • "+dur);
            dateDialog.dismiss();
        });

        dateDialog.show();
    }

    // Fills time options: only future hours if today, else full 24h
    private void populateTimeSpinnerFor(
            boolean isToday,
            Spinner spinner,
            Calendar now
    ) {
        List<String> times = new ArrayList<>();
        if (isToday) {
            for (int h=now.get(Calendar.HOUR_OF_DAY)+1; h<24; h++) {
                int hh = (h%12==0?12:h%12);
                String am = h<12?"AM":"PM";
                times.add(hh+":00 "+am);
            }
            if (!times.isEmpty()) {
                setTimePickerAdapter(times, spinner);
                return;
            }
            // if none left today, fall through to full list
        }
        for (int h=0; h<24; h++) {
            int hh = (h%12==0?12:h%12);
            String am = h<12?"AM":"PM";
            times.add(hh+":00 "+am);
        }
        setTimePickerAdapter(times, spinner);
    }

    private void setTimePickerAdapter(
            List<String> items,
            Spinner spinner
    ) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        ) {
            @Override
            public View getView(int pos, View cv, ViewGroup parent) {
                TextView tv = (TextView)super.getView(pos, cv, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
            @Override
            public View getDropDownView(int pos, View cv, ViewGroup parent) {
                TextView tv = (TextView)super.getDropDownView(pos, cv, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
        };
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(adapter);
        spinner.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
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