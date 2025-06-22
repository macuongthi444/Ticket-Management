package com.example.ticket_management.activity.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticket_management.R;
import com.example.ticket_management.DAO.AuthDAO;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText edtEmail;
    private Button btnSendResetEmail;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edt_email);
        txtLogin = findViewById(R.id.txtLogin);
        btnSendResetEmail = findViewById(R.id.btn_send_reset_email);


        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class );
                startActivity(intent);
            }
        });

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString().trim();

                if (!email.isEmpty()) {
                    AuthDAO authDAO = new AuthDAO();
                    authDAO.sendPasswordResetEmail(ForgotPasswordActivity.this, email);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng điền địa chỉ email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}