package com.library.util;

import com.library.model.Book;
import com.library.model.Reader;
import com.library.service.BookService;
import com.library.service.ReaderService;
import com.library.service.BorrowService;

public class DataSeeder {

    public static void main(String[] args) {
//        System.out.println("⏳ Bắt đầu quá trình Database Seeding...");
//
        BookService bookService = new BookService();
        ReaderService readerService = new ReaderService();
        BorrowService borrowService = new BorrowService();
//
//        // 1. Tạo mảng dữ liệu Sách phong phú (Khoa học máy tính, Toán, Tiểu thuyết)
//        String[][] bookData = {
//                {"Introduction to Algorithms", "Thomas H. Cormen", "15"},
//                {"Clean Code", "Robert C. Martin", "10"},
//                {"Effective Java", "Joshua Bloch", "8"},
//                {"Head First Design Patterns", "Eric Freeman", "12"},
//                {"Calculus: Early Transcendentals", "James Stewart", "5"},
//                {"Linear Algebra and Its Applications", "David C. Lay", "7"},
//                {"Discrete Mathematics and Its Applications", "Kenneth H. Rosen", "10"},
//                {"The Pragmatic Programmer", "Andrew Hunt", "9"},
//                {"Dune", "Frank Herbert", "20"},
//                {"The Hobbit", "J.R.R. Tolkien", "15"},
//                {"Design Data-Intensive Applications", "Martin Kleppmann", "6"},
//                {"Artificial Intelligence: A Modern Approach", "Stuart Russell", "8"}
//        };
//
//        System.out.println("📚 Đang thêm Sách...");
//        for (String[] b : bookData) {
//            // Giả định hàm thêm sách của bạn nhận vào một đối tượng Book
//            // ID (UUID) sẽ được DAO hoặc PostgreSQL tự động sinh
//
//            bookService.addBook(b[0],b[1],Integer.parseInt(b[2]));
//        }
//
//        // 2. Tạo mảng dữ liệu Độc giả
//        String[][] readerData = {
//                {"Nguyen Van A", "0901111111", "nva@gmail.com"},
//                {"Tran Thi B", "0902222222", "ttb@gmail.com"},
//                {"Le Van C", "0903333333", "lvc@gmail.com"},
//                {"Pham Dinh D", "0904444444", "pdd@gmail.com"},
//                {"Hoang Thi E", "0905555555", "hte@gmail.com"}
//        };
//
//        System.out.println("👥 Đang thêm Độc giả...");
//        for (String[] r : readerData) {
//            readerService.addReader(r[0],r[1],r[2]);
//        }
//
//        System.out.println("✅ Seeding hoàn tất! Hãy mở ứng dụng JavaFX để kiểm tra UI.");
//
//        // Ghi chú: Việc test mượn sách (BorrowService) cần lấy được UUID thực tế
//        // của Sách và Độc giả vừa tạo trong DB. Tạm thời chúng ta seed Sách và Độc giả
//        // trước để test giao diện hiển thị danh sách (Read).
//        System.out.println("📝 Đang tạo Phiếu mượn (Borrow Records)...");

        // LƯU Ý: Tên sách, tác giả và SĐT phải gõ KHỚP CHÍNH XÁC 100%
        // với mảng dữ liệu bookData và readerData đã thêm ở phía trên.

        // 1. Độc giả A (0901111111) mượn cuốn Algorithms trong 14 ngày
        //borrowService.borrowBook("Introduction to Algorithms", "Thomas H. Cormen", "0901111111", 14);

        // 2. Độc giả B (0902222222) cũng mượn cuốn Algorithms trong 7 ngày
        // -> KỲ VỌNG UI: Cột "Đang mượn" của cuốn Algorithms phải nhảy lên số 2
        //borrowService.borrowBook("Dune", "Frank Herbert", "0123456789", 7);

        // 3. Độc giả C (0903333333) mượn cuốn Clean Code trong 10 ngày
        // -> KỲ VỌNG UI: Cột "Đang mượn" của cuốn Clean Code phải nhảy lên số 1
//        borrowService.borrowBook("Clean Code", "Robert C. Martin", "0903333333", 10);
//
//        // 4. Độc giả A (0901111111) mượn cuốn Effective Java trong 5 ngày
//        // -> KỲ VỌNG UI: Cột "Đang mượn" của cuốn Effective Java phải nhảy lên số 1
//        borrowService.borrowBook("Effective Java", "Joshua Bloch", "0901111111", 5);
        borrowService.deleteRecord("79398bf0-55ef-4d67-b414-ed806ad8dcfc");
        borrowService.deleteRecord("94dc3380-1841-4e51-b50b-5255177e62e7");
        borrowService.deleteRecord("2c56c75a-0286-4a1b-841a-f539a4e6413a");
        borrowService.deleteRecord("b2ee3611-ae90-4377-8df8-e58e4dc29b23");
        borrowService.deleteRecord("ecaa21f3-8008-4d37-84f0-b7e2379500b7");
        borrowService.deleteRecord("db83f185-5988-4932-a7ac-59b2d0411831");
        borrowService.deleteRecord("53acc178-e2ba-4df8-ba51-c85bb35389df");

        System.out.println("✅ Gieo mầm dữ liệu Phiếu mượn thành công!");
    }
}
