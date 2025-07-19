package com.example.ticket_management.activity.ShowTime;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.adapter.ShowTimeUserAdapter;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.model.Movie;

import java.util.List;

public class ShowtimeUserListActivity extends BaseActivity {

    private MovieDAO movieDao;
    private RecyclerView recyclerViewShowtimes;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_showtime_user_list;
    }

    @Override
    protected void initContent(View contentView) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch Chiếu Phim");
        }

        // Khởi tạo RecyclerView
        recyclerViewShowtimes = contentView.findViewById(R.id.recyclerViewShowtimes);
        if (recyclerViewShowtimes == null) {
            Toast.makeText(this, "Không tìm thấy RecyclerView", Toast.LENGTH_LONG).show();
            return;
        }
        recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(this));

        movieDao = new MovieDAO();

        // Lấy danh sách phim từ MovieDAO
        movieDao.getAllMovies(new MovieDAO.OnMovieOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result != null) {
                    List<Movie> movieList = (List<Movie>) result;
                    ShowTimeUserAdapter adapter = new ShowTimeUserAdapter(ShowtimeUserListActivity.this, movieList);
                    recyclerViewShowtimes.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ShowtimeUserListActivity.this, "Lỗi tải danh sách suất chiếu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}