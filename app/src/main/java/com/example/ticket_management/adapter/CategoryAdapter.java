package com.example.ticket_management.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.Category.AddCategoryActivity;
import com.example.ticket_management.DAO.CategoryDAO;
import com.example.ticket_management.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList != null ? categoryList : java.util.Collections.emptyList();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryId.setText("Mã thể loại: " + (position + 1));
        holder.tvCategoryName.setText("Tên thể loại: " + category.getCategoryName());

        holder.imgMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.imgMenu);
            popupMenu.getMenu().add("Chỉnh sửa");
            popupMenu.getMenu().add("Xóa");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Chỉnh sửa")) {
                    Intent intent = new Intent(context, AddCategoryActivity.class);
                    intent.putExtra("category", category);
                    context.startActivity(intent);
                } else if (item.getTitle().equals("Xóa")) {
                    CategoryDAO categoryDAO = new CategoryDAO();
                    categoryDAO.deleteCategory(category.getCategoryId());
                    categoryList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryId, tvCategoryName;
        ImageView imgMenu;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryId = itemView.findViewById(R.id.tvCategoryId);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            imgMenu = itemView.findViewById(R.id.imgMenu);
        }
    }

    public void updateData(List<Category> newCategories) {
        categoryList.clear();
        if (newCategories != null) {
            categoryList.addAll(newCategories);
        }
        notifyDataSetChanged();
    }
}