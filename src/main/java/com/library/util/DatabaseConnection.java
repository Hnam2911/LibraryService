package com.library.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        // Tạo đối tượng Properties để chứa dữ liệu đọc từ file
        Properties properties = new Properties();

        // Dùng InputStream để đọc file database.properties từ thư mục resources
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("database/properties")) {

            if (inputStream == null) {
                System.out.println("❌ Không tìm thấy file properties!");
                return;
            }

            // Tải dữ liệu từ file vào đối tượng properties
            properties.load(inputStream);

            // Lấy thông tin ra từ các key đã định nghĩa
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            // Tiến hành kết nối
            connection = DriverManager.getConnection(url, user, password);
            //System.out.println("✅ Đã kết nối Database thành công qua file properties!");

        } catch (Exception e) {
            System.out.println("❌ Lỗi cấu hình hoặc kết nối Database: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        try {
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}