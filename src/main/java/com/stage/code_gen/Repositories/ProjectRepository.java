package com.stage.code_gen.Repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.Project;


public interface ProjectRepository extends JpaRepository<Project,Long> {
	
	@Query("SELECT p.classes FROM Project p WHERE p.id= :projectId")
	List<MyClass> findClassesByProjectId(@Param("projectId") Long projectId);
	
	Project findByClassesId(Long classId);
}
