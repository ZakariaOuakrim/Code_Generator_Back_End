package com.stage.code_gen.Services;

import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.Project;
import com.stage.code_gen.Repositories.ClassRepository;
import com.stage.code_gen.Repositories.ProjectRepository;
import com.stage.code_gen.Requests_Responses.RequestCreateClass;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

@Service
@RequiredArgsConstructor
public class ProjectGenerationService {
	private final ProjectRepository projectRepository;
	private final ClassRepository classRepository;
	private final ClassGenerationService classGenerationService;
	private final ClassService classService;

	public void generateProject(Project project) throws IOException {
		RequestCreateClass _class;
		File f = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId());
		f.mkdir();
		File srcmainjava = new File(
				"C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\java");
		srcmainjava.mkdirs();
		//resources File
		File srcmainresources = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\resources");
		srcmainresources.mkdir();
		//static file
		srcmainresources = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\resources\\static");
		srcmainresources.mkdir();
		//templates file
		srcmainresources = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\resources\\templates");		
		srcmainresources.mkdir();
		//application.properties File
		srcmainresources = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\resources\\application.properties");		
		srcmainresources.createNewFile();

		classRepository.findAllPackages(project.getClasses());
		File files, files2;
		List<String> project_packageName = Arrays.asList(project.getGroupId().split("\\."));
		String packageCreated="";
		
		//creating the urls of the package of the project
		for (String _package : project_packageName) {
			packageCreated=packageCreated+"\\"+_package;
		}
		files = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()+ "\\src\\main\\java" + packageCreated);
		files.mkdirs();
		
		//-----------------------Creating the Init as mainConfig for the project----------------- 
		files =  new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()+ "\\src\\main\\java" + packageCreated+"\\Init");
		files.mkdir();
	
		//genererating the ServletInitializer File
		classGenerationService.load_file(generateServletInitializerCode(project.getGroupId()+".Init"), project.getArtifactId()+ "\\src\\main\\java" + packageCreated+"\\Init\\ServletInitializer");
		//genererating the DemoApplication File
		classGenerationService.load_file(generateDemoApplicationCode(project.getGroupId()+".Init"), project.getArtifactId()+ "\\src\\main\\java" + packageCreated+"\\Init\\DemoApplication");

		for (String packageName : classRepository.findAllPackages(project.getClasses())) {
			for (MyClass classOfPackage : project.getClasses()) {
				if (classOfPackage.getPackageName().equals(packageName)) {
					files2 = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()+ "\\src\\main\\java"+packageCreated+"\\"+classOfPackage.getPackageName().substring(classOfPackage.getPackageName().lastIndexOf('.')+1));		
					files2.mkdir();
					_class = classService.generateRequestCreateClassFromClass(classOfPackage);
					String codeJava = classGenerationService.generateClass(_class);
					classGenerationService.load_file(codeJava, project.getArtifactId() + "\\src\\main\\java\\"+ packageCreated+"\\"+classOfPackage.getPackageName().substring(classOfPackage.getPackageName().lastIndexOf('.')+1)+"\\"+_class.getClassName() );
				}
			}
		}

	}

	public MavenProject createPomFile(Project _project) {
		MavenProject project = new MavenProject();
		project.setGroupId(_project.getGroupId());
		project.setArtifactId(_project.getArtifactId());
		project.setVersion(_project.getVersion());
		project.setModelVersion(_project.getModelVersion());
		// adding dependcies to the project
		for(com.stage.code_gen.Models.Dependency depend :_project.getDependencies()) {
			addDependency(project, depend.getGroupId(), depend.getArtifactId(), depend.getVersion());
		}
		project.setPackaging(_project.getProjectVersion());
		
		return project;
	}

	public void generatePomFile(MavenProject project, String targetDirectory) {
		Model model = project.getModel();
		MavenXpp3Writer writer = new MavenXpp3Writer();
		File pomFile = new File(targetDirectory, "pom.xml");
		try {
			FileWriter fileWriter = new FileWriter(pomFile);
			writer.write(fileWriter, model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addDependency(MavenProject project, String groupId, String artifactId, String version) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(groupId);
		dependency.setArtifactId(artifactId);
		dependency.setVersion(version);
		Model model = project.getModel();
		model.addDependency(dependency);
	}
	
	private String generateDemoApplicationCode(String packageName) {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.addAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication");
		javaClass.setName("DemoApplication");
		javaClass.setPackage(packageName);
		MethodSource<JavaClassSource> method=javaClass.addMethod();
		method.setName("main");
		method.setVisibility(Visibility.PUBLIC);
		method.setStatic(true);
		method.addParameter("String[]", "args");
		method.setBody("SpringApplication.run(DemoApplication.class, args);");
		javaClass.addImport("org.springframework.boot.SpringApplication");
		
		return javaClass.toString();
	}
	
	private String generateServletInitializerCode(String packageName) {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(packageName);
		javaClass.setName("ServletInitializer");
		javaClass.setSuperType("SpringBootServletInitializer");
		javaClass.addImport("org.springframework.boot.web.servlet.support.SpringBootServletInitializer");
		javaClass.addImport("org.springframework.boot.builder.SpringApplicationBuilder");
		MethodSource<JavaClassSource> method=javaClass.addMethod();
		method.addAnnotation(Override.class);
		method.setVisibility(Visibility.PROTECTED);
		method.setReturnType("SpringApplicationBuilder");
		method.setName("configure");
		method.addParameter("SpringApplicationBuilder", "application");
		method.setBody("return application.sources(DemoApplication.class);");
		return javaClass.toString();
	}

}
