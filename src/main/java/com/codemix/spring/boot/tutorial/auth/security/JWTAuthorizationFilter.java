package com.codemix.spring.boot.tutorial.auth.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.codemix.spring.boot.tutorial.auth.model.Role;
import com.codemix.spring.boot.tutorial.auth.model.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private String jwtSecret;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, String jwtSecret) {
		super(authenticationManager);
		this.jwtSecret = jwtSecret;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// get the Authorization header
		String header = request.getHeader(AUTHORIZATION_HEADER);

		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
		// set the authentication object on the SecurityContext
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		try {
			String token = request.getHeader(AUTHORIZATION_HEADER);
			if (token != null) {
				// verify that the Authorization header is valid for our jwt secret key 
				DecodedJWT jwt = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes())).build()
						.verify(token.replace(BEARER_PREFIX, ""));
				String jsonUser = jwt.getClaim("user").asString();
				User user = getMapper().readValue(jsonUser, User.class);
				if (user != null) {
					// set the request attribute so that it can be readed by controllers
					request.setAttribute("user", user);
					Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
					
			        for (Role role : user.getRoles()){
			            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
			        }
					return new UsernamePasswordAuthenticationToken(user.getEmail(), null, grantedAuthorities);
				}
			}
		} catch (Throwable t) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error reading jwt token", t);
		}
		return null;
	}

	private ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

}
