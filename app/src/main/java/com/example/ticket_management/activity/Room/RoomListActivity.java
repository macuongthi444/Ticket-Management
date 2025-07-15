package com.example.ticket_management.activity.Room;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.adapter.MovieRoomAdapter;
import com.example.ticket_management.DAO.MovieRoomDAO;
import com.example.ticket_management.model.MovieRoom;

import java.util.List;

public class RoomListActivity extends BaseActivity {
    private MovieRoomDAO roomDao;

    ListView lv_room;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_room_list;
    }

    @Override
    protected void initContent(View contentView) {

        roomDao = new MovieRoomDAO();
        lv_room = findViewById(R.id.lv_room);

        roomDao.getAllRooms(new MovieRoomDAO.OnRoomOperationCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result != null) {
                    List<MovieRoom> roomList = (List<MovieRoom>) result;
                    MovieRoomAdapter adapter = new MovieRoomAdapter(RoomListActivity.this, roomList);
                    lv_room.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("RoomListActivity", "Lỗi tải danh sách phòng: " + error);
                Toast.makeText(RoomListActivity.this, "Lỗi tải danh sách phòng", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.fabAddMovie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RoomListActivity.this, RoomAdditionActivity.class);
                startActivity(i);

            }
        });
    }

}