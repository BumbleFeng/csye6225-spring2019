package com.me.webapi.controller;

import com.me.webapi.pojo.Reset;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.UserRepository;
import com.me.webapi.service.ResetService;
import com.me.webapi.service.UserService;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HomeController {

    private final UserRepository userRepository;

    private final UserService userService;

    private final ResetService resetService;

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private static final StatsDClient statsD = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @Autowired
    public HomeController(UserRepository userRepository, UserService userService, ResetService resetService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.resetService = resetService;
    }

    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity index(HttpServletRequest request) {
        statsD.incrementCounter("root");
        String token = request.getHeader("Authorization");
        return userService.login(token);
    }

    @PostMapping(value = "/user/register", produces = "application/json")
    public ResponseEntity userRegister(@RequestBody User user) {
        statsD.incrementCounter("user.register");
        return userService.register(user);
    }

    @PostMapping(value = "/reset", produces = "application/json")
    public ResponseEntity reset(@RequestBody Reset reset) {
        statsD.incrementCounter("reset");
        if (reset.getEmail() == null || userRepository.findByUsername(reset.getEmail()).orElse(null) == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        resetService.publish(reset);
        logger.info(reset.getEmail() + " reset password");
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
