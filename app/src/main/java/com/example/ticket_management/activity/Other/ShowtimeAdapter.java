package com.example.ticket_management.activity.Other;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticket_management.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
    private final List<CrudShowtimeActivity.Showtime> showtimes;
    public ShowtimeAdapter(List<CrudShowtimeActivity.Showtime> showtimes) { this.showtimes = showtimes; }
    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime, parent, false);
        return new ShowtimeViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder h, int pos) {
        CrudShowtimeActivity.Showtime s = showtimes.get(pos);
        h.tvFilm.setText(s.film);
        h.tvRoom.setText("Phòng: " + s.room);
        h.tvTime.setText("Thời gian: " + s.time);
        h.tvPrice.setText("Giá vé: " + s.price);
        h.btnEdit.setOnClickListener(v -> Toast.makeText(v.getContext(), "Sửa suất chiếu: " + s.film, Toast.LENGTH_SHORT).show());
        h.btnDelete.setOnClickListener(v -> Toast.makeText(v.getContext(), "Xóa suất chiếu: " + s.film, Toast.LENGTH_SHORT).show());
    }
    @Override
    public int getItemCount() { return showtimes.size(); }
    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilm, tvRoom, tvTime, tvPrice;
        MaterialButton btnEdit, btnDelete;
        ShowtimeViewHolder(View v) {
            super(v);
            tvFilm = v.findViewById(R.id.tvShowtimeFilm);
            tvRoom = v.findViewById(R.id.tvShowtimeRoom);
            tvTime = v.findViewById(R.id.tvShowtimeTime);
            tvPrice = v.findViewById(R.id.tvShowtimePrice);
            btnEdit = v.findViewById(R.id.btnEditShowtime);
            btnDelete = v.findViewById(R.id.btnDeleteShowtime);
        }
    }
} 