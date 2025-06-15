package com.y0gant.springDemo.controller;

import com.y0gant.springDemo.entity.JournalEntry;
import com.y0gant.springDemo.service.JournalEntryService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<JournalEntry>> getAll() {
        List<JournalEntry> entries = journalEntryService.getAll();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("id/{id}")
    public ResponseEntity<JournalEntry> getById(@PathVariable Long id) {
        return journalEntryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry) {
        return journalEntryService.saveEntry(entry).map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("id/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable long id) {
        boolean deleted = journalEntryService.deleteById(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else return ResponseEntity.notFound().build();
    }


    @PutMapping("id/{id}")
    public ResponseEntity<JournalEntry> updateJournalWithId(@PathVariable long id, @RequestBody JournalEntry entry) {
        return journalEntryService.updateJournalById(id, entry).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<JournalEntry>> createMultipleEntries(@RequestBody List<JournalEntry> entries) {
        return ResponseEntity.status(HttpStatus.CREATED).body(journalEntryService.saveMultipleEntries(entries));
    }
}