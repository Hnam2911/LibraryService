package com.library.main;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.util.DatabaseConnection;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        BookDAO bookDAO = new BookDAO();

        // Tạo 1 đối tượng Sách bằng Constructor của Lombok
        // Dùng UUID.randomUUID().toString() để Java tự sinh ra 1 mã ID ngẫu nhiên chuẩn form
        Book newBook = new Book(
                UUID.randomUUID().toString(),
                "Clean Code",
                "Robert C. Martin",
                15
        );

        // Đẩy xuống Database
        boolean isSuccess = bookDAO.add(newBook);

        if (isSuccess) {
            System.out.println("🎉 Đã thêm sách vào PostgreSQL thành công!");
        } else {
            System.out.println("Thất bại rồi, hãy kiểm tra lại cấu hình.");
        }
    }
}