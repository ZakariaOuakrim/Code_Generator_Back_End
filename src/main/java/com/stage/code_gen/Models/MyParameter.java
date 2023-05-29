package com.stage.code_gen.Models;

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
public class MyParameter {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
	@Column(name="parameter_id")
	private Long id;
	
	private String name;
	private String type;



}
