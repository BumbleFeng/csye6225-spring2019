package com.me.webapi.pojo;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(indexes = {@Index(name = "USER_INDEX", columnList = "username", unique = true)})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String username;
    private String password;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user", fetch = FetchType.LAZY)
    private List<Note> noteList;

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

}
