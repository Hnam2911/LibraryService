package com.library.model;


public class Reader {

    private String id;

    private String name;
    private String phone;
    private String email;

    private int borrowedQuantity;
    public Reader(String id,String name,String phone,String email){
        this.id=id;
        this.name=name;
        this.email=email;
        this.phone=phone;
    }
    public Reader(String id,String name,String phone){this(id,name,phone,null);}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getBorrowedQuantity() {
        return borrowedQuantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBorrowedQuantity(int borrowedQuantity) {
        this.borrowedQuantity = borrowedQuantity;
    }
}
