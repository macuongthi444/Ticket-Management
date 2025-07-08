package com.example.ticket_management.activity.Movie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.activity.MenuActivity;
import com.example.ticket_management.adapter.MovieAdapter;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.model.Movie;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends BaseActivity {

    private RecyclerView recyclerViewMovies;
    private MovieDAO movieDAO;
    private List<Movie> movieList;
    private MovieAdapter adapter;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_movie_list;
    }

    @Override
    protected void initContent(View contentView) {
        EdgeToEdge.enable(this);
        recyclerViewMovies = contentView.findViewById(R.id.recyclerViewMovies);
        movieDAO = new MovieDAO();
        movieList = new ArrayList<>();
        adapter = new MovieAdapter(this, movieList);

        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMovies.setAdapter(adapter);

        fetchMovies();

        FloatingActionButton fabAddMovie = contentView.findViewById(R.id.fabAddMovie);
        fabAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(MovieListActivity.this, AddMovieActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void fetchMovies() {
        movieDAO.getAllMovies(new MovieDAO.OnMovieOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    movieList.clear();
                    movieList.addAll((List<Movie>) result);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MovieListActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}