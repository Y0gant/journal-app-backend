package com.yogant.journal.controller;

import com.yogant.journal.cache.JournalConfigCache;
import com.yogant.journal.entity.User;
import com.yogant.journal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    public final JournalConfigCache cache;
    private final UserService userService;

    AdminController(UserService userService, JournalConfigCache cache) {
        this.userService = userService;
        this.cache = cache;
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        List<User> all = userService.getAll();
        if (all != null && !all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<User> saveAdmin(@RequestBody User user) {
        return userService.saveAdmin(user).map(ResponseEntity::ok).get();
    }

    @GetMapping("clear-config-cache")
    public ResponseEntity<String> clearCache() {
        cache.init();
        return ResponseEntity.ok("Cache cleared..");
    }

}
