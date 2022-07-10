package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.easyevent.custom.AuthContext;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.Event;
import com.example.easyevent.type.EventInput;
import com.example.easyevent.type.User;
import com.netflix.graphql.dgs.*;
import com.netflix.graphql.dgs.context.DgsContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class EventDataFetcher {
    private final EventEntityMapper eventEntityMapper;
    private final UserEntityMapper userEntityMapper;

    @DgsQuery
    public List<Event> events(){
        List<EventEntity> eventEntityList = eventEntityMapper.selectList(new QueryWrapper<>());
        List<Event> eventList = eventEntityList.stream()
                .map(Event::fromEntity).collect(Collectors.toList());
        return eventList;
    }

    @DgsMutation
    public Event createEvent(@InputArgument(name = "eventInput") EventInput input, DataFetchingEnvironment dfe){
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();


        EventEntity newEventEntity = EventEntity.fromEventInput(input);
        newEventEntity.setCreatorId(authContext.getUserEntity().getId());

        eventEntityMapper.insert(newEventEntity);

        Event newEvent = Event.fromEntity(newEventEntity);

        return newEvent;
    }

//    private void populateEventWithUser(Event event, Integer userID){
//        UserEntity userEntity = userEntityMapper.selectById(userID);
//        User user = User.fromEntity(userEntity);
//        event.setCreator(user);
//    }
    @DgsData(parentType = "Event", field = "creator")
    public User creator(DgsDataFetchingEnvironment dfe){
        Event event = dfe.getSource();
        UserEntity userEntity = userEntityMapper.selectById(event.getCreatorId());
        User user = User.fromEntity(userEntity);
        return user;
    }

}
