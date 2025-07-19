package com.example.ticket_management.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ticket_management.R;
import com.example.ticket_management.config.DatabaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    private DatabaseReference transactionsRef;
    private DatabaseReference seatsRef;
    private DatabaseReference moviesRef;
    private DatabaseReference movieRoomsRef;
    private DatabaseReference transactionHistoryRef;
    private TextView movieTitleText, showInfoText, selectedSeatsText, totalPriceText, showDateText, showTimeText, roomNameText, seatTypeText, timeRemainingText;
    private Button paymentButton;
    private CountDownTimer countDownTimer;
    private String txnRef;
    private ArrayList<String> selectedSeats;
    private String movieID, showDate, showTime, roomID, seatType, poster;
    private int totalPrice;
    private ImageView moviePoster;
    private String movieName;
    private String userId;
    private String roomName;
    private static final long PAYMENT_TIMEOUT = 10 * 60 * 1000; // 10 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            Log.e("PaymentActivity", "User not logged in: userId is null in SharedPreferences");
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("PaymentActivity", "Current userId from SharedPreferences: " + userId);

        // Khởi tạo Firebase
        transactionsRef = DatabaseManager.getInstance().getReference("transactions");
        seatsRef = DatabaseManager.getInstance().getReference("seats");
        moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        movieRoomsRef = FirebaseDatabase.getInstance().getReference("movieRooms");
        transactionHistoryRef = DatabaseManager.getInstance().getReference("transactionHistory");

        // Ánh xạ các thành phần giao diện
        moviePoster = findViewById(R.id.movie_poster);
        movieTitleText = findViewById(R.id.movie_title);
        showInfoText = findViewById(R.id.show_info);
        selectedSeatsText = findViewById(R.id.selected_seats_payment);
        totalPriceText = findViewById(R.id.total_price_payment);
        showDateText = findViewById(R.id.show_date);
        showTimeText = findViewById(R.id.show_time);
        roomNameText = findViewById(R.id.room_name);
        seatTypeText = findViewById(R.id.seat_type);
        timeRemainingText = findViewById(R.id.time_remaining);
        paymentButton = findViewById(R.id.btn_payment);

        // Lấy dữ liệu từ Intent
        selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        seatType = getIntent().getStringExtra("seatType");
        movieID = getIntent().getStringExtra("movieID");
        showDate = getIntent().getStringExtra("showDate");
        showTime = getIntent().getStringExtra("showTime");
        roomID = getIntent().getStringExtra("roomID");
        poster = getIntent().getStringExtra("poster");

        // Log dữ liệu nhận được
        Log.d("PaymentActivity", "MovieID: " + movieID + ", ShowDate: " + showDate + ", ShowTime: " + showTime + ", RoomID: " + roomID + ", SelectedSeats: " + selectedSeats + ", TotalPrice: " + totalPrice);

        // Hiển thị poster
        if (poster != null && !poster.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(poster, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                moviePoster.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Log.e("PaymentActivity", "Error decoding poster: " + e.getMessage());
                moviePoster.setImageResource(R.drawable.img_background);
            }
        } else {
            Log.w("PaymentActivity", "Poster is null or empty");
            moviePoster.setImageResource(R.drawable.img_background);
        }

        // Hiển thị thông tin ghế và tổng tiền
        if (selectedSeats != null && !selectedSeats.isEmpty()) {
            selectedSeatsText.setText("Ghế đã chọn: " + String.join(", ", selectedSeats));
        } else {
            selectedSeatsText.setText("Ghế đã chọn: Không có");
            Toast.makeText(this, "Không có ghế được chọn", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        totalPriceText.setText("Tổng tiền: " + String.format("%,dđ", totalPrice));
        seatTypeText.setText(seatType != null ? seatType : "Không xác định");

        // Hiển thị ngày và giờ chiếu
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            showDateText.setText(outputFormat.format(inputFormat.parse(showDate)));
        } catch (Exception e) {
            showDateText.setText(showDate);
            Log.e("PaymentActivity", "Error formatting date: " + e.getMessage());
        }
        showTimeText.setText(showTime);

        // Lấy tên phim từ bảng movies
        fetchMovieName();

        // Lấy tên phòng chiếu từ bảng movieRooms và lưu vào biến roomName
        fetchRoomName();

        // Tạo mã giao dịch duy nhất
        txnRef = "TXN_" + System.currentTimeMillis();
        saveTransactionToFirebase(txnRef, totalPrice, selectedSeats, "pending");

        // Cập nhật thời gian còn lại (10 phút)
        startTimer(PAYMENT_TIMEOUT);

        // Xử lý sự kiện nhấn nút Thanh toán
        paymentButton.setOnClickListener(v -> confirmPayment());
    }

    private void fetchMovieName() {
        if (movieID == null) {
            Log.e("PaymentActivity", "MovieID is null");
            movieTitleText.setText("Không tìm thấy tên phim");
            return;
        }

        moviesRef.child(movieID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    movieName = snapshot.child("movieName").getValue(String.class);
                    if (movieName != null) {
                        movieTitleText.setText(movieName);
                        showInfoText.setText("2D Phụ đề | " + showTime + " " + showDate);
                    } else {
                        movieTitleText.setText("Không tìm thấy tên phim");
                        showInfoText.setText("2D Phụ đề | " + showTime + " " + showDate);
                    }
                } else {
                    Log.e("PaymentActivity", "Movie not found for movieID: " + movieID);
                    movieTitleText.setText("Không tìm thấy tên phim");
                    showInfoText.setText("2D Phụ đề | " + showTime + " " + showDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PaymentActivity", "Error fetching movie: " + error.getMessage());
                movieTitleText.setText("Lỗi tải tên phim");
            }
        });
    }

    private void fetchRoomName() {
        if (roomID == null) {
            Log.e("PaymentActivity", "RoomID is null");
            roomNameText.setText("Không tìm thấy phòng chiếu");
            roomName = "Không tìm thấy phòng chiếu";
            return;
        }

        movieRoomsRef.child(roomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    roomName = snapshot.child("roomName").getValue(String.class);
                    roomNameText.setText(roomName != null ? roomName : "P" + roomID);
                    if (roomName == null) {
                        roomName = "P" + roomID;
                    }
                } else {
                    Log.e("PaymentActivity", "Room not found for roomID: " + roomID);
                    roomNameText.setText("P" + roomID);
                    roomName = "P" + roomID;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PaymentActivity", "Error fetching room: " + error.getMessage());
                roomNameText.setText("Lỗi tải tên phòng");
                roomName = "Lỗi tải tên phòng";
            }
        });
    }

    private void startTimer(long millisInFuture) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timeRemainingText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timeRemainingText.setText("00:00");
                Toast.makeText(PaymentActivity.this, "Hết thời gian! Vé đã bị hủy.", Toast.LENGTH_LONG).show();
                if (txnRef != null) {
                    transactionsRef.child(txnRef).removeValue();
                    if (selectedSeats != null) {
                        DatabaseReference specificSeatsRef = seatsRef
                                .child(roomID)
                                .child(showDate)
                                .child(showTime);
                        for (String seatId : selectedSeats) {
                            specificSeatsRef.child(seatId).child("status").setValue("empty");
                        }
                    }
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("payment_status", "failed");
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        }.start();
    }

    private void confirmPayment() {
        // Giả lập thanh toán thành công
        transactionsRef.child(txnRef).child("status").setValue("success");
        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

        // Cập nhật trạng thái ghế thành "sold"
        if (selectedSeats != null) {
            DatabaseReference specificSeatsRef = seatsRef
                    .child(roomID)
                    .child(showDate)
                    .child(showTime);
            for (String seatId : selectedSeats) {
                specificSeatsRef.child(seatId).child("status").setValue("sold");
            }
        }

        // Lưu lịch sử giao dịch
        if (movieName != null && showDate != null && showTime != null) {
            saveTransactionHistory(txnRef, movieName, showDate, showTime, totalPrice);
        } else {
            Log.e("PaymentActivity", "Cannot save transaction history: Missing movieName, showDate, or showTime");
            Toast.makeText(this, "Không thể lưu lịch sử giao dịch: Thiếu thông tin", Toast.LENGTH_LONG).show();
        }

        // Trả kết quả và chuyển sang TransactionHistoryActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_status", "success");
        setResult(RESULT_OK, resultIntent);
        Intent historyIntent = new Intent(PaymentActivity.this, TransactionHistoryActivity.class);
        startActivity(historyIntent);
        finish();
    }

    private void saveTransactionToFirebase(String txnRef, int totalPrice, ArrayList<String> selectedSeats, String status) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("totalPrice", totalPrice);
        transaction.put("selectedSeats", selectedSeats);
        transaction.put("status", status);
        transaction.put("timestamp", System.currentTimeMillis());
        transactionsRef.child(txnRef).setValue(transaction);
    }

    private void saveTransactionHistory(String txnRef, String movieName, String showDate, String showTime, int totalPrice) {
        if (userId == null) {
            Log.e("PaymentActivity", "Cannot save transaction history: userId is null");
            return;
        }

        Map<String, Object> history = new HashMap<>();
        history.put("movieName", movieName);
        history.put("showDate", showDate);
        history.put("showTime", showTime);
        history.put("totalPrice", totalPrice);
        history.put("timestamp", System.currentTimeMillis());
        history.put("roomName", roomName);
        history.put("selectedSeats", selectedSeats);
        history.put("poster", poster);

        transactionHistoryRef.child(userId).child(txnRef).setValue(history)
                .addOnSuccessListener(aVoid -> Log.d("PaymentActivity", "Transaction history saved successfully for userId: " + userId))
                .addOnFailureListener(e -> Log.e("PaymentActivity", "Error saving transaction history: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}