package com.library.model;

import lombok.*;

@Data
@NoArgsConstructor
public class Reader {
    @Setter(AccessLevel.NONE)
    private String id;

    private String name;
    private String phone;
    private String email;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private int borrowedQuantity;
    public Reader(String id,String name,String phone,String email){
        this.id=id;
        this.name=name;
        this.email=email;
        this.phone=phone;
    }
    public Reader(String id,String name,String phone){this(id,name,phone,null);}
}
