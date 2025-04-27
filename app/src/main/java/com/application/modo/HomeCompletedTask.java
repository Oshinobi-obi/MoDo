package com.application.modo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeCompletedTask extends AppCompatActivity {

    private RecyclerView rvTaskList;
    private CompletedTaskAdapter adapter;
    private List<AddTask> completedTaskList;
    private List<AddTask> filteredTaskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etSearch1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_completedtask);

        rvTaskList = findViewById(R.id.rvTaskList);
        etSearch1 = findViewById(R.id.etSearch1);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        completedTaskList = new ArrayList<>();
        filteredTaskList = new ArrayList<>();

        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompletedTaskAdapter(filteredTaskList, this);
        rvTaskList.setAdapter(adapter);

        findViewById(R.id.ivReturnCompletedTask).setOnClickListener(v -> {
            startActivity(new Intent(HomeCompletedTask.this, Home.class));
            finish();
        });

        fetchCompletedTasks();

        etSearch1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence query, int i, int i1, int i2) {
                filterTasks(query.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void fetchCompletedTasks() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("status", "Completed")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        completedTaskList.clear();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (var doc : queryDocumentSnapshots.getDocuments()) {
                                AddTask task = doc.toObject(AddTask.class);
                                completedTaskList.add(task);
                            }
                            filterTasks(""); // initial load
                        } else {
                            Toast.makeText(this, "No completed tasks yet.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load completed tasks.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void filterTasks(String text) {
        filteredTaskList.clear();
        for (AddTask task : completedTaskList) {
            if (task.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                    task.getDescription().toLowerCase().contains(text.toLowerCase())) {
                filteredTaskList.add(task);
            }
        }
        adapter.notifyDataSetChanged();
    }
}