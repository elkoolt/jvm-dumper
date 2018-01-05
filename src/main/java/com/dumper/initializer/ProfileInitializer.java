package com.dumper.initializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Initializer for beans profiles.
 * 
 * dbprofile - enables DB features. basicprofile - disables DB features.
 * 
 * Profile should be set in profile/profile.properties file
 * 
 * @author ksalnis
 *
 */
@Configuration
public class ProfileInitializer implements WebApplicationInitializer {

	final static Logger logger = Logger.getLogger(ProfileInitializer.class);

	final private static String ACTIVE_PROFILE = "spring.profiles.active";

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		StringBuilder profilesList = new StringBuilder();
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

		try {
			Resource resource = new ClassPathResource("profile/profile.properties");
			Properties props = PropertiesLoaderUtils.loadProperties(resource);

			int i = 1;
			for (String activeProfile : context.getEnvironment().getActiveProfiles()) {

				profilesList.append(activeProfile);
				if (i++ != context.getEnvironment().getActiveProfiles().length) {
					profilesList.append(",");
				}
			}

			String propertyProfile = props.getProperty(ACTIVE_PROFILE);

			if (StringUtils.hasLength(propertyProfile)) {
				if (context.getEnvironment().getActiveProfiles().length > 0) {
					profilesList.append(",");
				}
				profilesList.append(propertyProfile);
				servletContext.setInitParameter(ACTIVE_PROFILE, profilesList.toString());
			}

		} catch (IOException e) {
			logger.error("Failed to load spring profile property", e);
		} finally {
			context.close();
		}

		logger.error("Using profiles " + profilesList);
	}

}