package com.application.modo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSettings extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvFirstName2, tvMiddleName2, tvLastName3, tvSuffix2, tvBirthdate2, tvAge, tvUsername5;
    private TextView tvTaskReminder2, tvBreakReminder2, tvEmail4;
    private Button btnEditPersonal, btnEditAccount, btnEditPrefs, btnSignOut;

    private ImageView ivReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        // Return button logic
        ivReturn = findViewById(R.id.ivReturn);
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileSettings.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });

        // Firebase and other setups
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        fetchUserData();
        setupEditActions();
        setupNavigation();
    }


    private void initializeViews() {
        tvFirstName2 = findViewById(R.id.tvFirstName2);
        tvMiddleName2 = findViewById(R.id.tvMiddleName2);
        tvLastName3 = findViewById(R.id.tvLastName3);
        tvSuffix2 = findViewById(R.id.tvSuffix2);
        tvBirthdate2 = findViewById(R.id.tvBirthdate2);
        tvAge = findViewById(R.id.tvAge);
        tvTaskReminder2 = findViewById(R.id.tvTaskReminder2);
        tvBreakReminder2 = findViewById(R.id.tvBreakReminder2);
        tvEmail4 = findViewById(R.id.tvEmail4);
        tvUsername5 = findViewById(R.id.tvUsername5);

        btnEditPersonal = findViewById(R.id.btnEditPersonalDetails1);
        btnEditAccount = findViewById(R.id.btnEditAccountDetails1);
        btnEditPrefs = findViewById(R.id.btnEditPreferences1);
        btnSignOut = findViewById(R.id.btnSignOut1);
    }

    private void fetchUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvFirstName2.setText(doc.getString("firstname"));
                        tvMiddleName2.setText(doc.getString("middlename"));
                        tvLastName3.setText(doc.getString("lastname"));
                        tvSuffix2.setText(doc.getString("suffix"));
                        tvBirthdate2.setText(doc.getString("birthdate"));
                        tvAge.setText(doc.getString("age"));
                        tvTaskReminder2.setText(doc.getString("taskreminder"));
                        tvBreakReminder2.setText(doc.getString("breakreminder"));
                        tvEmail4.setText(doc.getString("email"));
                        tvUsername5.setText(doc.getString("username"));

                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupEditActions() {
        btnEditPersonal.setOnClickListener(v -> showEditPersonalDialog());
        btnEditAccount.setOnClickListener(v -> showEditAccountDialog());
        btnEditPrefs.setOnClickListener(v -> showEditPrefsDialog());
        btnSignOut.setOnClickListener(v -> showSignOutConfirmation());
    }

    private void showEditPersonalDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = inflater.inflate(R.layout.dialog_edit_personaldetails, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        EditText etFirstName = view.findViewById(R.id.etUsername5);
        EditText etMiddleName = view.findViewById(R.id.etMiddleName1);
        EditText etLastName = view.findViewById(R.id.etLastName1);
        EditText etSuffix = view.findViewById(R.id.etSuffix1);
        EditText etBirthdate = view.findViewById(R.id.etBirthday);
        Button btnDone = view.findViewById(R.id.btnDone1);

        etFirstName.setText(tvFirstName2.getText());
        etMiddleName.setText(tvMiddleName2.getText());
        etLastName.setText(tvLastName3.getText());
        etSuffix.setText(tvSuffix2.getText());
        etBirthdate.setText(tvBirthdate2.getText());

        etBirthdate.setFocusable(false);
        etBirthdate.setClickable(true);

        final int[] computedAge = new int[1]; // use array to allow update inside inner class

        etBirthdate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileSettings.this,
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etBirthdate.setText(selectedDate);

                        // Compute age
                        computedAge[0] = calculateAge(selectedYear, selectedMonth, selectedDay);

                    }, year, month, day);

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        btnDone.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstname", etFirstName.getText().toString().trim());
            updates.put("middlename", etMiddleName.getText().toString().trim());
            updates.put("lastname", etLastName.getText().toString().trim());
            updates.put("suffix", etSuffix.getText().toString().trim());
            updates.put("birthdate", etBirthdate.getText().toString().trim());

            // Include age if computed
            if (computedAge[0] > 0) {
                updates.put("age", String.valueOf(computedAge[0]));
                tvAge.setText(String.valueOf(computedAge[0]));
            }

            updateUserFirestore(updates);

            tvFirstName2.setText(etFirstName.getText());
            tvMiddleName2.setText(etMiddleName.getText());
            tvLastName3.setText(etLastName.getText());
            tvSuffix2.setText(etSuffix.getText());
            tvBirthdate2.setText(etBirthdate.getText());

            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private int calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    private void showEditAccountDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = inflater.inflate(R.layout.dialog_edit_accountdetails, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        EditText etUsername = view.findViewById(R.id.etUsername5);
        EditText etEmail = view.findViewById(R.id.etEmail2);
        EditText etPassword = view.findViewById(R.id.etPassword3);
        Button btnDone = view.findViewById(R.id.btnDone1);

        // Optional: Pre-fill existing values if you want
        etUsername.setText(tvUsername5.getText()); // You can fetch and fill the username
        etEmail.setText(tvEmail4.getText());

        btnDone.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", newUsername);
            updates.put("email", newEmail);

            updateUserFirestore(updates);
            tvUsername5.setText(newUsername);
            tvEmail4.setText(newEmail);
            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void showEditPrefsDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = inflater.inflate(R.layout.dialog_edit_preferences, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        Spinner spinnerTaskReminder = view.findViewById(R.id.spinnerTaskReminder1);
        Spinner spinnerBreakReminder = view.findViewById(R.id.spinnerBreakReminder1);
        Button btnDone = view.findViewById(R.id.btnDone1);

        // Add hint item to both spinners
        List<String> taskReminderList = new ArrayList<>();
        taskReminderList.add("Select task reminder"); // Hint
        for (int i = 1; i <= 12; i++) {
            taskReminderList.add(i + " hour before");
        }

        List<String> breakReminderList = new ArrayList<>();
        breakReminderList.add("Select break reminder"); // Hint
        breakReminderList.add("30 minutes");
        for (int i = 1; i <= 12; i++) {
            breakReminderList.add(i + (i == 1 ? " hour" : " hours"));
        }

        // Custom adapter with color and font
        ArrayAdapter<String> taskReminderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, taskReminderList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.GRAY : Color.parseColor("#3b3b3b"));
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

        ArrayAdapter<String> breakReminderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, breakReminderList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.GRAY : Color.parseColor("#3b3b3b"));
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

        taskReminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breakReminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskReminder.setAdapter(taskReminderAdapter);
        spinnerBreakReminder.setAdapter(breakReminderAdapter);

        // Select saved values or show hint (position 0)
        String currentTaskReminder = tvTaskReminder2.getText().toString();
        String currentBreakReminder = tvBreakReminder2.getText().toString();

        int taskIndex = taskReminderList.indexOf(currentTaskReminder);
        spinnerTaskReminder.setSelection(taskIndex >= 0 ? taskIndex : 0);

        int breakIndex = breakReminderList.indexOf(currentBreakReminder);
        spinnerBreakReminder.setSelection(breakIndex >= 0 ? breakIndex : 0);

        btnDone.setOnClickListener(v -> {
            String selectedTaskReminder = spinnerTaskReminder.getSelectedItem().toString();
            String selectedBreakReminder = spinnerBreakReminder.getSelectedItem().toString();

            // Optionally ignore if hint is selected
            if (selectedTaskReminder.equals("Select task reminder") || selectedBreakReminder.equals("Select break reminder")) {
                Toast.makeText(this, "Please select valid preferences", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("taskreminder", selectedTaskReminder);
            updates.put("breakreminder", selectedBreakReminder);

            updateUserFirestore(updates);
            tvTaskReminder2.setText(selectedTaskReminder);
            tvBreakReminder2.setText(selectedBreakReminder);

            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void updateUserFirestore(Map<String, Object> updates) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Update successful!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
                );
    }

    private void showSignOutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(ProfileSettings.this, Landing.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupNavigation() {
        ImageButton ibtnHome = findViewById(R.id.ibtnHome1);
        ImageButton ibtnCalendar = findViewById(R.id.ibtnCalendar1);
        ImageButton ibtnAnalytics = findViewById(R.id.ibtnAnalytics1);
        ImageButton ibtnProfile = findViewById(R.id.ibtnProfile1);

        ibtnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            overridePendingTransition(0, 0);
            finish();
        });

        ibtnCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calendar.class));
            overridePendingTransition(0, 0);
            finish();
        });

        ibtnAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(this, Analytics.class));
            overridePendingTransition(0, 0);
            finish();
        });

        ibtnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileBadges.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }
}