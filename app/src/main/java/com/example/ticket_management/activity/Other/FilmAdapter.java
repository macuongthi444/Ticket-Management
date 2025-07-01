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

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {
    private final List<CrudFilmActivity.Film> films;
    public FilmAdapter(List<CrudFilmActivity.Film> films) { this.films = films; }
    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film, parent, false);
        return new FilmViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder h, int pos) {
        CrudFilmActivity.Film f = films.get(pos);
        h.tvName.setText(f.name);
        h.tvGenre.setText("Thể loại: " + f.genre);
        h.tvYear.setText("Năm: " + f.year);
        h.tvDirector.setText("Đạo diễn: " + f.director);
        h.tvDuration.setText("Thời lượng: " + f.duration);
        h.btnEdit.setOnClickListener(v -> Toast.makeText(v.getContext(), "Sửa: " + f.name, Toast.LENGTH_SHORT).show());
        h.btnDelete.setOnClickListener(v -> Toast.makeText(v.getContext(), "Xóa: " + f.name, Toast.LENGTH_SHORT).show());
    }
    @Override
    public int getItemCount() { return films.size(); }
    static class FilmViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGenre, tvYear, tvDirector, tvDuration;
        MaterialButton btnEdit, btnDelete;
        FilmViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvFilmName);
            tvGenre = v.findViewById(R.id.tvFilmGenre);
            tvYear = v.findViewById(R.id.tvFilmYear);
            tvDirector = v.findViewById(R.id.tvFilmDirector);
            tvDuration = v.findViewById(R.id.tvFilmDuration);
            btnEdit = v.findViewById(R.id.btnEditFilm);
            btnDelete = v.findViewById(R.id.btnDeleteFilm);
        }
    }
} 