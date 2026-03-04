package com.library.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Setter(AccessLevel.NONE)
    private String id;

    private String title;
    private String author;
    private int quantity;
}
