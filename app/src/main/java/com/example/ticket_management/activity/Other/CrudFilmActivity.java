package com.example.ticket_management.activity.Other;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticket_management.R;
import java.util.ArrayList;
import java.util.List;

public class CrudFilmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_film);
        RecyclerView rv = findViewById(R.id.rvFilmList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Film> films = new ArrayList<>();
        films.add(new Film("Avengers: Endgame", "Hành động, Khoa học viễn tưởng", "2019", "Anthony Russo, Joe Russo", "181 phút"));
        films.add(new Film("Parasite", "Kịch tính, Hài đen", "2019", "Bong Joon-ho", "132 phút"));
        films.add(new Film("Coco", "Hoạt hình, Phiêu lưu", "2017", "Lee Unkrich", "105 phút"));
        FilmAdapter adapter = new FilmAdapter(films);
        rv.setAdapter(adapter);
    }

    public static class Film {
        public String name, genre, year, director, duration;
        public Film(String name, String genre, String year, String director, String duration) {
            this.name = name; this.genre = genre; this.year = year; this.director = director; this.duration = duration;
        }
    }
} 