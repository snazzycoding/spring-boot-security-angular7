package com.codemix.spring.boot.tutorial.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codemix.spring.boot.tutorial.auth.model.User;

public interface IUserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);
}
