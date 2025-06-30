package com.yogant.journal.model;

import lombok.Data;

@Data
public class SaveNewUserDTO {
    String userName;
    String password;
    String email;
}
