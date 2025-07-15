package com.example.ticket_management.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ticket_management.R;
import com.example.ticket_management.activity.Room.UpdateRoomActivity;
import com.example.ticket_management.DAO.MovieRoomDAO;
import com.example.ticket_management.DAO.ShowTimeDAO;
import com.example.ticket_management.model.MovieRoom;

import java.util.List;

public class MovieRoomAdapter extends ArrayAdapter<MovieRoom> {
    private Context context;
    private List<MovieRoom> roomList;

    MovieRoomDAO roomDAO = new MovieRoomDAO();
    ShowTimeDAO showTimeDAO = new ShowTimeDAO();

    public MovieRoomAdapter(Context context, List<MovieRoom> roomList) {
        super(context, 0, roomList);
        this.context = context;
        this.roomList = roomList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        }
        MovieRoom movieRoom = getItem(position);

        TextView roomCountTextView = convertView.findViewById(R.id.tv_number_seat);
        TextView tvRoomName = convertView.findViewById(R.id.tv_room_name);
        ImageView tvc = convertView.findViewById(R.id.tvc);

        if (movieRoom != null) {
            roomCountTextView.setText(String.valueOf(movieRoom.getSeatCount()));
            tvRoomName.setText(movieRoom.getRoomName());
        }

        tvc.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, tvc);
            popupMenu.inflate(R.menu.menu_options);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    Intent i = new Intent(MovieRoomAdapter.this.getContext(), UpdateRoomActivity.class);
                    i.putExtra("roomId", movieRoom.getRoomId());
                    context.startActivity(i);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    String roomId = movieRoom.getRoomId();
                    roomDAO.deleteMovieRoom(roomId);
                    showTimeDAO.deleteShowTimesByRoomId(roomId, new ShowTimeDAO.OnShowTimeOperationCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(context, "Đã xóa phòng: " + movieRoom.getRoomName(), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(context, "Xóa suất chiếu thất bại: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });

                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        return convertView;
    }
}
