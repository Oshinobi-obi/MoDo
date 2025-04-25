package com.application.modo;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Calendar extends AppCompatActivity {

    private RecyclerView rvCalendar;
    private CalendarAdapter calendarAdapter;
    private List<CalendarTask> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView
        rvCalendar = findViewById(R.id.rvTaskList);
        rvCalendar.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskList.add(new CalendarTask("Daily Journal", "Write down 3 things you're grateful for", "10:00am"));
        taskList.add(new CalendarTask("Study Math", "Practice algebra exercises", "11:00am"));
        taskList.add(new CalendarTask("Group Project", "Finalize and submit presentation", "12:00pm"));
        taskList.add(new CalendarTask("Clean Room", "Organize shelves and vacuum", "5:00pm"));
        taskList.add(new CalendarTask("Workout", "Cardio and core training", "8:00pm"));
        taskList.add(new CalendarTask("Read Book", "Finish chapter 4 of Atomic Habits", "10:00pm"));

        calendarAdapter = new CalendarAdapter(taskList);
        rvCalendar.setAdapter(calendarAdapter);
    }
}