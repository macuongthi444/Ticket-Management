package com.example.ticket_management.activity.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticket_management.R;
import com.example.ticket_management.DAO.AuthDAO;
import com.example.ticket_management.model.User;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText edtEmail, edtPassword, edtRepassword, edtFullName, edtPhone, edtAddess;
    private Button btnRegister;
    private TextView txtLogin;
    private CheckBox chkTerms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtRepassword = findViewById(R.id.edt_repassword);
        edtFullName = findViewById(R.id.edt_name);
        edtPhone = findViewById(R.id.edt_phone);
        txtLogin = findViewById(R.id.txtLogin);
        btnRegister = findViewById(R.id.btn_register);
        edtAddess = findViewById(R.id.edt_address);
        chkTerms = findViewById(R.id.chkTerms);



        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String repassword = edtRepassword.getText().toString().trim();
                String name = edtFullName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String address = edtAddess.getText().toString().trim();
                boolean termsChecked = chkTerms.isChecked();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repassword.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(repassword)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!termsChecked) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng đồng ý với các điều khoản sử dụng", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User();
                AuthDAO authDAO = new AuthDAO();
                authDAO.registerUser(RegisterActivity.this, email, password, name, phone,address , user);
            }
        });
    }
}