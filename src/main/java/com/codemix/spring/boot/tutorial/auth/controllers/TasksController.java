package com.codemix.spring.boot.tutorial.auth.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codemix.spring.boot.tutorial.auth.model.Task;
import com.codemix.spring.boot.tutorial.auth.model.User;
import com.codemix.spring.boot.tutorial.auth.repository.ITaskRepository;
import com.codemix.spring.boot.tutorial.auth.repository.IUserRepository;

@RestController
@RequestMapping("/tasks")
public class TasksController {

	@Autowired
	private IUserRepository userRepository;
	
	@Autowired
	private ITaskRepository taskRepository;
	
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity<List<Task>> getTasks(@RequestAttribute("user") User user) {
		user = userRepository.findById(user.getId()).get();
		return new ResponseEntity<List<Task>>(taskRepository.findByUser(user), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseEntity<Task> createTask(@RequestBody Task task, @RequestAttribute("user") User user) {
		if (task.getId() != null && task.getId() > 0) {
			Task tmpTask = taskRepository.getOne(task.getId());
			if (tmpTask != null && tmpTask.getUser().getId() != user.getId()) {
				return new ResponseEntity<Task>(HttpStatus.BAD_REQUEST);
			}
			tmpTask.setTask(task.getTask());
			task = tmpTask;
		} else {
			// new task
			task.setUser(userRepository.findByEmail(user.getEmail()));
		}
		return new ResponseEntity<Task>(taskRepository.save(task), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public void deleteTask(@RequestBody Task task) {
		taskRepository.delete(task);
	}
}
