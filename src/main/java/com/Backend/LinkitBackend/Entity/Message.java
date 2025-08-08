package com.Backend.LinkitBackend.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private String content;
    private List<String> participants;
    private List<String> fileNames;
}
