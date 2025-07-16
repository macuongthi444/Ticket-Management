package com.example.ticket_management.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ticket_management.R;
import com.example.ticket_management.activity.ShowTime.AddShowTimeActivity;
import com.example.ticket_management.activity.ShowTime.ShowTimeListActivity;
import com.example.ticket_management.DAO.MovieDAO;
import com.example.ticket_management.DAO.MovieRoomDAO;
import com.example.ticket_management.DAO.ShowTimeDAO;
import com.example.ticket_management.model.Movie;
import com.example.ticket_management.model.MovieRoom;
import com.example.ticket_management.model.ShowTime;

import java.util.List;
import java.util.function.Consumer;

public class ShowTimeAdapter extends RecyclerView.Adapter<ShowTimeAdapter.ShowTimeViewHolder> {

    private Context context;
    private List<ShowTime> showTimeList;
    private MovieDAO movieDAO;
    private MovieRoomDAO movieRoomDAO;

    public ShowTimeAdapter(Context context, List<ShowTime> showTimeList) {
        movieDAO = new MovieDAO();
        movieRoomDAO = new MovieRoomDAO();
        this.context = context;
        this.showTimeList = showTimeList != null ? showTimeList : java.util.Collections.emptyList();
    }

    @NonNull
    @Override
    public ShowTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_time, parent, false);
        return new ShowTimeViewHolder(view);
    }

    @SuppressLint("RecyclerView")

    @Override
    public void onBindViewHolder(@NonNull ShowTimeViewHolder holder, int position) {
        ShowTime showTime = showTimeList.get(position);

        // Lấy tên phim từ movieId dùng Consumer
        movieDAO.getMovieById(showTime.getMovieId(),
                movie -> {
                    holder.tvMovieName.setText("Tên Phim: " + movie.getMovieName());
                },
                errorMessage -> {
                    holder.tvMovieName.setText("Tên Phim: Không tìm thấy");
                    Log.e("ShowTimeAdapter", "Lỗi lấy phim: " + errorMessage);
                }
        );

        // Lấy tên phòng từ roomId dùng Consumer
        movieRoomDAO.getMovieRoomById(showTime.getRoomId(),
                room -> {
                    holder.tvRoomName.setText("Phòng: " + room.getRoomName());
                },
                error -> {
                    holder.tvRoomName.setText("Phòng: " + error);
                    Log.e("ShowTimeAdapter", "Lỗi lấy phòng: " + error);
                }
        );

        // Set ngày chiếu và giờ chiếu
        holder.tvShowDate.setText("Ngày Chiếu: " + showTime.getShowDate());
        holder.tvShowTime.setText("Giờ Chiếu: " + showTime.getShowTime());

        // Load poster bằng Glide
        if (showTime.getPoster() != null && !showTime.getPoster().isEmpty()) {
            Glide.with(context)
                    .load(showTime.getPoster())
                    .placeholder(R.drawable.ic_default_movie)
                    .error(R.drawable.ic_default_movie)
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.ic_menu_gallery);
        }

        // Xử lý menu chỉnh sửa và xóa
        holder.imgMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.imgMenu);
            popupMenu.getMenu().add("Chỉnh sửa");
            popupMenu.getMenu().add("Xóa");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Chỉnh sửa")) {
                    Intent intent = new Intent(context, AddShowTimeActivity.class);
                    intent.putExtra("showtime", showTime);
                    ((ShowTimeListActivity) context).startActivityForResult(intent, 1);
                } else if (item.getTitle().equals("Xóa")) {
                    ShowTimeDAO showTimeDAO = new ShowTimeDAO();
                    showTimeDAO.deleteShowTime(showTime.getShowtimeId(),
                            new ShowTimeDAO.OnShowTimeOperationCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();
                                    ((ShowTimeListActivity) context).fetchShowTimes();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(context, "Xóa thất bại: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
                return true;
            });

            popupMenu.show();
        });
    }


    @Override
    public int getItemCount() {
        return showTimeList.size();
    }

    public static class ShowTimeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster, imgMenu;
        TextView tvMovieName, tvShowDate, tvShowTime, tvRoomName;

        public ShowTimeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvMovieName = itemView.findViewById(R.id.tvMovieName);
            tvShowDate = itemView.findViewById(R.id.tvShowDate);
            tvShowTime = itemView.findViewById(R.id.tvShowTime);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            imgMenu = itemView.findViewById(R.id.imgMenu);
        }
    }
}