package com.stage.code_gen.Models;

import java.util.List;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
	public class MyMethod {
		@Id
		@GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
		@Column(name="method_id")
		private Long id;	
		
		private String visibility;
		private String returnType;
		@Column(name="method_name")
		private String name;
		private String body;
		private String requestMappingType;
		
		@ManyToOne(fetch = FetchType.LAZY, optional = true)
		@JoinColumn(name = "class_id", nullable = true)
		@OnDelete(action = OnDeleteAction.CASCADE) 
		@JsonIgnore
		private MyClass myclass;
		
		@OneToMany(targetEntity = MyParameter.class,cascade = CascadeType.ALL)
		@JoinColumn(name="mp_fk",referencedColumnName = "method_id")
		private List<MyParameter> parameters;
		


}
