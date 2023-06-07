package com.stage.code_gen.Services;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
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
		AnnotationSource<JavaClassSource> apiModelPropertyAnnotation;

		boolean FieldAlreadyGotAssigndTheIdAnnotation = false;

		// setting the class name and the package name
		if(_class.getPackageName()!=null)
			javaClass.setPackage(_class.getPackageName());
		javaClass.setName(_class.getClassName());
		// setting the annotation of the class
		if (_class.getClassType() != null && !_class.getClassType().equals("JPA_INTERFACE")) {
			javaClass.addAnnotation(typeAnnotation(_class.getClassType()));
			if(_class.getClassType().equals("Embeddable")) {
				javaClass.addInterface(Serializable.class);
			}
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
			for (MyProperty prop : properties) {//for each property of the class 
				property = javaClass.addProperty(prop.getType(), prop.getName());
				if(!_class.getClassType().equals("Entity") && !_class.getClassType().equals("Embeddable")){
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
				if(_class.isEmbeddedId() && !FieldAlreadyGotAssigndTheIdAnnotation) {
					field.addAnnotation("javax.persistence.EmbeddedId");
					FieldAlreadyGotAssigndTheIdAnnotation=true;
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
				
				if(prop.getColumnName()!=null) { //adding the @column annotation
					if (!prop.getColumnName().equals("")) {
						annotation = field.addAnnotation("javax.persistence.Column");
						annotation.setStringValue("name", prop.getColumnName()); //setting the name= in the @Column
						//adding the @ApiModelProperty
						apiModelPropertyAnnotation = field.addAnnotation("io.swagger.annotations.ApiModelProperty");
						apiModelPropertyAnnotation.setStringValue("value",prop.getColumnName()); //setting the value= in the @ApiModel

						if(prop.getLength()!=null ) {
							annotation.setLiteralValue("length", prop.getLength());
							apiModelPropertyAnnotation.setStringValue("allowableValues","range[1,"+prop.getLength()+"]");
							if(prop.getType().equals("String") || prop.getType().equals("java.lang.Character") || prop.getType().equals("char") || prop.getType().equals("Character")) 
								apiModelPropertyAnnotation.setStringValue("example",generateExampleWord(Integer.valueOf(prop.getLength())));
						}
					}
				}
			
				
				if (prop.isAutowired()) {
					field.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
				}
			}//end of for
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
		case "Embeddable":
			return "javax.persistence.Embeddable";
		default:
			return ClassType;
		}
	}
	
	private String generateExampleWord(int length) {
		StringBuilder word= new StringBuilder();
		for(int i=0;i<length;i++) {
			char randomChar = (char) ('a'+Math.random() * ('z'-'a'+1));
            word.append(randomChar);
		}
		return word.toString();
	}
	
	private String generateController(RequestCreateClass _class) {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		PropertySource<JavaClassSource> property;
		FieldSource<JavaClassSource> field;
		Project project = projectRepository.findByClassesId(_class.getId());
		MyProperty idPropertyOfEntity= propertyRepository.findById(_class.getIdOfPropertyId()).get();

		//adding the import if the entity class has a composite id
		if(_class.isEmbeddedId()) {
			javaClass.addImport(project.getGroupId()+".Entity."+idPropertyOfEntity.getType());
		}
		
		
		javaClass.setName(_class.getClassName());
		String serviceName = _class.getClassName().substring(0, 1).toLowerCase()
				+ _class.getClassName().substring(1).replace("Controller", "Service");
		String serviceType = _class.getClassName().replace("Controller", "Service");
		javaClass.setPackage(_class.getPackageName());
		javaClass.addAnnotation("org.springframework.web.bind.annotation.RestController");
		property = javaClass.addProperty(serviceType, serviceName).removeAccessor().removeMutator();
		field = javaClass.getField(serviceName);
		field.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
		javaClass = generateMethodsForController(javaClass, _class, serviceName,idPropertyOfEntity);
		
		
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

		MyProperty idPropertyOfEntity= propertyRepository.findById(_class.getIdOfPropertyId()).get();

		//adding the import if the entity class has a composite id
		if(_class.isEmbeddedId() && !idPropertyOfEntity.getType().toString().equals("String")) {
			javaClass.addImport(project.getGroupId()+".Entity."+idPropertyOfEntity.getType());
		}
		
		PropertySource<JavaClassSource> property;
		FieldSource<JavaClassSource> field;
		// injecting the repository
		String repositoryName = _class.getClassName().substring(0, 1).toLowerCase()
				+ _class.getClassName().substring(1).replace("Service", "Repository");
		String repositoryType = _class.getClassName().replace("Service", "Repository");
		property = javaClass.addProperty(repositoryType, repositoryName).removeAccessor().removeMutator();
		field = javaClass.getField(repositoryName);
		field.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
		javaClass = generateMethodsForService(javaClass, _class, repositoryName,idPropertyOfEntity);
		return javaClass.toString();
	}
	private String generateJpaRepository(RequestCreateClass _class) {
		MyProperty idPropertyOfEntity= propertyRepository.findById(_class.getIdOfPropertyId()).get();
		Project project = projectRepository.findByClassesId(_class.getId());
		if (idPropertyOfEntity != null) {
			// make sure that the first letter of the type is uppercase
			JavaInterfaceSource javainterface = Roaster.create(JavaInterfaceSource.class);
			String idPropertyTypeWithUpperCase = getIdPropertyType(idPropertyOfEntity.getType());
			if(idPropertyTypeWithUpperCase.equals("Int")) {
				idPropertyTypeWithUpperCase = "Integer";
			}
		
			if(_class.isEmbeddedId() && !idPropertyOfEntity.getType().toString().equals("String")) {
				javainterface.addImport(project.getGroupId()+".Entity."+idPropertyOfEntity.getType().toString()+"");
			}
			// setting the name of the repo
			javainterface.setName(_class.getClassName());
			
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

	private String getIdPropertyType(String propertyType) {
		return propertyType.substring(0, 1).toUpperCase() + propertyType.substring(1);
	}

	// this is method is used for generating the methods for the service class CRUD
	private JavaClassSource generateMethodsForService(JavaClassSource javaClass, RequestCreateClass _class,
			String repositoryName,MyProperty idPropertyOfEntity) {
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
		my_method = javaClass.addMethod().setName("getAll" + entityName ).setVisibility(Visibility.PUBLIC)
				.setReturnType(returnType);
		my_method.setBody("return " + repositoryName + ".findAll();");

		// -----------------------Updating an Entity--------------------
		my_method = javaClass.addMethod().setName("update" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("void");
		my_method.addParameter(entityName, "updated"+entityName); // add the as a parameter Entity updatedEntity
		my_method.addParameter(idPropertyOfEntity.getType(),idPropertyOfEntity.getName());
		String methodBody = entityName+ " existing"+entityName+" ="+repositoryName+".findById("+idPropertyOfEntity.getName()+").get();\n "
				+ "updated"+entityName+".set"+idPropertyOfEntity.getName().substring(0,1).toUpperCase()+idPropertyOfEntity.getName().substring(1)+"("+idPropertyOfEntity.getName()+");\n"
				+ "existing"+entityName+"=updated"+entityName+";\n"
				+ repositoryName+".save(existing"+entityName+");";
		my_method.setBody(methodBody);                    

		// ------------------------Deleting an Entity--------------------------
		my_method = javaClass.addMethod().setName("delete" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("void");
		my_method.addParameter(idPropertyOfEntity.getType(), idPropertyOfEntity.getName());
		my_method.setBody(repositoryName + ".deleteById(" + idPropertyOfEntity.getName() +");");
		return javaClass;
	}

	private JavaClassSource generateMethodsForController(JavaClassSource javaClass, RequestCreateClass _class,
			String serviceName,MyProperty idPropertyOfEntity ) {
		MethodSource<JavaClassSource> my_method;
		ParameterSource<JavaClassSource> my_parameter;
		String entityName = _class.getClassName().replace("Controller", "");
		String entityNameWithLowerCase = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);

		
		String addedCodeIfTheEntityClassHasAnEmbeddable=""; //this is the code that needs to be add if the class is embeddable it will add the id.set()..
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
		my_method.addAnnotation("org.springframework.web.bind.annotation.PostMapping").setStringValue("/" + entityNameWithLowerCase);

		// --------------------------------------------------Read entity----------------------------------------------
		my_method = javaClass.addMethod().setName("getAll" + entityName).setVisibility(Visibility.PUBLIC)
				.setReturnType("List<" + entityName + ">").setBody("return " + serviceName + ".getAll" + entityName+"();");
		my_method.addAnnotation("org.springframework.web.bind.annotation.GetMapping").setStringValue("/" + entityNameWithLowerCase);

		// ----------------------------------------------Update an entity----------------------------
		my_method = javaClass.addMethod().setName("update" + entityName).setVisibility(Visibility.PUBLIC)
										.setReturnType("void");
		my_parameter = my_method.addParameter(entityName, entityNameWithLowerCase);
		my_parameter.addAnnotation("org.springframework.web.bind.annotation.RequestBody");
		MyClass embeddedClass=null;
		if(_class.getEmbeddedIdClassId()!=null) {
			embeddedClass = classRepository.findById(_class.getEmbeddedIdClassId()).get();
		}

		if(_class.isEmbeddedId() && embeddedClass!=null) {
			String pathOfPathVariables="/"+entityNameWithLowerCase; //example of this path of (Entity/{id1}/{id2}) 

			 addedCodeIfTheEntityClassHasAnEmbeddable=idPropertyOfEntity.getType()+" "+idPropertyOfEntity.getName()+" = new "+idPropertyOfEntity.getType()+"();\n"; //this is the code that needs to be add if the class is embeddable it will add the id.set()..
			for(MyProperty property: propertyRepository.findByMyclassId(embeddedClass.getId())) {
				my_parameter= my_method.addParameter(property.getType(), "_"+property.getName());
				my_parameter.addAnnotation("org.springframework.web.bind.annotation.PathVariable");
				pathOfPathVariables +="/{_"+property.getName()+"}"; 
				addedCodeIfTheEntityClassHasAnEmbeddable +=idPropertyOfEntity.getName()+".set"+property.getName().substring(0,1).toUpperCase()+property.getName().substring(1)+"(_"+property.getName()+");\n"; //here we are setting the properties
			}
			my_method.addAnnotation("org.springframework.web.bind.annotation.PutMapping").setStringValue(pathOfPathVariables);

			addedCodeIfTheEntityClassHasAnEmbeddable+=entityNameWithLowerCase+".set"+idPropertyOfEntity.getName().substring(0,1).toUpperCase()+idPropertyOfEntity.getName().substring(1)+"("+idPropertyOfEntity.getName()+");\n";//setting the composite object created to the class 
		}
		else {
			my_parameter = my_method.addParameter(idPropertyOfEntity.getType(),"_"+idPropertyOfEntity.getName());
			my_parameter.addAnnotation("org.springframework.web.bind.annotation.PathVariable");
			my_method.addAnnotation("org.springframework.web.bind.annotation.PutMapping").setStringValue("/"+entityNameWithLowerCase+"/{_"+idPropertyOfEntity.getName()+"}");

		}
		String idString=idPropertyOfEntity.getName();
		//if(idPropertyOfEntity.getName().equals(entityNameWithLowerCase))
			//idString="_"+idPropertyOfEntity.getName();
		my_method.setBody(addedCodeIfTheEntityClassHasAnEmbeddable+serviceName+ ".update" + entityName + "("+ entityNameWithLowerCase +", "+idString+");");
		
		//-----------------------------------------------Delete an entity-------------------------
		my_method = javaClass.addMethod()
				.setName("delete"+entityName)
				.setVisibility(Visibility.PUBLIC)
				.setReturnType(String.class)
				.setBody(serviceName+".delete"+entityName+"("+idPropertyOfEntity.getName()+"); return \"Deleted Successfully\";");
		my_method.addParameter(idPropertyOfEntity.getType(),idPropertyOfEntity.getName()).addAnnotation("org.springframework.web.bind.annotation.RequestBody");
		my_method.addAnnotation("org.springframework.web.bind.annotation.DeleteMapping").setStringValue("/"+entityNameWithLowerCase);		
		
		return javaClass;
	}
}
