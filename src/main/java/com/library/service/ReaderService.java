package com.library.service;

import com.library.dao.IReaderDAO;
import com.library.dao.ReaderDAO;
import com.library.model.Reader;
import lombok.Locked;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExternalRow;

import java.util.List;
import java.util.UUID;

public class ReaderService {
    public enum ReaderStatus{
        PHONE_EXIST,
        EMAIL_EXIST,
        FORMAT_ERROR,
        ERROR,
        SUCCESS
    }
    private IReaderDAO readerDAO=new ReaderDAO();
    public static boolean check(Reader reader){
        if(reader.getEmail()!=null){
            if(!AppValidator.isEmail(reader.getEmail())){
                System.out.println("Invalid Email! Please check again");
                return false;
            }
        }
        if(!AppValidator.isPhone(reader.getPhone())) {
            System.out.println("Invalid phone number!Please check again");
            return false;
        }
        return true;
    }
    public ReaderStatus addReader(String name,String phone,String email){
        Reader reader= new Reader(
                UUID.randomUUID().toString(),
                name,phone,email
        );
        if(!check(reader)) return ReaderStatus.FORMAT_ERROR;
        if(readerDAO.findByPhone(reader.getPhone())!=null){
            System.out.println("Phone already exists!");
            return ReaderStatus.PHONE_EXIST;
        }
        if(reader.getEmail()!=null && readerDAO.findByEmail(reader.getEmail())!=null){
            System.out.println("Email already exists!");
            return ReaderStatus.EMAIL_EXIST;
        }
        if(readerDAO.add(reader)) return ReaderStatus.SUCCESS;
        return ReaderStatus.ERROR;
    }
    public ReaderStatus updateReader(String id,String name,String phone,String email){
        Reader reader=new Reader(id,name,phone,email);
        if(!check(reader)) return ReaderStatus.FORMAT_ERROR;
        Reader phoneReader=readerDAO.findByPhone(reader.getPhone());
        if(phoneReader!=null && !phoneReader.getId().equals(reader.getId())){
            System.out.println("Phone number already exists");
            return ReaderStatus.PHONE_EXIST;
        }
        if(reader.getEmail()!=null){
            Reader emailReader=readerDAO.findByEmail(reader.getEmail());
            if(emailReader!=null && !emailReader.getId().equals(reader.getId())) {
                System.out.println("Email already exists");
                return ReaderStatus.EMAIL_EXIST;
            }
        }
        if(readerDAO.update(reader)) return ReaderStatus.SUCCESS;
        return ReaderStatus.ERROR;
    }
    public boolean deleteReader(String id){return readerDAO.delete(id);}
    public List<Reader> getAllReader(){return readerDAO.getAll();}
    public List<Reader> searchReader(String keyword){return readerDAO.searchReader(keyword);}
}
