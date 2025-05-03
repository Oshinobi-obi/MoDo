package com.application.modo;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.Date;

public class CheckMissedTaskWorker extends Worker {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public CheckMissedTaskWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WorkManager", "Running missed task check...");

        if (mAuth.getCurrentUser() == null) {
            Log.w("WorkManager", "User not logged in — skipping.");
            return Result.success(); // Or retry later
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("tasks")
                .whereIn("status", java.util.Arrays.asList("Upcoming", "Ongoing"))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp ts = doc.getTimestamp("deadlineTimestamp");
                        if (ts != null && ts.toDate().before(new Date())) {
                            doc.getReference().update("status", "Missing")
                                    .addOnSuccessListener(unused -> Log.d("WorkManager", "Marked task as Missing: " + doc.getString("title")));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WorkManager", "Error checking tasks: " + e.getMessage()));

        return Result.success();
    }
}