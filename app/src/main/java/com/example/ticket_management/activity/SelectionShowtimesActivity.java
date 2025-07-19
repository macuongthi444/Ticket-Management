package com.example.ticket_management.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.adapter.DateAdapter;
import com.example.ticket_management.adapter.SelectionShowtimeAdapter;
import com.example.ticket_management.model.Movie;
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
import java.util.function.Consumer;

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
    private MovieDAO movieDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_showtime_selection);

        // Nhận movieID từ Intent
        movieID = getIntent().getStringExtra("movie_id");
        if (movieID == null || movieID.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy movieID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        moviePoster = findViewById(R.id.movie_poster);
        recyclerViewDates = findViewById(R.id.recycler_view_dates);
        recyclerViewShowtimes = findViewById(R.id.recycler_view_showtimes);

        // Setup RecyclerViews
        recyclerViewDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase and MovieDAO
        showtimeRef = FirebaseDatabase.getInstance().getReference("showTimes");
        movieDao = new MovieDAO();

        // Load movie poster
        loadMoviePoster();

        // Fetch showtimes from Firebase
        fetchShowtimes();
    }

    private void loadMoviePoster() {
        if (movieID == null || movieID.isEmpty()) {
            Log.e("SelectionShowtimes", "movieID is null or empty");
            moviePoster.setImageResource(R.drawable.img_background);
            Toast.makeText(SelectionShowtimesActivity.this, "movieID không hợp lệ.", Toast.LENGTH_LONG).show();
            return;
        }

        movieDao.getMovieById(movieID,
                movie -> {
                    if (movie != null) {
                        String imageBase64 = movie.getImageBase64();
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            Log.d("SelectionShowtimes", "Loading Base64 image for movie: " + movie.getMovieName());
                            new LoadImageTask(moviePoster, movie.getMovieName()).execute(imageBase64);
                        } else {
                            Log.w("SelectionShowtimes", "No ImageBase64 for movie: " + movie.getMovieName());
                            moviePoster.setImageResource(R.drawable.img_background);
                            runOnUiThread(() -> Toast.makeText(SelectionShowtimesActivity.this, "Không tìm thấy poster cho phim này.", Toast.LENGTH_LONG).show());
                        }
                    } else {
                        Log.e("SelectionShowtimes", "Movie is null");
                        moviePoster.setImageResource(R.drawable.img_background);
                        runOnUiThread(() -> Toast.makeText(SelectionShowtimesActivity.this, "Không tìm thấy thông tin phim.", Toast.LENGTH_LONG).show());
                    }
                },
                error -> {
                    Log.e("SelectionShowtimes", "Failed to load movie: " + error);
                    moviePoster.setImageResource(R.drawable.img_background);
                    runOnUiThread(() -> Toast.makeText(SelectionShowtimesActivity.this, "Lỗi tải poster: " + error, Toast.LENGTH_LONG).show());
                });
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

                for (DataSnapshot data : snapshot.getChildren()) {
                    ShowTime showTime = data.getValue(ShowTime.class);
                    if (showTime != null && movieID.equals(showTime.getMovieId())) {
                        String date = showTime.getShowDate();
                        String time = showTime.getShowTime();

                        Log.d("FirebaseData", "Date: " + date + ", Time: " + time);

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
                displayDateList.clear();
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
                                intent.putExtra("poster", selectedShowTime.getPoster()); // Truyền URL poster nếu cần
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

    // AsyncTask to decode Base64 and load Bitmap
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private final String movieName;

        LoadImageTask(ImageView imageView, String movieName) {
            this.imageView = imageView;
            this.movieName = movieName;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageBase64 = params[0];
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Log.d("SelectionShowtimes", "Base64 decoded for " + movieName + ", length: " + decodedString.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (bitmap != null) {
                        Log.d("SelectionShowtimes", "Bitmap decoded successfully for " + movieName + ", size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        int targetHeight = 200 * imageView.getResources().getDisplayMetrics().densityDpi / 160;
                        if (bitmap.getHeight() > targetHeight) {
                            float scale = (float) targetHeight / bitmap.getHeight();
                            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), targetHeight, true);
                        }
                        return bitmap;
                    } else {
                        Log.e("SelectionShowtimes", "Failed to decode Bitmap for " + movieName);
                    }
                } catch (Exception e) {
                    Log.e("SelectionShowtimes", "Error decoding Base64 for " + movieName + ": " + e.getMessage());
                }
            } else {
                Log.w("SelectionShowtimes", "No imageBase64 for movie: " + movieName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (!((Activity) imageView.getContext()).isFinishing()) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.invalidate();
                } else {
                    imageView.setImageResource(R.drawable.img_background);
                }
            }
        }
    }
}