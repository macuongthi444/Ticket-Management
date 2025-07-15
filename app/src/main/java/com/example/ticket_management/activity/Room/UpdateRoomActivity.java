package com.example.ticket_management.activity.Room;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.MovieRoomDAO;
import com.example.ticket_management.model.MovieRoom;

public class UpdateRoomActivity extends BaseActivity {
    EditText edt_room_name;
    EditText edt_room_count;
    Button btn_update;

    MovieRoomDAO movieRoomDAO;
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_update_room;
    }
    @Override
    protected void initContent(View contentView) {
        edt_room_name = findViewById(R.id.edt_room_name_update);
        edt_room_count = findViewById(R.id.edt_number_seat_update);
        btn_update = findViewById(R.id.btn_update);

        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");
        movieRoomDAO = new MovieRoomDAO();
        try{
            if (roomId != null && !roomId.isEmpty()) {
                movieRoomDAO.getMovieRoomById(roomId, movieRoom -> {
                    edt_room_name.setText(movieRoom.getRoomName());
                    edt_room_count.setText(String.valueOf(movieRoom.getSeatCount()));
                }, errorMessage -> {
                    Toast.makeText(UpdateRoomActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                });

            } else {
                Toast.makeText(this, "RoomId không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String roomName = edt_room_name.getText().toString();
                    String numberSeat = edt_room_count.getText().toString();

                    if (roomName.isEmpty() || numberSeat.isEmpty()) {
                        Toast.makeText(UpdateRoomActivity.this, "Tên phòng/số lượng ghế không được bỏ trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MovieRoom updatedRoom = new MovieRoom();
                    updatedRoom.setRoomId(roomId);
                    updatedRoom.setRoomName(roomName);
                    updatedRoom.setSeatCount(Integer.parseInt(numberSeat));

                    MovieRoomDAO movieRoomDAO = new MovieRoomDAO();
                    movieRoomDAO.updateMovieRoom(updatedRoom);

                    Toast.makeText(UpdateRoomActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    Intent newIntent = new Intent(UpdateRoomActivity.this, RoomListActivity.class);
                    startActivity(newIntent);
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
}