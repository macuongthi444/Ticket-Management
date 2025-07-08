package com.example.ticket_management.activity.Movie;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.CategoryDAO;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.model.Category;
import com.example.ticket_management.model.Movie;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddMovieActivity extends BaseActivity {

    private TextView tvReleaseDate;
    private EditText edtMovieName, edtDirector, edtDescription;
    private Button btnSave, btnCancel, btnSelectPoster;
    private ImageView ivPosterPreview;
    private TextView tvTitle;
    private Spinner spinnerCategory;
    private MovieDAO movieDAO;
    private CategoryDAO categoryDAO;
    private Movie movieToUpdate;
    private boolean isUpdateMode = false;
    private Uri selectedImageUri;
    private Date selectedReleaseDate;
    private List<Category> categoryList = new ArrayList<>();
    private static final int STORAGE_PERMISSION_CODE = 100;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this).load(selectedImageUri).into(ivPosterPreview);
                    } else {
                        Toast.makeText(this, "Không thể tải ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_add_movie;
    }

    @Override
    protected void initContent(View contentView) {
        tvTitle = findViewById(R.id.tvTitle);
        ivPosterPreview = findViewById(R.id.ivPosterPreview);
        btnSelectPoster = findViewById(R.id.btnSelectPoster);
        edtMovieName = findViewById(R.id.edtMovieName);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        edtDirector = findViewById(R.id.edtDirector);
        edtDescription = findViewById(R.id.edtDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        movieDAO = new MovieDAO();
        categoryDAO = new CategoryDAO();

        loadCategories();

        Intent intent = getIntent();
        if (intent.hasExtra("movie")) {
            isUpdateMode = true;
            movieToUpdate = (Movie) intent.getSerializableExtra("movie");
            if (movieToUpdate != null) {
                edtMovieName.setText(movieToUpdate.getMovieName());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvReleaseDate.setText(dateFormat.format(movieToUpdate.getReleaseDate()));
                selectedReleaseDate = movieToUpdate.getReleaseDate();
                edtDirector.setText(movieToUpdate.getDirector());
                edtDescription.setText(movieToUpdate.getDescription());
                tvTitle.setText("Cập Nhật Phim");
                btnSave.setText("Cập Nhật");
                if (movieToUpdate.getImageBase64() != null && !movieToUpdate.getImageBase64().isEmpty()) {
                    try {
                        byte[] decodedString = Base64.decode(movieToUpdate.getImageBase64(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedByte != null) {
                            ivPosterPreview.setImageBitmap(decodedByte);
                        } else {
                            ivPosterPreview.setImageResource(R.drawable.ic_default_movie);
                        }
                    } catch (Exception e) {
                        Log.e("AddMovieActivity", "Lỗi giải mã Base64: " + e.getMessage());
                        ivPosterPreview.setImageResource(R.drawable.ic_default_movie);
                    }
                }
                setSelectedCategory(movieToUpdate.getCategoryId());
            }
        } else {
            tvTitle.setText("Thêm Phim");
            btnSave.setText("Lưu");
        }

        tvReleaseDate.setOnClickListener(v -> showDatePickerDialog());

        btnSelectPoster.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                Intent intentPickImage = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intentPickImage);
            } else {
                requestStoragePermission();
            }
        });

        btnSave.setOnClickListener(v -> saveMovie());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCategories() {
        categoryDAO.getAllCategories(new CategoryDAO.OnDataFetchListener() {
            @Override
            public void onDataFetched(List<Category> categories) {
                if (categories != null) {
                    categoryList = categories;
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : categories) {
                        categoryNames.add(category.getCategoryName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddMovieActivity.this,
                            android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);

                    if (isUpdateMode && movieToUpdate != null) {
                        setSelectedCategory(movieToUpdate.getCategoryId());
                    }
                } else {
                    Toast.makeText(AddMovieActivity.this, "Lỗi tải danh sách thể loại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setSelectedCategory(String categoryId) {
        if (categoryList != null && categoryId != null) {
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getCategoryId().equals(categoryId)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intentPickImage = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intentPickImage);
            } else {
                Toast.makeText(this, "Quyền truy cập ảnh bị từ chối!", Toast.LENGTH_SHORT).show();
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
                    selectedReleaseDate = selectedDate.getTime();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvReleaseDate.setText(dateFormat.format(selectedReleaseDate));
                }, year, month, day);

        datePickerDialog.show();
    }

    private boolean isSaving = false;

    private void saveMovie() {
        if (isSaving) return; // Ngăn chặn nhấn nhiều lần

        String movieName = edtMovieName.getText().toString().trim();
        String director = edtDirector.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        int selectedPosition = spinnerCategory.getSelectedItemPosition();
        String categoryId = (selectedPosition >= 0 && selectedPosition < categoryList.size())
                ? categoryList.get(selectedPosition).getCategoryId()
                : "";

        if (movieName.isEmpty() || selectedReleaseDate == null || director.isEmpty() || categoryId.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        isSaving = true;
        btnSave.setEnabled(false); // Vô hiệu hóa nút "Lưu"

        String imageBase64 = null;
        if (selectedImageUri != null) {
            imageBase64 = convertImageToBase64(selectedImageUri);
            if (imageBase64 == null) {
                Toast.makeText(this, "Lỗi khi chuyển ảnh sang Base64!", Toast.LENGTH_SHORT).show();
                isSaving = false;
                btnSave.setEnabled(true);
                return;
            }
        } else if (isUpdateMode && movieToUpdate != null && movieToUpdate.getImageBase64() != null) {
            imageBase64 = movieToUpdate.getImageBase64();
        } else {
            Toast.makeText(this, "Vui lòng chọn ảnh poster!", Toast.LENGTH_SHORT).show();
            isSaving = false;
            btnSave.setEnabled(true);
            return;
        }

        saveMovieToDatabase(movieName, selectedReleaseDate, director, description, categoryId, imageBase64);
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            // Đọc InputStream từ Uri
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("AddMovieActivity", "Không thể mở InputStream từ Uri");
                return null;
            }

            // Giải mã ảnh thành Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e("AddMovieActivity", "Bitmap null sau khi giải mã");
                return null;
            }

            // Nén ảnh để giảm kích thước
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // Nén với chất lượng 80%
            byte[] byteArray = baos.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("AddMovieActivity", "Lỗi khi chuyển ảnh sang Base64: " + e.getMessage());
            return null;
        }
    }

    private void saveMovieToDatabase(String movieName, Date releaseDate, String director, String description, String categoryId, String imageBase64) {
        if (isUpdateMode && movieToUpdate != null) {
            movieToUpdate.setMovieName(movieName);
            movieToUpdate.setReleaseDate(releaseDate);
            movieToUpdate.setDirector(director);
            movieToUpdate.setDescription(description);
            movieToUpdate.setCategoryId(categoryId);
            movieToUpdate.setImageBase64(imageBase64);
            movieDAO.updateMovie(movieToUpdate, null, new MovieDAO.OnMovieOperationCallback() {
                @Override
                public void onSuccess(Object result) {
                    runOnUiThread(() -> {
                        Log.d("AddMovieDebug", "Cập nhật thành công");
                        Toast.makeText(AddMovieActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddMovieActivity.this, "Cập nhật thất bại: " + error, Toast.LENGTH_SHORT).show();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }
            });
        } else {
            Movie newMovie = new Movie();
            newMovie.setMovieId(null);
            newMovie.setMovieName(movieName);
            newMovie.setReleaseDate(releaseDate);
            newMovie.setDirector(director);
            newMovie.setDescription(description);
            newMovie.setCategoryId(categoryId);
            newMovie.setImageBase64(imageBase64);
            movieDAO.addMovie(newMovie, null, new MovieDAO.OnMovieOperationCallback() {
                @Override
                public void onSuccess(Object result) {
                    runOnUiThread(() -> {
                        Log.d("AddMovieDebug", "Thêm phim thành công");
                        Toast.makeText(AddMovieActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddMovieActivity.this, "Thêm thất bại: " + error, Toast.LENGTH_SHORT).show();
                        isSaving = false;
                        btnSave.setEnabled(true);
                    });
                }
            });
        }
    }
}