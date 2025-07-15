package com.example.ticket_management.DAO;
import android.util.Log;

import com.example.ticket_management.config.DatabaseManager;
import com.example.ticket_management.model.MovieRoom;
import com.example.ticket_management.util.GuidService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.NonNull;

public class MovieRoomDAO {
    private final DatabaseReference databaseReference;

    // Khởi tạo databaseReference trong constructor
    public MovieRoomDAO() {

        databaseReference = DatabaseManager.getInstance().getReference("movieRooms");

    }

    public interface OnRoomOperationCallback {
        void onSuccess(Object result);
        void onFailure(String error);
    }

    public void getAllRooms(OnRoomOperationCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MovieRoom> rooms = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    MovieRoom room = data.getValue(MovieRoom.class);
                    if (room != null) {
                        room.setRoomId(data.getKey());
                        rooms.add(room);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(rooms);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RoomDAO", "Lấy danh sách phòng thất bại: " + error.getMessage());
                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        });
    }

    // Thêm một MovieRoom mới
    public void addMovieRoom(MovieRoom movieRoom) {
        // Tạo roomId duy nhất nếu chưa có
        if (movieRoom.getRoomId() == null || movieRoom.getRoomId().isEmpty()) {
            movieRoom.setRoomId(GuidService.generateGuid());
        }

        databaseReference.child(movieRoom.getRoomId()).setValue(movieRoom)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MovieRoomDAO", "Thêm phòng chiếu thành công: " + movieRoom.getRoomName());
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieRoomDAO", "Thêm phòng chiếu thất bại: " + e.getMessage());
                });
    }

    // Cập nhật thông tin MovieRoom
    public void updateMovieRoom(MovieRoom movieRoom) {
        if (movieRoom.getRoomId() == null || movieRoom.getRoomId().isEmpty()) {
            Log.e("MovieRoomDAO", "roomId không hợp lệ, không thể cập nhật");
            return;
        }

        databaseReference.child(movieRoom.getRoomId()).setValue(movieRoom)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MovieRoomDAO", "Cập nhật phòng chiếu thành công: " + movieRoom.getRoomName());
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieRoomDAO", "Cập nhật phòng chiếu thất bại: " + e.getMessage());
                });
    }

    // Xóa MovieRoom theo roomId
    public void deleteMovieRoom(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            Log.e("MovieRoomDAO", "roomId không hợp lệ, không thể xóa");
            return;
        }

        databaseReference.child(roomId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("MovieRoomDAO", "Xóa phòng chiếu thành công: " + roomId);
                })
                .addOnFailureListener(e -> {
                    Log.e("MovieRoomDAO", "Xóa phòng chiếu thất bại: " + e.getMessage());
                });
    }

//    public void getAllMovieRooms(Consumer<List<MovieRoom>> onSuccess, Consumer<String> onFailure) {
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<MovieRoom> movieRooms = new ArrayList<>();
//                for (DataSnapshot data : snapshot.getChildren()) {
//                    MovieRoom movieRoom = data.getValue(MovieRoom.class);
//                    if (movieRoom != null) {
//                        movieRooms.add(movieRoom);
//                    }
//                }
//                onSuccess.accept(movieRooms);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("MovieRoomDAO", "Lấy danh sách phòng chiếu thất bại: " + error.getMessage());
//                onFailure.accept(error.getMessage());
//            }
//        });
//    }

    // Lấy MovieRoom theo roomId
    public void getMovieRoomById(String roomId, Consumer<MovieRoom> onSuccess, Consumer<String> onFailure) {
        if (roomId == null || roomId.isEmpty()) {
            Log.e("MovieRoomDAO", "roomId không hợp lệ, không thể lấy dữ liệu");
            onFailure.accept("roomId không hợp lệ");
            return;
        }

        databaseReference.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MovieRoom movieRoom = snapshot.getValue(MovieRoom.class);
                if (movieRoom != null) {
                    onSuccess.accept(movieRoom);
                } else {
                    onFailure.accept("Không tìm thấy phòng chiếu với roomId: " + roomId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MovieRoomDAO", "Lấy phòng chiếu thất bại: " + error.getMessage());
                onFailure.accept(error.getMessage());
            }
        });
    }

    // Getter cho databaseReference (nếu cần truy cập từ bên ngoài)
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
