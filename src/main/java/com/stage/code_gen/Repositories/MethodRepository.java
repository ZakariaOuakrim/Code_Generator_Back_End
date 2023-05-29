package com.stage.code_gen.Repositories;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyMethod;
import com.stage.code_gen.Models.MyProperty;

import jakarta.transaction.Transactional;


public interface MethodRepository extends JpaRepository<MyMethod, Long> {
	
	@Modifying
	@Transactional
    @Query("DELETE FROM MyMethod p WHERE p.myclass.id = :classId")
    void deleteByClassId(Long classId);
	
	
    List<MyMethod> findByMyclassId(Long classId);

}
