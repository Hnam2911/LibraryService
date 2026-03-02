package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorowRecord {
    private String id;
    private Reader reader;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String status;
}
