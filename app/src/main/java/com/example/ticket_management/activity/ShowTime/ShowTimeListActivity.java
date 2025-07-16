
package com.example.ticket_management.activity.ShowTime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.adapter.ShowTimeAdapter;
import com.example.ticket_management.DAO.ShowTimeDAO;
import com.example.ticket_management.model.ShowTime;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ShowTimeListActivity extends BaseActivity {

    private RecyclerView rvShowTimes;
    private ShowTimeDAO showTimeDAO;
    private List<ShowTime> showTimeList;
    private ShowTimeAdapter adapter;
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_show_time_list;
    }

    @Override
    protected void initContent(View contentView) {

        rvShowTimes = findViewById(R.id.rvShowTimes);
        showTimeDAO = new ShowTimeDAO();
        showTimeList = new ArrayList<>();
        adapter = new ShowTimeAdapter(this, showTimeList);

        rvShowTimes.setLayoutManager(new LinearLayoutManager(this));
        rvShowTimes.setAdapter(adapter);

        fetchShowTimes();

        FloatingActionButton fabAddShowTime = findViewById(R.id.fabAddShowTime);
        fabAddShowTime.setOnClickListener(v -> {
            Intent intent = new Intent(ShowTimeListActivity.this, AddShowTimeActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            fetchShowTimes();
        }
    }

    public void fetchShowTimes() {
        showTimeDAO.getAllShowTimes(new ShowTimeDAO.OnShowTimeOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    showTimeList.clear();
                    showTimeList.addAll((List<ShowTime>) result);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ShowTimeListActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}