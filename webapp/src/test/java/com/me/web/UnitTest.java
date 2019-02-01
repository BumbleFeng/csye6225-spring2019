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
	public void setUP(){
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
		UserDAO userDAO = new UserDAO();
		User testUser = userDAO.get("a@b.com");
		if(testUser!=null)
			userDAO.delete(testUser);
	}

	@Test
	public void testHomePage() throws UnsupportedEncodingException{
		String token;
		RestAssured.get("/web").then().body("errorNo",equalTo("401"));
		token = Base64.getEncoder().encodeToString("abc".getBytes("utf-8"));
		RestAssured.given().header("token",token).get("/web").then().body("errorNo",equalTo("498"));
		token = Base64.getEncoder().encodeToString("abc:cde".getBytes("utf-8"));
		RestAssured.given().header("token",token).get("/web").then().body("errorNo",equalTo("404"));
	}
	
	@Test
	public void testUserRegister() throws UnsupportedEncodingException{
		RestAssured.post("/web/user/register").then().statusCode(400);
		RestAssured.given().header("username","a").header("password","b").post("/web/user/register").then().body("errorNo",equalTo("406"));
		RestAssured.given().header("username","a@b.com").header("password","abcdefgh").post("/web/user/register").then().body("errorNo",equalTo("411"));
		RestAssured.given().header("username","a@b.com").header("password","12345678").post("/web/user/register").then().body("errorNo",equalTo("411"));
		RestAssured.given().header("username","a@b.com").header("password","a123456").post("/web/user/register").then().body("errorNo",equalTo("411"));
		RestAssured.given().header("username","a@b.com").header("password","a1234567").post("/web/user/register").then().body("errorNo",equalTo("200"));
		RestAssured.given().header("username","a@b.com").header("password","a1234568").post("/web/user/register").then().body("errorNo",equalTo("409"));
		String token = Base64.getEncoder().encodeToString("a@b.com:a1234567".getBytes("utf-8"));
		RestAssured.given().header("token",token).get("/web").then().body("errorNo",equalTo("200"));
	}

}
