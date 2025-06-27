package com.yogant.journal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentEmailDTO {
    private String email;
    private String subject;
    private String body;
}
