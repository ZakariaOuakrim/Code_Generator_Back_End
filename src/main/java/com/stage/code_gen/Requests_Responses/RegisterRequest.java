package com.stage.code_gen.Requests_Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder 
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
	private String email;
	private String password;
	private String userName;	

}
