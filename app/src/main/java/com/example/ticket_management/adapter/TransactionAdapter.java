package com.example.ticket_management.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.model.TransactionHistory;

import java.util.List;

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

        // Hiển thị poster phim từ Base64
        if (transaction.getPoster() != null && !transaction.getPoster().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(transaction.getPoster(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivPoster.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivPoster.setImageResource(R.drawable.img_background);
            }
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
            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvSelectedSeats = itemView.findViewById(R.id.tv_selected_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            ivPoster = itemView.findViewById(R.id.iv_poster);
        }
    }
}