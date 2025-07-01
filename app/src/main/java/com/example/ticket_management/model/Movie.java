package com.example.ticket_management.model;

import java.io.Serializable;
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
public class Movie implements Serializable {
    String movieId;
    String movieName;
    String director;
    Date releaseDate;
    String description;
    String poster;
    int ticketsSold;
    float averageRating;
    int revenue;
    String categoryId;

    public String toString() {
        return  "Tên phim: " + movieName +
                ", nhà sản xuất: " + director +
                ", ngày phát hành: " + releaseDate +
                ", mô tả: " + description +
                ", giá vé: " + ticketsSold;
    }
}
