package com.example.ticket_management.activity.Category;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.activity.MenuActivity;
import com.example.ticket_management.adapter.CategoryAdapter; // Sử dụng adapter từ package adapter
import com.example.ticket_management.DAO.CategoryDAO;
import com.example.ticket_management.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends BaseActivity {

    private RecyclerView recyclerViewCategories;
    private CategoryDAO categoryDAO;
    private List<Category> categoryList;
    private CategoryAdapter adapter;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_category;
    }

    @Override
    protected void initContent(View contentView) {
        recyclerViewCategories = contentView.findViewById(R.id.recyclerViewCategories);
        FloatingActionButton fabAddCategory = contentView.findViewById(R.id.fabAddCategory);

        categoryDAO = new CategoryDAO();
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(this, categoryList);

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(adapter);

        categoryDAO.getAllCategories(new CategoryDAO.OnDataFetchListener() {
            @Override
            public void onDataFetched(List<Category> categories) {
                if (categories != null) {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    adapter.updateData(categories);
                } else {
                    Toast.makeText(CategoryActivity.this, "Error when get data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fabAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, AddCategoryActivity.class);
            startActivity(intent);
        });
    }
}