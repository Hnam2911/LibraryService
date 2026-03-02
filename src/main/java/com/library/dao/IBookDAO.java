package com.library.dao;

import com.library.model.Book;

import java.util.List;

public interface IBookDAO {
    boolean add(Book book);
    boolean update(Book book);
    boolean delete(String id);
    List<Book> getAll();
}
