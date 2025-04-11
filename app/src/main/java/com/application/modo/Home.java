package com.application.modo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Home extends AppCompatActivity {

    private TextView tvUsername3;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ImageButton ibtnHome1, ibtnCalendar1, ibtnAnalytics1, ibtnProfile1;
    private FloatingActionButton fabAddTask1;
    private ImageView imgvPicture1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvUsername3 = findViewById(R.id.tvUsername3);
        imgvPicture1 = findViewById(R.id.imgvPicture1);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageButton ibtnCalendar = findViewById(R.id.ibtnCalendar1);
        ImageButton ibtnAnalytics = findViewById(R.id.ibtnAnalytics1);
        ImageButton ibtnProfile1 = findViewById(R.id.ibtnProfile1);
        FloatingActionButton fabAddTask1 = findViewById(R.id.fabAddTask1);

        ibtnCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calendar.class));
            overridePendingTransition(0, 0);
            finish();
        });

        ibtnAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(this, Analysis.class));
            overridePendingTransition(0, 0);
            finish();
        });

        ibtnProfile1.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, ProfileBadges.class));
            overridePendingTransition(0, 0);
            finish();
        });

        fabAddTask1.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, AddTask.class));
            overridePendingTransition(0, 0);
            finish();
        });

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        tvUsername3.setText(" " + username + "!");

                        // 🔁 Use profile drawable name from Firestore
                        String avatarName = document.getString("profile");
                        if (avatarName == null || avatarName.isEmpty()) {
                            avatarName = "default_avatar";
                        }

                        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                        imgvPicture1.setImageResource(resId);
                    } else {
                        tvUsername3.setText("Welcome!");
                        Toast.makeText(Home.this, "User data not found", Toast.LENGTH_SHORT).show();
                        imgvPicture1.setImageResource(R.drawable.default_avatar);
                    }
                })
                .addOnFailureListener(e -> {
                    tvUsername3.setText("Welcome!");
                    Toast.makeText(Home.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                    imgvPicture1.setImageResource(R.drawable.default_avatar);
                });
    }
}