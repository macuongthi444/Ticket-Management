package com.example.ticket_management.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.Category.CategoryActivity;
import com.example.ticket_management.activity.Movie.MovieListActivity;
import com.example.ticket_management.activity.Other.OtherActivity;
import com.example.ticket_management.DAO.AuthDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private int role;
    private static final String PREF_SELECTED_ITEM = "selected_bottom_nav_item";
    private AuthDAO authDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo AuthDAO
        authDAO = new AuthDAO();

        // Lấy role từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        role = sharedPreferences.getInt("role", 0);

        // Thiết lập layout dựa trên role
        if (role == 1) {
            setContentView(R.layout.activity_menu); // Layout cho admin (Navigation Drawer)
            setupNavigationDrawer();
        } else {
            setContentView(R.layout.activity_bottom); // Layout cho user (BottomNavigationView)
            setupBottomNavigation();
        }

        // Tìm FrameLayout frmHienthi
        FrameLayout frmHienthi = findViewById(R.id.frmHienthi);

        // Inflate layout nội dung của Activity con
        View contentView = getLayoutInflater().inflate(getContentLayoutId(), frmHienthi, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        frmHienthi.addView(contentView, params);

        // Khởi tạo nội dung của Activity con
        initContent(contentView);
    }

    // Phương thức trừu tượng để Activity con cung cấp layout nội dung
    protected abstract int getContentLayoutId();

    // Phương thức trừu tượng để Activity con khởi tạo nội dung
    protected abstract void initContent(View contentView);

    // Thiết lập Navigation Drawer (cho admin)
    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Hiển thị thông tin người dùng trong Navigation Drawer
        displayUserInfo();

        findViewById(R.id.toolbar).setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
        );
    }

    // Hiển thị thông tin người dùng trong Navigation Drawer
    private void displayUserInfo() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            // Lấy thông tin từ SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            String fullName = sharedPreferences.getString("fullName", "Tên người dùng");
            String email = sharedPreferences.getString("email", "email@example.com");

            // Lấy header view của Navigation Drawer
            View headerView = navigationView.getHeaderView(0);
            TextView tvFullName = headerView.findViewById(R.id.tv_full_name);
            TextView tvEmail = headerView.findViewById(R.id.tv_email);

            // Hiển thị thông tin
            tvFullName.setText("Xin chào, " + fullName);
            tvEmail.setText(email);
        }
    }

    // Xác định tab tương ứng với Activity hiện tại
//    private int getSelectedItemIdForCurrentActivity() {
//        if (this instanceof TransactionHistoryActivity) {
//            return R.id.hoadon; // Tab "Hóa Đơn"
//        } else if (this instanceof OtherActivity) {
//            return R.id.khac; // Tab "Khác"
//        }
//        return R.id.hoadon; // Mặc định là "Hóa Đơn"
//    }

    // Thiết lập BottomNavigationView (cho user)
    private void setupBottomNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.btnNavigation);

        // Đặt tab được chọn dựa trên Activity hiện tại
//        bottomNavigationView.setSelectedItemId(getSelectedItemIdForCurrentActivity());

        // Xử lý sự kiện khi người dùng nhấn vào tab
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Lưu trạng thái của mục được chọn
            SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_SELECTED_ITEM, id);
            editor.apply();

//            if (id == R.id.hoadon) {
//                openActivity(TransactionHistoryActivity.class);
//            } else if (id == R.id.khac) {
//                openActivity(OtherActivity.class);
//            }

            return true;
        });
    }

    // Xử lý Navigation Drawer (cho admin)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.Category) {
            openActivity(CategoryActivity.class);
        } else if (id == R.id.MovieList) {
            openActivity(MovieListActivity.class);
        } else
//            if (id == R.id.TKDT) {
//            openActivity(StatisticActivity.class);
//        } else
            if (id == R.id.Logout) {
            AuthDAO authDAO = new AuthDAO();
            authDAO.logout(BaseActivity.this);
        }

        drawerLayout.closeDrawers();
        return true;
    }

    protected void openActivity(Class<?> activityClass) {
        // Chỉ mở Activity nếu không phải là Activity hiện tại
        if (!this.getClass().equals(activityClass)) {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
            finish(); // Kết thúc Activity hiện tại để tránh chồng chất
        }
    }

    public void logout() {
        authDAO.logout(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}