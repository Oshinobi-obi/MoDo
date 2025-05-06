package com.application.modo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private RecyclerView rvCalendar;
    private CalendarAdapter calendarAdapter;
    private List<CalendarTask> taskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DatePicker calendarView;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvCalendar = view.findViewById(R.id.rvTaskList);
        calendarView = view.findViewById(R.id.calendarView);
        rvCalendar.setLayoutManager(new LinearLayoutManager(requireContext()));

        taskList = new ArrayList<>();
        calendarAdapter = new CalendarAdapter(taskList);
        rvCalendar.setAdapter(calendarAdapter);

        Calendar today = Calendar.getInstance();

        // Set up date change listener
        calendarView.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        fetchTasksForDate(selectedDate.getTime());
                    }
                });

        // Initialize with today's date
        fetchTasksForDate(today.getTime());

        return view;
    }

    private void fetchTasksForDate(Date date) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Format the selected date to match the deadline format
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);
        String formattedDate = sdf.format(date);

        db.collection("users").document(uid).collection("tasks")
                .whereIn("status", List.of("Upcoming", "Ongoing"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        AddTask addTask = doc.toObject(AddTask.class);
                        if (addTask != null) {
                            String deadline = addTask.getDeadline();
                            // Extract just the date part from the deadline (before the time)
                            String taskDate = deadline.split(" ")[0];
                            Log.d("CalendarFragment", "Formatted selected date: " + formattedDate);
                            Log.d("CalendarFragment", "Task deadline: " + deadline);
                            if (taskDate.equals(formattedDate)) {
                                // Convert AddTask to CalendarTask
                                CalendarTask calendarTask = new CalendarTask(
                                    addTask.getTitle(),
                                    addTask.getDescription(),
                                    addTask.getDeadline()
                                );
                                calendarTask.setStatus(addTask.getStatus());
                                calendarTask.setPriority(addTask.getPriority());
                                taskList.add(calendarTask);
                            }
                        }
                    }
                    calendarAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
                });
    }
}