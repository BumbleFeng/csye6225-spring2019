package com.me.webapi.controller;

import com.me.webapi.pojo.Attachment;
import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.AttachmentRepository;
import com.me.webapi.repository.NoteRepository;
import com.me.webapi.service.AttachmentService;
import com.me.webapi.service.NoteService;
import com.me.webapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class NoteController {

    //private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final UserService userService;

    private final NoteService noteService;

    private final NoteRepository noteRepository;

    private final AttachmentService attachmentService;

    private final AttachmentRepository attachmentRepository;

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

    @GetMapping(value = "/note-zixuan", produces = "application/json")
    public ResponseEntity<List<Note>> noteList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try {
            User user = userService.authorize(token);
            return new ResponseEntity<>(user.getNoteList(), HttpStatus.OK);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @PostMapping(value = "/note", produces = "application/json")
    public ResponseEntity<Note> creatNote(HttpServletRequest request, @RequestBody Note note) {
        String token = request.getHeader("Authorization");
        try {
            Note n = noteService.createNote(token, note);
            return new ResponseEntity<>(n, HttpStatus.CREATED);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }


    @GetMapping(value = "/note/{idNotes}", produces = "application/json")
    public ResponseEntity<Note> getNote(@PathVariable("idNotes") String idNotes, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try {
            Note note = noteService.verify(token, idNotes);
            return new ResponseEntity<>(note, HttpStatus.OK);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @PutMapping(value = "/note/{idNotes}", produces = "application/json")
    public ResponseEntity<Note> updateNote(@PathVariable("idNotes") String idNotes, HttpServletRequest request, @RequestBody Note note) {
        String token = request.getHeader("Authorization");
        try {
            noteService.updateNote(token, idNotes, note);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @DeleteMapping(value = "/note/{idNotes}", produces = "application/json")
    public ResponseEntity<Note> deleteNote(@PathVariable("idNotes") String idNotes, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try {
            Note note = noteService.verify(token, idNotes);
            noteService.deleteAttachment(note);
            noteRepository.delete(note);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @GetMapping(value = "/note/{idNotes}/attachments", produces = "application/json")
    public ResponseEntity<List<Attachment>> AttachmentList(@PathVariable("idNotes") String idNotes, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try{
            Note note = noteService.verify(token, idNotes);
            return new ResponseEntity<>(note.getAttachments(), HttpStatus.OK);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @PostMapping(value = "/note/{idNotes}/attachments", produces = "application/json")
    public ResponseEntity<Attachment> AttachAttachment(@PathVariable("idNotes") String idNotes, HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String token = request.getHeader("Authorization");
        try{
            Attachment attachment = noteService.attachAttachment(token, idNotes, file);
            return new ResponseEntity<>(attachment, HttpStatus.OK);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @PutMapping(value = "/note/{idNotes}/attachments/{idAttachments}", produces = "application/json")
    public ResponseEntity<Attachment> UpdateAttachment(@PathVariable("idNotes") String idNotes, @PathVariable("idAttachments") String idAttachments, HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String token = request.getHeader("Authorization");
        try{
            noteService.updateAttachment(token, idNotes, idAttachments, file);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }

    @DeleteMapping(value = "/note/{idNotes}/attachments/{idAttachments}", produces = "application/json")
    public ResponseEntity<Attachment> DeleteAttachment(@PathVariable("idNotes") String idNotes, @PathVariable("idAttachments") String idAttachments, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try{
            noteService.verify(token, idNotes, idAttachments);
            attachmentService.delete(idAttachments, store.equals("aws"));
            attachmentRepository.deleteById(idAttachments);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getStatus());
        }
    }
}