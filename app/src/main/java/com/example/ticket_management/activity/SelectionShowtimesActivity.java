package com.example.ticket_management.activity;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ticket_management.R;
import com.example.ticket_management.adapter.DateAdapter;
import com.example.ticket_management.adapter.SelectionShowtimeAdapter;
import com.example.ticket_management.model.ShowTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.NonNull;

public class SelectionShowtimesActivity extends AppCompatActivity {
    private ImageView moviePoster;
    private RecyclerView recyclerViewDates, recyclerViewShowtimes;
    private DateAdapter dateAdapter;
    private SelectionShowtimeAdapter showtimeAdapter;
    private List<String> dateList = new ArrayList<>(); // Lưu ngày đầy đủ (dd-MM-yyyy)
    private List<String> displayDateList = new ArrayList<>(); // Lưu ngày để hiển thị (dd-MM)
    private Map<String, List<String>> showtimesByDate = new HashMap<>();
    private Map<String, List<ShowTime>> showtimesByDateFull = new HashMap<>(); // Lưu toàn bộ thông tin ShowTime
    private DatabaseReference showtimeRef;
    private String movieID;
    private String selectedDate; // Lưu ngày được chọn


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_showtime_selection);

        // Nhận movieID từ Intent
         movieID = getIntent().getStringExtra("movie_id");
//        movieID = "-OLXiZTDU72XoJkwhspK";
//        if (movieID == null || movieID.isEmpty()) {
//            Toast.makeText(this, "Không tìm thấy movieID.", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }

        // Initialize views
        moviePoster = findViewById(R.id.movie_poster);
        recyclerViewDates = findViewById(R.id.recycler_view_dates);
        recyclerViewShowtimes = findViewById(R.id.recycler_view_showtimes);

        // Setup RecyclerViews
        recyclerViewDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        showtimeRef = FirebaseDatabase.getInstance().getReference("showTimes");

        // Fetch data from Firebase
        fetchShowtimes();
    }

    private void fetchShowtimes() {
        showtimeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dateList.clear();
                showtimesByDate.clear();
                showtimesByDateFull.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(SelectionShowtimesActivity.this, "Không có dữ liệu trong Firebase.", Toast.LENGTH_LONG).show();
                    return;
                }

                String posterUrl = null;
                for (DataSnapshot data : snapshot.getChildren()) {
                    ShowTime showTime = data.getValue(ShowTime.class);
                    if (showTime != null && movieID.equals(showTime.getMovieId())) {
                        String date = showTime.getShowDate();
                        String time = showTime.getShowTime();
                        posterUrl = showTime.getPoster();

                        Log.d("FirebaseData", "Date: " + date + ", Time: " + time + ", Poster: " + posterUrl);

                        if (date != null && !date.isEmpty() && !dateList.contains(date)) {
                            dateList.add(date);
                        }
                        if (date != null && time != null) {
                            showtimesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(time);
                            showtimesByDateFull.computeIfAbsent(date, k -> new ArrayList<>()).add(showTime);
                        }
                    }
                }
                // Sắp xếp dateList theo thứ tự thời gian tăng dần
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Collections.sort(dateList, new Comparator<String>() {
                    @Override
                    public int compare(String date1, String date2) {
                        try {
                            Date d1 = dateFormat.parse(date1);
                            Date d2 = dateFormat.parse(date2);
                            return d1.compareTo(d2); // Sắp xếp tăng dần
                        } catch (Exception e) {
                            Log.e("DateSortError", "Lỗi khi parse ngày: " + e.getMessage());
                            return 0; // Nếu có lỗi, giữ nguyên thứ tự
                        }
                    }
                });

                // Tạo displayDateList để hiển thị chỉ dd-MM
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM", Locale.getDefault());
                for (String date : dateList) {
                    try {
                        Date parsedDate = dateFormat.parse(date);
                        String displayDate = displayFormat.format(parsedDate);
                        displayDateList.add(displayDate);
                    } catch (Exception e) {
                        Log.e("DateFormatError", "Lỗi khi format ngày để hiển thị: " + e.getMessage());
                        displayDateList.add(date); // Nếu có lỗi, giữ nguyên ngày gốc
                    }
                }

                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Glide.with(SelectionShowtimesActivity.this)
                            .load(posterUrl)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(moviePoster);
                } else {
                    Log.e("FirebaseData", "Poster URL is null or empty for movieID: " + movieID);
                    Toast.makeText(SelectionShowtimesActivity.this, "Không tìm thấy poster cho phim này.", Toast.LENGTH_LONG).show();
                }

                Log.d("FirebaseData", "Dates: " + dateList.toString());
                Log.d("FirebaseData", "Showtimes by Date: " + showtimesByDate.toString());

                if (dateList.isEmpty()) {
                    Toast.makeText(SelectionShowtimesActivity.this, "Không có ngày chiếu nào cho phim này.", Toast.LENGTH_LONG).show();
                } else {
                    dateAdapter = new DateAdapter(dateList, date -> {
                        selectedDate = date; // Lưu ngày được chọn
                        List<String> showtimes = showtimesByDate.get(date);
                        showtimeAdapter.updateShowtimes(showtimes != null ? showtimes : new ArrayList<>());
                    });
                    recyclerViewDates.setAdapter(dateAdapter);
                }

                if (!dateList.isEmpty()) {
                    selectedDate = dateList.get(0); // Mặc định chọn ngày đầu tiên
                    List<String> showtimes = showtimesByDate.get(selectedDate);
                    if (showtimes != null && !showtimes.isEmpty()) {
                        final String finalPosterUrl = posterUrl;
                        showtimeAdapter = new SelectionShowtimeAdapter(showtimes, showtime -> {
                            // Xử lý khi người dùng chọn khung giờ
                            ShowTime selectedShowTime = null;
                            for (ShowTime st : showtimesByDateFull.get(selectedDate)) {
                                if (st.getShowTime().equals(showtime)) {
                                    selectedShowTime = st;
                                    break;
                                }
                            }

                            if (selectedShowTime != null) {
                                Intent intent = new Intent(SelectionShowtimesActivity.this, SeatSelectionActivity.class);
                                intent.putExtra("movieID", movieID);
                                intent.putExtra("showDate", selectedDate);
                                intent.putExtra("showTime", showtime);
                                intent.putExtra("roomID", selectedShowTime.getRoomId());
                                intent.putExtra("poster", finalPosterUrl);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SelectionShowtimesActivity.this, "Không tìm thấy thông tin suất chiếu.", Toast.LENGTH_LONG).show();
                            }
                        });
                        recyclerViewShowtimes.setAdapter(showtimeAdapter);
                    } else {
                        Toast.makeText(SelectionShowtimesActivity.this, "Không có suất chiếu cho ngày " + selectedDate, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SelectionShowtimesActivity.this, "Không có suất chiếu nào cho phim này.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectionShowtimesActivity.this, "Không thể tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }
}
