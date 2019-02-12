package com.me.webapi.repository;

import com.me.webapi.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);
    boolean existsByUsername(String username);

    @Transactional
    void deleteByUsername(String username);

}
