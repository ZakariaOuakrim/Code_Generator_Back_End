package com.stage.code_gen.Services;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyMethod;
import com.stage.code_gen.Models.MyParameter;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Models.Project;
import com.stage.code_gen.Repositories.ClassRepository;
import com.stage.code_gen.Repositories.MethodRepository;
import com.stage.code_gen.Repositories.ProjectRepository;
import com.stage.code_gen.Repositories.PropertyRepository;
import com.stage.code_gen.Requests_Responses.RequestCreateClass;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.persistence.GenerationType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassGenerationService {
	private final PropertyRepository propertyRepository;
	private final MethodRepository methodRepository;
	private final ClassRepository classRepository;
	private final ProjectRepository projectRepository;

	public String generateClass(RequestCreateClass _class) {
		String javaCode;
		if (_class.getClassType().equals("JPA_INTERFACE")) {
			javaCode=generateJpaRepository(_class);
			return javaCode.toString();
		}
		if (_class.getClassType().equals("Service_GEN")) {
			javaCode=generateService(_class);
			return javaCode.toString();
		}
		if (_class.getClassType().equals("Controller_GEN")) {
			javaCode=generateController(_class);
			return javaCode.toString();
		}
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		PropertySource<JavaClassSource> property;
		FieldSource<JavaClassSource> field;
		AnnotationSource<JavaClassSource> annotation;
		boolean FieldAlreadyGotAssigndTheIdAnnotation = false;

		// setting the class name and the package name
		if(_class.getPackageName()!=null)
			javaClass.setPackage(_class.getPackageName());
		javaClass.setName(_class.getClassName());
		// setting the annotation of the class
		if (_class.getClassType() != null && !_class.getClassType().equals("JPA_INTERFACE")) {
			javaClass.addAnnotation(typeAnnotation(_class.getClassType()));
		}
		if (_class.getTableName() != null) {
			javaClass.addAnnotation("javax.persistence.Table").setStringValue("name",_class.getTableName());
		}
		if(_class.getRequestMappingURL()!=null) {
			annotation = javaClass.addAnnotation("org.springframework.web.bind.annotation.RequestMapping");
			annotation.setStringValue(_class.getRequestMappingURL());
		}
			
		List<MyProperty> properties = propertyRepository.findByMyclassId(_class.getId());
		
		if (properties != null && !properties.isEmpty()) {
			for (MyProperty prop : properties) {
				property = javaClass.addProperty(prop.getType(), prop.getName());
				if(!_class.getClassType().equals("Entity")){
					property.removeAccessor().removeMutator();
				}
				field = javaClass.getField(prop.getName());
				switch (prop.getAccess_modifier()) {
				case "private":
					field.setPrivate();
					break;
				case "public":
					field.setPublic();
					break;
				case "protected":
					field.setProtected();
					break;
				case "default":
					field.setPackagePrivate();
					break;
				}
				

				if (_class.isIdGenerate() && !FieldAlreadyGotAssigndTheIdAnnotation) {
					field.addAnnotation("javax.persistence.Id");
					FieldAlreadyGotAssigndTheIdAnnotation = true;
					if (_class.isGeneratedValue()) {
						switch (_class.getGeneratedType()) {
						case "AUTO":
							field.addAnnotation("javax.persistence.GeneratedValue").setEnumValue("strategy",
									GenerationType.AUTO);
							;
							break;
						case "IDENTITY":
							field.addAnnotation("javax.persistence.GeneratedValue").setEnumValue("strategy",
									GenerationType.IDENTITY);
							break;
						case "SEQUENCE":
							field.addAnnotation("javax.persistence.GeneratedValue").setEnumValue("strategy",
									GenerationType.SEQUENCE);
							break;
						case "TABLE":
							field.addAnnotation("javax.persistence.GeneratedValue").setEnumValue("strategy",
									GenerationType.TABLE);
							break;
						}
					}
				}
				if(prop.getColumnName()!=null) {
					if (!prop.getColumnName().equals("")) {
						annotation = field.addAnnotation("javax.persistence.Column");
						annotation.setStringValue("name", prop.getColumnName());
						if(prop.getLength()!=null ) {
							annotation.setLiteralValue("length", prop.getLength());
						}
					}
				}
			
				
				if (prop.isAutowired()) {
					field.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
				}
			}
		}
		// ------------------------Method-------------------------
		MethodSource<JavaClassSource> my_method;
		ParameterSource<JavaClassSource> parameter;
		List<MyMethod> methods = methodRepository.findByMyclassId(_class.getId());
		if (methods != null) {
			for (MyMethod method : methods) {
				my_method = javaClass.addMethod().setName(method.getName()).setReturnType(method.getReturnType())
						.setBody(method.getBody());
				//setting the requestMappingType
				if(method.getRequestMappingType()!=null) {
					switch (method.getRequestMappingType()) {
						case "Post": 
							my_method.addAnnotation("org.springframework.web.bind.annotation.PostMapping");				
							break;
						case "Get": 
							my_method.addAnnotation("org.springframework.web.bind.annotation.GetMapping");				
							break;
						case "Put": 
							my_method.addAnnotation("org.springframework.web.bind.annotation.PutMapping");				
							break;
						case "Delete": 
							my_method.addAnnotation("org.springframework.web.bind.annotation.DeleteMapping");				
							break;
					}
					}
				
				
				switch (method.getVisibility()) {
				case "private":
					my_method.setVisibility(Visibility.PRIVATE);
					break;
				case "public":
					my_method.setVisibility(Visibility.PUBLIC);
					break;
				case "protected":
					my_method.setVisibility(Visibility.PROTECTED);
					break;
				case "default":
					my_method.setVisibility(Visibility.PACKAGE_PRIVATE);
					break;
				}
				if (method.getParameters() != null) {
					for (MyParameter param : method.getParameters()) {
						parameter = my_method.addParameter(param.getType(), param.getName());
					}
				}

			}
		}
 		return javaClass.toString();
	}

	public void load_file(String java_code, String file_name) {
		// TODO Auto-generated method stub
		try {
			File file = new File("C:\\Users\\Dell\\Desktop\\generated_classes\\"+file_name+".java");
			FileWriter writer = new FileWriter(file);
			writer.write(java_code);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String typeAnnotation(String ClassType) {
		switch (ClassType) {
		case "Entity":
			return "javax.persistence.Entity";
		case "Component":
			return "org.springframework.stereotype.Component";
		case "Controller":
			return "org.springframework.web.bind.annotation.RestController";
		case "Service":
			return "org.springframework.stereotype.Service";
		case "Configuration":
			return "org.springframework.context.annotation.Configuration";
		default:
			return "";
		}
	}

	private String generateController(RequestCreateClass _class) {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		PropertySource<JavaClassSource> property;
		FieldSource<JavaClassSource> field;
		//setting the request url
	
		javaClass.setName(_class.getClassName());
		String serviceName = _class.getClassName().substring(0, 1).toLowerCase()
				+ _class.getClassName().substring(1).replace("Controller", "Service");
		String serviceType = _class.getClassName().replace("Controller", "Service");
		javaClass.setPackage(_class.getPackageName());
		javaClass.addAnnotation("org.springframework.web.bind.annotation.RestController");
		property = javaClass.addProperty(serviceType, serviceName).removeAccessor().removeMutator();
		field = javaClass.getField(serviceName);
		field.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
		javaClass = generateMethodsForController(javaClass, _class, serviceName);
		
		
		return javaClass.toString();
	}

	private String generateService(RequestCreateClass _class) {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setName(_class.getClassName());
		javaClass.setPackage(_class.getPackageName());
		javaClass.addAnnotation(Service.class);
		Project project = projectRepository.findByClassesId(_class.getId());
		javaClass.addImport(project.getGroupId()+".Repositories."+_class.getClassName().replace("Service", "Repository"));
		javaClass.addImport(project.getGroupId()+".Entity."+_class.getClassName().replace("Service", ""));
		
		PropertySource<JavaClassSource> property;
		FieldSource<JavaClassSource> field;
		// injecting the repository
		String repositoryName = _class.getClassName().substring(0, 1).toLowerCase()
				+ _class.getClassName().substring(1).replace("Service", "Repository");
		String repositoryType = _class.getClassName().replace("Service", "Repository");
		property = javaClass.addProperty(repositoryType, repositoryName).removeAccessor().removeMutator();
		field = javaClass.getField(repositoryName);
		field.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
		javaClass = generateMethodsForService(javaClass, _class, repositoryName);
		return javaClass.toString();
	}

	private String generateJpaRepository(RequestCreateClass _class) {
		MyProperty idPropertyType = getIdPropertyType(_class);
		if (idPropertyType != null) {
			// make sure that the first letter of the type is uppercase
			JavaInterfaceSource javainterface = Roaster.create(JavaInterfaceSource.class);
			String idPropertyTypeWithUpperCase = getIdPropertyType(idPropertyType.getType());
			// setting the name of the repo
			javainterface.setName(_class.getClassName());
			
			Project project = projectRepository.findByClassesId(_class.getId());
			javainterface.addImport(project.getGroupId()+".Entity."+_class.getClassName().replace("Repository", ""));


			javainterface.setPackage(_class.getPackageName());
			javainterface.addAnnotation("org.springframework.stereotype.Repository");
			javainterface.addInterface("org.springframework.data.jpa.repository.JpaRepository<"
					+ _class.getClassName().replace("Repository", "") + ", " + idPropertyTypeWithUpperCase + ">");
			return javainterface.toString();
		} else {
			System.out.println("There is no id in this class so no Repository could be generated");
			return "";
		}

	}

	private MyProperty getIdPropertyType(RequestCreateClass requestCreateClass) {
		// -----------------id - 1 so I can get the entity
		List<MyProperty> properties = propertyRepository.findByMyclassId(requestCreateClass.getId() - 1);
		// -------------------getting the entity class
		MyClass _class = classRepository.findById(requestCreateClass.getId() - 1).get();
		if (properties != null) {
			if (_class.isIdGenerate()) {
				return properties.get(0);
			} else {
				return null;
			}

		}
		return null;
	}

	private String getIdPropertyType(String propertyType) {
		return propertyType.substring(0, 1).toUpperCase() + propertyType.substring(1);
	}

	// this is method is used for generating the methods for the service class CRUD
	private JavaClassSource generateMethodsForService(JavaClassSource javaClass, RequestCreateClass _class,
			String repositoryName) {
		MethodSource<JavaClassSource> my_method;
		String entityName = _class.getClassName().replace("Service", "");
		String entityNameWithLowerCase = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
		
		
		// ------------------Saving an Entity-------------------------------------
		my_method = javaClass.addMethod().setName("saveNew" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("void");
		my_method.addParameter(entityName, entityNameWithLowerCase);
		my_method.setBody(repositoryName + ".save(" + entityNameWithLowerCase + ");");

		// ----------------------Reading the Entities------------------------
		String returnType = "List<" + entityName + ">";
		javaClass.addImport("java.util.List");
		my_method = javaClass.addMethod().setName("getAll" + entityName + "s").setVisibility(Visibility.PUBLIC)
				.setReturnType(returnType);
		my_method.setBody("return " + repositoryName + ".findAll();");

		// -----------------------Updating an Entity--------------------
		my_method = javaClass.addMethod().setName("update" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("void");
		my_method.addParameter(entityName, entityNameWithLowerCase);
		my_method.setBody(repositoryName + ".save(" + entityNameWithLowerCase + ");");

		// ------------------------Deleting an Entity--------------------------
		my_method = javaClass.addMethod().setName("delete" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("void");
		my_method.addParameter(entityName, entityNameWithLowerCase);
		my_method.setBody(repositoryName + ".delete(" + entityNameWithLowerCase + ");");
		return javaClass;
	}

	private JavaClassSource generateMethodsForController(JavaClassSource javaClass, RequestCreateClass _class,
			String serviceName) {
		MethodSource<JavaClassSource> my_method;
		ParameterSource<JavaClassSource> my_parameter;
		String entityName = _class.getClassName().replace("Controller", "");
		String entityNameWithLowerCase = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
		String entities = entityNameWithLowerCase + "s";
		//Add the request url to the class
		
		//testing
		
		Project project = projectRepository.findByClassesId(_class.getId());
		javaClass.addImport(project.getGroupId()+".Entity."+entityName);
		javaClass.addImport(project.getGroupId()+".Services."+_class.getClassName().replace("Controller", "Service"));
		javaClass.addImport("java.util.List");
		// ----------------------------Save the Entity----------------------
		my_method = javaClass.addMethod().setName("save" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("void");
		my_parameter = my_method.addParameter(entityName, entityNameWithLowerCase);
		//my_parameter.addAnnotation("javax.validation.Valid");
		my_parameter.addAnnotation("org.springframework.web.bind.annotation.RequestBody");
		my_method.setBody(serviceName + ".saveNew" + entityName + "(" + entityNameWithLowerCase + ");");
		my_method.addAnnotation("org.springframework.web.bind.annotation.PostMapping").setStringValue("/" + entities);

		// --------------------------------------------------Read entity----------------------------------------------
		my_method = javaClass.addMethod().setName("getAll" + entityName+"s").setVisibility(Visibility.PUBLIC)
				.setReturnType("List<" + entityName + ">").setBody("return " + serviceName + ".getAll" + entityName+"s();");
		my_method.addAnnotation("org.springframework.web.bind.annotation.GetMapping").setStringValue("/" + entities);

		// ----------------------------------------------Update an entity----------------------------
		my_method = javaClass.addMethod().setName("update" + entityName).setVisibility(Visibility.PUBLIC)
										.setReturnType("void")
										.setBody( serviceName+ ".update" + entityName + "("+ entityNameWithLowerCase +");");
		my_parameter = my_method.addParameter(entityName, entityNameWithLowerCase);
		my_parameter.addAnnotation("org.springframework.web.bind.annotation.RequestBody");
		my_method.addAnnotation("org.springframework.web.bind.annotation.PutMapping").setStringValue("/"+entities);

		//-----------------------------------------------Delete an entity-------------------------
		my_method = javaClass.addMethod()
				.setName("delete"+entityName)
				.setVisibility(Visibility.PUBLIC)
				.setReturnType(String.class)
				.setBody(serviceName+".delete"+entityName+"("+entityNameWithLowerCase+"); return \"Deleted Successfully\";");
		my_method.addParameter(entityName,entityNameWithLowerCase).addAnnotation("org.springframework.web.bind.annotation.RequestBody");
		my_method.addAnnotation("org.springframework.web.bind.annotation.DeleteMapping").setStringValue("/"+entities);		
		
		return javaClass;
	}
}
