package com.example.ticket_management.DAO;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.ticket_management.activity.Auth.LoginActivity;
import com.example.ticket_management.activity.Other.OtherActivity;
import com.example.ticket_management.config.FirebaseAuthManager;
import com.example.ticket_management.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class AuthDAO {
    private final FirebaseAuth firebaseAuth;

    public AuthDAO() {
        firebaseAuth = FirebaseAuthManager.getInstance();
    }
    public void registerUser(Context context, String email, String password, String fullName, String phone,String address,  User user) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        user.setUserId(firebaseUser.getUid());
                        user.setEmail(email);
                        user.setFullName(fullName);
                        user.setPhone(phone);
                        user.setAddress(address);
                        user.setRole(0);

                        // Lưu trữ thông tin người dùng vào Realtime Database
                        UserDAO userDAO = new UserDAO();
                        userDAO.addUser(user);

                        Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void loginUser(Context context, String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Lấy toàn bộ thông tin người dùng từ Realtime Database
                            UserDAO userDAO = new UserDAO();
                            userDAO.getUserById(userId, user -> {
                                if (user != null) {
                                    // Lưu thông tin người dùng vào SharedPreferences
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.putString("userId", user.getUserId());
                                    editor.putString("email", user.getEmail());
                                    editor.putString("fullName", user.getFullName());
                                    editor.putString("phone", user.getPhone());
                                    editor.putInt("role", user.getRole());
                                    editor.apply(); // Lưu thay đổi



                                    Toast.makeText(context, "Xin chào: " + user.getFullName(), Toast.LENGTH_SHORT).show();

                                    // Chuyển đến màn hình chính
                                    Intent intent = new Intent(context, OtherActivity.class);
                                    context.startActivity(intent);
                                } else {
                                    Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(context, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void changePassword(Context context, String oldPassword, String newPassword, String confirmPassword) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập trước khi đổi mật khẩu", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            return;
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra độ dài mật khẩu mới (Firebase yêu cầu tối thiểu 6 ký tự)
        if (newPassword.length() < 6) {
            Toast.makeText(context, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xác thực lại người dùng với mật khẩu cũ trước khi đổi
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // Nếu xác thực thành công, tiến hành đổi mật khẩu
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();

                                        // Đăng xuất người dùng và chuyển về màn hình đăng nhập
                                        logout(context);
                                    } else {
                                        Toast.makeText(context, "Đổi mật khẩu thất bại: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getUserInfoFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String userId = sharedPreferences.getString("userId", "");
            String email = sharedPreferences.getString("email", "");
            String fullName = sharedPreferences.getString("fullName", "");
            String phone = sharedPreferences.getString("phone", "");
            int role = sharedPreferences.getInt("role", -1);

            Log.d("SHARED_PREFS", "User ID: " + userId);
            Log.d("SHARED_PREFS", "Email: " + email);
            Log.d("SHARED_PREFS", "Full Name: " + fullName);
            Log.d("SHARED_PREFS", "Phone: " + phone);
            Log.d("SHARED_PREFS", "Role: " + role);

            // Hiển thị thông tin hoặc sử dụng trong ứng dụng
        } else {
            Log.d("SHARED_PREFS", "Người dùng chưa đăng nhập.");
        }
    }


    public void checkLoginStatus(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Người dùng đã đăng nhập, chuyển sang màn hình chính
            Intent intent = new Intent(context, OtherActivity.class);
            context.startActivity(intent);
        } else {
            // Người dùng chưa đăng nhập, giữ nguyên màn hình đăng nhập
        }
    }

    public void logout(Context context) {
        firebaseAuth.signOut();

        // Xóa trạng thái đăng nhập trong SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("email");
        editor.apply();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public void sendPasswordResetEmail(Context context, String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Email đặt lại mật khẩu đã được gửi", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Lỗi khi gửi email đặt lại mật khẩu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}

