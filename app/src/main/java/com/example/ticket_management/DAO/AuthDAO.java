package com.example.ticket_management.DAO;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.ticket_management.activity.Auth.LoginActivity;
import com.example.ticket_management.activity.Category.CategoryActivity;
import com.example.ticket_management.activity.Other.OtherActivity;
import com.example.ticket_management.activity.SelectionShowtimesActivity;
import com.example.ticket_management.activity.ShowTime.ShowtimeUserListActivity;
import com.example.ticket_management.activity.StatisticActivity;
import com.example.ticket_management.config.FirebaseAuthManager;
import com.example.ticket_management.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthDAO {
    private final FirebaseAuth firebaseAuth;

    public AuthDAO() {
        firebaseAuth = FirebaseAuthManager.getInstance();
    }
    public void registerUser(Context context, String email, String password, String fullName, String phone, String address, User user) {
        if (password.length() < 6) {
            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            Toast.makeText(context, "Số điện thoại phải có đúng 10 số", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot emailSnapshot) {
                usersRef.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot phoneSnapshot) {
                        if (emailSnapshot.exists() || phoneSnapshot.exists()) {
                            Toast.makeText(context, "Email hoặc số điện thoại đã được đăng ký!", Toast.LENGTH_SHORT).show();
                            return;
                        }

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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(context, "Lỗi kết nối đến Firebase!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Lỗi kết nối đến Firebase!", Toast.LENGTH_SHORT).show();
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

                                    Log.d("AuthDAO", "Role saved: " + user.getRole());

                                    Toast.makeText(context, "Xin chào: " + user.getFullName(), Toast.LENGTH_SHORT).show();

                                    Intent intent;
                                    if (user.getRole() == 1) { // Role = 1 (admin) -> CategoryActivity
                                        intent = new Intent(context, StatisticActivity.class);
                                    } else { // Role = 0 (user) -> ShowListActivity
                                        intent = new Intent(context, ShowtimeUserListActivity.class);
                                    }
                                    context.startActivity(intent);
                                } else {
                                    Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        String errorMessage;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            errorMessage = "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "Email hoặc mật khẩu không chính xác.";
                        } catch (Exception e) {
                            errorMessage = "Đăng nhập thất bại: " + e.getMessage();
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "Email chưa được đăng ký trong hệ thống!", Toast.LENGTH_SHORT).show();
                    return;
                }

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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Lỗi kết nối đến Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }




    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
