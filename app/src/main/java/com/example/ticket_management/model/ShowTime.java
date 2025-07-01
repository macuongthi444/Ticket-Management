package com.example.ticket_management.model;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTime implements Serializable {
    String showtimeId;
    String showTime;
    String showDate;
    int price;
    String poster;
    String movieId;
    String roomId;
}

