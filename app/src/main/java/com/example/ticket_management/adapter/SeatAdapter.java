package com.example.ticket_management.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticket_management.R;
import com.example.ticket_management.model.Seat;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private List<Seat> seatList;
    private OnSeatClickListener onSeatClickListener;

    public interface OnSeatClickListener {
        void onSeatClicked(Seat seat, int position);
    }

    public SeatAdapter(List<Seat> seatList, OnSeatClickListener listener) {
        this.seatList = seatList;
        this.onSeatClickListener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);
        holder.bind(seat, position);
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    public class SeatViewHolder extends RecyclerView.ViewHolder {
        private Button seatButton;
        private ConstraintLayout seatContainer;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatButton = itemView.findViewById(R.id.seat_button);
            seatContainer = itemView.findViewById(R.id.seat_container);
        }

        public void bind(final Seat seat, final int position) {
            // Hiển thị nhãn ghế
            seatButton.setText(seat.getRow() + String.valueOf(seat.getNumber()));

            // Điều chỉnh kích thước cho ghế đôi (hàng I và J)
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) seatButton.getLayoutParams();
            if (seat.getRow() == 'I' || seat.getRow() == 'J') {
                params.width = (int) (40 * 2 * itemView.getContext().getResources().getDisplayMetrics().density); // Gấp đôi chiều rộng (40dp * 2)
            } else {
                params.width = (int) (40 * itemView.getContext().getResources().getDisplayMetrics().density); // Kích thước mặc định (40dp)
            }
            seatButton.setLayoutParams(params);

            // Đặt màu dựa trên trạng thái
            Context context = itemView.getContext();
            switch (seat.getStatus()) {
                case "empty":
                    seatButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                    seatButton.setEnabled(true);
                    break;
                case "selected":
                    seatButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
                    seatButton.setEnabled(true);
                    break;
                case "sold":
                    seatButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                    seatButton.setEnabled(false); // Vô hiệu hóa ghế đã bán
                    break;
            }

            // Xử lý sự kiện click
            seatButton.setOnClickListener(v -> {
                if (onSeatClickListener != null && !seat.getStatus().equals("sold")) {
                    onSeatClickListener.onSeatClicked(seat, position);
                }
            });
        }
    }
}