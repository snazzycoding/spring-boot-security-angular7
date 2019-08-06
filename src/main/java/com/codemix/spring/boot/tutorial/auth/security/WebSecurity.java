package com.codemix.spring.boot.tutorial.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.codemix.spring.boot.tutorial.auth.repository.IUserRepository;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private IUserRepository userRepository;
	
	@Value("${jwtSecret}")
	private String jwtSecret;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// configure CORS, authorize any post request to a list of endpoints
		http.cors().and().csrf().disable().authorizeRequests().antMatchers(HttpMethod.POST, "/users/sign-up", "/users/login", "/users/logout").permitAll()
				// any other request should be authenticated
				.anyRequest().authenticated().and()
				// add the filters to be used
				.addFilter(new JWTAuthenticationFilter(authenticationManager(), userRepository, jwtSecret))
				.addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtSecret))
				// we don't want sessions for our application
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
				
				
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// set the password encoder to be used
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return source;
	}
}
