package com.senyang.common.utils;

import org.springframework.context.ApplicationContext;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SenYangContext {
	/**
     * spring  applicationContext
     */
    public static ApplicationContext applicationContext;

    /**
     * Ĭ�� app�������ļ�Ϊapplication.properties
     */
    public static Config configContext;


    static {
        configContext = ConfigFactory.parseResources("application.properties");
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
    	SenYangContext.applicationContext = applicationContext;
    }

}
