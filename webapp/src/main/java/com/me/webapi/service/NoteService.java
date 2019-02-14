package com.me.webapi.service;

import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.NoteRepository;
import com.me.webapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<Note> create(Note note, User user) {
        if (user == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        if (note.getTitle() == null || note.getContent() == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        note.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        note.setCreated_on(now);
        note.setLast_updated_on(now);
        note.setUser(user);
        noteRepository.save(note);
        return new ResponseEntity<>(note, HttpStatus.CREATED);
    }

    public HttpStatus update(User user, String id, Note note) {
        HttpStatus httpStatus = verify(user, id);
        if (httpStatus != null)
            return httpStatus;
        if (note.getTitle() == null || note.getContent() == null)
            return HttpStatus.BAD_REQUEST;
        Note old = noteRepository.findById(id).get();
        LocalDateTime now = LocalDateTime.now();
        old.setTitle(note.getTitle());
        old.setContent(note.getContent());
        old.setLast_updated_on(now);
        noteRepository.save(old);
        return HttpStatus.NO_CONTENT;
    }

    public HttpStatus verify(User user, String id) {
        if (!noteRepository.existsById(id))
            return HttpStatus.NOT_FOUND;
        Note old = noteRepository.findById(id).get();
        if (user == null || !user.equals(old.getUser()))
            return HttpStatus.UNAUTHORIZED;
        return null;
    }
}
