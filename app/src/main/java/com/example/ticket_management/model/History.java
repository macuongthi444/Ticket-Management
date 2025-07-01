package com.example.ticket_management.model;

import java.util.Date;

import lombok.AccessLevel;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class History {
    String historyId;
    Date releaseDate;
    String theater;
    String showTime;
    String ticketCount;
    String selectedSeats;
    String paymentMethod;
    String totalAmount;

    String movieId;
}
