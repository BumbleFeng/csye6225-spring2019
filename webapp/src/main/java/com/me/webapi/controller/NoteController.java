package com.me.webapi.controller;

import com.me.webapi.pojo.Attachment;
import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.AttachmentRepository;
import com.me.webapi.repository.NoteRepository;
import com.me.webapi.service.AttachmentService;
import com.me.webapi.service.NoteService;
import com.me.webapi.service.UserService;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final UserService userService;

    private final NoteService noteService;

    private final NoteRepository noteRepository;

    private final AttachmentService attachmentService;

    private final AttachmentRepository attachmentRepository;

    private static final StatsDClient statsD = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @Value("${store}")
    private String store;

    @Autowired
    public NoteController(UserService userService, NoteService noteService, NoteRepository noteRepository, AttachmentService attachmentService, AttachmentRepository attachmentRepository) {
        this.userService = userService;
        this.noteService = noteService;
        this.noteRepository = noteRepository;
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
    }

    @GetMapping(value = "/note", produces = "application/json")
    public ResponseEntity noteList(HttpServletRequest request) {
        statsD.incrementCounter("note.list");
        String token = request.getHeader("Authorization");
        try {
            User user = userService.authorize(token);
            logger.info("Retrieve note list for user: " + user.getUsername());
            return new ResponseEntity<>(user.getNoteList(), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @PostMapping(value = "/note", produces = "application/json")
    public ResponseEntity creatNote(HttpServletRequest request, @RequestBody Note note) {
        statsD.incrementCounter("note.creat");
        String token = request.getHeader("Authorization");
        try {
            Note n = noteService.createNote(token, note);
            logger.info("Create note: " + note.getNoteId());
            return new ResponseEntity<>(n, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }


    @GetMapping(value = "/note/{idNotes}", produces = "application/json")
    public ResponseEntity getNote(@PathVariable("idNotes") String idNotes, HttpServletRequest request) {
        statsD.incrementCounter("note.get");
        String token = request.getHeader("Authorization");
        try {
            Note note = noteService.verify(token, idNotes);
            logger.info("Retrieve note: " + idNotes);
            return new ResponseEntity<>(note, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @PutMapping(value = "/note/{idNotes}", produces = "application/json")
    public ResponseEntity updateNote(@PathVariable("idNotes") String idNotes, HttpServletRequest request, @RequestBody Note note) {
        statsD.incrementCounter("note.update");
        String token = request.getHeader("Authorization");
        try {
            noteService.updateNote(token, idNotes, note);
            logger.info("Update note: " + idNotes);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @DeleteMapping(value = "/note/{idNotes}", produces = "application/json")
    public ResponseEntity deleteNote(@PathVariable("idNotes") String idNotes, HttpServletRequest request) {
        statsD.incrementCounter("note.delete");
        String token = request.getHeader("Authorization");
        try {
            Note note = noteService.verify(token, idNotes);
            noteService.deleteAttachment(note);
            noteRepository.delete(note);
            logger.info("Delete note: " + idNotes);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @GetMapping(value = "/note/{idNotes}/attachments", produces = "application/json")
    public ResponseEntity AttachmentList(@PathVariable("idNotes") String idNotes, HttpServletRequest request) {
        statsD.incrementCounter("attachment.list");
        String token = request.getHeader("Authorization");
        try {
            Note note = noteService.verify(token, idNotes);
            logger.info("Retrieve attachment list for note: " + idNotes);
            return new ResponseEntity<>(note.getAttachments(), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @PostMapping(value = "/note/{idNotes}/attachments", produces = "application/json")
    public ResponseEntity AttachAttachment(@PathVariable("idNotes") String idNotes, HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        statsD.incrementCounter("attachment.create");
        String token = request.getHeader("Authorization");
        try {
            Attachment attachment = noteService.attachAttachment(token, idNotes, file);
            logger.info("Create attachment: " + attachment.getAttachmentId());
            return new ResponseEntity<>(attachment, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @PutMapping(value = "/note/{idNotes}/attachments/{idAttachments}", produces = "application/json")
    public ResponseEntity UpdateAttachment(@PathVariable("idNotes") String idNotes, @PathVariable("idAttachments") String idAttachments, HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        statsD.incrementCounter("attachment.update");
        String token = request.getHeader("Authorization");
        try {
            noteService.updateAttachment(token, idNotes, idAttachments, file);
            logger.info("Update attachment: " + idAttachments);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }

    @DeleteMapping(value = "/note/{idNotes}/attachments/{idAttachments}", produces = "application/json")
    public ResponseEntity DeleteAttachment(@PathVariable("idNotes") String idNotes, @PathVariable("idAttachments") String idAttachments, HttpServletRequest request) {
        statsD.incrementCounter("attachment.delete");
        String token = request.getHeader("Authorization");
        try {
            noteService.verify(token, idNotes, idAttachments);
            attachmentService.delete(idAttachments, store.equals("aws"));
            attachmentRepository.deleteById(idAttachments);
            logger.info("Delete attachment: " + idAttachments);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ResponseStatusException e) {
            return new ResponseEntity(e.getStatus());
        }
    }
}