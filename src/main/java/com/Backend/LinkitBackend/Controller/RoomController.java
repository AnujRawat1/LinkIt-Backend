package com.Backend.LinkitBackend.Controller;

import com.Backend.LinkitBackend.Constants.AppConstants;
import com.Backend.LinkitBackend.Entity.JoinRoomRequest;
import com.Backend.LinkitBackend.Entity.MessageBody;
import com.Backend.LinkitBackend.Repository.RoomRepo;
import com.Backend.LinkitBackend.Services.GridFsService;
import com.Backend.LinkitBackend.Services.RoomService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/room")
@Slf4j
@CrossOrigin(origins = AppConstants.FRONTEND_URL_PROD + "," + AppConstants.FRONTEND_URL_DEV)
public class RoomController {

    @Autowired
    private RoomService roomServive;

    @Autowired
    private RoomRepo roomRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GridFsService gridFsService;

    @PostMapping("/createRoom")
    public ResponseEntity<MessageBody> createRoom(@RequestBody JoinRoomRequest requestBody) {
        try{
            MessageBody room =  roomServive.createRoom(requestBody.getName());
            return ResponseEntity.ok(room); // Placeholder for actual validation logic
        }catch (Exception e){
            log.error("Error creating room: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/joinRoom")
    public ResponseEntity<MessageBody> joinRoom(@RequestBody JoinRoomRequest request) {
        String roomId = request.getRoomId();
        String name = request.getName();
        boolean isValid = (roomRepo.findByRoomId(roomId) != null) ;
        if (isValid) { // Room Exists
            log.info("Room ID is valid: " + roomId);
            MessageBody room = roomServive.getRoom(roomId, name);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
            return ResponseEntity.ok(room);
        } else{
            log.error("Room ID is invalid or Room Does not Exists: " + roomId);
            return ResponseEntity.status(204).body(null);
        }
    }

    @GetMapping("/getContent")
    public ResponseEntity<String> getContent(@RequestParam String roomId) {
        boolean isValid = (roomRepo.findByRoomId(roomId) != null) ;
        if (isValid) { // Room Exists
            MessageBody room = roomServive.getRoom(roomId);
            log.info("Content fetched for Room ID: " + roomId);
            return ResponseEntity.ok(room.getContent());
        } else{
            return ResponseEntity.status(204).body(null);
        }
    }

    @GetMapping("/getParticipants")
    public ResponseEntity<List<String>> getParticipants(@RequestParam String roomId) {
        boolean isValid = (roomRepo.findByRoomId(roomId) != null) ;
        if (isValid) { // Room Exists
            MessageBody room = roomServive.getRoom(roomId);
            return ResponseEntity.ok(room.getParticipants());
        } else{
            return ResponseEntity.status(204).body(null);
        }
    }



    // RoomController.java
    @DeleteMapping("/removeParticipant")
    public ResponseEntity<MessageBody> removeParticipant(@RequestParam String roomId, @RequestParam String participantName) {
        boolean isValid = (roomRepo.findByRoomId(roomId) != null);
        if (isValid) { // Room Exists
            MessageBody room = roomServive.getRoom(roomId);
            List<String> participants = room.getParticipants();
            if (participants.remove(participantName)) {
                room.setParticipants(participants);
                if (participants.isEmpty()) {
                    // Delete GridFS files
                    room.getFileNames().forEach(file -> gridFsService.deleteFile(file.getGridFsId()));
                    roomRepo.delete(room);
                    log.info("Room deleted as no participants remain: " + roomId);
                    messagingTemplate.convertAndSend("/topic/room/" + roomId, Optional.ofNullable(null));
                    return ResponseEntity.ok(null);
                } else {
                    // Save updated room if participants remain
                    roomRepo.save(room);
                    log.info("Participant removed: " + participantName + " from Room ID: " + roomId);
                    messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
                    return ResponseEntity.ok(room);
                }
            } else {
                return ResponseEntity.status(404).body(null); // Participant not found
            }
        } else {
            return ResponseEntity.status(204).body(null); // Room not found
        }
    }

    @GetMapping("/getFileNames")
    public ResponseEntity<List<String>> getFileNames(@RequestParam String roomId) {
        MessageBody room = roomRepo.findByRoomId(roomId);
        if (room != null) { // Room exists
            List<String> fileNames = room.getFileNames() != null
                    ? room.getFileNames().stream()
                    .map(MessageBody.FileMetadata::getName)
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            log.info("File names fetched for Room ID: {}", roomId);
            return ResponseEntity.ok(fileNames);
        } else {
            log.error("Room ID is invalid or does not exist: {}", roomId);
            return ResponseEntity.status(204).body(null);
        }
    }
    @PostMapping("/uploadFiles")
    public ResponseEntity<MessageBody> uploadFiles(
            @RequestParam String roomId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            MessageBody room = roomRepo.findByRoomId(roomId);
            if (room == null) {
                return ResponseEntity.status(204).body(null);
            }

            List<MessageBody.FileMetadata> fileMetadataList = room.getFileNames() != null ? room.getFileNames() : new ArrayList<>();
            for (MultipartFile file : files) {
                String gridFsId = gridFsService.storeFile(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
                fileMetadataList.add(new MessageBody.FileMetadata(
                        file.getOriginalFilename(),
                        gridFsId,
                        file.getSize(),
                        file.getContentType()
                ));
            }

            room.setFileNames(fileMetadataList);
            roomRepo.save(room);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
            log.info("Files uploaded to Room ID: " + roomId);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            log.error("Error uploading files: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam String gridFsId, @RequestParam String roomId) {
        try {
            MessageBody room = roomRepo.findByRoomId(roomId);
            if (room == null) {
                return ResponseEntity.status(204).body(null);
            }

            GridFSFile gridFSFile = gridFsService.getFileMetadata(gridFsId);
            if (gridFSFile == null) {
                return ResponseEntity.status(404).body(null);
            }

            InputStream inputStream = gridFsService.downloadFile(gridFsId);
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + gridFSFile.getFilename() + "\"")
                    .contentType(MediaType.parseMediaType(gridFSFile.getMetadata().getString("contentType")))
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
}
