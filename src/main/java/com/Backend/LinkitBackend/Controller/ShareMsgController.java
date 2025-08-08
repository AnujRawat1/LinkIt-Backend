package com.Backend.LinkitBackend.Controller;

import com.Backend.LinkitBackend.Constants.AppConstants;
import com.Backend.LinkitBackend.Entity.Message;
import com.Backend.LinkitBackend.Entity.MessageBody;
import com.Backend.LinkitBackend.Repository.RoomRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

@Controller
@CrossOrigin(origins = AppConstants.FRONTEND_URL_PROD + "," + AppConstants.FRONTEND_URL_DEV)
public class ShareMsgController {

    @Autowired
    private RoomRepo roomRepo;

    // Method to send & receive a message
    @MessageMapping("/sendMessage/{roomId}")  // app/sendMessage/{roomId}
    @SendTo("/topic/room/{roomId}")  // Client Subscribe
    public MessageBody sendMessage(
            @DestinationVariable String roomId,
            @RequestBody MessageBody requestBody
    ) {
        MessageBody body = roomRepo.findByRoomId(requestBody.getRoomId());
        if (body == null) {
            throw new RuntimeException("Room not found with ID (For Sending Message): " + requestBody.getRoomId());
        }

        body.setContent(requestBody.getContent());
        body.setCreatedAt(new Date());
        body.setParticipants(requestBody.getParticipants());
        body.setFileNames(requestBody.getFileNames());
        roomRepo.save(body);

        return body;
    }
}
