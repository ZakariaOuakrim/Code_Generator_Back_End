package com.stage.code_gen.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.User;

@Service	
public interface UserService {
	void addUser(User user);
	List<User> fetchAllUsers();
	Optional<User> findByEmail(String email);
}
