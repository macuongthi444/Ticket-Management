package com.example.ticket_management.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ticket_management.R;
import com.example.ticket_management.model.TransactionHistory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticActivity extends BaseActivity {

    private TextView tvRevenue;
    private BarChart barChart;
    private DatabaseReference transactionsRef;
    private Map<String, Float> monthlyRevenue;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_statistic;
    }

    @Override
    protected void initContent(View contentView) {
        tvRevenue = contentView.findViewById(R.id.tv_revenue);
        barChart = contentView.findViewById(R.id.bar_chart);
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactionHistory");
        monthlyRevenue = new HashMap<>();

        setupBarChart();
        fetchTransactionData();
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false); // Tắt mô tả
        barChart.setFitBars(true); // Căn chỉnh cột
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Đặt nhãn trục X ở dưới
        barChart.getXAxis().setGranularity(1f); // Khoảng cách giữa các nhãn
        barChart.getXAxis().setLabelRotationAngle(45f); // Xoay nhãn 45 độ
        barChart.getAxisRight().setEnabled(false); // Tắt trục Y bên phải
        barChart.animateY(1000); // Hiệu ứng xuất hiện
    }

    private void fetchTransactionData() {
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Tạo danh sách 12 tháng gần nhất (4/2024 -> 3/2025)
                Calendar calendar = Calendar.getInstance();
                calendar.set(2025, Calendar.DECEMBER, 1); // Đặt thời gian hiện tại là 3/2025
                List<String> lastTwelveMonths = new ArrayList<>();
                for (int i = 11; i >= 0; i--) {
                    Calendar tempCalendar = (Calendar) calendar.clone();
                    tempCalendar.add(Calendar.MONTH, -i);
                    int month = tempCalendar.get(Calendar.MONTH) + 1;
                    int year = tempCalendar.get(Calendar.YEAR);
                    String monthYear = String.format("%02d/%d", month, year);
                    lastTwelveMonths.add(monthYear);
                    monthlyRevenue.put(monthYear, 0f); // Khởi tạo doanh thu bằng 0
                }

                // Tính doanh thu từ giao dịch
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                        TransactionHistory transaction = transactionSnapshot.getValue(TransactionHistory.class);
                        if (transaction != null) {
                            String[] dateParts = transaction.getShowDate().split("-");
                            int month = Integer.parseInt(dateParts[1]);
                            int year = Integer.parseInt(dateParts[2]);
                            String monthYear = String.format("%02d/%d", month, year);
                            if (lastTwelveMonths.contains(monthYear)) {
                                float currentTotal = monthlyRevenue.get(monthYear);
                                monthlyRevenue.put(monthYear, currentTotal + transaction.getTotalPrice());
                            }
                        }
                    }
                }
                displayRevenue(lastTwelveMonths);
                displayBarChart(lastTwelveMonths);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("StatisticActivity", "Lỗi tải dữ liệu: " + error.getMessage());
                Toast.makeText(StatisticActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRevenue(List<String> lastTwelveMonths) {
        StringBuilder revenueText = new StringBuilder("Doanh thu 12 tháng gần nhất:\n");
        DecimalFormat format = new DecimalFormat("#,###");

        for (String monthYear : lastTwelveMonths) {
            float revenue = monthlyRevenue.getOrDefault(monthYear, 0f);
            revenueText.append(monthYear)
                    .append(": ")
                    .append(format.format(revenue))
                    .append(" VND\n");
        }

        tvRevenue.setText(revenueText.toString());
    }

    private void displayBarChart(List<String> lastTwelveMonths) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < lastTwelveMonths.size(); i++) {
            String monthYear = lastTwelveMonths.get(i);
            float revenue = monthlyRevenue.getOrDefault(monthYear, 0f);
            entries.add(new BarEntry(i, revenue));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VND)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Màu sắc cho cột
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f); // Độ rộng cột

        // Đặt nhãn cho trục X
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(lastTwelveMonths));
        barChart.setData(barData);
        barChart.invalidate(); // Làm mới biểu đồ
    }
}