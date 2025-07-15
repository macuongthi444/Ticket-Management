package com.example.ticket_management.activity.Room;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.BaseActivity;
import com.example.ticket_management.DAO.MovieRoomDAO;
import com.example.ticket_management.model.MovieRoom;
import com.example.ticket_management.util.GuidService;

public class RoomAdditionActivity extends BaseActivity {

    MovieRoomDAO movieRoomDAO;
    EditText edt_roomName, edt_number_seat;
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_add_room;
    }

    @Override
    protected void initContent(View contentView) {
        movieRoomDAO = new MovieRoomDAO();
        edt_roomName = findViewById(R.id.edt_room_name);
        edt_number_seat = findViewById(R.id.edt_number_seat);

        findViewById(R.id.btn_add).setOnClickListener(view -> {
            try{
                String roomName = edt_roomName.getText().toString();
                String numberSeat = edt_number_seat.getText().toString();

                if (roomName.isEmpty() || numberSeat.isEmpty()) {
                    Toast.makeText(this, "Tên phòng/số lượng ghế không được bỏ trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                MovieRoom movieRoom = new MovieRoom(GuidService.generateGuid(), roomName, 0, Integer.parseInt(numberSeat));
                movieRoomDAO.addMovieRoom(movieRoom);

                Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                edt_roomName.setText("");
                edt_number_seat.setText("");

                Intent i = new Intent(RoomAdditionActivity.this, RoomListActivity.class);
                startActivity(i);
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        });
    }
}