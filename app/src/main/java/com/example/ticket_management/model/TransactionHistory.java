package com.example.ticket_management.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionHistory {
    private String movieName;
    private String showDate;
    private String showTime;
    private long timestamp;
    private int totalPrice;
    private String roomName;
    private List<String> selectedSeats;
    private String poster;
}
