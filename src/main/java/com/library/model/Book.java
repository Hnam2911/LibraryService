package com.library.model;

import lombok.*;

@Data
@NoArgsConstructor
public class Book {
    @Setter(AccessLevel.NONE)
    private String id;

    private String title;
    private String author;
    private int quantity;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private int borrowedQuantity;

    public Book(String id, String title, String author, int quantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.quantity = quantity;
    }
}
