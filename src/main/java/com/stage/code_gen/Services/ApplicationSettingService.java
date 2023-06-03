package com.stage.code_gen.Services;

import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.ApplicationSetting;
import com.stage.code_gen.Repositories.ApplicationSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationSettingService {
	private final ApplicationSettingRepository applicationSettingRepository;
	
	public ApplicationSetting getApplicationSetting(Long id) {
		return applicationSettingRepository.findById(id).get();
	}
	
	public void modifyApplicationSetting(ApplicationSetting applicationSetting) {
		ApplicationSetting existingApplicationSetting = applicationSettingRepository.findById(applicationSetting.getId()).get();
		applicationSetting.setId(applicationSetting.getId());
		existingApplicationSetting = applicationSetting;	
		applicationSettingRepository.save(applicationSetting);
	}
}
