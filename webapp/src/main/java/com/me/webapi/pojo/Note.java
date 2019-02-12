package com.me.webapi.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Note {

    @Id
    private String id;
    private String content;
    private String title;
    private LocalDateTime created_on;
    private LocalDateTime last_updated_on;

    @ManyToOne(optional = false)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private User user;

    public Note() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreated_on() { return created_on; }

    public void setCreated_on(LocalDateTime created_on) {
        this.created_on = created_on;
    }

    public LocalDateTime getLast_updated_on() {
        return last_updated_on;
    }

    public void setLast_updated_on(LocalDateTime last_updated_on) {
        this.last_updated_on = last_updated_on;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", created_on=" + created_on +
                ", last_updated_on=" + last_updated_on +
                ", user=" + user +
                '}';
    }
}
