package com.example.easyevent.custom;

import com.example.easyevent.entity.UserEntity;
import lombok.Data;

@Data
public class AuthContext {
    private UserEntity userEntity;
    private boolean tokenInvalid;

    public void ensureAuthenticated(){
        if(tokenInvalid) throw new RuntimeException("Invalid Token!");
        if(userEntity == null) throw new RuntimeException("Need login in!");
    }
}
