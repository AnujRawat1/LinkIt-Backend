package com.Backend.LinkitBackend.Repository;

import com.Backend.LinkitBackend.Entity.MessageBody;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepo extends MongoRepository<MessageBody, String>  {
//    public boolean existsByRoomId(String roomId);
    public MessageBody findByRoomId(String roomId);
}