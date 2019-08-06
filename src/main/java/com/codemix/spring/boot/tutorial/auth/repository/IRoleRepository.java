package com.codemix.spring.boot.tutorial.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codemix.spring.boot.tutorial.auth.model.Role;

public interface IRoleRepository extends JpaRepository<Role, Long> {

}
