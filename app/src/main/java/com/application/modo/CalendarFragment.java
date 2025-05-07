package com.application.modo;

import android.content.Context;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarFragment extends Fragment {

    private RecyclerView rvCalendar;
    private CalendarAdapter calendarAdapter;
    private List<CalendarTask> taskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DatePicker calendarView;
    private Calendar todayPhMidnight;

    // prevent overlap
    private boolean isFetching = false;

    public CalendarFragment() { /* Required empty constructor */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvCalendar = view.findViewById(R.id.rvTaskList);
        rvCalendar.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        calendarAdapter = new CalendarAdapter(taskList);
        rvCalendar.setAdapter(calendarAdapter);

        calendarView = view.findViewById(R.id.calendarView);
        TimeZone phTz = TimeZone.getTimeZone("Asia/Manila");
        todayPhMidnight = Calendar.getInstance(phTz);
        todayPhMidnight.set(Calendar.HOUR_OF_DAY, 0);
        todayPhMidnight.set(Calendar.MINUTE, 0);
        todayPhMidnight.set(Calendar.SECOND, 0);
        todayPhMidnight.set(Calendar.MILLISECOND, 0);

        calendarView.setMinDate(todayPhMidnight.getTimeInMillis());

        calendarView.init(
                todayPhMidnight.get(Calendar.YEAR),
                todayPhMidnight.get(Calendar.MONTH),
                todayPhMidnight.get(Calendar.DAY_OF_MONTH),
                (dp, year, month, day) -> {
                    if (!isAdded()) return;  // fragment detached?
                    Context ctx = requireContext();

                    if (isFetching) {
                        Toast.makeText(ctx,
                                "Still loading tasks, please wait…",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    Calendar sel = Calendar.getInstance(phTz);
                    sel.set(year, month, day, 0, 0, 0);
                    sel.set(Calendar.MILLISECOND, 0);

                    if (sel.before(todayPhMidnight)) {
                        calendarView.updateDate(
                                todayPhMidnight.get(Calendar.YEAR),
                                todayPhMidnight.get(Calendar.MONTH),
                                todayPhMidnight.get(Calendar.DAY_OF_MONTH)
                        );
                        Toast.makeText(ctx,
                                "Cannot select past dates",
                                Toast.LENGTH_SHORT
                        ).show();
                        fetchTasksForDate(todayPhMidnight.getTime());
                    } else {
                        fetchTasksForDate(sel.getTime());
                    }
                }
        );

        fetchTasksForDate(todayPhMidnight.getTime());

        return view;
    }

    private void fetchTasksForDate(Date date) {
        if (mAuth.getCurrentUser() == null || !isAdded()) return;
        String uid = mAuth.getCurrentUser().getUid();

        isFetching = true;
        calendarView.setEnabled(false);

        TimeZone phTz = TimeZone.getTimeZone("Asia/Manila");
        Calendar selCal = Calendar.getInstance(phTz);
        selCal.setTime(date);
        selCal.set(Calendar.HOUR_OF_DAY, 0);
        selCal.set(Calendar.MINUTE, 0);
        selCal.set(Calendar.SECOND, 0);
        selCal.set(Calendar.MILLISECOND, 0);
        final int selY = selCal.get(Calendar.YEAR);
        final int selM = selCal.get(Calendar.MONTH);
        final int selD = selCal.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat parser = new SimpleDateFormat("M/d/yyyy h:mm a", Locale.ENGLISH);
        parser.setTimeZone(phTz);

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .whereIn("status", Arrays.asList("Upcoming","Ongoing"))
                .get()
                .addOnSuccessListener(qSnap -> {
                    if (!isAdded()) return;
                    Context ctx = requireContext();

                    taskList.clear();
                    for (QueryDocumentSnapshot doc : qSnap) {
                        AddTask t = doc.toObject(AddTask.class);
                        if (t != null && t.getDeadline() != null) {
                            try {
                                Date taskDate = parser.parse(t.getDeadline());
                                Calendar taskCal = Calendar.getInstance(phTz);
                                taskCal.setTime(taskDate);

                                if (taskCal.get(Calendar.YEAR)  == selY &&
                                        taskCal.get(Calendar.MONTH) == selM &&
                                        taskCal.get(Calendar.DAY_OF_MONTH) == selD) {
                                    CalendarTask ct = new CalendarTask(
                                            t.getTitle(),
                                            t.getDescription(),
                                            t.getDeadline()
                                    );
                                    ct.setStatus(t.getStatus());
                                    ct.setPriority(t.getPriority());
                                    taskList.add(ct);
                                }
                            } catch (Exception ignored) { }
                        }
                    }
                    Collections.sort(taskList, Comparator.comparingInt(CalendarTask::getOrder));

                    calendarAdapter.notifyDataSetChanged();

                    SimpleDateFormat disp = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                    disp.setTimeZone(phTz);
                    if (taskList.isEmpty()) {
                        Toast.makeText(ctx,
                                "No tasks on " + disp.format(date),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    isFetching = false;
                    calendarView.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Context ctx = requireContext();

                    Toast.makeText(ctx,
                            "Failed to load tasks: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                    isFetching = false;
                    calendarView.setEnabled(true);
                });
    }
}
