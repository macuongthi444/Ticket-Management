package com.example.ticket_management.activity.ShowTime;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.DAO.MovieRoomDAO;
import com.example.ticket_management.DAO.ShowTimeDAO;
import com.example.ticket_management.model.Movie;
import com.example.ticket_management.model.MovieRoom;
import com.example.ticket_management.model.ShowTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddShowTimeActivity extends BaseActivity {

    private TextView tvTitle, tvShowDate, tvShowTime;
    private Spinner spinnerMovie, spinnerRoom;
    private Button btnSave, btnCancel;
    private ShowTimeDAO showTimeDAO;
    private MovieDAO movieDAO;
    private MovieRoomDAO roomDAO;
    private ShowTime showTimeToUpdate;
    private boolean isUpdateMode = false;
    private List<Movie> movieList = new ArrayList<>();
    private List<MovieRoom> roomList = new ArrayList<>();
    private Date selectedShowDate;
    private String selectedShowTime;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_add_show_time;
    }

    @Override
    protected void initContent(View contentView) {
        tvTitle = findViewById(R.id.tvTitle);
        tvShowDate = findViewById(R.id.tvShowDate);
        tvShowTime = findViewById(R.id.tvShowTime);
        spinnerMovie = findViewById(R.id.spinnerMovie);
        spinnerRoom = findViewById(R.id.spinnerRoom);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        showTimeDAO = new ShowTimeDAO();
        movieDAO = new MovieDAO();
        roomDAO = new MovieRoomDAO();

        loadMovies();
        loadRooms();

        Intent intent = getIntent();
        if (intent.hasExtra("showtime")) {
            isUpdateMode = true;
            showTimeToUpdate = (ShowTime) intent.getSerializableExtra("showtime");
            if (showTimeToUpdate != null) {
                tvShowDate.setText(showTimeToUpdate.getShowDate());
                tvShowTime.setText(showTimeToUpdate.getShowTime());
                tvTitle.setText("Chỉnh sửa xuất chiếu");
                btnSave.setText("Chỉnh sửa");
            }
        } else {
            tvTitle.setText("Tạo xuất chiếu");
            btnSave.setText("Tạo");
        }

        tvShowDate.setOnClickListener(v -> showDatePickerDialog());
        tvShowTime.setOnClickListener(v -> showTimePickerDialog());
        btnSave.setOnClickListener(v -> saveShowTime());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadMovies() {
        movieDAO.getAllMovies(new MovieDAO.OnMovieOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                movieList = (List<Movie>) result;
                List<String> movieNames = new ArrayList<>();
                for (Movie movie : movieList) {
                    movieNames.add(movie.getMovieName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddShowTimeActivity.this,
                        android.R.layout.simple_spinner_item, movieNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMovie.setAdapter(adapter);

                if (isUpdateMode && showTimeToUpdate != null) {
                    setSelectedMovie(showTimeToUpdate.getMovieId());
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddShowTimeActivity.this, "Lỗi tải danh sách phim: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRooms() {
        roomDAO.getAllRooms(new MovieRoomDAO.OnRoomOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                roomList = (List<MovieRoom>) result;
                List<String> roomNames = new ArrayList<>();
                for (MovieRoom room : roomList) {
                    roomNames.add(room.getRoomName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddShowTimeActivity.this,
                        android.R.layout.simple_spinner_item, roomNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoom.setAdapter(adapter);

                if (isUpdateMode && showTimeToUpdate != null) {
                    setSelectedRoom(showTimeToUpdate.getRoomId());
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddShowTimeActivity.this, "Lỗi tải danh sách phòng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSelectedMovie(String movieId) {
        if (movieList != null && movieId != null) {
            for (int i = 0; i < movieList.size(); i++) {
                if (movieList.get(i).getMovieId().equals(movieId)) {
                    spinnerMovie.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setSelectedRoom(String roomId) {
        if (roomList != null && roomId != null) {
            for (int i = 0; i < roomList.size(); i++) {
                if (roomList.get(i).getRoomId().equals(roomId)) {
                    spinnerRoom.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    selectedShowDate = selectedDate.getTime();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    tvShowDate.setText(dateFormat.format(selectedShowDate));
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    selectedShowTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    tvShowTime.setText(selectedShowTime);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private boolean isSaving = false;

    private void saveShowTime() {
        if (isSaving) return;

        String showDate = tvShowDate.getText().toString().trim();
        String showTime = tvShowTime.getText().toString().trim();
        int selectedMoviePosition = spinnerMovie.getSelectedItemPosition();
        int selectedRoomPosition = spinnerRoom.getSelectedItemPosition();

        if (showDate.isEmpty() || showTime.isEmpty() || selectedMoviePosition < 0 || selectedRoomPosition < 0) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String movieId = movieList.get(selectedMoviePosition).getMovieId();
        String roomId = roomList.get(selectedRoomPosition).getRoomId();
        String poster = movieList.get(selectedMoviePosition).getImageBase64();

        isSaving = true;
        btnSave.setEnabled(false);

        // Kiểm tra trùng suất chiếu trước khi lưu
        showTimeDAO.checkDuplicateShowTime(roomId, showDate, showTime, showTimeToUpdate != null ? showTimeToUpdate.getShowtimeId() : null,
                new ShowTimeDAO.OnShowTimeOperationCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if ((Boolean) result) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddShowTimeActivity.this, "Đã có suất chiếu vào giờ này trong cùng ngày và phòng!", Toast.LENGTH_LONG).show();
                                isSaving = false;
                                btnSave.setEnabled(true);
                            });
                        } else {
                            // Nếu không trùng, tiến hành lưu hoặc cập nhật
                            saveOrUpdateShowTime(showDate, showTime, movieId, roomId, poster);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddShowTimeActivity.this, "Lỗi kiểm tra trùng suất chiếu: " + error, Toast.LENGTH_SHORT).show();
                            isSaving = false;
                            btnSave.setEnabled(true);
                        });
                    }
                });
    }

    private void saveOrUpdateShowTime(String showDate, String showTime, String movieId, String roomId, String poster) {
        if (isUpdateMode && showTimeToUpdate != null) {
            showTimeToUpdate.setShowDate(showDate);
            showTimeToUpdate.setShowTime(showTime);
            showTimeToUpdate.setMovieId(movieId);
            showTimeToUpdate.setRoomId(roomId);
            showTimeToUpdate.setPoster(poster);

            showTimeDAO.updateShowTime(showTimeToUpdate, new ShowTimeDAO.OnShowTimeOperationCallback() {
                @Override
                public void onSuccess(Object result) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddShowTimeActivity.this, "Cập nhật xuất chiếu thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddShowTimeActivity.this, "Cập nhật thất bại: " + error, Toast.LENGTH_SHORT).show();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }
            });
        } else {
            ShowTime newShowTime = new ShowTime();
            newShowTime.setShowtimeId(null);
            newShowTime.setShowDate(showDate);
            newShowTime.setShowTime(showTime);
            newShowTime.setMovieId(movieId);
            newShowTime.setRoomId(roomId);
            newShowTime.setPoster(poster);

            showTimeDAO.addShowTime(newShowTime, new ShowTimeDAO.OnShowTimeOperationCallback() {
                @Override
                public void onSuccess(Object result) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddShowTimeActivity.this, "Thêm xuất chiếu thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddShowTimeActivity.this, "Thêm thất bại: " + error, Toast.LENGTH_SHORT).show();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }
            });
        }
    }
}