package com.application.modo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileBadges extends AppCompatActivity {

    private TextView tvUsername;
    private ImageView imgvPicture;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Dialog avatarDialog;
    private String selectedAvatarName = "default_avatar";

    private String[] avatarNames = {
            "bear", "cat", "chicken", "dog", "gorilla", "owl",
            "panda", "rabbit", "sealion"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_badges);

        tvUsername = findViewById(R.id.tvUsername);
        imgvPicture = findViewById(R.id.imgvPicture1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserInfo();

        Button btnRewards = findViewById(R.id.btnRewards);
        Button btnPoints = findViewById(R.id.btnPoints);
        Button btnSettings = findViewById(R.id.btnSettings1);
        ImageButton ibtnHome = findViewById(R.id.ibtnHome1);
        ImageButton ibtnCalendar = findViewById(R.id.ibtnCalendar1);
        ImageButton ibtnAnalytics = findViewById(R.id.ibtnAnalytics1);

        btnRewards.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileRewards.class));
            overridePendingTransition(0, 0);
            finish();
        });

        btnPoints.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfilePoints.class));
            overridePendingTransition(0, 0);
            finish();
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileSettings.class));
            overridePendingTransition(0, 0);
            finish();
        });

        ibtnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            overridePendingTransition(0, 0);
            finish();
        });

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

        imgvPicture.setOnClickListener(v -> showAvatarSelector());

        setupNavigation();
    }

    private void loadUserInfo() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        tvUsername.setText(document.getString("username"));
                        String avatarName = document.getString("profile");
                        if (avatarName == null || avatarName.isEmpty()) {
                            avatarName = "default_avatar";
                        }

                        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                        imgvPicture.setImageResource(resId);
                    }
                })
                .addOnFailureListener(e -> {
                    tvUsername.setText("User");
                    imgvPicture.setImageResource(R.drawable.default_avatar);
                });
    }

    private void setupNavigation() {
        ImageButton ibtnHome = findViewById(R.id.ibtnHome1);
        ImageButton ibtnCalendar = findViewById(R.id.ibtnCalendar1);
        ImageButton ibtnAnalytics = findViewById(R.id.ibtnAnalytics1);
        ImageButton ibtnProfile = findViewById(R.id.ibtnProfile1);

        ibtnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            overridePendingTransition(0, 0);
            finish();
        });

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

        ibtnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileBadges.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void showAvatarSelector() {
        avatarDialog = new Dialog(this);
        avatarDialog.setContentView(R.layout.dialog_avatar_preview);
        avatarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        avatarDialog.setCancelable(true);

        ImageView imgPreview = avatarDialog.findViewById(R.id.imgPreview);
        Button btnSave = avatarDialog.findViewById(R.id.btnSaveAvatar);
        GridLayout gridAvatars = avatarDialog.findViewById(R.id.gridAvatars);

        imgPreview.setImageDrawable(imgvPicture.getDrawable());

        for (String avatarName : avatarNames) {
            int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());

            ImageView avatarView = new ImageView(this);
            avatarView.setImageResource(resId);
            avatarView.setAdjustViewBounds(true);
            avatarView.setMaxHeight(150);
            avatarView.setMaxWidth(150);

            // ✅ Set LayoutParams to center inside the GridLayout cell
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(10, 10, 10, 10);
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.CENTER);
            avatarView.setLayoutParams(params);

            avatarView.setOnClickListener(v -> {
                selectedAvatarName = avatarName;
                imgPreview.setImageResource(resId);
            });

            gridAvatars.addView(avatarView);
        }

        btnSave.setOnClickListener(v -> {
            int resId = getResources().getIdentifier(selectedAvatarName, "drawable", getPackageName());
            imgvPicture.setImageResource(resId);
            updateAvatarInFirestore(selectedAvatarName);
            avatarDialog.dismiss();
        });

        avatarDialog.show();
    }

    private void updateAvatarInFirestore(String avatarName) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .update("profile", avatarName)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Avatar saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save avatar", Toast.LENGTH_SHORT).show();
                });
    }
}