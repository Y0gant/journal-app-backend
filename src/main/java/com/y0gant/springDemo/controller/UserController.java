package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping({"/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService1) {
        this.userService = userService1;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/userName/{userName}")
    public ResponseEntity<User> getUserByUserName(@PathVariable String userName) {
        Optional<User> user = userService.getByUsername(userName);
        if (user.isPresent()) {
            return ResponseEntity.of(user);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return userService.updateUser(user.getUserName(), user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/userName/{userNAme}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable String userName) {
        boolean deleted = userService.deleteUserById(userName);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }
}