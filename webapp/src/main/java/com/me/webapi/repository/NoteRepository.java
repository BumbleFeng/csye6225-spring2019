package com.me.webapi.repository;

import com.me.webapi.pojo.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, String> {

}
