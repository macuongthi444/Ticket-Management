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
public class Invoice {
    String invoiceId;
    int quantity;
    int totalAmount;
    int paymentMethod;
    int status;
    String time;
    String userId;
    String showtimeId;

}
