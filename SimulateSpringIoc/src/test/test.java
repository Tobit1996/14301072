package test;

import spring.factory.*;

public class test {

    public static void main(String[] args) {
        String[] locations = {"bean.xml"};
        BeanFactory ctx = 
		    new XMLBeanFactory(locations);
        boss boss = (boss) ctx.getBean("boss");
        System.out.println(boss.toString());
    }
} 