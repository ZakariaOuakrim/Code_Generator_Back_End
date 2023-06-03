package com.stage.code_gen.Services;

import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.ApplicationSetting;
import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.Project;
import com.stage.code_gen.Models.User;
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
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Build;
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
	private final AuthenticationService authenticationService;
	private final ApplicationSettingService applicationSettingService;

	public void generateProject(Project project,String email) throws IOException {
		RequestCreateClass _class;
		File f = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId());
		f.mkdir();
		File srcmainjava = new File(
				"C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\java");
		srcmainjava.mkdirs();
		// resources File
		File srcmainresources = new File(
				"C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\resources");
		srcmainresources.mkdir();
		// static file
		srcmainresources = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()
				+ "\\src\\main\\resources\\static");
		srcmainresources.mkdir();
		// templates file
		srcmainresources = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()
				+ "\\src\\main\\resources\\templates");
		srcmainresources.mkdir();
		// application.properties File
		User user = authenticationService.getUser(email);
		//get the project setting 
		ApplicationSetting applicationSettting=applicationSettingService.getApplicationSetting(user.getApp_SettingId());
		generateApplicationPropertiesFile(applicationSettting,project);

		classRepository.findAllPackages(project.getClasses());
		File files, files2;
		List<String> project_packageName = Arrays.asList(project.getGroupId().split("\\."));
		String packageCreated = "";

		// creating the urls of the package of the project
		for (String _package : project_packageName) {
			packageCreated = packageCreated + "\\" + _package;
		}
		files = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\java"
				+ packageCreated);
		files.mkdirs();

		// -------------------Creating the Config package for
		// configuration--------------------
		files = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\java"
				+ packageCreated + "\\Config");
		files.mkdir();
		files = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId() + "\\src\\main\\java"
				+ packageCreated );
		files.mkdir();
		// generating the config file
		classGenerationService.load_file(generateSwaggerConfigFile(project.getGroupId() + ".Config"),
				project.getArtifactId() + "\\src\\main\\java" + packageCreated + "\\Config\\SwaggerConfig");

		// generating the DemoApplication File
		classGenerationService.load_file(generateSpringApplicationMain(project.getGroupId()),
				project.getArtifactId() + "\\src\\main\\java" + packageCreated + "\\SpringApplicationMain");

		for (String packageName : classRepository.findAllPackages(project.getClasses())) {
			for (MyClass classOfPackage : project.getClasses()) {
				if (classOfPackage.getPackageName().equals(packageName)) {
					files2 = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()
							+ "\\src\\main\\java" + packageCreated + "\\" + classOfPackage.getPackageName()
									.substring(classOfPackage.getPackageName().lastIndexOf('.') + 1));
					files2.mkdir();
					_class = classService.generateRequestCreateClassFromClass(classOfPackage);
					String codeJava = classGenerationService.generateClass(_class);
					classGenerationService.load_file(codeJava,
							project.getArtifactId() + "\\src\\main\\java\\" + packageCreated + "\\"
									+ classOfPackage.getPackageName()
											.substring(classOfPackage.getPackageName().lastIndexOf('.') + 1)
									+ "\\" + _class.getClassName());
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
		for (com.stage.code_gen.Models.Dependency depend : _project.getDependencies()) {
			addDependency(project, depend.getGroupId(), depend.getArtifactId(), depend.getVersion());
		}
		project.setPackaging(_project.getProjectVersion());
		
		
		
		return project;
	}

	public void generatePomFile(MavenProject project, String targetDirectory) {
		Model model = project.getModel();
		MavenXpp3Writer writer = new MavenXpp3Writer();
		File pomFile = new File(targetDirectory, "pom.xml");
		Parent projectParent = new Parent();
		projectParent.setGroupId("org.springframework.boot");
		projectParent.setArtifactId("spring-boot-starter-parent");
		projectParent.setVersion("2.5.9");
		
		Build build = new Build();
	
		// Adding the plugins to the build
		Plugin springBootPlugin = new Plugin();
		springBootPlugin.setGroupId("org.springframework.boot");
		springBootPlugin.setArtifactId("spring-boot-maven-plugin");

		build.addPlugin(springBootPlugin);
		model.setBuild(build);
		model.setParent(projectParent);
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
		if(!version.equals("")) {
			dependency.setVersion(version);
		}
		Model model = project.getModel();
		model.addDependency(dependency);
	}

	private String generateSpringApplicationMain(String packageName) {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.addAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication");
		javaClass.setName("SpringApplicationMain");
		javaClass.setPackage(packageName);
		MethodSource<JavaClassSource> method = javaClass.addMethod();
		method.setName("main");
		method.setVisibility(Visibility.PUBLIC);
		method.setStatic(true);
		method.addParameter("String[]", "args");
		method.setBody("SpringApplication.run(SpringApplicationMain.class, args);");
		javaClass.addImport("org.springframework.boot.SpringApplication");

		return javaClass.toString();
	}

	private String generateSwaggerConfigFile(String packageName) {
		String controllerPackageName = packageName.replace("Config", "Controller");
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(packageName);
		javaClass.setName("SwaggerConfig");
	
		javaClass.addImport("springfox.documentation.spring.web.plugins.Docket");
		javaClass.addImport("springfox.documentation.builders.PathSelectors");
		javaClass.addImport("springfox.documentation.builders.RequestHandlerSelectors");
		javaClass.addImport("springfox.documentation.service.ApiInfo");
		javaClass.addImport("springfox.documentation.spi.DocumentationType");
		javaClass.addAnnotation("org.springframework.context.annotation.Configuration");		
		javaClass.addAnnotation("springfox.documentation.swagger2.annotations.EnableSwagger2");

		
		MethodSource<JavaClassSource> method = javaClass.addMethod();
		method.addAnnotation("org.springframework.context.annotation.Bean");
		method.setVisibility(Visibility.PUBLIC);
		method.setReturnType("Docket");
		method.setName("api");
		method.setBody("return new Docket(DocumentationType.SWAGGER_2)" + ".select()"
				+ ".apis(RequestHandlerSelectors.basePackage(\"" + controllerPackageName + "\"))"
				+ ".paths(PathSelectors.any())" + ".build();");

		return javaClass.toString();
	}

	private String generateApplicationPropertiesFile(ApplicationSetting applicationSetting,Project project) {
		String applicationPort ="server.port=";
		applicationPort += applicationSetting.getApplicationPort();
		String dataBaseType = "spring.datasource.url=";
		switch (applicationSetting.getTypeOfDatabase()) {
		case "PostgreSQL":
			dataBaseType += "jdbc:postgresql://localhost:5432/code_gen";
			break;
		case "MySQL":
			dataBaseType += "jdbc:mysql://localhost:3306/database";
			break;
		case "Oracle":
			dataBaseType += "jdbc:oracle:thin:@localhost:1521:database";
			break;
		}
		String userName = "spring.datasource.username=";
		userName+=applicationSetting.getUserNameDataSource();
		String password = "spring.datasource.password=";
		password +=applicationSetting.getPasswordDataSource();
		
		String fileText = applicationPort+"\n"+dataBaseType+"\n"+userName+"\n"+password+"\n"
				+"spring.jpa.properties.hibernate.dialect org.hibernate.dialect.PostgreSQLDialect"+"\n"
				+"spring.datasource.driverClassName=org.postgresql.Driver\n"+"spring.jpa.hibernate.ddl-auto = update\n"
				+ "spring.jpa.properties.hibernate.default_schema=my_schema";
		try {
			File file = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\" + project.getArtifactId()
			+ "\\src\\main\\resources\\application.properties");
			FileWriter writer = new FileWriter(file);
			writer.write(fileText);
			writer.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return fileText;
	}
}
