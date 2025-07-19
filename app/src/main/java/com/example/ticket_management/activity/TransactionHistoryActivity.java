package com.example.ticket_management.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.adapter.TransactionAdapter;
import com.example.ticket_management.config.DatabaseManager;
import com.example.ticket_management.model.TransactionHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionHistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<TransactionHistory> transactionList;
    private DatabaseReference transactionHistoryRef;
    private String userId;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_transaction_history;
    }

    @Override
    protected void initContent(View contentView) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hóa Đơn");
        }

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            Log.e("TransactionHistory", "User not logged in: userId is null in SharedPreferences");
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử giao dịch", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("TransactionHistory", "Current userId from SharedPreferences: " + userId);

        // Ánh xạ RecyclerView từ contentView
        recyclerView = contentView.findViewById(R.id.recycler_view_transactions);
        if (recyclerView == null) {
            Log.e("TransactionHistory", "RecyclerView is null, check layout ID in activity_transaction_history.xml");
            Toast.makeText(this, "Lỗi giao diện: Không tìm thấy RecyclerView", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Khởi tạo danh sách và adapter
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(transactionAdapter);

        // Khởi tạo Firebase
        transactionHistoryRef = DatabaseManager.getInstance().getReference("transactionHistory");

        // Tải lịch sử giao dịch của người dùng hiện tại
        loadTransactionHistory(userId);
    }

    private void loadTransactionHistory(String userId) {
        transactionHistoryRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TransactionHistory transaction = dataSnapshot.getValue(TransactionHistory.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                    }
                }

                Collections.sort(transactionList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                transactionAdapter.notifyDataSetChanged();

                if (transactionList.isEmpty()) {
                    Toast.makeText(TransactionHistoryActivity.this, "Không có giao dịch nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TransactionHistory", "Error loading transactions: " + error.getMessage());
                Toast.makeText(TransactionHistoryActivity.this, "Lỗi tải lịch sử giao dịch", Toast.LENGTH_SHORT).show();
            }
        });
    }
}