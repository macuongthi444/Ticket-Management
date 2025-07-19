package com.example.ticket_management.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.Movie.AddMovieActivity;
import com.example.ticket_management.activity.Movie.MovieListActivity;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.model.Movie;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movie> movieList;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList != null ? movieList : java.util.Collections.emptyList();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.tvMovieName.setText("Tên phim: " + (movie.getMovieName() != null ? movie.getMovieName() : ""));
        holder.tvDirector.setText("Đạo diễn: " + (movie.getDirector() != null ? movie.getDirector() : ""));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvReleaseDate.setText("Ngày phát hành: " + (movie.getReleaseDate() != null ? dateFormat.format(movie.getReleaseDate()) : ""));

        // Hiển thị ảnh từ Base64
        if (movie.getImageBase64() != null && !movie.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(movie.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageBase64.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imageBase64.setImageResource(R.drawable.ic_default_movie);
            }
        } else {
            holder.imageBase64.setImageResource(R.drawable.ic_default_movie);
        }

        holder.imgMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.imgMenu);
            popupMenu.getMenu().add("Chỉnh sửa");
            popupMenu.getMenu().add("Xóa");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Chỉnh sửa")) {
                    Intent intent = new Intent(context, AddMovieActivity.class);
                    intent.putExtra("movie", movie);
                    ((MovieListActivity) context).startActivityForResult(intent, 1);
                } else if (item.getTitle().equals("Xóa")) {
                    MovieDAO movieDAO = new MovieDAO();
                    movieDAO.deleteMovie(movie.getMovieId(), new MovieDAO.OnMovieOperationCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            // Xóa movie khỏi danh sách và cập nhật RecyclerView
                            movieList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, movieList.size());
                            Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(context, "Xóa thất bại: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // Cập nhật danh sách phim
    public void updateMovies(List<Movie> newMovieList) {
        this.movieList = newMovieList != null ? newMovieList : java.util.Collections.emptyList();
        notifyDataSetChanged();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageBase64, imgMenu;
        TextView tvMovieName, tvReleaseDate, tvDirector;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageBase64 = itemView.findViewById(R.id.imageBase64);
            tvMovieName = itemView.findViewById(R.id.tvMovieName);
            tvReleaseDate = itemView.findViewById(R.id.tvReleaseDate);
            tvDirector = itemView.findViewById(R.id.tvDirector);
            imgMenu = itemView.findViewById(R.id.imgMenu);
        }
    }
}