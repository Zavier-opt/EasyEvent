package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.easyevent.custom.AuthContext;
import com.example.easyevent.entity.BookingEntity;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.BookingEntityMapper;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.*;
import com.example.easyevent.util.TokenUtil;
import com.netflix.graphql.dgs.*;
import com.netflix.graphql.dgs.context.DgsContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DgsComponent
public class UserDataFetcher {
    private final UserEntityMapper userEntityMapper;
    private final EventEntityMapper eventEntityMapper;
    private final PasswordEncoder passwordEncoder;
    private final BookingEntityMapper bookingEntityMapper;


    public UserDataFetcher(UserEntityMapper userEntityMapper, EventEntityMapper eventEntityMapper, PasswordEncoder passwordEncoder, BookingEntityMapper bookingEntityMapper) {
        this.userEntityMapper = userEntityMapper;
        this.eventEntityMapper = eventEntityMapper;
        this.passwordEncoder = passwordEncoder;
        this.bookingEntityMapper = bookingEntityMapper;
    }
    @DgsQuery
    public List<User> users(DataFetchingEnvironment dfe){
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();

        List<UserEntity> userEntityList = userEntityMapper.selectList(null);
        List<User> userList = userEntityList.stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());
        return userList;
    }
    @DgsQuery
    public AuthData login(@InputArgument LoginInput loginInput){
        UserEntity userEntity = this.findUserByEmail(loginInput.getEmail());
        if(userEntity==null){
            throw new RuntimeException("The user who used the email does not exist");
        }

        boolean match = passwordEncoder.matches(loginInput.getPassword(), userEntity.getPassword());
        if(!match){
            throw new RuntimeException("Password incorrect!");
        }

        String token = TokenUtil.signToken(userEntity.getId(), 1);

        AuthData authData = new AuthData()
                .setUserId(userEntity.getId())
                .setToken(token)
                .setTokenExpiration(1);
        return authData;

    }


    @DgsMutation
    public User createUser(@InputArgument UserInput userInput){
        ensureUserNotExists(userInput);
        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setEmail(userInput.getEmail());
        newUserEntity.setPassword(passwordEncoder.encode(userInput.getPassword()));

        userEntityMapper.insert(newUserEntity);

        User newUser = User.fromEntity(newUserEntity);
        newUser.setPassword(null);

        return newUser;

    }
    @DgsData(parentType = "User", field = "createdEvents")
    public List<Event> createdEvents(DgsDataFetchingEnvironment dfe){
        User user = dfe.getSource();
        QueryWrapper<EventEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EventEntity::getCreatorId,user.getId());
        List<EventEntity> eventEntityList = eventEntityMapper.selectList(queryWrapper);
        List<Event> eventList = eventEntityList.stream()
                .map(Event::fromEntity)
                .collect(Collectors.toList());
        return eventList;
    }

    private void ensureUserNotExists(UserInput userInput){
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<UserEntity>();
        queryWrapper.lambda().eq(UserEntity::getEmail, userInput.getEmail());
        if(userEntityMapper.selectCount(queryWrapper)>0){
            throw new RuntimeException("This is email has been used");
        }
    }

    private UserEntity findUserByEmail(String email){
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getEmail, email);
        return userEntityMapper.selectOne(queryWrapper);
    }

    @DgsData(parentType = "User", field = "bookings")
    public List<Booking> bookings(DgsDataFetchingEnvironment dfe){
        User user = dfe.getSource();
        QueryWrapper<BookingEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BookingEntity::getUserId, user.getId());
        List<BookingEntity> bookingEntityList = bookingEntityMapper.selectList(queryWrapper);
        List<Booking> bookings = bookingEntityList
                .stream()
                .map(Booking::fromEntity)
                .collect(Collectors.toList());
        return bookings;


    }


}
