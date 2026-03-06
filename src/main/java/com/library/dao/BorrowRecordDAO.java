package com.library.dao;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Reader;
import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO implements IBorrowRecordDAO {
    private Connection conn= DatabaseConnection.getInstance().getConnection();
    @Override
    public boolean add(BorrowRecord record) {
        String sql="INSERT INTO  borrow_record(id,reader_id,book_id,borrow_date,return_date,status)" +
                "values(?::uuid,?::uuid,?::uuid,?::Date,?::Date,?)";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,record.getId());
            pstmt.setString(2,record.getReader().getId());
            pstmt.setString(3,record.getBook().getId());
            pstmt.setString(4, record.getBorrowDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            pstmt.setString(5, record.getReturnDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            pstmt.setString(6,record.getStatus());

            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (Exception e) {
            System.out.println("Error!!");
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(BorrowRecord record) {
        String sql="Update borrow_record " +
                "set reader_id=?::uuid,book_id=?::uuid,borrow_date=?::Date,return_date=?::Date,status=?"+
                " where id=?::uuid";
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,record.getReader().getId());
            pstmt.setString(2,record.getBook().getId());
            pstmt.setString(3,record.getBorrowDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            pstmt.setString(4,record.getReturnDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            pstmt.setString(5,record.getStatus());
            pstmt.setString(6,record.getId());

            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql="delete from borrow_record where id=?::uuid";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,id);
            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public List<BorrowRecord> getAll() {
        List<BorrowRecord> list=new ArrayList<>();
        String sql="select borrow.id,r.id as reader_id,r.name,r.phone,r.email," +
                "b.id as book_id,b.title,b.author,b.quantity," +
                "borrow.borrow_date,borrow.return_date,borrow.status " +
                "from borrow_record borrow " +
                "inner join book b on b.id=borrow.book_id " +
                "inner join reader r on r.id=borrow.reader_id;";
        try(PreparedStatement pstmt= conn.prepareStatement(sql);
            ResultSet rs=pstmt.executeQuery()){
            while(rs.next()){
                list.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
    // 1. Hàm Helper dùng chung cho toàn class
    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs) throws SQLException {
        Book book = new Book(
                rs.getString("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("quantity")
        );

        Reader reader = new Reader(
                rs.getString("reader_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email")
        );

        return new BorrowRecord(
                rs.getString("id"),
                reader,
                book,
                rs.getDate("borrow_date").toLocalDate(),
                rs.getDate("return_date").toLocalDate(),
                rs.getString("status")
        );
    }

    // 2. Hàm Search bây giờ cực kỳ ngắn gọn
    @Override
    public List<BorrowRecord> searchRecord(String keyword) {
        List<BorrowRecord> list = new ArrayList<>();
        // SQL JOIN 3 bảng để có thể tìm theo tên sách hoặc tên người mượn
        String sql = "SELECT br.id, br.borrow_date, br.return_date, br.status, " +
                "b.id AS book_id, b.title, b.author, b.quantity, " +
                "r.id AS reader_id, r.name, r.phone, r.email " +
                "FROM borrow_record br " +
                "INNER JOIN book b ON br.book_id = b.id " +
                "INNER JOIN reader r ON br.reader_id = r.id " +
                "WHERE br.borrow_date::text LIKE ? OR br.return_date::text LIKE ? OR br.status LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Tận dụng hàm helper giúp code sạch sẽ
                    list.add(mapResultSetToBorrowRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi tìm kiếm phiếu mượn: " + e.getMessage());
        }
        return list;
    }
    @Override
    public BorrowRecord findById(String id){
        String sql = "SELECT br.id, br.borrow_date, br.return_date, br.status, " +
                "b.id AS book_id, b.title, b.author, b.quantity, " +
                "r.id AS reader_id, r.name, r.phone, r.email " +
                "FROM borrow_record br " +
                "INNER JOIN book b ON br.book_id = b.id " +
                "INNER JOIN reader r ON br.reader_id = r.id " +
                "WHERE br.id=?::uuid";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) return mapResultSetToBorrowRecord(rs);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    @Override
    public void checkOverdue(){
        String sql = "UPDATE borrow_record SET status = 'overdue' " +
                "WHERE status = 'borrowed' AND return_date < CURRENT_DATE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
