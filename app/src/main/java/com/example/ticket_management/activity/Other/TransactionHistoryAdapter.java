package com.example.ticket_management.activity.Other;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticket_management.R;
import com.example.ticket_management.model.TransactionHistory;
import java.util.List;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> {
    private final List<TransactionHistory> historyList;

    public TransactionHistoryAdapter(List<TransactionHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionHistory item = historyList.get(position);
        holder.tvInvoiceCode.setText("Phim: " + item.getMovieName());
        holder.tvDate.setText("Ngày: " + item.getShowDate() + " " + item.getShowTime());
        holder.tvAmount.setText("Số tiền: " + item.getTotalPrice() + "đ");
        holder.tvStatus.setText("Phòng: " + item.getRoomName());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceCode, tvDate, tvAmount, tvStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceCode = itemView.findViewById(R.id.tvInvoiceCode);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
} 