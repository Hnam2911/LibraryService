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
        STATUS_ERROR
    }
    public BorrowStatus borrowBook(String title,String author,String phone,int days){
        Book book=bookDAO.find(title,author);
        if(book==null) return BorrowStatus.BOOK_NOT_FOUND;
        if(book.getQuantity()<=0) return BorrowStatus.BOOK_OUT_OF_STOCK;
        Reader reader=readerDAO.findByPhone(phone);
        if(reader==null) return BorrowStatus.READER_NOT_FOUND;
        BorrowRecord newRecord = new BorrowRecord(
                UUID.randomUUID().toString(), // Tự sinh ID
                reader,                       // Object Reader xịn lấy từ DB
                book,                         // Object Book xịn lấy từ DB
                LocalDate.now(),
                LocalDate.now().plusDays(days),
                "BORROWED"
        );

        borrowDAO.add(newRecord);
        book.setQuantity(book.getQuantity() - 1);
        bookDAO.update(book);

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
        if(!oldBook.getId().equals(book.getId())) {
            if(book.getQuantity()<=0) return  BorrowStatus.BOOK_OUT_OF_STOCK;
            bookDAO.update(new Book(oldBook.getId(), oldBook.getTitle(),
                    oldBook.getAuthor(), oldBook.getQuantity() + 1));
            bookDAO.update(new Book(book.getId(), book.getTitle(),
                    book.getAuthor(), book.getQuantity() - 1));
        }
        record.setBook(book);
        record.setStatus(status);
        record.setReader(reader);
        record.setBorrowDate(borrowDate);
        record.setReturnDate(returnDate);
        borrowDAO.update(record);
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
    public boolean deleteRecord(String id){return borrowDAO.delete(id);}
    public List<BorrowRecord> getAllRecord(){return borrowDAO.getAll();}
    public List<BorrowRecord> searchRecord(String keyword){return borrowDAO.searchRecord(keyword);}
}
