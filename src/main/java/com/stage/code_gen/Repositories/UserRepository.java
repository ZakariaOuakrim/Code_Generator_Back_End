package com.stage.code_gen.Repositories;

import java.util.Optional;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stage.code_gen.Models.User;

@Repository
public interface UserRepository extends JpaRepository<User, String >{
	Optional<User> findByEmail(String email);
	User findByemail(String email);
	
}
