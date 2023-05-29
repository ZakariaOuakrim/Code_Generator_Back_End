package com.stage.code_gen.Models;



import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="projects_table", schema="my_schema")
public class Project {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="project_id")
	private Long id;
	private String artifactId;
	private String groupId;
	private String version;
	private String modelVersion;
	private String projectVersion;
	private String description;
	
	@OneToMany(targetEntity = MyClass.class,cascade = CascadeType.ALL)
	@JoinColumn(name="pc_fk",referencedColumnName = "project_id")
	private List<MyClass> classes;
	
	@ManyToMany
	@JoinTable(name="project_dependency",
	joinColumns=@JoinColumn(name="id_project"),
	inverseJoinColumns = @JoinColumn(name="id_dependency")
	)
    @JsonIgnoreProperties("projects")
	private List<Dependency> dependencies;
	

}
