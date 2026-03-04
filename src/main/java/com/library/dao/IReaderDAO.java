package com.library.dao;

import com.library.model.Reader;

import java.util.List;

public interface IReaderDAO {
    boolean add(Reader reader);
    boolean update(Reader reader);
    boolean delete(String id);
    List<Reader> getAll();
    Reader findById(String id);
    List<Reader> searchReader(String keyword);
    Reader findByPhone(String phone);
    Reader findByEmail(String email);
}
