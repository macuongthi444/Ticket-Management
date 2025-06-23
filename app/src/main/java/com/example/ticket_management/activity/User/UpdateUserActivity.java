package com.example.ticket_management.activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.Other.OtherActivity;
import com.example.ticket_management.DAO.UserDAO;
import com.example.ticket_management.model.User;

public class UpdateUserActivity extends AppCompatActivity {
    private static final String TAG = "UpdateUserActivity";

    private EditText editTextName, editTextEmail, editTextPhone, editTextAddress;
    private Button btnUpdate, btnCancel; // Ánh xạ đúng với layout
    private UserDAO userDAO;
    private String userId;
    private boolean isFinishing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_user);


        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        userDAO = new UserDAO();
        userId = getIntent().getStringExtra("userId");
        Log.d(TAG, "userId từ Intent: " + userId);
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId không hợp lệ");
            Toast.makeText(getApplicationContext(), "Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        loadUserData();


        btnUpdate.setOnClickListener(v -> {
            updateUserInfo();
        });


        btnCancel.setOnClickListener(v -> {

            Intent intent = new Intent(UpdateUserActivity.this, OtherActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinishing = true;
    }

    private void loadUserData() {
        userDAO.fetchUserById(userId, user -> {
            if (isFinishing || isDestroyed()) {
                return;
            }
            if (user != null) {
                editTextName.setText(user.getFullName() != null ? user.getFullName() : "");
                editTextEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                editTextPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                editTextAddress.setText(user.getAddress()!= null ? user.getAddress() : "" );
            } else {
                Toast.makeText(getApplicationContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setUserId(userId);
        user.setFullName(name);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);

        userDAO.updateUser(user, new UserDAO.OnUserOperationListener() {
            @Override
            public void onSuccess() {
                if (isFinishing || isDestroyed()) {
                    return;
                }
                Toast.makeText(getApplicationContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing && !isDestroyed()) {
                        Intent intent = new Intent(UpdateUserActivity.this, OtherActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                        finish();
                    }
                }, 500);
            }

            @Override
            public void onFailure(Exception e) {
                if (isFinishing || isDestroyed()) {
                    return;
                }
                Log.e(TAG, "updateUserInfo: Cập nhật thất bại - " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}