package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reader {
    private String id;
    private String name;
    private String phone;
    private String email;
    public Reader(String id,String name,String phone){
        this(id,name,phone,null);
    }
}
