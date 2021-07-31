package com.rex.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 4690774687363097265L;

    private int id;

    private String name;

    private String nickName;

    private String password;

    private String headImage;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
