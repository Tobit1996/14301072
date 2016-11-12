package spring.factory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import spring.bean.BeanDefinition;
import spring.bean.BeanUtil;
import spring.bean.PropertyValue;
import spring.bean.PropertyValues;
import spring.resource.*;
import test.*;

public class XMLBeanFactory extends AbstractBeanFactory{
	
	NodeList beanList = null;
	
	public XMLBeanFactory(String[] locations)
	{
		MyComponent();
		Resource resource = new LocalFileResource(locations[0]);
				
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
			Document document = dbBuilder.parse(resource.getInputStream());
            beanList = document.getElementsByTagName("bean");
            for(int i = 0 ; i < beanList.getLength(); i++)
            {
            	Node bean = beanList.item(i);
            	UseBeanFactory(bean.getAttributes().getNamedItem("id").getNodeValue());
            }
            	
	        } catch (Exception e){
	        	e.printStackTrace();
	        }
	}

	private void UseBeanFactory(String BeanName) {
		// TODO Auto-generated method stub
		if(getBean(BeanName)==null){
		for(int i = 0 ; i < beanList.getLength(); i++){
			
			Node bean=beanList.item(i);
        	String beanClassName = bean.getAttributes().getNamedItem("class").getNodeValue();
        	String beanName = bean.getAttributes().getNamedItem("id").getNodeValue();
        	
        	if(BeanName.equals(beanName)){
        		BeanDefinition beandef = new BeanDefinition();
        		beandef.setBeanClassName(beanClassName);
        		
        		try {
					Class<?> beanClass = Class.forName(beanClassName);
					beandef.setBeanClass(beanClass);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
                PropertyValues propertyValues = new PropertyValues();	
        		NodeList propertyList = bean.getChildNodes();
        		
        		for(int j = 0 ; j < propertyList.getLength(); j++){
        			Node property = propertyList.item(j);
        			if (property instanceof Element) {
        				Element ele = (Element) property;
        				
        				String name = ele.getAttribute("name");
        				
        				//读取value
        				if(ele.getAttribute("value").isEmpty()==false){
        					Class<?> type;
        					try {
    							type = beandef.getBeanClass().getDeclaredField(name).getType();
    							Object value = ele.getAttribute("value");
    	        				
    	        				if(type == Integer.class)
    	        				{
    	        					value = Integer.parseInt((String) value);
    	        				}   	        				
    	        				propertyValues.AddPropertyValue(new PropertyValue(name,value));
    						} catch (NoSuchFieldException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						} 
        				}
        				//读取ref注入方式
        				if(ele.getAttribute("ref").isEmpty()==false){
        					String ref=ele.getAttribute("ref");
        					
        					if(getBean(ref) == null){
        						UseBeanFactory(ref);
        					}
        				}
        		       }
        		
        	     }
                 beandef.setPropertyValues(propertyValues);
            	
                 this.registerBeanDefinition(beanName, beandef);	
		}
	}
    }
}
	public Object MyAutowire(BeanDefinition beanDefinition) {

		Class<?> cla = beanDefinition.getBeanClass();
		Constructor<?>[] con = cla.getConstructors();
        
		for (Constructor<?> cons : con) {

			Autowired auto = (Autowired) cons.getAnnotation(Autowired.class);

			if (auto != null) {

				Object[] obj = new Object[cons.getParameterTypes().length];

				for (int i = 0; i < cons.getParameterTypes().length; i++) {
					String objectName = cons.getParameterTypes()[i].getName().split("\\.")[(cons.getParameterTypes()[i].getName().split("\\.").length) - 1];
					if (getBean(objectName) != null) {
						obj[i] = getBean(objectName);
					}
				}
				try {
					return cons.newInstance(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}
	
	private void MyComponent(){
		String packageName = "";
		File root = new File(System.getProperty("user.dir") + "/src");
		try {
			loop(root, packageName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loop(File folder, String packageName) throws Exception {
		File[] files = folder.listFiles();
		for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
		File file = files[fileIndex];
		if (file.isDirectory()) {
		   loop(file, packageName + file.getName() + ".");
		}
		else {
		   listMethodNames(file.getName(), packageName);
		}
	 }
	}
	
	public void listMethodNames(String filename, String packageName){
		
		try {
			String name = filename.substring(0, filename.length() - 5);
			Class<?> obj = Class.forName(packageName + name);
			
			Component com=(Component) obj.getAnnotation(Component.class);
			if(com!=null){
				
			   BeanDefinition beandef = new BeanDefinition();
			   beandef.setBeanClassName(packageName + name);
			   beandef.setBeanClass(obj);

			   this.registerBeanDefinition(com.value(), beandef);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected BeanDefinition GetCreatedBean(BeanDefinition beanDefinition) {
		
		try {
			// set BeanClass for BeanDefinition
			
			Class<?> beanClass = beanDefinition.getBeanClass();
			Object bean = this.MyAutowire(beanDefinition);
			// set Bean Instance for BeanDefinition
			if(bean==null){
			     bean = beanClass.newInstance();	
			}
			
			if(beanDefinition.getPropertyValues() != null){
			   List<PropertyValue> fieldDefinitionList = beanDefinition.getPropertyValues().GetPropertyValues();
			   for(PropertyValue propertyValue: fieldDefinitionList)
			   {
				   BeanUtil.invokeSetterMethod(bean, propertyValue.getName(), propertyValue.getValue());
			   }
			}
			
			beanDefinition.setBean(bean);
			
			return beanDefinition;
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
