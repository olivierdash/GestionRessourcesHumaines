package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartPointDto {
    private String date;  // format "yyyy-MM-dd"
    private long count;
}