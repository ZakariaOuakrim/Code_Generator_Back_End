package com.stage.code_gen.Models;

import javax.persistence.GenerationType;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class MyClass {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
	@Column(name="class_id")
	private Long id;
	
	private String className;
	private String packageName;
	private String classType;
	private boolean service;
	private boolean isGenerateRepository;
	private boolean isGenerateController;
	private String tableName;
	private boolean isIdGenerate; //does the class has the @id
	private boolean isGeneratedValue; //does the class has @GeneratedValue
	private String generatedType;
	private String requestMappingURL;
}
