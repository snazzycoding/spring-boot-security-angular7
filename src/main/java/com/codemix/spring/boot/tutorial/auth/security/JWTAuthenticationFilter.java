package com.codemix.spring.boot.tutorial.auth.security;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.auth0.jwt.JWT;
import com.codemix.spring.boot.tutorial.auth.model.Role;
import com.codemix.spring.boot.tutorial.auth.model.User;
import com.codemix.spring.boot.tutorial.auth.repository.IUserRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	@Autowired
	private AuthenticationManager authenticationManager;

	private IUserRepository userRepository;

	private String jwtSecret;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, IUserRepository userRepository,
			String jwtSecret) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.jwtSecret = jwtSecret;
		setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/users/login", "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			// Read body request with Jackson
			User user = getMapper().readValue(request.getInputStream(), User.class);
			Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

			if (user.getRoles() != null) {
				for (Role role : user.getRoles()) {
					grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
				}
			}
			// use the authentication manager to authenticate
			return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),
					user.getPassword(), grantedAuthorities.isEmpty() ? null : grantedAuthorities));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		// get the username from the auth result
		String username = (((org.springframework.security.core.userdetails.User) authResult.getPrincipal()))
				.getUsername();

		// find JPA Entity by email
		User user = userRepository.findByEmail(username);
		// Initialize the lazy loaded roles
		Hibernate.initialize(user.getRoles());
		// Set the password to null so we don't send it on the JWT payload
		user.setPassword(null);
		// get the json string of the user with Jackson
		StringWriter writer = new StringWriter();
		getMapper().writeValue(writer, user);
		writer.flush();

		// generate the token
		String token = JWT.create().withSubject(username).withClaim("user", writer.toString())
				.sign(HMAC512(jwtSecret.getBytes()));
		StringBuilder builder = new StringBuilder();
		builder.append("{ \"token\": \"").append(token).append("\"}");
		// write the token on the response
		response.getWriter().write(builder.toString());
	}

	private ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}
}
