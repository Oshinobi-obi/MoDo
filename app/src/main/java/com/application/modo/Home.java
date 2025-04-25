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
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class Home extends AppCompatActivity {

    private TextView tvUsername3, tvCurrentTaskTitle, tvCurrentTaskDescription, tvCurrentTaskDeadline;
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

        findViewById(R.id.ibtnCalendar1).setOnClickListener(v -> startActivity(new Intent(this, Calendar.class)));
        findViewById(R.id.ibtnAnalytics1).setOnClickListener(v -> startActivity(new Intent(this, Analytics.class)));
        findViewById(R.id.ibtnProfile1).setOnClickListener(v -> startActivity(new Intent(this, ProfileBadges.class)));
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

        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, List.of("Uncategorized")) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#3b3b3b"));
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
                AddTask newTask = new AddTask(title, description, priority, label, selectedDeadline[0]);

                db.collection("users").document(uid).collection("tasks").add(newTask)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, getString(R.string.task_added), Toast.LENGTH_SHORT).show();
                            tvCurrentTaskTitle.setText(title);
                            tvCurrentTaskDescription.setText(description);
                            tvCurrentTaskDeadline.setText(selectedDeadline[0]);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.task_save_error), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void showDateTimePicker(Button btnDateTime, String[] selectedDeadline) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskdatetimepicker, null);
        AlertDialog dateDialog = new AlertDialog.Builder(this).setView(view).create();
        if (dateDialog.getWindow() != null) dateDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DatePicker datePicker = view.findViewById(R.id.datePicker1);
        Spinner timePicker = view.findViewById(R.id.spnrTimePicker1);
        Button btnDone = view.findViewById(R.id.btnDone);

        List<String> timeOptions = new ArrayList<>();
        for (int i = 1; i <= 12; i++) timeOptions.add(i + (i == 1 ? " hour" : " hours"));

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeOptions);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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