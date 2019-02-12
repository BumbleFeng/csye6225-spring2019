package com.me.webapi.controller;

import com.me.webapi.pojo.User;
import com.me.webapi.service.UserService;
import com.me.webapi.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<ErrorMessage> index(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        return userService.login(token);
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<ErrorMessage> userRegister(@RequestBody User user, HttpServletResponse response) {
        return userService.register(user);
    }
}
