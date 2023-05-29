package com.stage.code_gen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stage.code_gen.Models.Dependency;
@Repository
public interface DependencyRepository extends JpaRepository<Dependency, Long> {

}
