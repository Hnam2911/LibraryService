package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.IBookDAO;
import com.library.model.Book;

import java.util.List;
import java.util.UUID;

public class BookService {
    private IBookDAO bookDAO=new BookDAO();
    public boolean addBook(String title,String author,int quantity){
        Book book=new Book(
                UUID.randomUUID().toString(),
                title,author,quantity
        );
        if(book.getQuantity()<0){
            System.out.println("Error! quantity must be >=0");
            return false;
        }
        Book savedBook=bookDAO.find(book.getTitle(), book.getAuthor());
        if(savedBook==null) {
            return bookDAO.add(book);
        }
        else {
            System.out.println("This book has already existed so we updated its quantity instead");
            return bookDAO.update(new Book(savedBook.getId(), savedBook.getTitle(),
                    savedBook.getAuthor(),savedBook.getQuantity()+book.getQuantity() ));
        }
    }
    public boolean updateBook(String id,String title,String author,int quantity){
        return bookDAO.update(new Book(
                id,title,author,quantity
        ));
    }
    public boolean deleteBook(String id){return  bookDAO.delete(id);}
    public List<Book> getAllBook(){return bookDAO.getAll();}
    public List<Book> searchBook(String keyword){return bookDAO.searchBook(keyword);}
}
