package com.me.webapi.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.UserRepository;
import com.me.webapi.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public ErrorMessage index(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        ErrorMessage err = new ErrorMessage();

        if (token == null||!token.startsWith("Basic ")) {
            err.setError("Unauthorized");
            err.setMessage("Please log in first!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return err;
        }

        byte[] bytes = Base64.getDecoder().decode(token.substring(6));

        String decode = null;
        try {
            decode = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(token);
        System.out.println(decode);

        String[] paramas = decode.split(":");
        String username = paramas[0];
        String password = paramas[1];

        if (!userRepository.existsByUsername(username)) {
            err.setError("User Not Exit");
            err.setMessage("User name does not exist!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return err;
        }

        User user = userRepository.findByUsername(username);
        if (!BCrypt.checkpw(password, user.getPassword())) {
            err.setError("Invalid Token");
            err.setMessage("Password is incorrect!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return err;
        }

        Date date = new Date();
        logger.info("No." + user.getUserid() +"log in at "+date.toString());
        err.setError("Success");
        err.setMessage("Welcome! The time on the server is " + date.toString());
        return err;
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST, produces = "application/json")
    public  ErrorMessage userRegister(@RequestBody User user, HttpServletResponse response){
        ErrorMessage err = new ErrorMessage();

        String username =  user.getUsername();
        String password = user.getPassword();

        String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);

        if (!matcher.matches()) {
            err.setError("Invalid UserName");
            err.setMessage("Please enter a valid email address!");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return err;
        }

        if (userRepository.existsByUsername(username)) {
            err.setError("User Existed");
            err.setMessage("Username already exist, please enter another one!");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return err;
        }

        regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            err.setError("Week Password");
            err.setMessage("Minimum eight characters, at least one letter and one number!");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return err;
        }

        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashPassword);
        userRepository.save(user);
        logger.info("No." + userRepository.findByUsername(username).getUserid() + " User Registered");
        err.setError("Success");
        err.setMessage("Resisted Succeeded");
        return err;
    }
}
