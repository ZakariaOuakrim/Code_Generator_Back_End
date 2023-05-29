package com.stage.code_gen.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.Dependency;
import com.stage.code_gen.Repositories.DependencyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DependencyService {
	private final DependencyRepository dependencyRepository;
	
	public void addNewDependency(Dependency dependency) {
		dependencyRepository.save(dependency);;
	}
	
	public List<Dependency> getAllDependencies(){
		return dependencyRepository.findAll();
	}
	public void deleteDependency(Long dependencyId) {
		dependencyRepository.deleteById(dependencyId);
	}
	public Dependency getDependencyById(Long id) {
		return dependencyRepository.findById(id).get();
	}
	public void modifyDependency(Dependency dependency) {
		dependencyRepository.save(dependency);
	}
	
}
