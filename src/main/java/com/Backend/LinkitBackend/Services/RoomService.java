package com.Backend.LinkitBackend.Services;

import com.Backend.LinkitBackend.Entity.MessageBody;
import com.Backend.LinkitBackend.Repository.RoomRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class RoomService {

    @Autowired
    private RoomRepo roomRepo;

    public static String generateRoomId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
    }

    public MessageBody createRoom(String name) {
        try {
            String roomId = "";

            // Ensure the room ID is unique
            do {
                roomId = generateRoomId();
            } while (roomRepo.findByRoomId(roomId) != null);

            // Create a new room with the unique ID
            MessageBody body = new MessageBody();
            body.setRoomId(roomId);
            body.setContent("// Start typing your Ideas here... \n\n");
            body.setParticipants(new ArrayList<>());
            body.getParticipants().add(name); // Add the creator as the first participant
            body.setFileNames(new ArrayList<>());
            body.setCreatedAt(new Date());

            roomRepo.save(body);
            log.info("Room created with ID: {}, {}", roomId, name);
            return body;

        } catch (Exception e) {
            log.error("Exception occurred while creating the room: {}", e.getMessage());
            throw new RuntimeException("Exception occurred while creating the room: " + e.getMessage());
        }
    }

    public MessageBody getRoom(String roomId, String name) {
        try {
            MessageBody room = roomRepo.findByRoomId(roomId);
            if (room != null) {
                room.getParticipants().add(name); // Add the new participant
                roomRepo.save(room); // Save the updated room
                return room;
            } else {
                log.error("Room not found with ID: " + roomId);
                throw new RuntimeException("Room not found with ID: " + roomId);
            }
        } catch (Exception e) {
            log.error("Exception occurred while fetching the room: " + e.getMessage());
            throw new RuntimeException("Exception occurred while fetching the room: " + e.getMessage());
        }
    }
    public MessageBody getRoom(String roomId) {
        try {
            MessageBody room = roomRepo.findByRoomId(roomId);
            if (room != null) {
                return room;
            } else {
                log.error("Room not found with ID: " + roomId);
                throw new RuntimeException("Room not found with ID: " + roomId);
            }
        } catch (Exception e) {
            log.error("Exception occurred while fetching the room: " + e.getMessage());
            throw new RuntimeException("Exception occurred while fetching the room: " + e.getMessage());
        }
    }
}
