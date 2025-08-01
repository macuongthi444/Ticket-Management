package com.example.ticket_management.activity.Other;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.Auth.ChangePasswordActivity;
import com.example.ticket_management.activity.User.UpdateUserActivity;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.AuthDAO;
import com.example.ticket_management.DAO.UserDAO;

public class OtherActivity extends BaseActivity {
    private static final String TAG = "OtherActivity";

    private Button btn_dangxuat;
    private LinearLayout btnUpdatePro, btnChangePassword;
    private TextView txtTen, txtEmail, txtSDT;
    private UserDAO userDAO;
    private String userId;
    private boolean isFinishing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không cần setContentView ở đây vì BaseActivity sẽ xử lý
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_khac; // Layout của OtherActivity
    }

    @Override
    protected void initContent(View contentView) {
        Log.d(TAG, "initContent: Bắt đầu khởi tạo nội dung");

        // Ánh xạ view
        txtTen = contentView.findViewById(R.id.txt_ten_TCN);
        txtEmail = contentView.findViewById(R.id.txt_email_TCN);
        txtSDT = contentView.findViewById(R.id.txt_SDT_TCN);
        btn_dangxuat = contentView.findViewById(R.id.btn_DangXuat);
        btnUpdatePro = contentView.findViewById(R.id.btn_updateprofile);
        btnChangePassword = contentView.findViewById(R.id.btn_change_password);

        userDAO = new UserDAO();

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        Log.d(TAG, "userId từ SharedPreferences: " + userId);
        if (userId.isEmpty()) {
            Log.e(TAG, "userId không hợp lệ");
            Toast.makeText(getApplicationContext(), "userId không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải dữ liệu ban đầu
        loadUserData();

        // Thiết lập sự kiện
        btnChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "Chuyển đến ChangePasswordActivity");
            Intent intent = new Intent(OtherActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnUpdatePro.setOnClickListener(v -> {
            Log.d(TAG, "Chuyển đến UpdateUserActivity với userId: " + userId);
            Intent intent = new Intent(OtherActivity.this, UpdateUserActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btn_dangxuat.setOnClickListener(v -> {
            Log.d(TAG, "Nhấn nút Đăng xuất");
            logout(); // Gọi phương thức logout từ BaseActivity
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Làm mới dữ liệu người dùng");
        loadUserData(); // Làm mới dữ liệu khi quay lại từ UpdateUserActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinishing = true;
        Log.d(TAG, "onDestroy: Activity bị hủy");
    }

    private void loadUserData() {
        // Lấy dữ liệu từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        String fullName = sharedPreferences.getString("fullName", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");

        Log.d(TAG, "Dữ liệu từ SharedPreferences - fullName: " + fullName + ", email: " + email + ", phone: " + phone);

        // Hiển thị thông tin
        txtTen.setText(fullName);
        txtEmail.setText(email);
        txtSDT.setText(phone);

        // Tùy chọn: Lấy dữ liệu mới nhất từ Firebase nếu cần
        userDAO.fetchUserById(userId, user -> {
            if (isFinishing || isDestroyed()) {
                Log.w(TAG, "loadUserData: Activity đã bị hủy, bỏ qua callback");
                return;
            }
            if (user != null) {
                Log.d(TAG, "loadUserData: Tải dữ liệu từ Firebase thành công - User: " + user.getUserName());
                // Cập nhật SharedPreferences với dữ liệu mới nhất
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fullName", user.getFullName());
                editor.putString("email", user.getEmail());
                editor.putString("phone", user.getPhone());
                editor.apply();

                // Cập nhật giao diện
                txtTen.setText(user.getFullName() != null ? user.getFullName() : "");
                txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                txtSDT.setText(user.getPhone() != null ? user.getPhone() : "");
            } else {
                Log.e(TAG, "loadUserData: Không tìm thấy người dùng trên Firebase");
            }
        });
    }
}