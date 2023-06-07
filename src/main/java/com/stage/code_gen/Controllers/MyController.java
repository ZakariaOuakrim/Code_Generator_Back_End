package com.stage.code_gen.Controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import org.apache.maven.project.MavenProject;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.code_gen.Models.ApplicationSetting;
import com.stage.code_gen.Models.Dependency;
import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyMethod;
import com.stage.code_gen.Models.MyParameter;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Models.Project;
import com.stage.code_gen.Repositories.ClassRepository;
import com.stage.code_gen.Repositories.ProjectRepository;
import com.stage.code_gen.Repositories.PropertyRepository;
import com.stage.code_gen.Repositories.UserRepository;
import com.stage.code_gen.Requests_Responses.RequestCreateClass;
import com.stage.code_gen.Requests_Responses.RequestCreateProject;
import com.stage.code_gen.Services.ClassGenerationService;
import com.stage.code_gen.Services.ClassService;
import com.stage.code_gen.Services.CompressionService;
import com.stage.code_gen.Services.DependencyService;
import com.stage.code_gen.Services.ProjectGenerationService;
import com.stage.code_gen.Services.ProjectService;
import com.stage.code_gen.Services.XmlFileService;

import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;

import net.minidev.json.parser.ParseException;

@RestController
@CrossOrigin(origins = "*")
public class MyController {
	@Autowired
	private CompressionService compressionService;
	@Autowired
	private ClassService classService;
	@Autowired
	private ClassGenerationService classGenerationService;
	@Autowired
	private ProjectGenerationService projectGenerationService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DependencyService dependencyService;
	@Autowired
	private XmlFileService xmlFileService;
	
	private Logger log = LoggerFactory.getLogger(MyController.class);
	
	@CrossOrigin(origins = "*")
	@PostMapping("/class/createnewclass")
	public ResponseEntity<String> createNewClass(@RequestBody RequestCreateClass requestCreateClass) {
		Long id = classService.addANewClass(requestCreateClass);
		String response = "";
		if (id == -1L)
			response = "ClassAlreadyExists";
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/class/listOfClasses/{projectId}")
	public List<MyClass> getAllClasses(@PathVariable Long projectId) {
		return classService.getAllClasses(projectId);
	}

	@GetMapping("/class/listOfClassesByEmail/{email}")
	public List<MyClass> getAllClassesOfAUser(@PathVariable("email") String email) {
		return classService.getClassesByUserEmail(email);
	}

	// getting the package name
	@GetMapping("/class/listOfPackageNames/{projectId}")
	public List<String> getAllPackageNames(@PathVariable Long projectId) {
		return classService.getAllPackageNames(projectId);
	}

	@GetMapping("/class/{packageName}")
	public List<MyClass> getClassesByPackageName(@PathVariable String packageName) {
		return classService.getClassesByPackageName(packageName);
	}

	@PostMapping("/class/downloadClass")
	public ResponseEntity<Resource> downloadClass(@RequestBody RequestCreateClass requestCreateClass) {
		File file = new File(
				"C:\\Users\\Dell\\Desktop\\generated_classes\\" + requestCreateClass.getClassName() + ".java");
		Resource resource = new FileSystemResource(file);
		String codeJavaGenerated = classGenerationService.generateClass(requestCreateClass);
		String contentType = "application/octet-stream";
		classGenerationService.load_file(codeJavaGenerated, requestCreateClass.getClassName());
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
	}

	@DeleteMapping("/class/deleteClass/{idProject}/{id}")
	public void deleteClass(@PathVariable("idProject") int idProject, @PathVariable("id") int id) {
		classService.deleteClass(Long.valueOf(id), Long.valueOf(idProject));
	}

	@GetMapping("/class/listOfClassesOfAUser")
	public List<MyClass> getClassesByUserEmail(@PathVariable String email) {

		return null;
	}

	@GetMapping("/class/getAllPackageNamesByEmail/{email}")
	public List<String> getAllPackageNameByEmail(@PathVariable("email") String email) {
		return classService.getAllPackageNamesOfAUser(email);
	}

	@PostMapping("/project/createNewProject")
	public void createNewProject(@RequestBody RequestCreateProject requestCreateProject) {
		projectService.createNewProject(requestCreateProject.getProject(), requestCreateProject.getEmail());
	}

	@GetMapping("/project/getAllProjects/{email}")
	public List<Project> getAllProjects(@PathVariable("email") String email) {
		return projectService.getAllProjects(email);
	}

	@PostMapping("/project/downloadProject/{email}")
	public ResponseEntity<Resource> downloadProject(@RequestBody Project project,@PathVariable("email") String email) throws IOException {
		projectGenerationService.generateProject(project,email);
		
		String generationPath = "C:\\Users\\Dell\\Desktop\\generated_classes";
		File myFile = new File(generationPath);
		if(myFile.listFiles().length>0) {
			for(File _file: myFile.listFiles()) {
				_file.delete();
			}
		}
		String folderPath = "C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId();
		String zipFilePath = "C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + ".zip";
	
		
		MavenProject _project = projectGenerationService.createPomFile(project);
		projectGenerationService.generatePomFile(_project, folderPath);

		try {
			compressionService.compressFolder(folderPath, zipFilePath);
			System.out.println("Folder compressed successfully.");
		} catch (IOException e) {
			System.err.println("Error occurred while compressing the folder: " + e.getMessage());
		}

		File fileZip = new File(zipFilePath);
		Resource resource = new FileSystemResource(fileZip);
		String contentType = "application/octet-stream";
		
		//we need to delete the project from the server 

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
	}

	@GetMapping("/class/getClass/{id}")
	public RequestCreateClass getClass(@PathVariable("id") Long id) {
		System.out.println("id " + id);
		return classService.getClassById(id);
	}

	@PostMapping("/class/getCodeOfClass")
	public String getCodeOfClass(@RequestBody RequestCreateClass requestCreateClass) {
		return classGenerationService.generateClass(requestCreateClass);
	}

	@GetMapping("/project/getAllProjectsAdminVersion")
	public List<Project> getAllProjects() {
		return projectService.getAllProjectsAdminVersion();
	}
	@GetMapping("/project/getProjectById/{id}")
	public Project getProjectById(@PathVariable("id") Long projectId) {
		return projectService.getProjectById(projectId);
	}

	@PostMapping("/dependency/addANewDependency")
	public void addANewDependency(@RequestBody Dependency dependency) {
		dependencyService.addNewDependency(dependency);
	}

	@GetMapping("/dependency/getAllDependencies")
	public List<Dependency> getAllDepnDependencies() {
		return dependencyService.getAllDependencies();
	}

	@DeleteMapping("/dependency/deleteADependency/{id}")
	public void deleteDependency(@PathVariable("id") Long dependencyId) {
		dependencyService.deleteDependency(dependencyId);
	}

	@PostMapping("/project/addDependencyToProject/{projectId}")
	public void addDepenedencyToProject(@PathVariable("projectId") Long projectId, @RequestBody Long dependencyId) {
		projectService.addANewDependencyToProject(projectId, dependencyId);
	}

	// delete dependency from a project
	@DeleteMapping("/project/deleteDependencyFromProject/{project_id}/{dependencyId}")
	public void deleteDependencyFromProject(@PathVariable("project_id") Long projectId,
			@PathVariable("dependencyId") Long dependencyId) {
		projectService.deleteDependencyFromProject(projectId, dependencyId);
	}
	@PutMapping("/project/modifyProject/{id}")
	public void modifyProject(@PathVariable("id") Long id,@RequestBody Project project) {
		Project existingProject = projectService.getProjectById(id);
		project.setId(id);
		existingProject = project;
		projectService.modifyProject(existingProject);
	}

	
	//delete a project
	@DeleteMapping("/project/deleteProject/{projectId}")
	public void deleteProject(@PathVariable("projectId") Long projectId) {
		//delete classes of project
		for(MyClass _class:projectService.getProjectById(projectId).getClasses()) {
			classService.deleteClass(_class.getId(), projectId);
		}
		//delete project
		projectService.deleteProject(projectId);
	}
	

	@GetMapping("/dependency/projectDependencies/{id}")
	public List<Long> getDependenciesOfAProjectById(@PathVariable("id") Long projectId) {
		return projectService.getAllDepenenciesOfAProject(projectId);
	}

	// get dependency By Id
	@GetMapping("/dependency/getDependencyById/{id}")
	public Dependency getDependencyById(@PathVariable("id") Long id) {
		return dependencyService.getDependencyById(id);
	}

	@PutMapping("/dependency/modifyDependency/{id}")
	public void modifyDependency(@PathVariable("id") Long id, @RequestBody Dependency dependency) {
		Dependency existingDependency = dependencyService.getDependencyById(id);
		dependency.setId(id);
		existingDependency = dependency;
		dependencyService.modifyDependency(dependency);
	}
	
	// file uploading
	@PostMapping("/file/upload/{email}")
	public List<RequestCreateClass> uploadFile(@PathVariable("email") String email,@RequestParam("files") MultipartFile[] files) throws JDOMException {
		try {
			String uploadDir = "C:\\Users\\Dell\\Desktop\\generated_classes";
			List<RequestCreateClass> results = new ArrayList<>();
			File dest;
			String filePath;
			String fileName;
			Project _project = new Project();
			_project.setArtifactId("ProjectFromHibernateXmlConvertor");
			_project.setGroupId("com.example");
			_project.setModelVersion("4.0.0");
			_project.setVersion("1.0.0");
			_project.setProjectVersion("jar");
			_project.setClasses(new ArrayList<>());
			projectService.createNewProject(_project, email);
			//getting the id of the last created project
		 	Long idOfThelastCreatedProject= projectService.getAllProjects(email).get(projectService.getAllProjects(email).size()-1).getId();
			
		 	for (MultipartFile file : files) {
				fileName = file.getOriginalFilename();
				filePath = uploadDir + File.separator + fileName;
				dest = new File(filePath);
				file.transferTo(dest);
				List<RequestCreateClass> fileResult = xmlFileService.getProperitesFromXmlFile(dest,idOfThelastCreatedProject);
				results.addAll(fileResult);
				dest.delete();
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
