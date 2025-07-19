package com.example.ticket_management.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ticket_management.R;
import com.example.ticket_management.adapter.SeatAdapter;
import com.example.ticket_management.model.Seat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatSelectionActivity extends AppCompatActivity implements SeatAdapter.OnSeatClickListener {

    private RecyclerView recyclerView;
    private TextView selectedSeatsText;
    private TextView totalPriceText;
    private TextView timeRemainingText;
    private List<Seat> seatList;
    private SeatAdapter seatAdapter;
    private int totalPrice = 0;
    private DatabaseReference seatsRef;
    private Button continueButton;
    private static final int PAYMENT_REQUEST_CODE = 100;
    private ImageView movieBackground;
    private String movieID;
    private String showDate;
    private String showTime;
    private String poster;
    private String roomID;
    private CountDownTimer countDownTimer;
    private static final long SEAT_SELECTION_TIMEOUT = 5 * 60 * 1000; // 5 phút (300 giây)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_seat_selection);
//
//        // Nhận dữ liệu từ Intent
//        movieID = getIntent().getStringExtra("movieID");
//        showDate = getIntent().getStringExtra("showDate");
//        showTime = getIntent().getStringExtra("showTime");
//        poster = getIntent().getStringExtra("poster");
//        roomID = getIntent().getStringExtra("roomID");
//
//        recyclerView = findViewById(R.id.seat_grid);
//        selectedSeatsText = findViewById(R.id.selected_seats);
//        totalPriceText = findViewById(R.id.total_price);
//        timeRemainingText = findViewById(R.id.time_remaining);
//        continueButton = findViewById(R.id.btn_continue);
//        movieBackground = findViewById(R.id.movie_background);
//
//        // Hiển thị poster
//        if (poster != null && !poster.isEmpty()) {
//            Glide.with(this).load(poster).into(movieBackground);
//        } else {
//            movieBackground.setImageResource(R.drawable.img_background);
//            Log.e("SeatSelection", "Poster URL is null or empty");
//        }
//
//        // Khởi tạo Firebase
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        seatsRef = database.getReference("seats");
//
//        // Khởi tạo danh sách ghế
//        seatList = new ArrayList<>();
//
//        // Cấu hình RecyclerView
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 10);
//        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                Seat seat = seatList.get(position);
//                return (seat.getRow() == 'I' || seat.getRow() == 'J') ? 2 : 1;
//            }
//        });
//        recyclerView.setLayoutManager(gridLayoutManager);
//        recyclerView.setNestedScrollingEnabled(false);
//        seatAdapter = new SeatAdapter(seatList, this);
//        recyclerView.setAdapter(seatAdapter);
//
//        // Lấy dữ liệu từ Firebase
//        loadSeatsFromFirebase();
//
//        // Xử lý sự kiện nhấn nút Tiếp tục
//        continueButton.setOnClickListener(v -> {
//            List<String> selectedSeats = getSelectedSeats();
//            if (selectedSeats.isEmpty()) {
//                Toast.makeText(SeatSelectionActivity.this, "Vui lòng chọn ít nhất một ghế!", Toast.LENGTH_SHORT).show();
//            } else {
//                showAgeConfirmationDialog(selectedSeats);
//            }
//        });
//
//        // Bắt đầu đếm ngược 5 phút
//        startTimer();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại activity, xóa các ghế đang chọn và reset thời gian
        clearSelectedSeats();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đếm ngược khi activity không còn hiển thị
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đếm ngược khi activity bị hủy
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Xóa các ghế đang chọn trước khi quay lại trang trước
        clearSelectedSeats();
        super.onBackPressed(); // Tiếp tục hành động back mặc định
    }

    private void startTimer() {
        // Hủy đếm ngược cũ nếu có
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Bắt đầu đếm ngược 5 phút
        countDownTimer = new CountDownTimer(SEAT_SELECTION_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timeRemainingText.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timeRemainingText.setText("Thời gian còn lại: 00:00");
                Toast.makeText(SeatSelectionActivity.this, "Hết thời gian! Vui lòng chọn lại suất chiếu.", Toast.LENGTH_LONG).show();
                clearSelectedSeats();
                // Quay lại SelectionShowtimesActivity
                Intent intent = new Intent(SeatSelectionActivity.this, SelectionShowtimesActivity.class);
                intent.putExtra("movieID", movieID);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void clearSelectedSeats() {
        // Đặt lại trạng thái của các ghế "selected" về "empty"
        for (Seat seat : seatList) {
            if (seat.getStatus().equals("selected")) {
                seat.setStatus("empty");
                updateSeatInFirebase(seat);
            }
        }
        seatAdapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    private void loadSeatsFromFirebase() {
        if (roomID == null || showDate == null || showTime == null) {
            Log.e("SeatSelection", "Thông tin không hợp lệ: roomID, showDate hoặc showTime là null");
            Toast.makeText(this, "Thông tin suất chiếu không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference specificSeatsRef = seatsRef
                .child(roomID)
                .child(showDate)
                .child(showTime);

        Log.d("SeatSelection", "Truy vấn ghế: roomID=" + roomID + ", showDate=" + showDate + ", showTime=" + showTime);

        specificSeatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seatList.clear();

                if (!dataSnapshot.exists()) {
                    Log.d("Firebase", "No data found in 'seats' node");
                    Toast.makeText(SeatSelectionActivity.this, "Không có dữ liệu ghế, đang tải dữ liệu mặc định", Toast.LENGTH_SHORT).show();
                    initializeDefaultSeatsIfEmpty();
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String seatId = snapshot.getKey();
                    if (seatId == null || seatId.equals("showtimeId")) {
                        continue; // Bỏ qua node showtimeId
                    }

                    String row = snapshot.child("row").getValue(String.class);
                    Integer number = snapshot.child("number").getValue(Integer.class);
                    String status = snapshot.child("status").getValue(String.class);
                    Integer price = snapshot.child("price").getValue(Integer.class);

                    if (row != null && number != null && status != null && price != null) {
                        Seat seat = new Seat(row.charAt(0), number, status, price);
                        seatList.add(seat);
                    } else {
                        Log.w("Firebase", "Dữ liệu ghế không hợp lệ tại: " + seatId);
                    }
                }

                // Sắp xếp seatList theo row và number
                Collections.sort(seatList, (seat1, seat2) -> {
                    int rowCompare = Character.compare(seat1.getRow(), seat2.getRow());
                    if (rowCompare != 0) {
                        return rowCompare;
                    }
                    return Integer.compare(seat1.getNumber(), seat2.getNumber());
                });

                seatAdapter.notifyDataSetChanged();
                updateTotalPrice();
                Log.d("Firebase", "Loaded " + seatList.size() + " seats");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
                Toast.makeText(SeatSelectionActivity.this, "Lỗi kết nối Firebase: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeDefaultSeatsIfEmpty() {
        if (roomID == null || showDate == null || showTime == null) {
            Log.e("SeatSelection", "Không thể tạo dữ liệu ghế mặc định: roomID, showDate hoặc showTime là null");
            return;
        }

        DatabaseReference specificSeatsRef = seatsRef
                .child(roomID)
                .child(showDate)
                .child(showTime);

        specificSeatsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
                List<String> soldSeats = new ArrayList<>();

                List<String> selectedSeats = new ArrayList<>();

                for (char row : rows) {
                    if (row != 'I' && row != 'J') {
                        for (int num = 1; num <= 10; num++) {
                            int price = (row == 'A' || row == 'B' || row == 'C') ? 65000 : 70000;
                            String status;

                            String seatLabel = row + String.valueOf(num);
                            if (soldSeats.contains(seatLabel)) {
                                status = "sold";
                            } else if (selectedSeats.contains(seatLabel)) {
                                status = "selected";
                            } else {
                                status = "empty";
                            }

                            String seatId = row + String.valueOf(num);
                            specificSeatsRef.child(seatId).child("row").setValue(String.valueOf(row));
                            specificSeatsRef.child(seatId).child("number").setValue(num);
                            specificSeatsRef.child(seatId).child("status").setValue(status);
                            specificSeatsRef.child(seatId).child("price").setValue(price);
                        }
                    } else {
                        for (int num = 1; num <= 5; num++) {
                            int price = 140000;
                            String status;

                            String seatLabel = row + String.valueOf(num);
                            if (soldSeats.contains(seatLabel)) {
                                status = "sold";
                            } else if (selectedSeats.contains(seatLabel)) {
                                status = "selected";
                            } else {
                                status = "empty";
                            }

                            String seatId = row + String.valueOf(num);
                            specificSeatsRef.child(seatId).child("row").setValue(String.valueOf(row));
                            specificSeatsRef.child(seatId).child("number").setValue(num);
                            specificSeatsRef.child(seatId).child("status").setValue(status);
                            specificSeatsRef.child(seatId).child("price").setValue(price);
                        }
                    }
                }
                Toast.makeText(SeatSelectionActivity.this, "Đã tạo dữ liệu ghế mặc định", Toast.LENGTH_SHORT).show();
                Log.d("SeatSelection", "Đã tạo dữ liệu ghế mặc định tại: roomID=" + roomID + ", showDate=" + showDate + ", showTime=" + showTime);
            } else if (!task.isSuccessful()) {
                Log.e("SeatSelection", "Lỗi khi kiểm tra dữ liệu ghế mặc định: " + task.getException().getMessage());
            }
        });
    }

    @Override
    public void onSeatClicked(Seat seat, int position) {
        if (seat.getStatus().equals("empty")) {
            seat.setStatus("selected");
        } else if (seat.getStatus().equals("selected")) {
            seat.setStatus("empty");
        } else {
            return;
        }

        updateSeatInFirebase(seat);
        seatAdapter.notifyItemChanged(position);
        updateTotalPrice();
    }

    private void updateSeatInFirebase(Seat seat) {
        String seatId = seat.getRow() + String.valueOf(seat.getNumber());
        DatabaseReference specificSeatsRef = seatsRef
                .child(roomID)
                .child(showDate)
                .child(showTime);
        specificSeatsRef.child(seatId).child("status").setValue(seat.getStatus());
    }

    private void updateTotalPrice() {
        totalPrice = 0;
        List<String> selectedSeats = new ArrayList<>();

        for (Seat seat : seatList) {
            if (seat.getStatus().equals("selected")) {
                totalPrice += seat.getPrice();
                selectedSeats.add(seat.getRow() + String.valueOf(seat.getNumber()));
            }
        }

        selectedSeatsText.setText("Ghế đã chọn: " + (selectedSeats.isEmpty() ? "" : String.join(", ", selectedSeats)));
        totalPriceText.setText("Tổng tiền: " + totalPrice + "đ");
    }

    private List<String> getSelectedSeats() {
        List<String> selectedSeats = new ArrayList<>();
        for (Seat seat : seatList) {
            if (seat.getStatus().equals("selected")) {
                selectedSeats.add(seat.getRow() + String.valueOf(seat.getNumber()));
            }
        }
        return selectedSeats;
    }

//    private void showAgeConfirmationDialog(List<String> selectedSeats) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Xác nhận tuổi");
//        builder.setMessage("Tôi xác nhận mua vé cho người xem từ 18 tuổi trở lên theo quy định của cục điện ảnh. " +
//                "Tôi chắc chắn muốn mua " + selectedSeats.size() + " vé.");
//        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String seatType = determineSeatTypes(selectedSeats);
//
//                // Chuyển sang PaymentActivity với startActivityForResult
//                Intent intent = new Intent(SeatSelectionActivity.this, PaymentActivity.class);
//                intent.putStringArrayListExtra("selectedSeats", new ArrayList<>(selectedSeats));
//                intent.putExtra("totalPrice", totalPrice);
//                intent.putExtra("seatType", seatType);
//                intent.putExtra("movieID", movieID);
//                intent.putExtra("showDate", showDate);
//                intent.putExtra("showTime", showTime);
//                intent.putExtra("roomID", roomID);
//                intent.putExtra("poster", poster);
//                startActivityForResult(intent, PAYMENT_REQUEST_CODE);
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String paymentStatus = data.getStringExtra("payment_status");
                if ("success".equals(paymentStatus)) {
                    Toast.makeText(this, "Thanh toán thành công! Ghế đã được đặt.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Thanh toán thất bại!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Thanh toán bị hủy!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String determineSeatTypes(List<String> selectedSeats) {
        if (selectedSeats.isEmpty()) return "Không xác định";

        Set<String> seatTypeSet = new HashSet<>();
        for (String seat : selectedSeats) {
            char row = seat.charAt(0);
            if (row == 'I' || row == 'J') {
                seatTypeSet.add("Ghế đôi");
            } else if (row == 'A' || row == 'B' || row == 'C') {
                seatTypeSet.add("Ghế thường");
            } else {
                seatTypeSet.add("Ghế VIP");
            }
        }
        return String.join(", ", seatTypeSet);
    }
}