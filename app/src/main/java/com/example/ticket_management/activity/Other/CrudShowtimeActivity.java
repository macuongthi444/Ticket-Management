package com.example.ticket_management.activity.Other;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticket_management.R;
import java.util.ArrayList;
import java.util.List;

public class CrudShowtimeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_showtime);
        RecyclerView rv = findViewById(R.id.rvShowtimeList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Showtime> showtimes = new ArrayList<>();
        showtimes.add(new Showtime("Avengers: Endgame", "1", "20:00 01/01/2024", "100.000đ"));
        showtimes.add(new Showtime("Parasite", "2", "18:30 02/01/2024", "90.000đ"));
        showtimes.add(new Showtime("Coco", "3", "16:00 03/01/2024", "80.000đ"));
        ShowtimeAdapter adapter = new ShowtimeAdapter(showtimes);
        rv.setAdapter(adapter);
    }

    public static class Showtime {
        public String film, room, time, price;
        public Showtime(String film, String room, String time, String price) {
            this.film = film; this.room = room; this.time = time; this.price = price;
        }
    }
} 