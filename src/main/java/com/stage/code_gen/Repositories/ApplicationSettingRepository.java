package com.stage.code_gen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stage.code_gen.Models.ApplicationSetting;

@Repository
public interface ApplicationSettingRepository extends JpaRepository<ApplicationSetting, Long>{

}
