package com.me.web;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.junit.Before;
import org.junit.Test;

import com.me.dao.UserDAO;
import com.me.pojo.User;

import io.restassured.RestAssured;

public class UnitTest {

	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost/web";
		RestAssured.port = 8080;
		UserDAO userDAO = new UserDAO();
		User testUser = userDAO.get("a@b.com");
		if (testUser != null)
			userDAO.delete(testUser);
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
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(200).body("error",equalTo("OK"));
        json = "{\"username\":\"a@b.com\",\"password\":\"a1234568\"}";
        RestAssured.given().contentType("application/json").body(json).post(url).then().statusCode(406).body("error",equalTo("User Existed"));
        RestAssured.given().auth().preemptive().basic("a@b.com", "a1234567").when().get("/").then().statusCode(200).body("error",equalTo("OK"));
    }

}
