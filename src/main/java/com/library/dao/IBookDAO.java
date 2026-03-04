package com.library.dao;

import com.library.model.Book;

import java.util.List;

public interface IBookDAO {
    boolean add(Book book);
    boolean update(Book book);
    boolean delete(String id);
    List<Book> getAll();
    Book findById(String id);
    List<Book> searchBook(String keyword);
    Book find(String title,String author);
}
