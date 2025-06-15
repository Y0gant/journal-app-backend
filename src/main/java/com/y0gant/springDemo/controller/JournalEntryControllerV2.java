package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.service.JournalEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping({"/journal", "/"})
public class JournalEntryControllerV2 {

    private final JournalEntryService journalEntryService;

    public JournalEntryControllerV2(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }


    @GetMapping
    public List<JournalEntry> getAll() {
        return journalEntryService.getAll();
    }

    @GetMapping("id/{id}")
    public ResponseEntity<JournalEntry> getById(@PathVariable Long id) {
        return journalEntryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry) {
        return ResponseEntity.ok(journalEntryService.saveEntry(entry));
    }

    @DeleteMapping("id/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable long id) {
        boolean deleted = journalEntryService.deleteById(id);
        return ResponseEntity.ok(deleted);
    }


    @PutMapping("id/{id}")
    public ResponseEntity<JournalEntry> updateJournalWithId(@PathVariable long id, @RequestBody JournalEntry entry) {
        JournalEntry updated = journalEntryService.updateJournalById(id, entry);
        return ResponseEntity.ok(updated);
    }
}