package com.library.dao;

import com.library.model.BorrowRecord;

import java.util.List;

public interface IBorrowRecordDAO {
    boolean add(BorrowRecord record);
    boolean update(BorrowRecord record);
    boolean delete(String id);
    List<BorrowRecord> getAll();
    List<BorrowRecord> searchRecord(String keyword);
    BorrowRecord findById(String id);
    void checkOverdue();
}