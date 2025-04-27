package com.application.modo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeCurrentTask extends AppCompatActivity {

    private RecyclerView rvTaskList;
    private TaskAdapter adapter;
    private List<AddTask> taskList;
    private List<AddTask> filteredList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etSearch1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_currenttask);

        rvTaskList = findViewById(R.id.rvTaskList);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new TaskAdapter(filteredList);
        rvTaskList.setAdapter(adapter);

        etSearch1 = findViewById(R.id.etSearch1);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.ivReturnCurrentTask).setOnClickListener(v -> {
            startActivity(new Intent(HomeCurrentTask.this, Home.class));
            finish();
        });

        fetchOngoingTasks();
        setupSearch();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void fetchOngoingTasks() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("status", "Ongoing")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        taskList.clear();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                AddTask task = document.toObject(AddTask.class);
                                taskList.add(task);
                            }
                        }
                        filteredList.clear();
                        filteredList.addAll(taskList);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupSearch() {
        etSearch1.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTasks(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void searchTasks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(taskList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AddTask task : taskList) {
                if (task.getTitle().toLowerCase().contains(lowerQuery) ||
                        task.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(task);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private final List<AddTask> tasks;

        TaskAdapter(List<AddTask> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_currenttask, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            AddTask task = tasks.get(position);
            holder.tvTitle.setText(task.getTitle());
            holder.tvDescription.setText(task.getDescription());
            holder.tvDeadline.setText(task.getDeadline());

            holder.itemView.setOnClickListener(v -> showTaskDialog(task));

            holder.itemView.setOnLongClickListener(v -> {
                showDeleteWarningDialog(task);
                return true; // Important to consume the long press
            });
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvDeadline;

            TaskViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvCurrentTaskTitle1);
                tvDescription = itemView.findViewById(R.id.tvCurrentTaskDescription1);
                tvDeadline = itemView.findViewById(R.id.tvCurrentTaskDeadline1);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void showTaskDialog(AddTask task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ongoingtask_view, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTaskTitle = view.findViewById(R.id.tvCTTaskTitle);
        TextView tvTaskDescription = view.findViewById(R.id.tvCTTaskDescription);
        TextView tvDeadline = view.findViewById(R.id.tvCTDeadlineDuration);
        TextView tvLabel = view.findViewById(R.id.tvCTLabel);

        tvTaskTitle.setText(task.getTitle());
        tvTaskDescription.setText(task.getDescription());
        tvDeadline.setText("Deadline: " + task.getDeadline());
        tvLabel.setText("Label: " + task.getLabel());

        view.findViewById(R.id.btnTaskSettings).setOnClickListener(v -> {
            dialog.dismiss();
            showTaskOptionsDialog(task);
        });

        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null && task != null) {
                String uid = mAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("tasks")
                        .whereEqualTo("title", task.getTitle())
                        .whereEqualTo("description", task.getDescription())
                        .whereEqualTo("deadline", task.getDeadline()) // or timestamp if available
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
                                            dialog.dismiss();
                                            fetchOngoingTasks(); // refresh list to remove completed task
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error finding task.", Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    private void showDeleteWarningDialog(AddTask task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_task, null);
        AlertDialog deleteDialog = new AlertDialog.Builder(this).setView(view).create();
        if (deleteDialog.getWindow() != null) deleteDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvWarningTitle = view.findViewById(R.id.tvWarningTitle);
        TextView tvWarningMessage = view.findViewById(R.id.tvWarningMessage);
        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);

        tvWarningTitle.setText("Warning!");
        tvWarningMessage.setText("Are you sure you want to delete this task?");

        btnYes.setOnClickListener(v -> {
            deleteTask(task);
            deleteDialog.dismiss();
        });

        btnNo.setOnClickListener(v -> {
            deleteDialog.dismiss();
        });

        deleteDialog.show();
    }

    private void deleteTask(AddTask task) {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("title", task.getTitle())
                    .whereEqualTo("description", task.getDescription())
                    .whereEqualTo("deadline", task.getDeadline())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String docId = querySnapshot.getDocuments().get(0).getId();

                            db.collection("users").document(uid).collection("tasks")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Task deleted successfully!", Toast.LENGTH_SHORT).show();
                                        fetchOngoingTasks();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete task.", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Task not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error finding task.", Toast.LENGTH_SHORT).show());
        }
    }

    private void showTaskOptionsDialog(AddTask task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskoptions, null);
        AlertDialog optionsDialog = new AlertDialog.Builder(this).setView(view).create();
        if (optionsDialog.getWindow() != null) optionsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnEditTask = view.findViewById(R.id.btnEditTask);
        Button btnEditLabel = view.findViewById(R.id.btnEditLabel);
        Button btnRemoveTask = view.findViewById(R.id.btnRemoveTask);

        btnEditTask.setOnClickListener(v -> {
            optionsDialog.dismiss();
            showEditTaskDialog(task);
        });

        btnEditLabel.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Label clicked!", Toast.LENGTH_SHORT).show();
            optionsDialog.dismiss();
        });

        btnRemoveTask.setOnClickListener(v -> {
            Toast.makeText(this, "Remove Task clicked!", Toast.LENGTH_SHORT).show();
            optionsDialog.dismiss();
        });

        optionsDialog.show();
    }

    private void showEditTaskDialog(AddTask task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_taskedit, null);
        AlertDialog editDialog = new AlertDialog.Builder(this).setView(view).create();
        if (editDialog.getWindow() != null) editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitle = view.findViewById(R.id.etTaskTitle1);
        EditText etDescription = view.findViewById(R.id.etmlTaskDescription1);
        Spinner spnrPriority = view.findViewById(R.id.spnrPriorityLevel1);
        Spinner spnrLabel = view.findViewById(R.id.spnrLabelName1);
        Button btnDateTime = view.findViewById(R.id.btnDateTime);
        Button btnDone = view.findViewById(R.id.btnDone);

        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        btnDateTime.setText(task.getDeadline());

        spnrPriority.setAdapter(getPriorityAdapter());
        spnrPriority.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        if (task.getPriority() != null) {
            int pos = ((ArrayAdapter<String>) spnrPriority.getAdapter()).getPosition(task.getPriority());
            spnrPriority.setSelection(pos);
        }

        spnrLabel.setAdapter(getLabelAdapter());
        spnrLabel.setPopupBackgroundResource(R.drawable.spinner_dropdown_bg);
        if (task.getLabel() != null) {
            int pos = ((ArrayAdapter<String>) spnrLabel.getAdapter()).getPosition(task.getLabel());
            spnrLabel.setSelection(pos);
        }

        btnDateTime.setOnClickListener(v -> showDateTimePicker(btnDateTime));

        btnDone.setOnClickListener(v -> {
            Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show();
            editDialog.dismiss();
        });

        editDialog.show();
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
}