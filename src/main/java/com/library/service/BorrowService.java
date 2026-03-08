package com.library.service;

import com.library.model.*;
import com.library.dao.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BorrowService {
    private IReaderDAO readerDAO=new ReaderDAO();
    private IBookDAO bookDAO=new BookDAO();
    private IBorrowRecordDAO borrowDAO=new BorrowRecordDAO();

    // Tạo một Enum để định nghĩa các trạng thái trả về
    public enum BorrowStatus {
        SUCCESS,
        BOOK_NOT_FOUND,
        BOOK_OUT_OF_STOCK,
        READER_NOT_FOUND,
        RECORD_NOT_FOUND,
        BORROW_DATE_ERROR,
        RETURN_DATE_ERROR,
        STATUS_ERROR,
        ADD_ERROR,
        ERROR,
        LIMIT_EXCEEDED
    }
    public BorrowStatus borrowBook(String title,String author,String phone,int days){
        Book book=bookDAO.find(title,author);
        if(book==null) return BorrowStatus.BOOK_NOT_FOUND;
        if(book.getQuantity()<=0) return BorrowStatus.BOOK_OUT_OF_STOCK;
        Reader reader=readerDAO.findByPhone(phone);
        int maxAllowed = getMaxBorrowBooks();

        // Đếm số phiếu mượn của độc giả này đang ở trạng thái borrowed hoặc overdue
        long unreturnedCount = borrowDAO.getAll().stream()
                .filter(record -> record.getReader().getPhone().equals(phone))
                .filter(record ->"overdue".equalsIgnoreCase(record.getStatus()))
                .count();

        if (unreturnedCount >= maxAllowed) {
            System.out.println("Độc giả đã đạt giới hạn mượn sách chưa trả: " + unreturnedCount + "/" + maxAllowed);
            return BorrowStatus.LIMIT_EXCEEDED;
        }
        if(reader==null) return BorrowStatus.READER_NOT_FOUND;
        BorrowRecord newRecord = new BorrowRecord(
                UUID.randomUUID().toString(), // Tự sinh ID
                reader,                       // Object Reader xịn lấy từ DB
                book,                         // Object Book xịn lấy từ DB
                LocalDate.now(),
                LocalDate.now().plusDays(days),
                "borrowed"
        );

        if(borrowDAO.add(newRecord)){
            book.setQuantity(book.getQuantity() - 1);
            bookDAO.update(book);
        }
        else return BorrowStatus.ADD_ERROR;

        return BorrowStatus.SUCCESS;
    }
    public BorrowStatus updateRecord(String id,String title,String author,
                   String phone,LocalDate borrowDate,LocalDate returnDate,String status){
        BorrowRecord record=borrowDAO.findById(id);
        Book book=bookDAO.find(title,author);
        Reader reader=readerDAO.findByPhone(phone);
        if(record==null) return BorrowStatus.RECORD_NOT_FOUND;
        if(book==null) return BorrowStatus.BOOK_NOT_FOUND;
        if(reader==null) return BorrowStatus.READER_NOT_FOUND;
        if(borrowDate.isAfter(LocalDate.now())) return BorrowStatus.BORROW_DATE_ERROR;
        if(returnDate.isBefore(borrowDate)) return BorrowStatus.RETURN_DATE_ERROR;
        List<String> list =new ArrayList<>(Arrays.asList("borrowed","overdue","returned"));
        status=status.toLowerCase();
        if(!list.contains(status)) return BorrowStatus.STATUS_ERROR;
        if(status.equals("returned") && !record.getStatus().equalsIgnoreCase("returned")) {
            System.out.println("❌ Vui lòng sử dụng tính năng 'Trả sách' riêng biệt để hệ thống cập nhật kho bãi!");
            return BorrowStatus.STATUS_ERROR;
        }

        Book oldBook=record.getBook();
        record.setBook(book);
        record.setStatus(status);
        record.setReader(reader);
        record.setBorrowDate(borrowDate);
        record.setReturnDate(returnDate);
        if (!borrowDAO.update(record)) return BorrowStatus.ERROR;
        if(!oldBook.getId().equals(book.getId())) {
            if(book.getQuantity()<=0) return  BorrowStatus.BOOK_OUT_OF_STOCK;
            bookDAO.update(new Book(oldBook.getId(), oldBook.getTitle(),
                    oldBook.getAuthor(), oldBook.getQuantity() + 1));
            bookDAO.update(new Book(book.getId(), book.getTitle(),
                    book.getAuthor(), book.getQuantity() - 1));
        }
        return BorrowStatus.SUCCESS;
    }
    public boolean returnBook(String id){
        BorrowRecord record=borrowDAO.findById(id);
        if(record==null || record.getStatus()=="return") return false;
        Book book=record.getBook();
        book.setQuantity(book.getQuantity()+1);
        bookDAO.update(book);
        record.setStatus("returned");
        borrowDAO.update(record);
        return true;
    }
    public void checkOverdue(){ borrowDAO.checkOverdue();}
    public boolean deleteRecord(String id){
        // 1. Tìm thông tin phiếu mượn trước khi xóa
        BorrowRecord record = borrowDAO.findById(id);
        if (record == null) return false;

        // 2. Nếu trạng thái là đang mượn hoặc quá hạn, phải trả lại số lượng cho sách
        if ("borrowed".equals(record.getStatus()) || "overdue".equals(record.getStatus())) {
            Book book = record.getBook();
            book.setQuantity(book.getQuantity() + 1);
            bookDAO.update(book); // Cập nhật lại kho
        }

        // 3. Thực hiện xóa phiếu mượn
        return borrowDAO.delete(id);
    }
    private int getMaxBorrowBooks() {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File configFile = new java.io.File("config.properties");
            if (configFile.exists()) {
                props.load(new java.io.FileInputStream(configFile));
                return Integer.parseInt(props.getProperty("MAX_BORROW_BOOKS", "3"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file cấu hình trong Service: " + e.getMessage());
        }
        return 3; // Mặc định nếu không tìm thấy file
    }
    public List<BorrowRecord> getAllRecord(){return borrowDAO.getAll();}
    public List<BorrowRecord> searchRecord(String keyword){return borrowDAO.searchRecord(keyword);}
}
