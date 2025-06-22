package com.example.ticket_management.DAO;


import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.ticket_management.config.DatabaseManager;
import com.example.ticket_management.model.User;
import com.example.ticket_management.util.GuidService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final DatabaseReference databaseReference;

    public UserDAO() {
        databaseReference = DatabaseManager.getInstance().getReference("users");
    }

    public void addUser(User user) {
        databaseReference.child(user.getUserId()).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserDAO", "Thêm người dùng thành công: " + user.getUserName());
                })
                .addOnFailureListener(e -> {
                    Log.e("UserDAO", "Thêm người dùng thất bại: " + e.getMessage());
                });
    }

    public void updateUser(User user, OnUserOperationListener listener) {
        if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
            if (listener != null) {
                listener.onFailure(new IllegalArgumentException("User hoặc userId không được null hoặc rỗng"));
            }
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", user.getFullName());
        updates.put("email", user.getEmail());
        updates.put("phone", user.getPhone());
        updates.put("address", user.getAddress());


        databaseReference.child(user.getUserId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật người dùng thành công: " + user.getUserName());
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Cập nhật người dùng thất bại: " + e.getMessage());
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public void deleteUser(String userId) {
        databaseReference.child(userId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserDAO", "Xóa người dùng thành công: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserDAO", "Xóa người dùng thất bại: " + e.getMessage());
                });
    }
    public void fetchUserById(String userId, OnUserRetrievedListener listener) {
        if (userId == null || userId.isEmpty()) {
            if (listener != null) {
                listener.onUserRetrieved(null);
            }
            return;
        }

        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().getValue(User.class);
                Log.d(TAG, "Lấy thông tin người dùng thành công: " + userId);
                if (listener != null) {
                    listener.onUserRetrieved(user);
                }
            } else {
                Log.e(TAG, "Không tìm thấy người dùng với userId: " + userId);
                if (listener != null) {
                    listener.onUserRetrieved(null);
                }
            }
        });
    }
    public void getUserById(String userId, OnUserRetrievedListener listener) {
        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().getValue(User.class);
                listener.onUserRetrieved(user);
            } else {
                listener.onUserRetrieved(null);
            }
        });
    }

    // Interface để xử lý callback khi lấy được thông tin người dùng
    public interface OnUserOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnUserRetrievedListener {
        void onUserRetrieved(User user);
    }
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
