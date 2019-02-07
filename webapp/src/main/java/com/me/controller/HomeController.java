package com.me.controller;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = { "application/json; charset=UTF-8" })
	@ResponseBody
	public ErrorMessage home(Locale locale, HttpServletRequest request, HttpServletResponse response) {
		String token = request.getHeader("Authorization");
		ErrorMessage err = new ErrorMessage();
		if (token == null || !token.startsWith("Basic ")) {
			err.setError(Constants.ERR_UNAUTH);
			err.setMessage(Constants.MESSAGE_UNAUTH);
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

		String[] paramas = decode.split(":");
		String username = paramas[0];
		String password = paramas[1];
		User user = userDAO.get(username);

		if (user == null) {
			err.setError(Constants.ERR_USER_NOT_EXIST);
			err.setMessage(Constants.MESSAGE_USER_NOT_EXIST);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return err;
		}

		if (!BCrypt.checkpw(password, user.getPassword())) {
			err.setError(Constants.ERR_INVA_TOKEN);
			err.setMessage(Constants.MESSAGE_INVA_TOKEN);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return err;
		}

		Date date = new Date();
		logger.info("No." + user.getUserid() + " locale is {}.", locale);
		err.setError(Constants.ERR_OK);
		err.setMessage("Welcome! The time on the server is " + date.toString());
		return err;
	}

	@RequestMapping(value = "/user/register", method = RequestMethod.POST, produces = {
			"application/json; charset=UTF-8" })
	@ResponseBody
	public ErrorMessage userRegister(@RequestBody User user, HttpServletResponse response) {
		ErrorMessage err = new ErrorMessage();

		String username = user.getUsername();
		String password = user.getPassword();

		String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(username);

		if (!matcher.matches()) {
			err.setError(Constants.ERR_NOT_EMAIL);
			err.setMessage(Constants.MESSAGE_NOT_EMAIL);
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return err;
		}

		if (userDAO.get(username) != null) {
			err.setError(Constants.ERR_USER_EXISTED);
			err.setMessage(Constants.MESSAGE_USER_EXISTED);
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return err;
		}

		regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(password);

		if (!matcher.matches()) {
			err.setError(Constants.ERR_WEAK_PASSWD);
			err.setMessage(Constants.MESSAGE_WEAK_PASSWD);
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return err;
		}

		String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		user.setPassword(hashPassword);
		userDAO.add(user);
		logger.info("No." + userDAO.get(username).getUserid() + " User Registered");
		err.setError(Constants.ERR_OK);
		err.setMessage(Constants.MESSAGE_OK);
		return err;
	}
}
