package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final UserService userService;

    public PublicController(UserService userService1) {
        this.userService = userService1;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return userService.saveUser(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
