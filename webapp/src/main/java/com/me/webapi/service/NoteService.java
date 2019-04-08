package com.me.webapi.service;

import com.me.webapi.pojo.Attachment;
import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.AttachmentRepository;
import com.me.webapi.repository.NoteRepository;
import com.me.webapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NoteService {

    private final UserRepository userRepository;

    private final UserService userService;

    private final NoteRepository noteRepository;

    private final AttachmentService attachmentService;

    private final AttachmentRepository attachmentRepository;

    private final EntityManager entityManager;

    @Autowired
    public NoteService(UserRepository userRepository, UserService userService, NoteRepository noteRepository, AttachmentRepository attachmentRepository, AttachmentService attachmentService, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.noteRepository = noteRepository;
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
        this.entityManager = entityManager;
    }


    public Note createNote(String token, Note note) {
        String title = note.getTitle();
        String content = note.getContent();
        if(title == null || title.isEmpty() && content == null || content.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        User user = userService.authorize(token);
        note.setNoteId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        note.setCreated_on(now);
        note.setLast_updated_on(now);
        note.setUser(user);
        noteRepository.save(note);
        return note;
    }

    public void updateNote(String token, String noteId, Note note) {
        String title = note.getTitle();
        String content = note.getContent();
        if(title == null || title.isEmpty() && content == null || content.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Note n = verify(token, noteId);
        LocalDateTime now = LocalDateTime.now();
        n.setTitle(note.getTitle());
        n.setContent(note.getContent());
        n.setLast_updated_on(now);
        noteRepository.save(n);
    }

    public Attachment attachAttachment(String token, String noteId, MultipartFile file) {
        Note note = verify(token, noteId);
        Attachment attachment = attachmentService.createAttachment(file);
        attachment.setNote(note);
        attachmentRepository.save(attachment);
        return attachment;
    }

    public void updateAttachment(String token, String noteId, String attachmentId, MultipartFile file) {
        Attachment attachment = verify(token, noteId, attachmentId);
        Note n = attachment.getNote();
        LocalDateTime now = LocalDateTime.now();
        n.setLast_updated_on(now);
        noteRepository.save(n);
        attachmentService.store(attachment, file);
        attachmentRepository.save(attachment);
    }

    public Note verify(String token, String noteId) {
        User user = userService.authorize(token);
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!note.getUser().getUserId().equals(user.getUserId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return note;
    }

    public Attachment verify(String token, String noteId, String attachmentId) {
        Note note = verify(token, noteId);
        Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
        if (attachment == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!attachment.getNote().getNoteId().equals(note.getNoteId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return attachment;
    }

    @Transactional
    public void truncate() {
        attachmentService.deleteList(attachmentRepository.findAll());
        userRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE user AUTO_INCREMENT = 1").executeUpdate();
    }

}
