package com.Backend.LinkitBackend.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinRoomRequest {
    private String roomId;
    private String name;
    private List<String> participants;
}
