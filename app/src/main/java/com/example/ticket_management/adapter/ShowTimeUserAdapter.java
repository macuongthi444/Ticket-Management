package com.example.ticket_management.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.SelectionShowtimesActivity;
import com.example.ticket_management.model.Movie;

import java.util.List;

public class ShowTimeUserAdapter extends RecyclerView.Adapter<ShowTimeUserAdapter.ShowTimeViewHolder> {
    private Context context;
    private List<Movie> movieList;

    public ShowTimeUserAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList != null ? movieList : java.util.Collections.emptyList();
    }

    @NonNull
    @Override
    public ShowTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_user, parent, false);
        return new ShowTimeViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ShowTimeViewHolder holder, int position) {
        int leftPosition = position * 2;
        int rightPosition = leftPosition + 1;

        Movie showTimeLeft = movieList.get(leftPosition);
        Movie showTimeRight = (rightPosition < movieList.size()) ? movieList.get(rightPosition) : null;

        // LEFT SIDE
        if (showTimeLeft != null) {
            holder.layoutLeft.setVisibility(View.VISIBLE);
            holder.layoutLeft.setTag(showTimeLeft.getMovieId());
            holder.tvNameMovieLeft.setText(showTimeLeft.getMovieName() != null ? showTimeLeft.getMovieName() : "N/A");

            // Load image asynchronously
            new LoadImageTask(holder.ivShowtimeLeft, showTimeLeft.getMovieName()).execute(showTimeLeft.getImageBase64());

            holder.layoutLeft.setOnClickListener(v -> {
                Intent intent = new Intent(context, SelectionShowtimesActivity.class);
                intent.putExtra("movie_id", showTimeLeft.getMovieId());
                context.startActivity(intent);
            });
        } else {
            holder.layoutLeft.setVisibility(View.GONE);
        }

        // RIGHT SIDE
        if (showTimeRight != null) {
            holder.layoutRight.setVisibility(View.VISIBLE);
            holder.layoutRight.setTag(showTimeRight.getMovieId());
            holder.tvNameMovieRight.setText(showTimeRight.getMovieName() != null ? showTimeRight.getMovieName() : "N/A");

            // Load image asynchronously
            new LoadImageTask(holder.ivShowtimeRight, showTimeRight.getMovieName()).execute(showTimeRight.getImageBase64());

            holder.layoutRight.setOnClickListener(v -> {
                Intent intent = new Intent(context, SelectionShowtimesActivity.class);
                intent.putExtra("movie_id", showTimeRight.getMovieId());
                context.startActivity(intent);
            });
        } else {
            holder.layoutRight.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (movieList.size() + 1) / 2;
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
                    Log.d("ShowTimeUserAdapter", "Base64 decoded for " + movieName + ", length: " + decodedString.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (bitmap != null) {
                        Log.d("ShowTimeUserAdapter", "Bitmap decoded successfully for " + movieName + ", size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        // Scale bitmap to fit ImageView
                        int targetHeight = 180 * imageView.getResources().getDisplayMetrics().densityDpi / 160;
                        if (bitmap.getHeight() > targetHeight) {
                            float scale = (float) targetHeight / bitmap.getHeight();
                            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), targetHeight, true);
                        }
                        return bitmap;
                    } else {
                        Log.e("ShowTimeUserAdapter", "Failed to decode Bitmap for " + movieName);
                    }
                } catch (Exception e) {
                    Log.e("ShowTimeUserAdapter", "Error decoding Base64 for " + movieName + ": " + e.getMessage());
                }
            } else {
                Log.w("ShowTimeUserAdapter", "No imageBase64 for movie: " + movieName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
            } else {
                imageView.setImageResource(R.drawable.img_background);
            }
        }
    }

    // ViewHolder for RecyclerView
    public static class ShowTimeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivShowtimeLeft, ivShowtimeRight;
        TextView tvNameMovieLeft, tvNameMovieRight;
        LinearLayout layoutLeft, layoutRight;

        public ShowTimeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivShowtimeLeft = itemView.findViewById(R.id.iv_showtime_left);
            ivShowtimeRight = itemView.findViewById(R.id.iv_showtime_right);
            tvNameMovieLeft = itemView.findViewById(R.id.tv_name_movie_left);
            tvNameMovieRight = itemView.findViewById(R.id.tv_name_movie_right);
            layoutLeft = itemView.findViewById(R.id.layout_left);
            layoutRight = itemView.findViewById(R.id.layout_right);
        }
    }

    // Cập nhật danh sách phim
    public void updateMovies(List<Movie> newMovieList) {
        this.movieList = newMovieList != null ? newMovieList : java.util.Collections.emptyList();
        notifyDataSetChanged();
    }
}