package com.example.ticket_management.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ticket_management.R;
import com.example.ticket_management.model.TransactionHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<TransactionHistory> transactionList;

    public TransactionAdapter(Context context, List<TransactionHistory> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionHistory transaction = transactionList.get(position);

        // Hiển thị tên phim
        holder.tvMovieName.setText(transaction.getMovieName());

        // Hiển thị ngày và giờ chiếu
        String dateTime = transaction.getShowDate() + " " + transaction.getShowTime();
        holder.tvShowDateTime.setText(dateTime);

        // Hiển thị tên phòng chiếu
        holder.tvRoomName.setText("Phòng chiếu: " + (transaction.getRoomName() != null ? transaction.getRoomName() : "Không xác định"));

        // Hiển thị danh sách ghế
        String seats = transaction.getSelectedSeats() != null ? String.join(", ", transaction.getSelectedSeats()) : "Không xác định";
        holder.tvSelectedSeats.setText("Ghế: " + seats);

        // Hiển thị tổng giá
        holder.tvTotalPrice.setText("Tổng tiền: " + String.format("%,d", transaction.getTotalPrice()) + "đ");

        // Hiển thị poster phim
        if (transaction.getPoster() != null && !transaction.getPoster().isEmpty()) {
            Glide.with(context)
                    .load(transaction.getPoster())
                    .placeholder(R.drawable.img_background)
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.img_background);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieName, tvShowDateTime, tvRoomName, tvSelectedSeats, tvTotalPrice;
        ImageView ivPoster;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieName = itemView.findViewById(R.id.tv_movie_name);
            tvShowDateTime = itemView.findViewById(R.id.tv_show_date_time);
            tvRoomName = itemView.findViewById(R.id.tv_room_name); // Thay tvRoomId bằng tvRoomName
            tvSelectedSeats = itemView.findViewById(R.id.tv_selected_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            ivPoster = itemView.findViewById(R.id.iv_poster);
        }
    }
}