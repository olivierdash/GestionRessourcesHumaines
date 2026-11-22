package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiringDelayDto {

    private LocalDateTime dateCreation;
    private LocalDate hiringDate;
}