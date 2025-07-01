package com.example.ticket_management.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat {
    char row;
    int number;
    String status;
    int price;
}

