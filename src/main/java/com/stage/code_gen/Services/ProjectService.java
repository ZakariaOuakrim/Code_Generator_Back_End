package com.stage.code_gen.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.Dependency;
import com.stage.code_gen.Models.Project;
import com.stage.code_gen.Models.User;
import com.stage.code_gen.Repositories.DependencyRepository;
import com.stage.code_gen.Repositories.ProjectRepository;
import com.stage.code_gen.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final DependencyRepository dependencyRepository;
	
	public void createNewProject(Project project,String email) {
		User user= userRepository.findByemail(email);
		project.setDependencies(new ArrayList<>());
		project.getDependencies().add(dependencyRepository.findById(202L).get());
		project.getDependencies().add(dependencyRepository.findById(203L).get());
		project.getDependencies().add(dependencyRepository.findById(252L).get());
		project.getDependencies().add(dependencyRepository.findById(552L).get());
		project.getDependencies().add(dependencyRepository.findById(602L).get());
		user.getProjects().add(project);
		
		userRepository.save(user);
	}
	
	public Project getProjectById(Long projectId) {
		return projectRepository.findById(projectId).get();
	}
	
	public List<Project> getAllProjects(String email) {
		Optional<User> user= userRepository.findById(email);
		return user.get().getProjects();
	}
	
	public List<Project> getAllProjectsAdminVersion(){
		return projectRepository.findAll();
	}
	public void addANewDependencyToProject(Long projectId,Long dependencyId){
		Project _project = projectRepository.findById(projectId).get();
		Dependency _depenedency = dependencyRepository.findById(dependencyId).get();
		_project.getDependencies().add(_depenedency);
		projectRepository.save(_project);
	}
	public List<Long> getAllDepenenciesOfAProject(Long projectId){
		List<Long> dependList = new ArrayList<>() ;
		 for( Dependency dep : projectRepository.findById(projectId).get().getDependencies())
			 dependList.add(dep.getId());
		 return dependList;
	}
	public void deleteDependencyFromProject(Long projectId,Long dependencyId) {
		Project _project = projectRepository.findById(projectId).get();
		Dependency _dependency = dependencyRepository.findById(dependencyId).get();
		_project.getDependencies().remove(_dependency);
		projectRepository.save(_project);
		
	}
	
	public void deleteProject(Long projectId) {
		projectRepository.deleteById(projectId);
	}
	
	
}
