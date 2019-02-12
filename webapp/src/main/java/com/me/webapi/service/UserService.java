package com.me.webapi.service;

import com.me.webapi.pojo.User;
import com.me.webapi.repository.UserRepository;
import com.me.webapi.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<ErrorMessage> register(User user){
        ErrorMessage err = new ErrorMessage();

        String username = user.getUsername();
        String password = user.getPassword();

        if(username==null||password==null){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);

        if (!matcher.matches()) {
            err.setError("Invalid UserName");
            err.setMessage("Please enter a valid email address!");
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(username)) {
            err.setError("User Existed");
            err.setMessage("Username already exist, please enter another one!");
            return new ResponseEntity<>(err, HttpStatus.CONFLICT);
        }

        regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            err.setError("Week Password");
            err.setMessage("Minimum eight characters, at least one letter and one number!");
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }

        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashPassword);
        userRepository.save(user);
        logger.info("No." + userRepository.findByUsername(username).getUserid() + " User Registered");
        err.setError("Success");
        err.setMessage("Resisted Succeeded");
        return new ResponseEntity<>(err, HttpStatus.OK);
    }

    public String[] decode(String token){
        byte[] bytes = Base64.getDecoder().decode(token.substring(6));

        String decode = null;
        try {
            decode = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return decode.split(":");
    }

    public ResponseEntity<ErrorMessage> login(String token){
        ErrorMessage err = new ErrorMessage();

        if (token == null || !token.startsWith("Basic ")) {
            err.setError("Unauthorized");
            err.setMessage("Please log in first!");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        String[] params = decode(token);
        String username = params[0];
        String password = params[1];

        if (!userRepository.existsByUsername(username)) {
            err.setError("User Not Exit");
            err.setMessage("User name does not exist!");
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUsername(username);
        if (!BCrypt.checkpw(password, user.getPassword())) {
            err.setError("Invalid Token");
            err.setMessage("Password is incorrect!");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = df.format(localDateTime);
        logger.info("No." + user.getUserid() + "log in at " + time);
        err.setError("Success");
        err.setMessage("Welcome! The time on the server is " + time);
        return new ResponseEntity<>(err, HttpStatus.OK);
    }

    public User authorize(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Basic ")) {
            String[] params = decode(token);
            String username = params[0];
            String password = params[1];
            if (userRepository.existsByUsername(username)) {
                User user = userRepository.findByUsername(username);
                if (BCrypt.checkpw(password, user.getPassword()))
                    return user;
            }
        }
        return null;
    }
}
