package com.stage.code_gen.Requests_Responses;

import com.stage.code_gen.Models.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
	private String token;
	private User user;
}
