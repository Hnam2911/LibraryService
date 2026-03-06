package com.library.dao;

import com.library.model.Book;
import com.library.util.DatabaseConnection;

import java.sql.ResultSet;
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
        String notice="Error! Cannot insert book into database!!";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setInt(4,book.getQuantity());
            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (SQLException e) {
            System.out.println(notice);
            return false;
        }
    }
    @Override
    public boolean update(Book book){
        String sql="UPDATE book set title=?,author=?,quantity=? where id=?::uuid";
        String notice="Error! Cannot update data!!";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3,book.getQuantity());
            pstmt.setString(4,book.getId());
            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(notice);
            return false;
        }
    }
    @Override
    public boolean delete(String id){
        String sql="delete from book where id=?::uuid";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,id);
            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (SQLException e) {
            System.out.println("Cannot delete book! please check again");
            return false;
        }
    }
    @Override
    public List<Book> getAll(){
        List<Book> list=new ArrayList<>();
        String sql = "SELECT b.*, " +
                "COALESCE((SELECT COUNT(*) FROM borrow_record br " +
                "          WHERE br.book_id = b.id " +
                "          AND br.status IN ('borrowed', 'overdue')), 0) AS borrowed_qty " +
                "FROM book b";
        try(PreparedStatement pstmt=conn.prepareStatement(sql);ResultSet rs=pstmt.executeQuery()){
            while(rs.next()){
                Book book=mapResult(rs);
                book.setBorrowedQuantity(rs.getInt("borrowed_qty"));
                list.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error!! Cannot select table book");
            System.out.println(e.getMessage());
        }
        return list;
    }
    @Override
    public List<Book> searchBook(String keyword) {
        List<Book> list = new ArrayList<>();
        // Dùng LOWER để tìm kiếm không phân biệt hoa thường
        // Dùng %?% để tìm kiếm một phần của chuỗi
        String sql = "SELECT * FROM reader WHERE " +
                "LOWER(title) LIKE LOWER(?) OR " +
                "LOWER(author) LIKE LOWER(?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResult(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi tìm kiếm sách: " + e.getMessage());
        }
        return list;
    }
    @Override
    public Book findById(String id) {
        String sql = "SELECT id, title, author, quantity FROM book WHERE id = ?::uuid";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Nếu tìm thấy (chỉ có 1 dòng vì ID là duy nhất)
                    return mapResult(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tìm sách theo ID: " + e.getMessage());
        }
        return null; // Nếu không tìm thấy
    }
    @Override
    public Book find(String title,String author) {
        String sql = "SELECT id, title, author, quantity FROM book WHERE title = ? and author=?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResult(rs);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public Book mapResult(ResultSet rs) throws SQLException {
        return new Book(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("quantity")
        );
    }
}
