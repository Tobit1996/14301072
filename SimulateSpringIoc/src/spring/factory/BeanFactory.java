package spring.factory;

import spring.bean.BeanDefinition;

public interface BeanFactory {
	Object getBean(String beanName);
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
}
