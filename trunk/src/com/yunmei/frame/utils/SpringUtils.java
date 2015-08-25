package com.yunmei.frame.utils;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringUtils implements ApplicationContextAware {
	private static Logger log = Logger.getLogger(SpringUtils.class);
	private static ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext app)
			throws BeansException {
		applicationContext = app;
	}

	public static Object getBean(String id) {
		if (applicationContext == null)
			return null;
		return applicationContext.getBean(id);
	}

	public static String[] getBeans() {
		if (applicationContext == null)
			return new String[0];
		return applicationContext.getBeanDefinitionNames();
	}
}
