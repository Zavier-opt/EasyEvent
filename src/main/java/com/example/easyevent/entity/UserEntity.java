package com.example.easyevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.easyevent.type.UserInput;
import lombok.Data;

@Data
@TableName(value = "tb_user")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String email;
    private String password;

    public UserEntity fromUserInput(UserInput userInput){
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userInput.getEmail());
        userEntity.setPassword(userInput.getPassword());
        return userEntity;
    }
}
