package com.library.dao;

import com.library.model.Reader;
import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO implements IReaderDAO{
    private Connection conn=DatabaseConnection.getInstance().getConnection();
    @Override
    public boolean add(Reader reader){
        String sql="INSERT INTO reader(id,name,phone,email) values(?::uuid,?,?,?)";
        try(PreparedStatement pstmt= conn.prepareStatement(sql)){
            pstmt.setString(1,reader.getId());
            pstmt.setString(2,reader.getName());
            pstmt.setString(3,reader.getPhone());
            pstmt.setString(4,reader.getEmail());

            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (Exception e) {
            System.out.println("Error! Cannot add reader into database. Please check your phone number");
            return false;
        }
    }
    @Override
    public boolean update(Reader reader){
        String sql="Update reader set name=?,phone=?,email=? where id=?::uuid";
        try(PreparedStatement pstmt= conn.prepareStatement(sql)){
            pstmt.setString(1,reader.getName());
            pstmt.setString(2,reader.getPhone());
            pstmt.setString(3,reader.getEmail());
            pstmt.setString(4,reader.getId());

            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (Exception e) {
            System.out.println("Error! Cannot update reader into database.Phone number maybe duplicated");
            System.out.println(e.getMessage());
            return false;
        }
    }
    public boolean delete(String id){
        String sql="DELETE from reader where id=?::uuid";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,id);
            int rowsAffected=pstmt.executeUpdate();
            return rowsAffected>0;
        } catch (Exception e) {
            System.out.println("Cannot delete reader!");
            System.out.println(e.getMessage());
            return false;
        }
    }
    @Override
    public List<Reader> getAll(){
        List<Reader> list=new ArrayList<>();
        String sql="Select id,name,phone,email from reader";
        try(PreparedStatement pstmt= conn.prepareStatement(sql);
            ResultSet rs=pstmt.executeQuery()){
            while(rs.next()){
                String id=rs.getString("id");
                String name=rs.getString("name");
                String phone=rs.getString("phone");
                String email=rs.getString("email");
                list.add(new Reader(id,name,phone,email));
            }
        } catch (Exception e) {
            System.out.println("Cannot access reader database");
            System.out.println(e.getMessage());
        }
        return list;
    }
    @Override
    public List<Reader> searchReader(String keyword) {
        List<Reader> list = new ArrayList<>();
        // Dùng LOWER để tìm kiếm không phân biệt hoa thường
        // Dùng %?% để tìm kiếm một phần của chuỗi
        String sql = "SELECT * FROM reader WHERE " +
                "LOWER(name) LIKE LOWER(?) OR " +
                "LOWER(phone) LIKE LOWER(?) OR " +
                "LOWER(email) LIKE LOWER(?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Reader(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi tìm kiếm độc giả: " + e.getMessage());
        }
        return list;
    }
    @Override
    public Reader findById(String id) {
        String sql = "Select name,phone,email from reader where id=?::uuid ";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,id);
            try(ResultSet rs=pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Reader(
                            id,
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Cannot find reader!Please check again id");
        }
        return null;
    }
}
