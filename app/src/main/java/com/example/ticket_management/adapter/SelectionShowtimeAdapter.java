package com.example.ticket_management.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;

import java.util.List;

public class SelectionShowtimeAdapter extends RecyclerView.Adapter<SelectionShowtimeAdapter.ShowtimeViewHolder> {
    private List<String> showtimes;
    private OnShowtimeClickListener onShowtimeClickListener;

    public SelectionShowtimeAdapter(List<String> showtimes, OnShowtimeClickListener listener) {
        this.showtimes = showtimes;
        this.onShowtimeClickListener = listener;
        Log.d("ShowtimeAdapter", "Showtimes loaded: " + showtimes.toString());
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        String showtime = showtimes.get(position);
        holder.showtimeText.setText(showtime);
        Log.d("ShowtimeAdapter", "Binding showtime: " + showtime);

        // Xử lý sự kiện click vào khung giờ
        holder.itemView.setOnClickListener(v -> {
            if (onShowtimeClickListener != null) {
                onShowtimeClickListener.onShowtimeClick(showtime);
            }
        });
    }

    @Override
    public int getItemCount() {
        return showtimes != null ? showtimes.size() : 0;
    }

    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView showtimeText;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            showtimeText = itemView.findViewById(R.id.showtime_text);
        }
    }

    public void updateShowtimes(List<String> newShowtimes) {
        this.showtimes = newShowtimes;
        notifyDataSetChanged();
        Log.d("ShowtimeAdapter", "Showtimes updated: " + newShowtimes.toString());
    }

    // Interface để xử lý sự kiện click
    public interface OnShowtimeClickListener {
        void onShowtimeClick(String showtime);
    }
}