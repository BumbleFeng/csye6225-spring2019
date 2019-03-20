package com.me.webapi.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Note {

    @Id
    private String noteId;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private LocalDateTime created_on;
    private LocalDateTime last_updated_on;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "note", fetch = FetchType.EAGER)
    private List<Attachment> attachments;

    @ManyToOne(optional = false)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private User user;

    public Note() {
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreated_on() {
        return created_on;
    }

    public void setCreated_on(LocalDateTime created_on) {
        this.created_on = created_on;
    }

    public LocalDateTime getLast_updated_on() {
        return last_updated_on;
    }

    public void setLast_updated_on(LocalDateTime last_updated_on) {
        this.last_updated_on = last_updated_on;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
