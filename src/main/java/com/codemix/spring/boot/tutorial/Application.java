package com.codemix.spring.boot.tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.codemix.spring.boot.tutorial.auth.security.UserDetailsServiceImpl;

@SpringBootApplication
public class Application {

	@Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public UserDetailsServiceImpl userDetailsServiceImpl() {
		return new UserDetailsServiceImpl();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

