package com.me.webapi;

import static org.hamcrest.CoreMatchers.equalTo;

import com.me.webapi.pojo.User;
import com.me.webapi.repository.UserRepository;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebapiApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        if(userRepository.existsByUsername("a@b.com")) {
            User testUser = userRepository.findByUsername("a@b.com");
            userRepository.delete(testUser);
        }
    }

    @Test
    public void testHomePage() throws UnsupportedEncodingException {
        RestAssured.get("/").then().statusCode(401).body("error",equalTo("Unauthorized"));
        String token = Base64.getEncoder().encodeToString("abc".getBytes("utf-8"));
        RestAssured.given().header("Authorization",token).get("/").then().statusCode(401).body("error",equalTo("Unauthorized"));
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().get("/").then().statusCode(401).body("error",equalTo("User Not Exit"));
    }

    @Test
    public void testUserRegister() throws UnsupportedEncodingException{
        String url = "/user/register";
        RestAssured.given().contentType("application/json").post(url).then().statusCode(400);
        String json = "{\"username\":\"a\",\"password\":\"b\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(406).body("error",equalTo("Invalid UserName"));
        json = "{\"username\":\"a@b.com\",\"password\":\"b\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(406).body("error",equalTo("Week Password"));
        json = "{\"username\":\"a@b.com\",\"password\":\"abcdefgh\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(406).body("error",equalTo("Week Password"));
        json = "{\"username\":\"a@b.com\",\"password\":\"12345679\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(406).body("error",equalTo("Week Password"));
        json = "{\"username\":\"a@b.com\",\"password\":\"a1234567\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(200).body("error",equalTo("Success"));
        json = "{\"username\":\"a@b.com\",\"password\":\"a1234568\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(406).body("error",equalTo("User Existed"));
        RestAssured.given().auth().preemptive().basic("a@b.com", "a1234567").when().get("/").then().statusCode(200).body("error",equalTo("Success"));
    }

}

