package com.application.modo;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private Button btnBadges, btnPoints, btnRewards, btnSettings;
    private TextView tvUsername;
    private ImageView imgvPicture;
    private Dialog avatarDialog;
    private String selectedAvatarName = "default_avatar";

    private final String[] avatarNames = {
            "bear", "cat", "chicken", "dog", "gorilla", "owl", "panda", "rabbit", "sealion"
    };

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find Views
        btnBadges = view.findViewById(R.id.btnBadges);
        btnPoints = view.findViewById(R.id.btnPoints);
        btnRewards = view.findViewById(R.id.btnRewards);
        btnSettings = view.findViewById(R.id.btnSettings);
        tvUsername = view.findViewById(R.id.tvUsername);
        imgvPicture = view.findViewById(R.id.imgvPicture);

        // Load user info
        loadUserInfo();

        // Default fragment inside profile
        loadFragment(new ProfileBadges());
        updateButtonStyles(btnBadges);

        // Button click events
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
            startActivity(new android.content.Intent(getContext(), ProfileSettings.class));
        });

        imgvPicture.setOnClickListener(v -> showAvatarSelector());

        return view;
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        tvUsername.setText(document.getString("username"));
                        String avatarName = document.getString("profile");
                        if (avatarName == null || avatarName.isEmpty()) avatarName = "default_avatar";
                        int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
                        imgvPicture.setImageResource(resId);
                    }
                })
                .addOnFailureListener(e -> {
                    tvUsername.setText("User");
                    imgvPicture.setImageResource(R.drawable.default_avatar);
                });
    }

    private void showAvatarSelector() {
        avatarDialog = new Dialog(requireContext());
        avatarDialog.setContentView(R.layout.dialog_avatar_preview);
        avatarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        avatarDialog.setCancelable(true);

        ImageView imgPreview = avatarDialog.findViewById(R.id.imgPreview);
        Button btnSave = avatarDialog.findViewById(R.id.btnSaveAvatar);
        GridLayout gridAvatars = avatarDialog.findViewById(R.id.gridAvatars);

        imgPreview.setImageDrawable(imgvPicture.getDrawable());

        for (String avatarName : avatarNames) {
            int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
            ImageView avatarView = getImageView(avatarName, resId, imgPreview);
            gridAvatars.addView(avatarView);
        }

        btnSave.setOnClickListener(v -> {
            int resId = getResources().getIdentifier(selectedAvatarName, "drawable", requireContext().getPackageName());
            imgvPicture.setImageResource(resId);
            updateAvatarInFirestore(selectedAvatarName);
            avatarDialog.dismiss();
        });

        avatarDialog.show();
    }

    @NonNull
    private ImageView getImageView(String avatarName, int resId, ImageView imgPreview) {
        ImageView avatarView = new ImageView(requireContext());
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
        return avatarView;
    }

    private void updateAvatarInFirestore(String avatarName) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .update("profile", avatarName)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Avatar saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save avatar", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.clProfileFragment, fragment);
        transaction.commit();
    }

    private void updateButtonStyles(Button selectedButton) {
        int selectedBg = getResources().getColor(R.color.selected_button_bg, requireContext().getTheme());
        int selectedText = getResources().getColor(R.color.white, requireContext().getTheme());
        int defaultBg = getResources().getColor(R.color.default_button_bg, requireContext().getTheme());
        int defaultText = getResources().getColor(R.color.default_text_color, requireContext().getTheme());

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