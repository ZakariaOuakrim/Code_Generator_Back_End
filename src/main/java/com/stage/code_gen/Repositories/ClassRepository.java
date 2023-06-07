package com.stage.code_gen.Repositories;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyMethod;

public interface ClassRepository extends JpaRepository<MyClass, Long> {
	
	default List<String> findAllPackages(List<MyClass> myClasses){
	
		return myClasses.stream().map(MyClass::getPackageName).distinct().collect(Collectors.toList());
	}
	
	List<MyClass> findByPackageName(String packageName);
	
    MyClass findByClassName(String className);

}
