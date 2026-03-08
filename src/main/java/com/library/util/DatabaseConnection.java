package com.library.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            // 1. Tải các biến từ file .env lên RAM
            Dotenv dotenv = Dotenv.configure().directory("./").load();

            // 2. Lấy thông tin thông qua KEY
            String url = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");

            // 3. Thực hiện kết nối an toàn
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Kết nối Database an toàn thành công!");

            // Tiến hành kết nối
            connection = DriverManager.getConnection(url, user, password);
            //System.out.println("✅ Đã kết nối Database thành công qua file .env!");

        } catch (SQLException e) {
            System.out.println("Lỗi kết nối database" + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("❌ Lỗi đọc file .env " + e.getMessage());
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
    public static void closeConnection() {
        try {
            if (instance.connection!= null && !instance.connection.isClosed()) {
                instance.connection.close();
                System.out.println("🔒 Đã đóng kết nối Database an toàn!");
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Lỗi khi đóng Database: " + e.getMessage());
        }
    }
}