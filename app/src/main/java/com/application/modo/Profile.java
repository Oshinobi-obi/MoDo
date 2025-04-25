package com.application.modo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    Button btnBadges, btnPoints, btnRewards, btnSettings;
    TextView tvUsername;
    ImageView imgvPicture;

    private Dialog avatarDialog;
    private String selectedAvatarName = "default_avatar";
    private String[] avatarNames = {
            "bear", "cat", "chicken", "dog", "gorilla", "owl", "panda", "rabbit", "sealion"
    };

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // UI
        btnBadges = findViewById(R.id.btnBadges);
        btnPoints = findViewById(R.id.btnPoints);
        btnRewards = findViewById(R.id.btnRewards);
        btnSettings = findViewById(R.id.btnSettings);
        tvUsername = findViewById(R.id.tvUsername);
        imgvPicture = findViewById(R.id.imgvPicture);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load user info
        loadUserInfo();

        // Default fragment
        loadFragment(new ProfileBadges());
        updateButtonStyles(btnBadges);

        btnBadges.setOnClickListener(v -> {
            loadFragment(new ProfileBadges());
            updateButtonStyles(btnBadges);
        });

        btnPoints.setOnClickListener(v -> {
            loadFragment(new ProfilePoints());
            updateButtonStyles(btnPoints);
        });

        btnRewards.setOnClickListener(v -> {
            loadFragment(new ProfileRewards());
            updateButtonStyles(btnRewards);
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, ProfileSettings.class));
        });

        imgvPicture.setOnClickListener(v -> showAvatarSelector());

        setupNavigation();
    }

    private void loadUserInfo() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        tvUsername.setText(document.getString("username"));
                        String avatarName = document.getString("profile");
                        if (avatarName == null || avatarName.isEmpty()) avatarName = "default_avatar";
                        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                        imgvPicture.setImageResource(resId);
                    }
                })
                .addOnFailureListener(e -> {
                    tvUsername.setText("User");
                    imgvPicture.setImageResource(R.drawable.default_avatar);
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

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(10, 10, 10, 10);
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

    private void setupNavigation() {
        ImageButton ibtnHome = findViewById(R.id.ibtnHome1);
        ImageButton ibtnCalendar = findViewById(R.id.ibtnCalendar1);
        ImageButton ibtnAnalytics = findViewById(R.id.ibtnAnalytics1);

        ibtnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            overridePendingTransition(0, 0);
        });

        ibtnCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calendar.class));
            overridePendingTransition(0, 0);
        });

        ibtnAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(this, Analytics.class));
            overridePendingTransition(0, 0);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.clProfileFragment, fragment)
                .commit();
    }

    private void updateButtonStyles(Button selectedButton) {
        int selectedBg = getResources().getColor(R.color.selected_button_bg);
        int selectedText = getResources().getColor(R.color.white);
        int defaultBg = getResources().getColor(R.color.default_button_bg);
        int defaultText = getResources().getColor(R.color.default_text_color);

        Button[] buttons = {btnBadges, btnPoints, btnRewards};

        for (Button btn : buttons) {
            if (btn == selectedButton) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBg));
                btn.setTextColor(selectedText);
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultBg));
                btn.setTextColor(defaultText);
            }
        }
    }
}