package com.application.modo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPassword extends AppCompatActivity {

    private EditText etEnterEmail1;
    private Button btnResetPassword1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEnterEmail1 = findViewById(R.id.etEnterEmail1);
        btnResetPassword1 = findViewById(R.id.btnResetPassword1);

        btnResetPassword1.setOnClickListener(view -> {
            String email = etEnterEmail1.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgotPassword.this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            db.collection("users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnSuccessListener(query -> {
                                        if (!query.isEmpty()) {
                                            String uid = query.getDocuments().get(0).getId();
                                            db.collection("users").document(uid)
                                                    .update("mustResetPassword", true)
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(ForgotPassword.this, "Reset link sent! You must change your password to unlock account.", Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(ForgotPassword.this, Login.class));
                                                        overridePendingTransition(0, 0);
                                                        finish();
                                                    });
                                        }
                                    });
                        } else {
                            Toast.makeText(ForgotPassword.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}