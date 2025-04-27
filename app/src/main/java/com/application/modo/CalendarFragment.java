package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        RecyclerView rvCalendar = view.findViewById(R.id.rvTaskList);
        rvCalendar.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<CalendarTask> taskList = new ArrayList<>();
        taskList.add(new CalendarTask("Daily Journal", "Write down 3 things you're grateful for", "10:00 AM"));
        taskList.add(new CalendarTask("Study Math", "Practice algebra exercises", "11:00 AM"));
        taskList.add(new CalendarTask("Group Project", "Finalize and submit presentation", "12:00 PM"));
        taskList.add(new CalendarTask("Clean Room", "Organize shelves and vacuum", "5:00 PM"));
        taskList.add(new CalendarTask("Workout", "Cardio and core training", "8:00 PM"));
        taskList.add(new CalendarTask("Read Book", "Finish chapter 4 of Atomic Habits", "10:00 PM"));

        CalendarAdapter calendarAdapter = new CalendarAdapter(taskList);
        rvCalendar.setAdapter(calendarAdapter);

        return view;
    }
}
