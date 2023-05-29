package com.stage.code_gen.Models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Dependency {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
	@Column(name="dependency_id")
	private Long id;
	
	private String groupId;
	private String artifactId;
	private String version;
	@ManyToMany(mappedBy="dependencies")
    @JsonIgnoreProperties("dependencies")
	private List<Project> projects;
	
	
}
