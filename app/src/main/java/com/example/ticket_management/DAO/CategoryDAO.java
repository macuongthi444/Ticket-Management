package com.example.ticket_management.DAO;

import android.content.Context;
import android.util.Log;

import com.example.ticket_management.config.DatabaseManager;
import com.example.ticket_management.model.Category;
import com.example.ticket_management.util.GuidService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CategoryDAO {
    private final DatabaseReference databaseReference;

    public CategoryDAO() {
        databaseReference = DatabaseManager.getInstance().getReference("categories");
    }

    public interface OnDataFetchListener {
        void onDataFetched(List<Category> categories);
    }
    public void getAllCategories(OnDataFetchListener listener) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                listener.onDataFetched(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("CategoryDAO", "Lỗi khi lấy dữ liệu: " + databaseError.getMessage());
                listener.onDataFetched(null);
            }
        });
    }
    public void addCategory(Category category, Context context) {
        // Generate a unique ID for the category
        category.setCategoryId(GuidService.generateGuid());
        databaseReference.child(category.getCategoryId()).setValue(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CategoryDAO", "Insert thành công: " + category.getCategoryName());
                    Toast.makeText(context, "Thêm thể loại thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryDAO", "Insert thất bại: " + e.getMessage());
                    Toast.makeText(context, "Thêm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void updateCategory(Category category) {
        databaseReference.child(category.getCategoryId()).setValue(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CategoryDAO", "Cập nhật thành công: " + category.getCategoryName());
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryDAO", "Cập nhật thất bại: " + e.getMessage());
                });
    }




    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}

