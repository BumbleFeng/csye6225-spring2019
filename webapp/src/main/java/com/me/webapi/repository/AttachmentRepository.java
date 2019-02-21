package com.me.webapi.repository;

import com.me.webapi.pojo.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {

}
