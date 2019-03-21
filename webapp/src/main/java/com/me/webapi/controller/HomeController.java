package com.me.webapi.controller;

import com.me.webapi.pojo.User;
import com.me.webapi.service.UserService;
import com.me.webapi.util.ErrorMessage;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HomeController {

    private final UserService userService;

    private static final StatsDClient statsD = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity<ErrorMessage> index(HttpServletRequest request) {
        statsD.incrementCounter("root");
        String token = request.getHeader("Authorization");
        return userService.login(token);
    }

    @PostMapping(value = "/user/register", produces = "application/json")
    public ResponseEntity<ErrorMessage> userRegister(@RequestBody User user) {
        statsD.incrementCounter("user.register");
        return userService.register(user);
    }

}
