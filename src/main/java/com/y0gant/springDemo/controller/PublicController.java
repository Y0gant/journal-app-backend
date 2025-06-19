package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/public", "/"})
public class PublicController {

    private final UserService userService;

    public PublicController(UserService userService1) {
        this.userService = userService1;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return userService.saveNewUser(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping
    public String healthCheckup() {
        return "Server is running...";
    }
}
