package com.application.modo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProfilePoints extends Fragment {

    private RecyclerView rvProfilePoints;
    private ProfilePointsAdapter adapter;

    public ProfilePoints() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_points, container, false);

        rvProfilePoints = view.findViewById(R.id.rvProfilePoints);
        rvProfilePoints.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ProfilePointsItem> pointList = new ArrayList<>();
        // Sample data
        pointList.add(new ProfilePointsItem("Task Title 1", "01/01/2025", "+10 points"));
        pointList.add(new ProfilePointsItem("Task Title 2", "03/10/2025", "+5 points"));
        pointList.add(new ProfilePointsItem("Task Title 3", "04/25/2025", "+15 points"));

        adapter = new ProfilePointsAdapter(pointList);
        rvProfilePoints.setAdapter(adapter);

        return view;
    }
}
