package com.stage.code_gen.Services;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.ApplicationSetting;
import com.stage.code_gen.Models.Role;
import com.stage.code_gen.Models.User;
import com.stage.code_gen.Repositories.UserRepository;
import com.stage.code_gen.Requests_Responses.AuthenticationRequest;
import com.stage.code_gen.Requests_Responses.AuthenticationResponse;
import com.stage.code_gen.Requests_Responses.RegisterRequest;
import com.stage.code_gen.config.JwtService;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final JavaMailSender mailSender;
	private final ApplicationSettingService applicationSettingService;

	private UserDetails loadUserByEmail(String email) {
		User user = userRepository.findById(email).get();
		if (user != null) {
			return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
					user.getAuthorities());
		} else {
			throw new UsernameNotFoundException("Email is not valid");
		}
	}
	private void sendResetPasswordEmail(String email,String siteUrl) throws UnsupportedEncodingException, MessagingException {
		String subject ="Reset password";
		String senderName="SpringBoot code generator";
		String mailContent="<p>Dear,<br> Please Follow this Link to reset your password";
		mailContent+="<h3><a href=\"[[URL]]\" target=\"_self\">Click here to reset your password</h3>";
		mailContent+="<p>Thank you<br> The SpringBoot Code Generator Team</p>";
		String toAddress = email;
		String fromAddress = "codegeneratorspringboot@gmail.com";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setFrom(fromAddress,senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		String resetPasswordURL = siteUrl+"/"+email;
		mailContent = mailContent.replace("[[URL]]",resetPasswordURL);
		helper.setText(mailContent,true);
		mailSender.send(message);
	}
	private void sendVerificationEmail(User user,String siteURL)  throws MessagingException, UnsupportedEncodingException{
		String subject = "Please verify your registration";
		String senderName="SpringBoot Code_Gen Team";
		String mailContent = "<p>Dear "+user.getName()+",</p>";
		mailContent+="<p>Please click the link bellow to verify your registration: </p>";
		mailContent+="<h3><a href=\"[[URL]]\" target=\"_self\">Click here to verify</h3>";
		mailContent+="<p>Thank you<br> The SpringBoot Code_Gen Team</p>";
		String toAddress=user.getEmail();
		String fromAddress = "codegeneratorspringboot@gmail.com";
		
		
		MimeMessage message =mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setFrom(fromAddress,senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		String verifyURL = siteURL +"/"+user.getEmail();
		mailContent = mailContent.replace("[[URL]]",verifyURL);
		helper.setText(mailContent,true);
		mailSender.send(message);
	}

	public AuthenticationResponse register(RegisterRequest request) throws UnsupportedEncodingException, MessagingException {
		// TODO Auto-generated method stub
		var user = User.builder().userName(request.getUserName()).email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword())).role(Role.USER).build();
		
		user.setEnabled(false);
		//setting the default settings of a user
		user.setApp_SettingId(0L);
		userRepository.save(user);
		sendVerificationEmail(user,"http://localhost:4200/verifyAccount");
		
		var jwtToken = jwtService.generateToken(user);

		return AuthenticationResponse.builder().token(jwtToken).build();
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (DisabledException e) {
			return new AuthenticationResponse("UserDisabled",null);
		} catch (BadCredentialsException e) {
			System.out.println("Invalid email or password");
			return new AuthenticationResponse(null, null);
		}
		// final UserDetails userDetails = loadUserByEmail(request.getEmail());

		var user = userRepository.findById(request.getEmail()).orElseThrow();

		var jwtToken = jwtService.generateToken(user);
		System.out.println(user.getRole());
		return new AuthenticationResponse(jwtToken, user);
	}

	public void verifyAccount(String email) {
		User user = userRepository.findById(email).get();
		user.setEnabled(true);
		user.setRole(Role.USER);
		userRepository.save(user);
	}
	public List<User> getAllUsers(){
		return userRepository.findAll();
	}
	public User getUser(String _email) {
		return userRepository.findById(_email).get();
	}
	public void userForgotPassword(String email) throws UnsupportedEncodingException, MessagingException {
		sendResetPasswordEmail(email,"http://localhost:4200/verifyAccount");
	}
	
	
}
