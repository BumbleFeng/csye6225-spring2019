package com.me.controller;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.me.dao.UserDAO;
import com.me.pojo.User;
import com.me.util.Constants;
import com.me.util.ErrorMessage;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private UserDAO userDAO;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String home(Locale locale, HttpServletRequest request) {
		String token = request.getHeader("token");
		ErrorMessage err = new ErrorMessage();
		Gson gson = new Gson();
		if (token == null) {
			err.setErrorNo(Constants.ERRID_UNAUTH);
			err.setErrorMessage(Constants.ERRMESSAGE_UNAUTH);
			return gson.toJson(err);
		}

		byte[] bytes = Base64.getDecoder().decode(token);
		String decode = null;
		try {
			decode = new String(bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!decode.contains(":")) {
			err.setErrorNo(Constants.ERRID_INVA_TOKEN);
			err.setErrorMessage(Constants.ERRMESSAGE_INVA_TOKEN);
			return gson.toJson(err);
		}

		String[] paramas = decode.split(":");
		String username = paramas[0];
		String password = paramas[1];
		User user = userDAO.get(username);

		if (user == null) {
			err.setErrorNo(Constants.ERRID_USER_NOT_EXIST);
			err.setErrorMessage(Constants.ERRMESSAGE_USER_NOT_EXIST);
			return gson.toJson(err);
		}

		if (!BCrypt.checkpw(password, user.getPassword())) {
			err.setErrorNo(Constants.ERRID_INVA_TOKEN);
			err.setErrorMessage(Constants.ERRMESSAGE_INVA_TOKEN);
			return gson.toJson(err);
		}

		Date date = new Date();
		logger.info("No." + user.getUserid() + " locale is {}.", locale);
		err.setErrorNo(Constants.ERRID_OK);
		err.setErrorMessage("Welcome! The time on the server is " + date.toString());
		return gson.toJson(err);
	}

	@RequestMapping(value = "/user/register", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public String userRegister(@RequestHeader(value = "username", required = true) String username,
			@RequestHeader(value = "password", required = true) String password) {
		ErrorMessage err = new ErrorMessage();
		Gson gson = new Gson();

		String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(username);

		if (!matcher.matches()) {
			err.setErrorNo(Constants.ERRID_NOT_EMAIL);
			err.setErrorMessage(Constants.ERRMESSAGE_NOT_EMAIL);
			return gson.toJson(err);
		}

		if (userDAO.get(username) != null) {
			err.setErrorNo(Constants.ERRID_USER_EXISTED);
			err.setErrorMessage(Constants.ERRMESSAGE_USER_EXISTED);
			return gson.toJson(err);
		}

		regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(password);

		if (!matcher.matches()) {
			err.setErrorNo(Constants.ERRID_WEAK_PASSWD);
			err.setErrorMessage(Constants.ERRMESSAGE_WEAK_PASSWD);
			return gson.toJson(err);
		}

		String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		User user = new User();
		user.setUsername(username);
		user.setPassword(hashPassword);
		userDAO.add(user);
		logger.info("No." + userDAO.get(username).getUserid() + " User Registered");
		err.setErrorNo(Constants.ERRID_OK);
		err.setErrorMessage(Constants.ERRMESSAGE_OK);
		return gson.toJson(err);
	}
}
