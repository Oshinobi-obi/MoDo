package com.application.modo;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private Button btnBadges, btnPoints, btnRewards, btnSettings;
    private TextView tvUsername, tvStatus1, tvJoinDate1;
    private ImageView imgvPicture;
    private Dialog avatarDialog;
    private String selectedAvatarName = "default_avatar";

    // Include default avatar first
    private final String[] avatarNames = {
        "bear", "cat", "chicken", "dog", "gorilla", "owl", "panda", "rabbit", "sealion"
    };

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBadges = view.findViewById(R.id.btnBadges);
        btnPoints = view.findViewById(R.id.btnPoints);
        btnRewards = view.findViewById(R.id.btnRewards);
        btnSettings = view.findViewById(R.id.btnSettings);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvStatus1  = view.findViewById(R.id.tvStatus1);
        tvJoinDate1 = view.findViewById(R.id.tvJoinDate1);
        imgvPicture = view.findViewById(R.id.imgvPicture);

        loadUserInfo();
        loadFragment(new ProfileBadges());
        updateButtonStyles(btnBadges);

        btnBadges.setOnClickListener(v -> { loadFragment(new ProfileBadges()); updateButtonStyles(btnBadges); });
        btnPoints.setOnClickListener(v -> { loadFragment(new ProfilePoints()); updateButtonStyles(btnPoints); });
        btnRewards.setOnClickListener(v -> { loadFragment(new ProfileRewards()); updateButtonStyles(btnRewards); });
        btnSettings.setOnClickListener(v -> startActivity(new android.content.Intent(getContext(), ProfileSettings.class)));
        imgvPicture.setOnClickListener(v -> showAvatarSelector());

        return view;
    }

    private int extractPoints(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!isAdded()) return;
                    if (document.exists()) {
                        String username = document.getString("username");
                        tvUsername.setText(username != null ? username : "User");

                        String avatarName = document.getString("profile");
                        if (avatarName == null || avatarName.isEmpty()) avatarName = "default_avatar";
                        int resId = requireContext().getResources().getIdentifier(
                                avatarName, "drawable", requireContext().getPackageName());
                        imgvPicture.setImageResource(resId);

                        List<String> earned = (List<String>) document.get("profile_badges");
                        tvStatus1.setText((earned != null && !earned.isEmpty())
                                ? earned.get(earned.size() - 1)
                                : "Newcomer");

                        Date joinDate = document.getDate("JoinedDate");
                        if (joinDate != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                            tvJoinDate1.setText(sdf.format(joinDate));
                        } else {
                            tvJoinDate1.setText("Unknown");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    tvUsername.setText("User");
                    imgvPicture.setImageResource(R.drawable.default_avatar);
                    tvStatus1.setText("Newcomer");
                    tvJoinDate1.setText("Unknown");
                });
    }

    private void showAvatarSelector() {
        if (!isAdded() || mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Fetch total points
        db.collection("users").document(uid).collection("profile_points")
                .get()
                .addOnSuccessListener(pointsSnap -> {
                    if (!isAdded()) return;
                    int totalPts = 0;
                    for (QueryDocumentSnapshot d : pointsSnap) {
                        totalPts += extractPoints(d.getString("points"));
                    }

                    // Determine unlocked avatars based on milestones
                    Map<String, String[]> defs = RewardDefinitions.getAll();
                    List<String> unlocked = new ArrayList<>();
                    unlocked.add("default_avatar");
                    for (Map.Entry<String, String[]> e : defs.entrySet()) {
                        String name = e.getKey().toLowerCase();
                        String milestoneStr = e.getValue()[1];
                        int thresh;
                        try {
                            thresh = Integer.parseInt(milestoneStr.replaceAll("[^\\d]", ""));
                        } catch (Exception ex) {
                            continue;
                        }
                        if (totalPts >= thresh) unlocked.add(name);
                    }

                    avatarDialog = new Dialog(requireContext());
                    avatarDialog.setContentView(R.layout.dialog_avatar_preview);
                    avatarDialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT)
                    );
                    avatarDialog.setCancelable(true);

                    ImageView imgPreview = avatarDialog.findViewById(R.id.imgPreview);
                    Button btnSave   = avatarDialog.findViewById(R.id.btnSaveAvatar);
                    GridLayout gridAvatars = avatarDialog.findViewById(R.id.gridAvatars);

                    // → switch to 3 columns, use default margins
                    gridAvatars.setColumnCount(3);
                    gridAvatars.setUseDefaultMargins(true);
                    gridAvatars.setAlignmentMode(GridLayout.ALIGN_MARGINS);
                    gridAvatars.removeAllViews();

                    imgPreview.setImageDrawable(imgvPicture.getDrawable());

                    for (String avatarName : avatarNames) {
                        int resId = requireContext().getResources()
                                .getIdentifier(avatarName, "drawable",
                                        requireContext().getPackageName());
                        ImageView avatarView = new ImageView(requireContext());
                        avatarView.setImageResource(resId);
                        avatarView.setAdjustViewBounds(true);

                        // → give each cell equal weight so rows of 3
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                                GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        );
                        params.width  = 0;  // weight=1f will distribute evenly
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        params.setMargins(10, 10, 10, 10);
                        avatarView.setLayoutParams(params);

                        if (!unlocked.contains(avatarName)) {
                            avatarView.setAlpha(0.3f);
                            avatarView.setEnabled(false);
                        } else {
                            avatarView.setOnClickListener(v -> {
                                selectedAvatarName = avatarName;
                                imgPreview.setImageResource(resId);
                            });
                        }
                        gridAvatars.addView(avatarView);
                    }

                    btnSave.setOnClickListener(v -> {
                        int finalRes = requireContext().getResources()
                                .getIdentifier(selectedAvatarName, "drawable",
                                        requireContext().getPackageName());
                        imgvPicture.setImageResource(finalRes);
                        updateAvatarInFirestore(selectedAvatarName);
                        avatarDialog.dismiss();
                    });

                    avatarDialog.show();
                });
    }

    private void updateAvatarInFirestore(String avatarName) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .update("profile", avatarName)
                .addOnSuccessListener(unused -> Toast.makeText(requireContext(), "Avatar saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save avatar", Toast.LENGTH_SHORT).show());
    }

    private void loadFragment(Fragment fragment) {
        if (!isAdded()) return;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.clProfileFragment, fragment);
        transaction.commitAllowingStateLoss();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avatarDialog != null && avatarDialog.isShowing()) {
            avatarDialog.dismiss();
        }
    }
}