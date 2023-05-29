package com.stage.code_gen.Controllers;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stage.code_gen.Models.User;
import com.stage.code_gen.Repositories.UserRepository;
import com.stage.code_gen.Requests_Responses.AuthenticationRequest;
import com.stage.code_gen.Requests_Responses.AuthenticationResponse;
import com.stage.code_gen.Requests_Responses.RegisterRequest;
import com.stage.code_gen.Services.AuthenticationService;
import com.stage.code_gen.Services.UserService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200/") 

public class user_controller {
	
	private final UserService userService;
	
	private final AuthenticationService authenticationService;
	
	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register( @RequestBody RegisterRequest request) throws UnsupportedEncodingException, MessagingException{
		return ResponseEntity.ok(authenticationService.register(request));
	}
	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authnenticate( @RequestBody AuthenticationRequest request) throws Exception  {
		
		return ResponseEntity.ok(authenticationService.authenticate(request));
	}
	@PutMapping("/verifyAccount/{code}")
	public void verifyAccount(@PathVariable("code") String email) {
		authenticationService.verifyAccount(email);
	}

	
}
