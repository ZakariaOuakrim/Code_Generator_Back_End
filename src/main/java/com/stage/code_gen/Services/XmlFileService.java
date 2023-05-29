package com.stage.code_gen.Services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.Property;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.stereotype.Service;

import com.stage.code_gen.Models.MyClass;
import com.stage.code_gen.Models.MyProperty;
import com.stage.code_gen.Repositories.ClassRepository;
import com.stage.code_gen.Requests_Responses.RequestCreateClass;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class XmlFileService {
	private final ClassRepository classRepository;
	private final ClassService classService;

	public List<RequestCreateClass> getProperitesFromXmlFile(File file,Long idOfProject) throws JDOMException, IOException {
		RequestCreateClass _class = new RequestCreateClass();
		RequestCreateClass _class2 = new RequestCreateClass();

		MyProperty _property;
		List<MyProperty> properties = new ArrayList<>();
		List<RequestCreateClass> classes = new ArrayList<>();
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(file);

		Element rootElement = document.getRootElement();
		Element classElement = rootElement.getChild("class");

		// setting the name of the class
		_class.setClassName(classElement.getAttributeValue("name").replace("Hibernate.Model.", ""));
		// setting the table Name of the class
		_class.setTableName(classElement.getAttributeValue("table"));
		// setting the class to an Entity
		_class.setClassType("Entity");
		_class.setPackageName("com.example.Entity");
		//generate repo and service 
		_class.setGenerateController(true);
		_class.setGenerateRepository(true);
		_class.setService(true);
		// --------------------------Properties----------------------
		String propertyName;
		String propertyType;
		Element column;
		String columnName;
		String length;
		List<Element> propertyElements = classElement.getChildren("property");
		
		Element idElement = classElement.getChild("id");
		// if the class has an id then
		if (idElement != null) {
			// set the class to have the @id
			_class.setIdGenerate(true);

			_property = new MyProperty();
			// setting the name and the type of the property
			_property.setName(idElement.getAttributeValue("name"));
			_property.setType(idElement.getAttributeValue("type"));

			_property.setAccess_modifier("private");
			// getting the column attributes
			column = idElement.getChild("column");
			columnName = idElement.getAttributeValue("name");
			length = idElement.getAttributeValue("length");

			// setting the column attributes
			_property.setColumnName(columnName);
			_property.setLength(length);

			// add the property to the list of properties of the class
			properties.add(_property);
		}
		RequestCreateClass compositeClass = null ;
		//---------------------------------composite-id-----------------------
		if(classElement.getChild("composite-id")!=null) {
			Element compositeId;
			Element columnComposite;
			MyProperty compositePop;
			List<Element> keyProperty;
			List<MyProperty> compositeProperties = new ArrayList<>();
			compositeClass = new RequestCreateClass();
			compositeId=classElement.getChild("composite-id");
			compositeClass.setClassName(compositeId.getAttributeValue("class").replace("Hibernate.Model.", ""));
			keyProperty = compositeId.getChildren("key-property");
			compositeClass.setClassType("Entity");
			compositeClass.setPackageName("com.example.Entity");
			for(Element keyProp : keyProperty) {
				compositePop = new MyProperty();
				compositePop.setName(keyProp.getAttributeValue("name"));
				compositePop.setType(keyProp.getAttributeValue("type"));
				columnComposite = keyProp.getChild("column");
				compositePop.setColumnName(columnComposite.getAttributeValue("name"));
				compositePop.setLength(columnComposite.getAttributeValue("length"));
				compositePop.setAccess_modifier("private");
				compositeProperties.add(compositePop);
			}
			
			_class.setIdGenerate(true);
			_property = new MyProperty();
			_property.setName(compositeId.getAttributeValue("name"));
			_property.setType(compositeId.getAttributeValue("class").replace("Hibernate.Model.", ""));
			_property.setAccess_modifier("private");
			properties.add(_property);

			compositeClass.setProperties(compositeProperties.toArray(new MyProperty[0]));
			compositeClass.setProjectId(idOfProject);
			Long id= classService.addANewClass(compositeClass);
			compositeClass.setId(id);
			
		}
		
		
		for (Element propertyElement : propertyElements) {
			//check if it has composite id 
			
			_property = new MyProperty();
			propertyName = propertyElement.getAttributeValue("name");
			propertyType = propertyElement.getAttributeValue("type");
			_property.setAccess_modifier("private");

			// getting the name of the column and the length of the variable
			column = propertyElement.getChild("column");
			columnName = column.getAttributeValue("name");
			length = column.getAttributeValue("length");

			// setting the property attributes
			_property.setName(propertyName);
			_property.setType(propertyType);
			_property.setLength(length);
			_property.setColumnName(columnName);

			properties.add(_property);
		}
		
		_class.setProperties(properties.toArray(new MyProperty[0]));
		if(compositeClass!=null) {
			classes.add(compositeClass);
		}
		
		classes.add(_class);
		_class.setProjectId(idOfProject);
		classService.addANewClass(_class);
		
		return classes;
	}
}
