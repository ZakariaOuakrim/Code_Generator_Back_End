package com.stage.code_gen.Requests_Responses;

import com.stage.code_gen.Models.MyMethod;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Models.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@AllArgsConstructor 
@NoArgsConstructor
public class RequestCreateProject {
	private Project project;
	private String email;	
}
