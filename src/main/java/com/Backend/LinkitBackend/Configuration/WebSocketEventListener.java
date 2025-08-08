package com.Backend.LinkitBackend.Configuration;

import com.Backend.LinkitBackend.Entity.MessageBody;
import com.Backend.LinkitBackend.Repository.RoomRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Optional;

@Component
public class WebSocketEventListener {

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        // Map sessionId to roomId and participantName (requires storing this mapping)
        // For simplicity, assume you have a way to get roomId and participantName
        // Example: You may need a service to track active sessions
        String roomId = getRoomIdForSession(sessionId); // Implement this
        String participantName = getParticipantNameForSession(sessionId); // Implement this

        if (roomId != null && participantName != null) {
            MessageBody room = roomRepo.findByRoomId(roomId);
            if (room != null) {
                List<String> participants = room.getParticipants();
                if (participants.remove(participantName)) {
                    if (participants.isEmpty()) {
                        roomRepo.delete(room);
                        messagingTemplate.convertAndSend("/topic/room/" + roomId, Optional.ofNullable(null));
                    } else {
                        roomRepo.save(room);
                        messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
                    }
                }
            }
        }
    }

    // Placeholder methods (implement based on your session management)
    private String getRoomIdForSession(String sessionId) {
        // Implement logic to map sessionId to roomId
        return null;
    }

    private String getParticipantNameForSession(String sessionId) {
        // Implement logic to map sessionId to participantName
        return null;
    }
}