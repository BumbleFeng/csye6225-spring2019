package com.me.webapi;

import static org.hamcrest.CoreMatchers.equalTo;

import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.repository.UserRepository;
import io.restassured.RestAssured;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebapiApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @BeforeClass
    public static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    @Test
    public void testHomePage() throws UnsupportedEncodingException {
        RestAssured.get("/").then().statusCode(401).body("error",equalTo("Unauthorized"));
        String token = Base64.getEncoder().encodeToString("abc".getBytes("utf-8"));
        RestAssured.given().header("Authorization",token).get("/").then().statusCode(401).body("error",equalTo("Unauthorized"));
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().get("/").then().statusCode(404).body("error",equalTo("User Not Exit"));
    }

    @Test
    public void testUserRegister() throws UnsupportedEncodingException{
        if(userRepository.existsByUsername("a@b.com"))
            userRepository.deleteByUsername("a@b.com");
        String url = "/user/register";
        RestAssured.given().contentType("application/json").post(url).then().statusCode(400);
        User user = new User();
        user.setUsername("a");
        user.setPassword("b");
        RestAssured.given().contentType("application/json").body(user).post(url).then().statusCode(400).body("error",equalTo("Invalid UserName"));
        user.setUsername("a@b.com");
        RestAssured.given().contentType("application/json").body(user).post(url).then().statusCode(400).body("error",equalTo("Week Password"));
        user.setPassword("abcdefgh");
        RestAssured.given().contentType("application/json").body(user).post(url).then().statusCode(400).body("error",equalTo("Week Password"));
        user.setPassword("12345678");
        RestAssured.given().contentType("application/json").body(user).post(url).then().statusCode(400).body("error",equalTo("Week Password"));
        user.setPassword("a1234567");
        RestAssured.given().contentType("application/json").body(user).post(url).then().statusCode(200).body("error",equalTo("Success"));
        RestAssured.given().contentType("application/json").body(user).post(url).then().statusCode(409).body("error",equalTo("User Existed"));
        RestAssured.given().auth().preemptive().basic("a@b.com", "a1234567").when().get("/").then().statusCode(200).body("error",equalTo("Success"));
    }

    @Test
    public void testNote(){
        if(userRepository.existsByUsername("a@c.com"))
            userRepository.deleteByUsername("a@c.com");
        if(userRepository.existsByUsername("a@d.com"))
            userRepository.deleteByUsername("a@d.com");
        User user = new User();
        user.setUsername("a@c.com");
        user.setPassword("a1234567");
        RestAssured.given().contentType("application/json").body(user).post("/user/register").then().statusCode(200).body("error",equalTo("Success"));
        user.setUsername("a@d.com");
        user.setPassword("b1234567");
        RestAssured.given().contentType("application/json").body(user).post("/user/register").then().statusCode(200).body("error",equalTo("Success"));
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().get("/note").then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note").then().statusCode(200);
        Note note = new Note();
        note.setTitle("Title1");
        note.setContent("Content1");
        RestAssured.given().auth().preemptive().basic("abc", "cde").contentType("application/json").body(note).when().post("/note").then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").when().post("/note").then().statusCode(400);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(user).when().post("/note").then().statusCode(400);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(note).when().post("/note").then().statusCode(201).body("title",equalTo("Title1"));
        RestAssured.given().auth().preemptive().basic("a@d.com", "b1234567").contentType("application/json").body(note).when().post("/note").then().statusCode(201).body("title",equalTo("Title1"));
        note.setTitle("Title2");
        note.setContent("Content2");
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(note).when().post("/note").then().statusCode(201).body("title",equalTo("Title2"));
        List<Note> noteList1 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note").then().statusCode(200).extract().body().jsonPath().getList(".", Note.class);
        assertEquals(noteList1.size(),2);
        List<Note> noteList2 = RestAssured.given().auth().preemptive().basic("a@d.com", "b1234567").when().get("/note").then().statusCode(200).extract().body().jsonPath().getList(".", Note.class);
        assertEquals(noteList2.size(),1);
        String id1 = noteList1.get(0).getId();
        String id2 = noteList2.get(0).getId();
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().get("/note/"+id1).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/"+id2).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/"+id1).then().statusCode(200).body("id",equalTo(id1));
        note.setTitle("Title3");
        note.setContent("Content3");
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(note).when().put("/note/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").when().put("/note/"+id1).then().statusCode(400);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(user).when().put("/note/"+id1).then().statusCode(400);
        RestAssured.given().auth().preemptive().basic("abc", "cde").contentType("application/json").body(note).when().put("/note/"+id1).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(note).when().put("/note/"+id2).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType("application/json").body(note).when().put("/note/"+id1).then().statusCode(204);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/"+id1).then().statusCode(200).body("title",equalTo("Title3"));

        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().delete("/note/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().delete("/note/"+id1).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().delete("/note/"+id2).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().delete("/note/"+id1).then().statusCode(204);
        List<Note> noteList3 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note").then().statusCode(200).extract().body().jsonPath().getList(".", Note.class);
        assertEquals(noteList3.size(),1);

    }

}

