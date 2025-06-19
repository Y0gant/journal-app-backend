package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.User;
import com.y0gant.springDemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    public final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        List<User> all = userService.getAll();
        if (all != null && !all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create" +
            "-admin")
    public ResponseEntity<User> saveAdmin(@RequestBody User user) {
        return userService.saveAdmin(user).map(ResponseEntity::ok).get();
    }

}
