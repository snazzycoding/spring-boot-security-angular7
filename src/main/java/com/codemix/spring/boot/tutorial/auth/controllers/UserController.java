package com.codemix.spring.boot.tutorial.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codemix.spring.boot.tutorial.auth.model.User;
import com.codemix.spring.boot.tutorial.auth.repository.IUserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private IUserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping(value = "/sign-up", method = RequestMethod.POST)
	public ResponseEntity<User> register(@RequestBody User user) {
		if (user == null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}
		if (userRepository.findByEmail(user.getEmail()) != null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}
		
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		
		user = userRepository.save(user);
		
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logout() {
		// do nothing
	}
}
