package com.example.ticket_management.activity.Other;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticket_management.R;
import com.example.ticket_management.activity.Auth.LoginActivity;
import com.example.ticket_management.model.TransactionHistory;
import java.util.Arrays;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kiểm tra trạng thái đăng nhập
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_transaction_history);

        RecyclerView recyclerView = findViewById(R.id.rvTransactionHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu mẫu
        List<TransactionHistory> historyList = Arrays.asList(
                new TransactionHistory("Avengers: Endgame", "01/06/2024", "18:00", System.currentTimeMillis(), 150000, "Phòng 1", Arrays.asList("A1", "A2"), ""),
                new TransactionHistory("Joker", "28/05/2024", "20:00", System.currentTimeMillis(), 200000, "Phòng 2", Arrays.asList("B1", "B2"), ""),
                new TransactionHistory("Spider-Man", "20/05/2024", "21:30", System.currentTimeMillis(), 120000, "Phòng 3", Arrays.asList("C1"), "")
        );

        TransactionHistoryAdapter adapter = new TransactionHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);
    }
} 