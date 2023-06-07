package com.stage.code_gen.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyMethod;
import com.stage.code_gen.Models.MyParameter;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Models.Project;
import com.stage.code_gen.Models.User;
import com.stage.code_gen.Repositories.ClassRepository;
import com.stage.code_gen.Repositories.MethodRepository;
import com.stage.code_gen.Repositories.ParameterRepository;
import com.stage.code_gen.Repositories.ProjectRepository;
import com.stage.code_gen.Repositories.PropertyRepository;
import com.stage.code_gen.Requests_Responses.RequestCreateClass;

import jakarta.transaction.Transactional;

@Service
public class ClassService {

	@Autowired
	private PropertyRepository propertyRepository;
	@Autowired
	private MethodRepository methodRepository;
	@Autowired
	private ClassRepository classRepository;
	@Autowired
	private ParameterRepository parameterRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ClassGenerationService classGenerationService;
	@Autowired
	ProjectService projectService;

	public Long addANewClass(RequestCreateClass requestCreateClass) {
		// TODO Auto-generated method stub
		MyClass myclass = new MyClass();
		// ------------------Mapping the request to the class and property and method
		// Entities
		myclass.setPackageName(requestCreateClass.getPackageName());
		myclass.setClassName(requestCreateClass.getClassName());
		if (requestCreateClass.getTableName() != null) {
			myclass.setTableName(requestCreateClass.getTableName());
		}
		myclass.setRequestMappingURL(requestCreateClass.getRequestMappingURL());
		myclass.setClassType(requestCreateClass.getClassType());
		myclass.setService(requestCreateClass.isService());
		myclass.setGenerateRepository(requestCreateClass.isGenerateRepository());
		myclass.setGenerateController(requestCreateClass.isGenerateController());
		myclass.setGeneratedType(requestCreateClass.getGeneratedType());
		myclass.setIdGenerate(requestCreateClass.isIdGenerate());
		myclass.setGeneratedType(requestCreateClass.getGeneratedType());
		myclass.setGeneratedValue(requestCreateClass.isGeneratedValue());
		myclass.setEmbeddedId(requestCreateClass.isEmbeddedId());
		myclass.setIdOfPropertyId(requestCreateClass.getIdOfPropertyId());
		if (requestCreateClass.getMode() != null && requestCreateClass.getMode().equals("modify")) {
			classRepository.findById(requestCreateClass.getId());
			return 0L;
		}
		MyClass classToGetId;
		try {
			classToGetId = classRepository.save(myclass);
		} catch (DataIntegrityViolationException e) {
			return -1L;
		}
		boolean getIdPropertyOnce = false;
		Long idOfProperty = 0L;
		// adding the properties
		if (requestCreateClass.getProperties() != null && requestCreateClass.getProperties().length >= 1) {
			for (MyProperty prop : requestCreateClass.getProperties()) {

				classRepository.findById(myclass.getId()).map(my_class -> {
					prop.setMyclass(myclass);
					return propertyRepository.save(prop);
				});
			}
		}
		List<MyProperty> properties=propertyRepository.findByMyclassId(classToGetId.getId()); 
		getIdPropertyOnce = true;
		idOfProperty = properties.get(0).getId();

		// adding the methods
		if (requestCreateClass.getMethods() != null && requestCreateClass.getMethods().length >= 1) {
			List<MyParameter> params;
			for (MyMethod meth : requestCreateClass.getMethods()) {
				if (meth.getParameters() != null && meth.getParameters().size() >= 1) {
					params = new ArrayList<>();
					for (MyParameter param : meth.getParameters()) {
						parameterRepository.save(param);
						params.add(param);
					}
					meth.setParameters(params);
				}
				classRepository.findById(myclass.getId()).map(my_class -> {
					meth.setMyclass(myclass);
					return methodRepository.save(meth);
				});
			}
		}
		// testing if the user wants to generate the jpaRepo, the controller and the the
		// service
		MyClass _classRepo = null;
		MyClass _classService = null;
		MyClass _classController = null;
		if (requestCreateClass.isGenerateRepository()) {
			_classRepo = createRepository(requestCreateClass, idOfProperty,requestCreateClass.isEmbeddedId());
		}
		if (requestCreateClass.isService()) {
			_classService = createService(requestCreateClass, idOfProperty,requestCreateClass.isEmbeddedId());

		}
		if (requestCreateClass.isGenerateController()) {
			_classController = createController(requestCreateClass, idOfProperty,requestCreateClass.isEmbeddedId());
		}
		try {
			Project project = projectRepository.findById(requestCreateClass.getProjectId()).get();
			project.getClasses().add(myclass);
			if (_classRepo != null) {
				project.getClasses().add(_classRepo);
			}
			if (_classService != null) {
				project.getClasses().add(_classService);
			}
			if (_classController != null) {
				project.getClasses().add(_classController);
			}
			projectRepository.save(project);
		} catch (DataIntegrityViolationException e) {
			System.out.println("this Class already exists ");
		}
		return classToGetId.getId();

	}

	private MyClass createRepository(RequestCreateClass requestCreateClass, Long idOfProperty,Boolean isEntityClassEmbedded) {
		MyClass _class = new MyClass();
		_class.setClassName(requestCreateClass.getClassName() + "Repository");
		_class.setClassType("JPA_INTERFACE");
		Long projectId = requestCreateClass.getProjectId();
		Project project = projectRepository.findById(projectId).get();
		_class.setPackageName(project.getGroupId() + ".Repositories");
		_class.setIdOfPropertyId(idOfProperty);
		if(isEntityClassEmbedded)
			_class.setEmbeddedId(true);//setting o embedded so we can import the id class
		return _class;
	}

	private MyClass createService(RequestCreateClass requestCreateClass, Long idOfProperty,Boolean isEntityClassEmbedded) {
		MyClass _class = new MyClass();
		_class.setClassName(requestCreateClass.getClassName() + "Service");
		Project project = projectRepository.findById(requestCreateClass.getProjectId()).get();
		_class.setPackageName(project.getGroupId() + ".Services");
		_class.setClassType("Service_GEN");
		System.out.println(idOfProperty);
		_class.setIdOfPropertyId(idOfProperty);
		if(isEntityClassEmbedded)
			_class.setEmbeddedId(true);//setting o embedded so we can import the id class
		return _class;
	}

	private MyClass createController(RequestCreateClass requestCreateClass, Long idOfProperty,Boolean isEntityClassEmbedded) {
		MyClass _class = new MyClass();
		_class.setClassName(requestCreateClass.getClassName() + "Controller");
		Project project = projectRepository.findById(requestCreateClass.getProjectId()).get();
		_class.setPackageName(project.getGroupId() + ".Controller");
		_class.setClassType("Controller_GEN");
		_class.setIdOfPropertyId(idOfProperty);
		if(isEntityClassEmbedded)
			_class.setEmbeddedId(true);//setting o embedded so we can import the id class
		return _class;
	}

	public List<MyClass> getAllClasses(Long projectId) {
		return projectRepository.findClassesByProjectId(projectId);
	}

	public List<String> getAllPackageNames(Long project_id) {

		List<MyClass> list = projectRepository.findClassesByProjectId(project_id);
		return classRepository.findAllPackages(list);
	}

	public List<MyClass> getClassesByPackageName(String packageName) {
		return classRepository.findByPackageName(packageName);
	}

	public void deleteClass(Long id, Long projectId) {
		// TODO Auto-generated method stub
		// Project project=projectRepository.findById(projectId).get();
		// project.getClasses().clear();
		// project.getClasses().remove(classRepository.findById(id).get());
		// projectRepository.save(project);
		for (MyMethod meth : methodRepository.findByMyclassId(id)) {
			if (meth.getParameters() != null && meth.getParameters().size() >= 1) {
				for (MyParameter param : meth.getParameters()) {
					parameterRepository.deleteById(param.getId());
				}
			}
			methodRepository.deleteById(meth.getId());
		}

		classRepository.deleteById(id);
	}

	public RequestCreateClass getClassById(Long id) {
		RequestCreateClass requestCreateClass;
		MyClass _class = classRepository.findById(id).get();
		requestCreateClass = generateRequestCreateClassFromClass(_class);
		List<MyProperty> _properties = propertyRepository.findByMyclassId(id);
		MyProperty[] props = _properties.toArray(new MyProperty[_properties.size()]);
		requestCreateClass.setProperties(props);
		List<MyMethod> _methods = methodRepository.findByMyclassId(id);
		MyMethod[] methods = _methods.toArray(new MyMethod[_methods.size()]);
		requestCreateClass.setMethods(methods);
		return requestCreateClass;
	}

	public RequestCreateClass generateRequestCreateClassFromClass(MyClass _class) {
		RequestCreateClass requestCreateClass = new RequestCreateClass();
		requestCreateClass.setId(_class.getId());
		requestCreateClass.setClassName(_class.getClassName());
		requestCreateClass.setClassType(_class.getClassType());
		requestCreateClass.setGenerateController(_class.isGenerateController());
		requestCreateClass.setGeneratedValue(_class.isGeneratedValue());
		requestCreateClass.setGenerateRepository(_class.isGenerateRepository());
		requestCreateClass.setPackageName(_class.getPackageName());
		requestCreateClass.setService(_class.isService());
		requestCreateClass.setTableName(_class.getTableName());
		requestCreateClass.setIdGenerate(_class.isIdGenerate());
		requestCreateClass.setGeneratedType(_class.getGeneratedType());
		requestCreateClass.setRequestMappingURL(_class.getRequestMappingURL());
		requestCreateClass.setEmbeddedId(_class.isEmbeddedId());
		requestCreateClass.setIdOfPropertyId(_class.getIdOfPropertyId());
		return requestCreateClass;
	}

	public List<MyClass> getClassesByUserEmail(String email) {
		List<Project> projects = projectService.getAllProjects(email);
		List<MyClass> classes = new ArrayList<>();
		for (Project project : projects) {
			for (MyClass _class : project.getClasses()) {
				classes.add(_class);
			}
		}
		return classes;
	}

	public List<String> getAllPackageNamesOfAUser(String email) {
		List<Project> projects = projectService.getAllProjects(email);
		List<String> packageNames = new ArrayList<>();
		for (Project project : projects) {
			for (String _package : getAllPackageNames(project.getId())) {
				packageNames.add(_package);
			}
		}
		return packageNames;
	}

}
