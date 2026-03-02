package com.library.dao;

import com.library.model.Book;
import com.library.util.DatabaseConnection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Connection;

public class BookDAO implements IBookDAO {
    private Connection conn= DatabaseConnection.getInstance().getConnection();
    @Override
    public boolean add(Book book){
        String sql="INSERT INTO book(id,title,author,quantity) VALUES(?::uuid,?,?,?)";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setInt(4,book.getQuantity());

            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (SQLException e) {
            System.out.println("Error! Cannot insert book into database!!");
            return false;
        }
    }
    @Override
    public boolean update(Book book){
        //pass
        return true;
    }
    @Override
    public boolean delete(String id){
        //pass
        return true;
    }
    @Override
    public List<Book> getAll(){
        //pass
        return new ArrayList<>();
    }

}
