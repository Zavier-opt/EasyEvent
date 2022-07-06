package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.User;
import com.example.easyevent.type.UserInput;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@DgsComponent
public class UserDataFetcher {
    private final UserEntityMapper userEntityMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDataFetcher(UserEntityMapper userEntityMapper, PasswordEncoder passwordEncoder) {
        this.userEntityMapper = userEntityMapper;
        this.passwordEncoder = passwordEncoder;
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

    private void ensureUserNotExists(UserInput userInput){
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<UserEntity>();
        queryWrapper.lambda().eq(UserEntity::getEmail, userInput.getEmail());
        if(userEntityMapper.selectCount(queryWrapper)>1){
            throw new RuntimeException("This is email has been used");
        }
    }


}
