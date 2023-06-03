package com.stage.code_gen.Models;

import javax.persistence.EmbeddedId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "applicationSetting")
public class ApplicationSetting {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private Long id;

	private String typeOfDatabase;
	private String applicationPort;
	private String applicationDataSourceUrl;
	private String userNameDataSource;
	private String passwordDataSource;
	
	
}
