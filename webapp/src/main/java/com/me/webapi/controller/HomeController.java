package com.me.webapi.controller;

import com.me.webapi.pojo.User;
import com.me.webapi.service.UserService;
import com.me.webapi.util.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HomeController {

    //private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity<ErrorMessage> index(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.login(token);
    }

    @PostMapping(value = "/user/register", produces = "application/json")
    public ResponseEntity<ErrorMessage> userRegister(@RequestBody User user) {
        return userService.register(user);
    }
}
