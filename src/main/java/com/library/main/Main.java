package com.library.main;

import com.library.util.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        // Cố tình gọi 2 lần để xem nó có tạo ra 2 kết nối hay chỉ dùng 1 kết nối
        System.out.println("Lần gọi 1:");
        DatabaseConnection conn1 = DatabaseConnection.getInstance();

        System.out.println("Lần gọi 2:");
        DatabaseConnection conn2 = DatabaseConnection.getInstance();

        // Kiểm tra xem 2 biến này có trỏ về cùng 1 địa chỉ bộ nhớ không
        if (conn1 == conn2) {
            System.out.println("🎉 Tuyệt vời! Singleton hoạt động hoàn hảo. Cả 2 đều dùng chung 1 Object.");
        }
    }
}