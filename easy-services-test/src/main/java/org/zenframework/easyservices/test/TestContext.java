package org.zenframework.easyservices.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public interface TestContext {

    ApplicationContext CONTEXT = new ClassPathXmlApplicationContext("classpath:default-context.xml");

}
