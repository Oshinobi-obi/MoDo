package com.application.modo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device rebooted — rescheduling notifications...");

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (mAuth.getCurrentUser() == null) {
                Log.w("BootReceiver", "User not logged in. Cannot reschedule notifications.");
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).collection("tasks")
                    .whereIn("status", Arrays.asList("Upcoming", "Ongoing"))
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String status = doc.getString("status");
                            String title = doc.getString("title");
                            String deadline = doc.getString("deadline");
                            Timestamp deadlineTS = doc.getTimestamp("deadlineTimestamp");

                            if (deadlineTS == null || title == null || deadline == null) continue;

                            long triggerAtMillis = deadlineTS.toDate().getTime() - 24 * 60 * 60 * 1000L;
                            if (triggerAtMillis > System.currentTimeMillis()) {

                                if ("Upcoming".equals(status)) {
                                    String message = "Hey! You have an upcoming task you haven't started yet. Get it started before you miss it!";
                                    DeadlineNotificationScheduler.scheduleWithMessage(context, title, message, triggerAtMillis);

                                } else if ("Ongoing".equals(status)) {
                                    long diffMillis = deadlineTS.toDate().getTime() - System.currentTimeMillis();
                                    String timeLeft = getTimeLeftReadable(diffMillis);
                                    String message = "Hey! Your ongoing task is nearing its deadline. Try to complete it within " + timeLeft + "!";
                                    DeadlineNotificationScheduler.scheduleWithMessage(context, title, message, triggerAtMillis);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("BootReceiver", "Failed to reschedule tasks: " + e.getMessage())
                    );
        }
    }

    private String getTimeLeftReadable(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;

        if (days > 0) return days + " day" + (days > 1 ? "s" : "");
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "");
        return "less than an hour";
    }
}