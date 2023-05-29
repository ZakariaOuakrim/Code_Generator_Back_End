package com.stage.code_gen.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.User;
import com.stage.code_gen.Repositories.UserRepository;

@Service
@ComponentScan("com.stage.code_gen.Models")

public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public void addUser(User user) {
		// TODO Auto-generated method stub
		userRepository.saveAndFlush(user); 
	}

	@Override
	public List<User> fetchAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<User> findByEmail(String email) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
