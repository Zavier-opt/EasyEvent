package com.example.easyevent.type;

import com.example.easyevent.entity.UserEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {
    private Integer id;
    private String email;
    private String password;
    private List<Event> createdEvents = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    public static User fromEntity(UserEntity userEntity){
        User user = new User();
        user.setEmail(userEntity.getEmail());
        user.setId(userEntity.getId());
        return user;
    }
}
