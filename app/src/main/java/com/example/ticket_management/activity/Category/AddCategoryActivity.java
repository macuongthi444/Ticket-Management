package com.example.ticket_management.activity.Category;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.CategoryDAO;
import com.example.ticket_management.model.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCategoryActivity extends BaseActivity {

    private EditText edtCategoryName;
    private TextView tvTitle;
    private Button btnSave, btnCancel;
    private CategoryDAO categoryDAO;
    private Category categoryToUpdate; // Đối tượng category để chỉnh sửa
    private boolean isUpdateMode = false;
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_add_category;
    }
    @Override
    protected void initContent(View contentView) {

        // anh xa cac view
        tvTitle = findViewById(R.id.tvTitle);
        edtCategoryName = findViewById(R.id.edtCategoryName);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        categoryDAO = new CategoryDAO();
        // Khoi tao Firebase
        Intent intent = getIntent();
        if (intent.hasExtra("category")) {
            isUpdateMode = true;
            categoryToUpdate = (Category) intent.getSerializableExtra("category");
            if (categoryToUpdate != null) {
                // Điền dữ liệu hiện tại vào EditText
                edtCategoryName.setText(categoryToUpdate.getCategoryName());
                // Thay đổi giao diện cho chế độ Update
                tvTitle.setText("Cập nhật thể loại phim");
                btnSave.setText("Cập nhật");
            }
        } else {
            // Chế độ Add
            tvTitle.setText("Thêm mới thể loại phim");
            btnSave.setText("Lưu");
        }

        // Co the su dung lamda hoac cac thong thuong
        /*btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategory();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại CategoryActivity khi nhấn Cancel
            }
        });*/
        btnSave.setOnClickListener(v -> saveCategory());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Hãy nhập tên thể loại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdateMode && categoryToUpdate != null) {
            // Chế độ Update: Cập nhật category hiện tại
            categoryToUpdate.setCategoryName(categoryName);
            categoryDAO.updateCategory(categoryToUpdate);
            Toast.makeText(this, "Cập nhật thể loại thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Chế độ Add: Thêm category mới
            categoryName = edtCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                // Tạo đối tượng Category với tên nhập vào
                Category category = new Category(null, categoryName); // ID sẽ được sinh trong DAO
                // Gọi phương thức addCategory từ DAO
                categoryDAO.addCategory(category, AddCategoryActivity.this);
                Toast.makeText(this, "Đang lưu thể loại...", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại sau khi gọi DAO
            } else {
                Toast.makeText(this, "Hãy nhập tên thể loại!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

