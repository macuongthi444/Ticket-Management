package com.example.ticket_management.activity.Auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.AuthDAO;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends BaseActivity {
    private AuthDAO authDAO;
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_change_password;
    }

    @Override
    protected void initContent(View contentView) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đổi mật khẩu");
        }
        authDAO = new AuthDAO();

        // Ánh xạ các thành phần giao diện
        TextInputEditText edtOldPassword = findViewById(R.id.edt_old_password);
        TextInputEditText edtNewPassword = findViewById(R.id.edt_new_password);
        TextInputEditText edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        Button btnChangePassword = findViewById(R.id.btn_change_password);
        TextView txtBackToLogin = findViewById(R.id.txt_back);

        // Xử lý sự kiện nhấn nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener(v -> {
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            authDAO.changePassword(this, oldPassword, newPassword, confirmPassword);
        });

        // Xử lý sự kiện nhấn "Quay lại đăng nhập"
        txtBackToLogin.setOnClickListener(v -> finish());
    }
}