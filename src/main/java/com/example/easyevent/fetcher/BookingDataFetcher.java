package com.example.easyevent.fetcher;

import com.example.easyevent.custom.AuthContext;
import com.example.easyevent.entity.BookingEntity;
import com.example.easyevent.entity.EventEntity;
import com.example.easyevent.entity.UserEntity;
import com.example.easyevent.mapper.BookingEntityMapper;
import com.example.easyevent.mapper.EventEntityMapper;
import com.example.easyevent.mapper.UserEntityMapper;
import com.example.easyevent.type.Booking;
import com.example.easyevent.type.Event;
import com.example.easyevent.type.User;
import com.netflix.graphql.dgs.*;
import com.netflix.graphql.dgs.context.DgsContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DgsComponent
public class BookingDataFetcher {
    private final BookingEntityMapper bookingEntityMapper;
    private final EventEntityMapper eventEntityMapper;
    private final UserEntityMapper userEntityMapper;

    public BookingDataFetcher(BookingEntityMapper bookingEntityMapper, EventEntityMapper eventEntityMapper, UserEntityMapper userEntityMapper) {
        this.bookingEntityMapper = bookingEntityMapper;
        this.eventEntityMapper = eventEntityMapper;
        this.userEntityMapper = userEntityMapper;
    }

    @DgsQuery
    public List<Booking> bookings(){
        List<Booking> bookings = bookingEntityMapper.selectList(null)
                .stream()
                .map(Booking::fromEntity)
                .collect(Collectors.toList());
        return bookings;

    }


    @DgsMutation
    public Booking bookEvent(@InputArgument String eventId, DataFetchingEnvironment dfe){
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();

        UserEntity userEntity = authContext.getUserEntity();

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setUserId(userEntity.getId());
        bookingEntity.setEventId(Integer.parseInt(eventId));
        bookingEntity.setCreatedAt(new Date());
        bookingEntity.setUpdatedAt(new Date());

        bookingEntityMapper.insert(bookingEntity);

        Booking booking = Booking.fromEntity(bookingEntity);

        return booking;
    }

    @DgsMutation
    public Event cancelBooking(@InputArgument("bookingId") String bookingIdString, DataFetchingEnvironment dfe){
        AuthContext authContext = DgsContext.getCustomContext(dfe);
        authContext.ensureAuthenticated();

        Integer bookingId = Integer.parseInt(bookingIdString);

        BookingEntity bookingEntity = bookingEntityMapper.selectById(bookingId);

        if(bookingEntity == null){
            throw new RuntimeException(String.format("Booking with id $s does not exist", bookingIdString));
        }

        Integer userId = bookingEntity.getUserId();
        UserEntity userEntity = authContext.getUserEntity();
        if(!userEntity.getId().equals(userId)){
            throw new RuntimeException("You are not allowed to cancel other people's booking!");
        }

        bookingEntityMapper.deleteById(bookingId);

        Integer eventId = bookingEntity.getEventId();
        EventEntity eventEntity = eventEntityMapper.selectById(eventId);
        Event event = Event.fromEntity(eventEntity);

        return event;
    }

    @DgsData(parentType = "Booking", field = "user")
    public User user(DgsDataFetchingEnvironment dfe){
        Booking booking =dfe.getSource();
        UserEntity userEntity = userEntityMapper.selectById(booking.getUserId());
        User user = User.fromEntity(userEntity);
        return user;
    }

    @DgsData(parentType = "Booking", field = "event")
    public Event event(DgsDataFetchingEnvironment dfe){
        Booking booking = dfe.getSource();
        EventEntity eventEntity = eventEntityMapper.selectById(booking.getEventId());
        Event event = Event.fromEntity(eventEntity);
        return event;
    }
}
