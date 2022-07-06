package com.example.easyevent.type;

import com.example.easyevent.entity.UserEntity;
import lombok.Data;

@Data
public class User {
    private Integer id;
    private String email;
    private String password;

    public static User fromEntity(UserEntity userEntity){
        User user = new User();
        user.setEmail(userEntity.getEmail());
        user.setId(userEntity.getId());
        return user;
    }
}
