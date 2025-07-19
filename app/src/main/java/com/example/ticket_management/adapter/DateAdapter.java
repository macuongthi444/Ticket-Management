package com.example.ticket_management.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;

import java.util.List;

import lombok.NonNull;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder>  {
    private List<String> dates;
    private OnDateClickListener onDateClickListener;

    // Constructor
    public DateAdapter(List<String> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.onDateClickListener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dates.get(position);
        try {
            // Tách chuỗi ngày: "19-03-2025"
            String[] parts = date.split("-");
            String day = parts[0]; // "19"
            String month = parts[1]; // "03"

            // Hiển thị ngày và tháng: "19-03"
            String displayDate = day + "-" + month;
            holder.dateText.setText(displayDate);

            // Ẩn dayText vì không cần hiển thị thứ
            holder.dayText.setVisibility(View.GONE);

            // Xử lý sự kiện click
            holder.itemView.setOnClickListener(v -> {
                Log.d("DateAdapter", "Date clicked: " + date);
                onDateClickListener.onDateClick(date);
            });
        } catch (Exception e) {
            Log.e("DateAdapter", "Error parsing date: " + date, e);
            holder.dateText.setText("N/A");
            holder.dayText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dates != null ? dates.size() : 0;
    }

    // ViewHolder class
    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, dayText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text);
            dayText = itemView.findViewById(R.id.day_text);
        }
    }

    // Interface để xử lý sự kiện click
    public interface OnDateClickListener {
        void onDateClick(String date);
    }
}
