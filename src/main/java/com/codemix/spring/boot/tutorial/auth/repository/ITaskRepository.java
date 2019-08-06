package com.codemix.spring.boot.tutorial.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codemix.spring.boot.tutorial.auth.model.Task;
import com.codemix.spring.boot.tutorial.auth.model.User;

public interface ITaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUser(User user);
}
