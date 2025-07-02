package com.yogant.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleAuthResponseDTO {
    private String token;
    private String email;
    private String userName;
    private String picture;
}
