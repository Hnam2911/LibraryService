package com.library.main;

import com.library.dao.BookDAO;
import com.library.dao.BorrowRecordDAO;
import com.library.dao.ReaderDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Reader;
import com.library.util.DatabaseConnection;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        BookDAO bookDAO = new BookDAO();
        ReaderDAO readerDAO = new ReaderDAO();
        BorrowRecordDAO borrowDAO = new BorrowRecordDAO();

        // 1. Lấy dữ liệu mồi (Mock data) từ Database
        List<Book> books = bookDAO.getAll();
        List<Reader> readers = readerDAO.getAll();

        if (books.isEmpty() || readers.isEmpty()) {
            System.out.println("❌ Database phải có ít nhất 1 cuốn sách và 1 độc giả để test!");
            return;
        }

        Book sampleBook = books.get(0);
        Reader sampleReader = readers.get(0);

        // 2. Test chức năng THÊM (CREATE)
        System.out.println("--- TEST ADD ---");
        BorrowRecord newRecord = new BorrowRecord(
                UUID.randomUUID().toString(),
                sampleReader,
                sampleBook,
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "borrowed"
        );

        boolean isAdded = borrowDAO.add(newRecord);
        System.out.println("Thêm phiếu mượn: " + (isAdded ? "THÀNH CÔNG" : "THẤT BẠI"));

        // 3. Test chức năng ĐỌC (READ)
        System.out.println("\n--- TEST GET ALL ---");
        List<BorrowRecord> records = borrowDAO.getAll();
        for (BorrowRecord record : records) {
            System.out.println("Phiếu: " + record.getId() +
                    " | Độc giả: " + record.getReader().getName() +
                    " | Sách: " + record.getBook().getTitle());
        }

        // 4. Test chức năng SỬA (UPDATE)
        if (!records.isEmpty()) {
            System.out.println("\n--- TEST UPDATE ---");
            BorrowRecord recordToUpdate = records.get(0);
            recordToUpdate.setStatus("returned");
            boolean isUpdated = borrowDAO.update(recordToUpdate);
            System.out.println("Cập nhật trạng thái thành 'returned': " + (isUpdated ? "THÀNH CÔNG" : "THẤT BẠI"));
        }
        System.out.println(borrowDAO.searchRecord("2026"));

        // 5. Test chức năng XÓA (DELETE)
        if (!records.isEmpty()) {
            System.out.println("\n--- TEST DELETE ---");
            // Lấy ID của phiếu mượn đầu tiên để xóa
            String idToDelete = records.get(0).getId();
            boolean isDeleted = borrowDAO.delete(idToDelete);
            System.out.println("Xóa phiếu mượn " + idToDelete + ": " + (isDeleted ? "THÀNH CÔNG" : "THẤT BẠI"));
        }
    }
}