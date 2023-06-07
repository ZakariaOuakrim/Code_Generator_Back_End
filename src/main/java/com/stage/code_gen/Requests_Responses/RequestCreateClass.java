package com.stage.code_gen.Requests_Responses;

import com.stage.code_gen.Models.MyMethod;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Models.MyParameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@AllArgsConstructor 
@NoArgsConstructor
public class RequestCreateClass {
	private Long id;
	private String packageName;
	private String className;
	private String classType;
	private Long projectId;
	private boolean service;
	private boolean generateRepository;
	private boolean generateController;
	private String tableName;
	private boolean idGenerate;
	private boolean generatedValue;
	private String generatedType;
	private MyProperty[] properties;
	private MyMethod[] methods;
	private String requestMappingURL;
	private String mode;
	private boolean isEmbeddedId;
	private Long idOfPropertyId;
	private Long embeddedIdClassId;

}
