package com.example.ticket_management.DAO;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ticket_management.config.DatabaseManager;
import com.example.ticket_management.model.ShowTime;
import com.example.ticket_management.util.GuidService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowTimeDAO {
    private final DatabaseReference databaseReference;
    private final DatabaseReference seatsRef;

    public ShowTimeDAO() {
        databaseReference = DatabaseManager.getInstance().getReference("showTimes");
        seatsRef = DatabaseManager.getInstance().getReference("seats");
    }

    public interface OnShowTimeOperationCallback {
        void onSuccess(Object result);
        void onFailure(String error);
    }

    public void getAllShowTimes(OnShowTimeOperationCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ShowTime> showTimes = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ShowTime showTime = data.getValue(ShowTime.class);
                    if (showTime != null) {
                        showTime.setShowtimeId(data.getKey());
                        showTimes.add(showTime);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(showTimes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowTimeDAO", "Lấy danh sách lịch chiếu thất bại: " + error.getMessage());
                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        });
    }

    // Hàm kiểm tra trùng suất chiếu
    public void checkDuplicateShowTime(String roomId, String showDate, String showTime, String currentShowtimeId, OnShowTimeOperationCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isDuplicate = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    ShowTime existingShowTime = data.getValue(ShowTime.class);
                    if (existingShowTime != null) {
                        String existingShowtimeId = data.getKey();
                        // Kiểm tra trùng roomId, showDate, showTime, nhưng bỏ qua chính suất chiếu đang cập nhật
                        if (existingShowTime.getRoomId().equals(roomId) &&
                                existingShowTime.getShowDate().equals(showDate) &&
                                existingShowTime.getShowTime().equals(showTime) &&
                                (currentShowtimeId == null || !existingShowtimeId.equals(currentShowtimeId))) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
                if (callback != null) {
                    callback.onSuccess(isDuplicate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowTimeDAO", "Kiểm tra trùng suất chiếu thất bại: " + error.getMessage());
                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        });
    }
    public void addShowTime(ShowTime showTime, OnShowTimeOperationCallback callback) {
        // Generate a unique ID for the showtime
        String showtimeId = GuidService.generateGuid();
        showTime.setShowtimeId(showtimeId);

        databaseReference.child(showtimeId).setValue(showTime)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ShowTimeDAO", "Thêm lịch chiếu thành công: " + showTime.getShowTime() + " - " + showTime.getShowDate());
                    // Tạo dữ liệu ghế mặc định cho suất chiếu mới
                    createDefaultSeatsForShowtime(showTime.getRoomId(), showTime.getShowDate(), showTime.getShowTime(), showtimeId);
                    if (callback != null) {
                        callback.onSuccess(showtimeId); // Trả về showtimeId
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ShowTimeDAO", "Thêm lịch chiếu thất bại: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void updateShowTime(ShowTime showTime, OnShowTimeOperationCallback callback) {
        // Lưu thông tin cũ để so sánh
        DatabaseReference showTimeRef = databaseReference.child(showTime.getShowtimeId());
        showTimeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                ShowTime oldShowTime = task.getResult().getValue(ShowTime.class);
                if (oldShowTime != null) {
                    String oldRoomId = oldShowTime.getRoomId();
                    String oldShowDate = oldShowTime.getShowDate();
                    String oldShowTimeValue = oldShowTime.getShowTime();

                    // Cập nhật suất chiếu
                    showTimeRef.setValue(showTime)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ShowTimeDAO", "Cập nhật lịch chiếu thành công: " + showTime.getShowTime() + " - " + showTime.getShowDate());
                                // Kiểm tra xem roomId, showDate, hoặc showTime có thay đổi không
                                if (!oldRoomId.equals(showTime.getRoomId()) ||
                                        !oldShowDate.equals(showTime.getShowDate()) ||
                                        !oldShowTimeValue.equals(showTime.getShowTime())) {
                                    // Di chuyển dữ liệu ghế từ node cũ sang node mới
                                    moveSeatsData(oldRoomId, oldShowDate, oldShowTimeValue,
                                            showTime.getRoomId(), showTime.getShowDate(), showTime.getShowTime(),
                                            showTime.getShowtimeId());
                                }
                                if (callback != null) {
                                    callback.onSuccess(null);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ShowTimeDAO", "Cập nhật lịch chiếu thất bại: " + e.getMessage());
                                if (callback != null) {
                                    callback.onFailure(e.getMessage());
                                }
                            });
                } else {
                    if (callback != null) {
                        callback.onFailure("Không tìm thấy suất chiếu để cập nhật");
                    }
                }
            } else {
                if (callback != null) {
                    callback.onFailure("Lỗi khi lấy dữ liệu suất chiếu cũ: " + task.getException().getMessage());
                }
            }
        });
    }
    public void deleteShowTime(String showtimeId, OnShowTimeOperationCallback callback) {
        // Lấy thông tin suất chiếu trước khi xóa
        databaseReference.child(showtimeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                ShowTime showTime = task.getResult().getValue(ShowTime.class);
                if (showTime != null) {
                    String roomId = showTime.getRoomId();
                    String showDate = showTime.getShowDate();
                    String showTimeValue = showTime.getShowTime(); // Đổi tên biến để tránh xung đột

                    // Xóa suất chiếu
                    databaseReference.child(showtimeId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ShowTimeDAO", "Xóa lịch chiếu thành công: " + showtimeId);
                                // Xóa dữ liệu ghế tương ứng
                                deleteSeatsData(roomId, showDate, showTimeValue);
                                if (callback != null) {
                                    callback.onSuccess(null);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ShowTimeDAO", "Xóa lịch chiếu thất bại: " + e.getMessage());
                                if (callback != null) {
                                    callback.onFailure(e.getMessage());
                                }
                            });
                } else {
                    if (callback != null) {
                        callback.onFailure("Không tìm thấy suất chiếu để xóa");
                    }
                }
            } else {
                if (callback != null) {
                    callback.onFailure("Lỗi khi lấy dữ liệu suất chiếu: " + task.getException().getMessage());
                }
            }
        });
    }

    public void deleteShowTimesByMovieId(String movieId, OnShowTimeOperationCallback callback) {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean hasShowTime = false;

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    ShowTime showTime = snapshot.getValue(ShowTime.class);
                    if (showTime != null && movieId.equals(showTime.getMovieId())) {
                        hasShowTime = true;
                        String showtimeId = snapshot.getKey();
                        String roomId = showTime.getRoomId();
                        String showDate = showTime.getShowDate();
                        String showTimeValue = showTime.getShowTime();

                        // Xóa suất chiếu
                        databaseReference.child(showtimeId).removeValue();
                        deleteSeatsData(roomId, showDate, showTimeValue);
                    }
                }

                if (callback != null) {
                    callback.onSuccess(null);
                }

            } else {
                if (callback != null) {
                    callback.onFailure("Lỗi khi lấy dữ liệu ShowTime: " + task.getException().getMessage());
                }
            }
        });
    }



    public void deleteShowTimesByRoomId(String roomId, OnShowTimeOperationCallback callback) {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean hasDeleted = false;
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    ShowTime showTime = snapshot.getValue(ShowTime.class);
                    if (showTime != null && roomId.equals(showTime.getRoomId())) {
                        String showtimeId = snapshot.getKey();
                        String showDate = showTime.getShowDate();
                        String showTimeValue = showTime.getShowTime();

                        // Xóa suất chiếu
                        databaseReference.child(showtimeId).removeValue();

                        // Xóa dữ liệu ghế
                        deleteSeatsData(roomId, showDate, showTimeValue);
                        hasDeleted = true;
                    }
                }
                if (callback != null) {
                    if (hasDeleted) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure("Không tìm thấy suất chiếu nào thuộc phòng này.");
                    }
                }
            } else {
                if (callback != null) {
                    callback.onFailure("Lỗi khi lấy dữ liệu: " + task.getException().getMessage());
                }
            }
        });
    }
    private void createDefaultSeatsForShowtime(String roomId, String showDate, String showTime, String showtimeId) {
        DatabaseReference specificSeatsRef = seatsRef
                .child(roomId)
                .child(showDate)
                .child(showTime);

        // Lưu showtimeId để liên kết với suất chiếu
        specificSeatsRef.child("showtimeId").setValue(showtimeId);

        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        for (char row : rows) {
            if (row != 'I' && row != 'J') {
                for (int num = 1; num <= 10; num++) {
                    int price = (row == 'A' || row == 'B' || row == 'C') ? 65000 : 70000;
                    String status = "empty";
                    String seatId = row + String.valueOf(num);

                    Map<String, Object> seatData = new HashMap<>();
                    seatData.put("row", String.valueOf(row));
                    seatData.put("number", num);
                    seatData.put("status", status);
                    seatData.put("price", price);

                    specificSeatsRef.child(seatId).setValue(seatData);
                }
            } else {
                for (int num = 1; num <= 5; num++) {
                    int price = 140000;
                    String status = "empty";
                    String seatId = row + String.valueOf(num);

                    Map<String, Object> seatData = new HashMap<>();
                    seatData.put("row", String.valueOf(row));
                    seatData.put("number", num);
                    seatData.put("status", status);
                    seatData.put("price", price);

                    specificSeatsRef.child(seatId).setValue(seatData);
                }
            }
        }
    }

    private void moveSeatsData(String oldRoomId, String oldShowDate, String oldShowTime,
                               String newRoomId, String newShowDate, String newShowTime, String showtimeId) {
        DatabaseReference oldSeatsRef = seatsRef
                .child(oldRoomId)
                .child(oldShowDate)
                .child(oldShowTime);

        DatabaseReference newSeatsRef = seatsRef
                .child(newRoomId)
                .child(newShowDate)
                .child(newShowTime);

        oldSeatsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Copy dữ liệu ghế từ node cũ sang node mới
                newSeatsRef.setValue(task.getResult().getValue()).addOnCompleteListener(copyTask -> {
                    if (copyTask.isSuccessful()) {
                        // Cập nhật showtimeId trong node mới
                        newSeatsRef.child("showtimeId").setValue(showtimeId);
                        // Xóa node cũ sau khi copy thành công
                        oldSeatsRef.removeValue();
                    }
                });
            } else {
                // Nếu không có dữ liệu ghế ở node cũ, tạo dữ liệu ghế mới
                createDefaultSeatsForShowtime(newRoomId, newShowDate, newShowTime, showtimeId);
            }
        });
    }

    private void deleteSeatsData(String roomId, String showDate, String showTime) {
        DatabaseReference specificSeatsRef = seatsRef
                .child(roomId)
                .child(showDate)
                .child(showTime);

        specificSeatsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("ShowTimeDAO", "Xóa dữ liệu ghế thành công: " + roomId + "/" + showDate + "/" + showTime);
            } else {
                Log.e("ShowTimeDAO", "Xóa dữ liệu ghế thất bại: " + task.getException().getMessage());
            }
        });
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}