package com.stage.code_gen.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Requests_Responses.RequestCreateClass;

import jakarta.transaction.Transactional;

@Repository
public interface PropertyRepository extends JpaRepository<MyProperty,Long>  {
	List<MyProperty> findByMyclassId(Long classId);
	

    @Modifying
    @Transactional
    @Query("DELETE FROM MyProperty p WHERE p.myclass.id = :classId")
    void deleteByClassId(Long classId);

}
