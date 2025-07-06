package com.yogant.journal.service;

import com.yogant.journal.entity.JournalEntry;
import com.yogant.journal.entity.User;
import com.yogant.journal.repository.JournalEntryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepo journalEntryRepo;

    @Mock
    private UserService userService;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private User testUser;
    private JournalEntry entry;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserName("yogant");
        testUser.setJournalEntries(new ArrayList<>());

        entry = new JournalEntry();
        entry.setId("entry-123");
        entry.setTitle("Test Title");
        entry.setContent("Test Content");
        entry.setDate(LocalDateTime.now());
    }

    @Test
    void testSaveEntry_Success() {
        when(userService.getByUsername("yogant")).thenReturn(Optional.of(testUser));
        when(journalEntryRepo.save(any(JournalEntry.class))).thenReturn(entry);

        Optional<JournalEntry> result = journalEntryService.saveEntry(entry, "yogant");

        assertTrue(result.isPresent());
        assertEquals("entry-123", result.get().getId());
        verify(userService).saveUser(testUser);
    }

    @Test
    void testSaveEntry_UserNotFound() {
        when(userService.getByUsername("unknown")).thenReturn(Optional.empty());

        Optional<JournalEntry> result = journalEntryService.saveEntry(entry, "unknown");

        assertTrue(result.isEmpty());
        verify(journalEntryRepo, never()).save(any());
    }

    @Test
    void testGetById_Found() {
        when(journalEntryRepo.findJournalEntriesById("entry-123")).thenReturn(Optional.of(entry));

        Optional<JournalEntry> result = journalEntryService.getById("entry-123");

        assertTrue(result.isPresent());
        assertEquals("entry-123", result.get().getId());
    }

    @Test
    void testGetById_NotFound() {
        when(journalEntryRepo.findJournalEntriesById("invalid")).thenReturn(Optional.empty());

        Optional<JournalEntry> result = journalEntryService.getById("invalid");

        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateJournalById_Success() {
        testUser.getJournalEntries().add(entry);
        JournalEntry update = new JournalEntry();
        update.setTitle("Updated Title");
        update.setContent("Updated Content");

        when(userService.getByUsername("yogant")).thenReturn(Optional.of(testUser));
        when(journalEntryRepo.save(any(JournalEntry.class))).thenReturn(entry);

        Optional<JournalEntry> result = journalEntryService.updateJournalById("yogant", "entry-123", update);

        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Content", result.get().getContent());
    }

    @Test
    void testUpdateJournalById_UserNotFound() {
        when(userService.getByUsername("unknown")).thenReturn(Optional.empty());

        Optional<JournalEntry> result = journalEntryService.updateJournalById("unknown", "entry-123", entry);

        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateJournalById_EntryNotFound() {
        when(userService.getByUsername("yogant")).thenReturn(Optional.of(testUser)); // No entries

        Optional<JournalEntry> result = journalEntryService.updateJournalById("yogant", "missing-id", entry);

        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteById_Success() {
        testUser.getJournalEntries().add(entry);

        when(userService.getByUsername("yogant")).thenReturn(Optional.of(testUser));

        boolean deleted = journalEntryService.deleteById("entry-123", "yogant");

        assertTrue(deleted);
        verify(journalEntryRepo).deleteById("entry-123");
    }

    @Test
    void testDeleteById_UserNotFound() {
        when(userService.getByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = journalEntryService.deleteById("entry-123", "unknown");

        assertFalse(result);
        verify(journalEntryRepo, never()).deleteById((String) any());
    }

    @Test
    void testDeleteById_EntryNotFound() {
        when(userService.getByUsername("yogant")).thenReturn(Optional.of(testUser));

        boolean result = journalEntryService.deleteById("entry-999", "yogant");

        assertFalse(result);
        verify(journalEntryRepo, never()).deleteById((Long) any());
    }

    @Test
    void testSaveMultipleEntries_Success() {
        List<JournalEntry> entries = List.of(entry);

        when(userService.getByUsername("yogant")).thenReturn(Optional.of(testUser));
        when(journalEntryRepo.saveAll(anyList())).thenReturn(entries);

        List<JournalEntry> result = journalEntryService.saveMultipleEntries(entries, "yogant");

        assertEquals(1, result.size());
        assertEquals("entry-123", result.get(0).getId());
        verify(userService).saveUser(testUser);
    }

    @Test
    void testSaveMultipleEntries_UserNotFound() {
        when(userService.getByUsername("unknown")).thenReturn(Optional.empty());

        List<JournalEntry> result = journalEntryService.saveMultipleEntries(List.of(entry), "unknown");

        assertTrue(result.isEmpty());
        verify(journalEntryRepo, never()).saveAll(anyList());
    }

    @Test
    void testSaveMultipleEntries_EmptyList() {
        List<JournalEntry> result = journalEntryService.saveMultipleEntries(Collections.emptyList(), "yogant");

        assertTrue(result.isEmpty());
        verify(journalEntryRepo, never()).saveAll(any());
    }
}
