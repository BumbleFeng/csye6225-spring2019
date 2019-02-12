package com.me.webapi.controller;

import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.NoteRepository;
import com.me.webapi.service.NoteService;
import com.me.webapi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @RequestMapping(value = "/note", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Note>> noteList(HttpServletRequest request, HttpServletResponse response) {
        User user = userService.authorize(request);
        if (user == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(user.getNoteList(), HttpStatus.OK);
    }

    @RequestMapping(value = "/note", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Note> creatNote(HttpServletRequest request, @RequestBody Note note, HttpServletResponse response) {
        User user = userService.authorize(request);
        return noteService.create(note, user);
    }


    @RequestMapping(value = "/note/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Note> getNote(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.authorize(request);
        if (!noteRepository.existsById(id))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        Note note = noteRepository.findById(id).get();
        if (user == null || !user.equals(note.getUser()))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(note, HttpStatus.OK);
    }

    @RequestMapping(value = "/note/{id}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Note> updateNote(@PathVariable("id") String id, HttpServletRequest request, @RequestBody Note note, HttpServletResponse response) {
        User user = userService.authorize(request);
        return noteService.update(user, id, note);
    }

    @RequestMapping(value = "/note/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Note> deleteNote(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.authorize(request);
        if (!noteRepository.existsById(id))
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        Note note = noteRepository.findById(id).get();
        if (user == null || !user.equals(note.getUser()))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        noteRepository.delete(note);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
