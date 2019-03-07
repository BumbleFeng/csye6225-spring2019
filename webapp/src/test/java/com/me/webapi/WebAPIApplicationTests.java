package com.me.webapi;

import static org.hamcrest.CoreMatchers.equalTo;

import com.me.webapi.pojo.Attachment;
import com.me.webapi.pojo.Note;
import com.me.webapi.pojo.User;
import com.me.webapi.service.NoteService;
import io.restassured.RestAssured;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebAPIApplicationTests {

    @Autowired
    private NoteService noteService;

    @LocalServerPort
    private int port;

    private final String ct = "application/json";

    @Before
    public void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void testHomePage() {
        RestAssured.get("/").then().statusCode(401).body("error",equalTo("Unauthorized"));
        String token = Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8));
        RestAssured.given().header("Authorization",token).get("/").then().statusCode(401).body("error",equalTo("Unauthorized"));
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().get("/").then().statusCode(404).body("error",equalTo("User Not Exit"));
    }

    @Test
    public void testUserRegister() {
        String url = "/user/register";
        RestAssured.given().contentType(ct).post(url).then().statusCode(400);
        User user = new User();
        user.setUsername("a");
        user.setPassword("b");
        RestAssured.given().contentType(ct).body(user).post(url).then().statusCode(400).body("error",equalTo("Invalid UserName"));
        user.setUsername("a@b.com");
        RestAssured.given().contentType(ct).body(user).post(url).then().statusCode(400).body("error",equalTo("Week Password"));
        user.setPassword("abcdefgh");
        RestAssured.given().contentType(ct).body(user).post(url).then().statusCode(400).body("error",equalTo("Week Password"));
        user.setPassword("12345678");
        RestAssured.given().contentType(ct).body(user).post(url).then().statusCode(400).body("error",equalTo("Week Password"));
        user.setPassword("a1234567");
        RestAssured.given().contentType(ct).body(user).post(url).then().statusCode(200).body("error",equalTo("Success"));
        RestAssured.given().contentType(ct).body(user).post(url).then().statusCode(409).body("error",equalTo("User Existed"));
        RestAssured.given().auth().preemptive().basic("a@b.com", "a1234567").when().get("/").then().statusCode(200).body("error",equalTo("Success"));
    }

    @Test
    public void testNote(){
        User user = new User();
        user.setUsername("a@c.com");
        user.setPassword("a1234567");
        RestAssured.given().contentType(ct).body(user).post("/user/register").then().statusCode(200).body("error",equalTo("Success"));
        user.setUsername("a@d.com");
        user.setPassword("b1234567");
        RestAssured.given().contentType(ct).body(user).post("/user/register").then().statusCode(200).body("error",equalTo("Success"));
        //post:/note
        Note note = new Note();
        note.setTitle("Title1");
        note.setContent("Content1");
        RestAssured.given().auth().preemptive().basic("abc", "123").contentType(ct).body(note).when().post("/note").then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).when().post("/note").then().statusCode(400);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(user).when().post("/note").then().statusCode(500);
        Note note1 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(note).when().post("/note").then().statusCode(201).extract().body().as(Note.class);
        assertEquals(note1.getTitle(),"Title1");
        Note note2 = RestAssured.given().auth().preemptive().basic("a@d.com", "b1234567").contentType(ct).body(note).when().post("/note").then().statusCode(201).extract().body().as(Note.class);
        assertEquals(note2.getTitle(),"Title1");
        note.setTitle("Title2");
        note.setContent("Content2");
        Note note3 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(note).when().post("/note").then().statusCode(201).extract().body().as(Note.class);
        assertEquals(note3.getTitle(),"Title2");
        //get:/note
        RestAssured.given().auth().preemptive().basic("abc", "123").when().get("/note").then().statusCode(401);
        List<Note> noteList1 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note").then().statusCode(200).extract().body().jsonPath().getList("", Note.class);
        assertEquals(noteList1.size(),2);
        List<Note> noteList2 = RestAssured.given().auth().preemptive().basic("a@d.com", "b1234567").when().get("/note").then().statusCode(200).extract().body().jsonPath().getList("", Note.class);
        assertEquals(noteList2.size(),1);
        //get:/note/{idNotes}
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "123").when().get("/note/"+note1.getNoteId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/"+note2.getNoteId()).then().statusCode(401);
        Note note4 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/"+note1.getNoteId()).then().statusCode(200).extract().body().as(Note.class);
        assertEquals(note1.getTitle(),note4.getTitle());
        //put:/note/{idNotes}
        note.setTitle("Title3");
        note.setContent("Content3");
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(note).when().put("/note/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).when().put("/note/"+note1.getNoteId()).then().statusCode(400);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(user).when().put("/note/"+note1.getNoteId()).then().statusCode(500);
        RestAssured.given().auth().preemptive().basic("abc", "123").contentType(ct).body(note).when().put("/note/"+note1.getNoteId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(note).when().put("/note/"+note2.getNoteId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").contentType(ct).body(note).when().put("/note/"+note1.getNoteId()).then().statusCode(204);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note/"+note1.getNoteId()).then().statusCode(200).body("title",equalTo("Title3"));
        //delete:/note/{idNotes}
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().delete("/note/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "123").when().delete("/note/"+note1.getNoteId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().delete("/note/"+note2.getNoteId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().delete("/note/"+note1.getNoteId()).then().statusCode(204);
        List<Note> noteList3 = RestAssured.given().auth().preemptive().basic("a@c.com", "a1234567").when().get("/note").then().statusCode(200).extract().body().jsonPath().getList("", Note.class);
        assertEquals(noteList3.size(),1);

    }

    @Test
    public void testAttachment(){
        User user = new User();
        user.setUsername("a@e.com");
        user.setPassword("a1234567");
        RestAssured.given().contentType(ct).body(user).post("/user/register").then().statusCode(200).body("error",equalTo("Success"));
        user.setUsername("a@f.com");
        user.setPassword("b1234567");
        RestAssured.given().contentType(ct).body(user).post("/user/register").then().statusCode(200).body("error",equalTo("Success"));
        RestAssured.given().auth().preemptive().basic("abc", "cde").when().get("/note").then().statusCode(401);
        Note note = new Note();
        note.setTitle("Title1");
        note.setContent("Content1");
        Note note1 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").contentType(ct).body(note).when().post("/note").then().statusCode(201).extract().body().as(Note.class);
        Note note2 = RestAssured.given().auth().preemptive().basic("a@f.com", "b1234567").contentType(ct).body(note).when().post("/note").then().statusCode(201).extract().body().as(Note.class);
        String url1 = "/note/"+note1.getNoteId()+"/attachments";
        String url2 = "/note/"+note2.getNoteId()+"/attachments";
        note.setTitle("Title2");
        note.setContent("Content2");
        Note note3 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").contentType(ct).body(note).when().post("/note").then().statusCode(201).extract().body().as(Note.class);
        String url3 = "/note/"+note3.getNoteId()+"/attachments";
        //post:/note/{idNotes}/attachments
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test1.txt")).when().post("/note/123/attachments").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "123").multiPart("file", new File("testfile/test1.txt")).when().post(url1).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test1.txt")).when().post(url2).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/empty.txt")).when().post(url1).then().statusCode(500);

        Attachment attachment1 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test1.txt")).when().post(url1).then().statusCode(200).extract().body().as(Attachment.class);
        Attachment attachment2 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test2.txt")).when().post(url1).then().statusCode(200).extract().body().as(Attachment.class);
        Attachment attachment3 = RestAssured.given().auth().preemptive().basic("a@f.com", "b1234567").multiPart("file", new File("testfile/test1.txt")).when().post(url2).then().statusCode(200).extract().body().as(Attachment.class);
        Attachment attachment4 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test1.txt")).when().post(url3).then().statusCode(200).extract().body().as(Attachment.class);
        //get:/note/{idNotes}/attachments
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().get("/note/123/attachments").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "123").when().get(url1).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().get(url2).then().statusCode(401);
        List<Attachment> attachmentList1 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().get(url1).then().statusCode(200).extract().body().jsonPath().getList("", Attachment.class);
        assertEquals(attachmentList1.size(),2);
        List<Attachment> attachmentList3 = RestAssured.given().auth().preemptive().basic("a@f.com", "b1234567").when().get(url2).then().statusCode(200).extract().body().jsonPath().getList("", Attachment.class);
        assertEquals(attachmentList3.size(),1);
        List<Attachment> attachmentList2 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().get(url3).then().statusCode(200).extract().body().jsonPath().getList("", Attachment.class);
        assertEquals(attachmentList2.size(),1);
        //put:/note/{idNotes}/attachments/{idAttachments}
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test3.txt")).when().put("/note/123/attachments/"+attachment1.getAttachmentId()).then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test3.txt")).when().put(url1+"/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "123").multiPart("file", new File("testfile/test3.txt")).when().put(url1+"/"+attachment1.getAttachmentId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test3.txt")).when().put(url1+"/"+attachment3.getAttachmentId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test3.txt")).when().put(url1+"/"+attachment4.getAttachmentId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/empty.txt")).when().put(url1+"/"+attachment1.getAttachmentId()).then().statusCode(500);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").multiPart("file", new File("testfile/test3.txt")).when().put(url1+"/"+attachment1.getAttachmentId()).then().statusCode(204);
        //delete:/note/{idNotes}/attachments/{idAttachments}
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().delete("/note/123/attachments/"+attachment2.getAttachmentId()).then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().delete(url1+"/123").then().statusCode(404);
        RestAssured.given().auth().preemptive().basic("abc", "123").when().delete(url1+"/"+attachment2.getAttachmentId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().delete(url1+"/"+attachment3.getAttachmentId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().delete(url1+"/"+attachment4.getAttachmentId()).then().statusCode(401);
        RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().delete(url1+"/"+attachment2.getAttachmentId()).then().statusCode(204);
        List<Attachment> attachmentList4 = RestAssured.given().auth().preemptive().basic("a@e.com", "a1234567").when().get(url1).then().statusCode(200).extract().body().jsonPath().getList("", Attachment.class);
        assertEquals(attachmentList4.size(),1);
    }

    @After
    public void clean(){
        noteService.deleteUser("a@b.com");
        noteService.deleteUser("a@c.com");
        noteService.deleteUser("a@d.com");
        noteService.deleteUser("a@e.com");
        noteService.deleteUser("a@f.com");
    }
}

