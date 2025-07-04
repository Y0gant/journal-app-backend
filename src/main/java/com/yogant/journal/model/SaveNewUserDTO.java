package com.yogant.journal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SaveNewUserDTO {
    String userName;
    String password;
    String email;
    @JsonProperty("sentiment_analysis")
    boolean sentimentAnalysis;
}
