package com.example.ticket_management.DAO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ticket_management.config.DatabaseManager;
import com.example.ticket_management.model.Movie;
import com.example.ticket_management.model.ShowTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MovieDAO {
    private final DatabaseReference databaseReference;
    private final DatabaseReference historyReference;
    private final DatabaseReference showtimeReference;

    public MovieDAO() {
        databaseReference = DatabaseManager.getInstance().getReference("movies");
        historyReference = DatabaseManager.getInstance().getReference("hstories");
        showtimeReference = DatabaseManager.getInstance().getReference("showTimes");
    }

    public interface OnMovieOperationCallback {
        void onSuccess(Object result);
        void onFailure(String error);
    }

    // Hàm chuyển ảnh thành Base64
    private String convertImageToBase64(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                Log.e("MovieDAO", "Failed to decode image: " + imagePath);
                return null;
            }
            // Giảm kích thước ảnh
            int maxSize = 512; // Kích thước tối đa (pixel)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scale = Math.min((float) maxSize / width, (float) maxSize / height);
            if (scale < 1) {
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); // Tăng chất lượng lên 90
            byte[] byteArray = baos.toByteArray();
            String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d("MovieDAO", "Base64 length for " + imagePath + ": " + base64.length());
            return base64;
        } catch (Exception e) {
            Log.e("MovieDAO", "Error converting image to Base64: " + e.getMessage());
            return null;
        }
    }

    // Lấy danh sách phim
    public void getAllMovies(OnMovieOperationCallback callback) {
        if (callback == null) return;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Movie> movies = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Movie movie = data.getValue(Movie.class);
                    if (movie != null) {
                        movie.setMovieId(data.getKey());
                        movies.add(movie);
                    }
                }
                callback.onSuccess(movies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MovieDAO", "Lấy danh sách phim thất bại: " + error.getMessage());
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Thêm một Movie mới với ảnh Base64
    public void addMovie(Movie movie, String imagePath, OnMovieOperationCallback callback) {
        String movieId = databaseReference.push().getKey();
        if (movieId != null) {
            movie.setMovieId(movieId);
            // Chuyển ảnh thành Base64
            if (imagePath != null && !imagePath.isEmpty()) {
                String base64Image = convertImageToBase64(imagePath);
                if (base64Image != null) {
                    movie.setImageBase64(base64Image);
                } else {
                    callback.onFailure("Lỗi khi mã hóa ảnh thành Base64");
                    return;
                }
            }

            Log.d("MovieDAO", "Adding movie with ID: " + movieId);
            databaseReference.child(movieId).setValue(movie)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MovieDAO", "Movie added successfully: " + movieId);
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MovieDAO", "Failed to add movie: " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                    });
        } else {
            Log.e("MovieDAO", "Failed to generate movie ID");
            if (callback != null) {
                callback.onFailure("Không thể tạo ID cho phim");
            }
        }
    }

    // Cập nhật thông tin Movie với ảnh Base64
    public void updateMovie(Movie movie, String imagePath, OnMovieOperationCallback callback) {
        if (movie.getMovieId() == null) {
            if (callback != null) {
                callback.onFailure("Movie ID không hợp lệ");
            }
            return;
        }

        // Chuyển ảnh thành Base64 nếu có
        if (imagePath != null && !imagePath.isEmpty()) {
            String base64Image = convertImageToBase64(imagePath);
            if (base64Image != null) {
                movie.setImageBase64(base64Image);
            } else {
                callback.onFailure("Lỗi khi mã hóa ảnh thành Base64");
                return;
            }
        }

        Log.d("MovieDAO", "Updating movie with ID: " + movie.getMovieId());
        databaseReference.child(movie.getMovieId()).setValue(movie)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MovieDAO", "Movie updated successfully: " + movie.getMovieId());
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieDAO", "Failed to update movie: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    // Xóa Movie theo movieId
    public void deleteMovie(String movieId, OnMovieOperationCallback callback) {
        ShowTimeDAO showTimeDAO = new ShowTimeDAO();
        showTimeDAO.deleteShowTimesByMovieId(movieId, new ShowTimeDAO.OnShowTimeOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                databaseReference.child(movieId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("MovieDAO", "Xóa movie thành công");
                            if (callback != null) {
                                callback.onSuccess(null);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MovieDAO", "Xóa movie thất bại: " + e.getMessage());
                            if (callback != null) {
                                callback.onFailure(e.getMessage());
                            }
                        });
            }

            @Override
            public void onFailure(String error) {
                Log.e("MovieDAO", "Xóa ShowTime thất bại: " + error);
                if (callback != null) {
                    callback.onFailure("Không thể xóa ShowTime: " + error);
                }
            }
        });
    }

    // Lấy tất cả Movie
    public void getAllMovies(Consumer<List<Movie>> onSuccess, Consumer<String> onFailure) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Movie> movies = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Movie movie = data.getValue(Movie.class);
                    if (movie != null) {
                        movie.setMovieId(data.getKey());
                        movies.add(movie);
                    }
                }
                onSuccess.accept(movies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MovieDAO", "Lấy danh sách phim thất bại: " + error.getMessage());
                onFailure.accept(error.getMessage());
            }
        });
    }

    // Lấy Movie theo movieId
    public void getMovieById(String movieId, Consumer<Movie> onSuccess, Consumer<String> onFailure) {
        if (movieId == null || movieId.isEmpty()) {
            Log.e("MovieDAO", "movieId không hợp lệ, không thể lấy dữ liệu");
            onFailure.accept("movieId không hợp lệ");
            return;
        }

        databaseReference.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Movie movie = snapshot.getValue(Movie.class);
                if (movie != null) {
                    movie.setMovieId(snapshot.getKey());
                    onSuccess.accept(movie);
                } else {
                    onFailure.accept("Không tìm thấy phim với movieId: " + movieId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MovieDAO", "Lấy phim thất bại: " + error.getMessage());
                onFailure.accept(error.getMessage());
            }
        });
    }

    // Getter cho databaseReference
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}