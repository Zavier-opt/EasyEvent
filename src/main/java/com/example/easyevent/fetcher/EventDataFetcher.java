package com.example.easyevent.fetcher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.Event;
import com.example.easyevent.type.EventInput;
import com.example.easyevent.type.User;
import com.netflix.graphql.dgs.*;
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
                .map(eventEntity -> {
                    Event event = Event.fromEntity(eventEntity);
                    populateEventWithUser(event, eventEntity.getCreatorId());
                    return event;
                }).collect(Collectors.toList());
        return eventList;
    }

    @DgsMutation
    public Event createEvent(@InputArgument(name = "eventInput") EventInput input){
        EventEntity newEventEntity = EventEntity.fromEventInput(input);

        eventEntityMapper.insert(newEventEntity);

        Event newEvent = Event.fromEntity(newEventEntity);

        populateEventWithUser(newEvent, newEventEntity.getCreatorId());

        return newEvent;
    }

    private void populateEventWithUser(Event event, Integer userID){
        UserEntity userEntity = userEntityMapper.selectById(userID);
        User user = User.fromEntity(userEntity);
        event.setCreator(user);
    }

}
