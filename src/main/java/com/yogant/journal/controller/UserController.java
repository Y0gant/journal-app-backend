package com.yogant.journal.controller;

import com.yogant.journal.entity.User;
import com.yogant.journal.model.SaveNewUserDTO;
import com.yogant.journal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping({"/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService1) {
        this.userService = userService1;
    }


    @GetMapping("/userInfo")
    public ResponseEntity<User> getUserByUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userService.getByUsername(auth.getName());
        if (user.isPresent()) {
            return ResponseEntity.of(user);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody SaveNewUserDTO user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        return userService.updateUser(userName, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Boolean> deleteUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean deleted = userService.deleteUserByUserName(auth.getName());
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }
}