package com.library.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reader {
    @Setter(AccessLevel.NONE)
    private String id;

    private String name;
    private String phone;
    private String email;
    public Reader(String id,String name,String phone){
        this(id,name,phone,null);
    }
}
