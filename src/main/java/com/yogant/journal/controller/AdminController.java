package com.yogant.journal.controller;

import com.yogant.journal.cache.JournalConfigCache;
import com.yogant.journal.entity.JournalConfig;
import com.yogant.journal.entity.User;
import com.yogant.journal.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    public final JournalConfigCache cache;
    private final UserService userService;
    private JournalConfigCache journalConfigCache;


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

    @PostMapping("create-configs")
    public ResponseEntity<String> createConfig(@RequestBody JournalConfig data) {
        String saved = "failed";
        saved = journalConfigCache.saveNewConfig(data);
        if (saved.equalsIgnoreCase("success")) return ResponseEntity.ok(saved);
        else return ResponseEntity.badRequest().body(saved);
    }

    @PostMapping("/{userName}/roles/admin")
    public ResponseEntity<?> grantAdmin(@PathVariable String userName) {
        log.info("Received request to grant ADMIN to user: {}", userName);
        try {
            Optional<User> userOpt = userService.grantAdminPrivilege(userName);
            return userOpt.map(user -> ResponseEntity.ok().body(user))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            log.error("Error while granting ADMIN to user [{}]: {}", userName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userName}/roles/admin")
    public ResponseEntity<?> revokeAdmin(@PathVariable String userName) {
        log.info("Received request to revoke ADMIN from user: {}", userName);
        try {
            Optional<User> userOpt = userService.revokeAdminPrivilege(userName);
            return userOpt.map(user -> ResponseEntity.ok().body(user))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            log.error("Error while revoking ADMIN from user [{}]: {}", userName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
