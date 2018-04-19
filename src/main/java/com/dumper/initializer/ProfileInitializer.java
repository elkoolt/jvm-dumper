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

	final private static String DEFAULT_PROFILE = "spring.profiles.default";
	final private static String ACTIVE_PROFILE = "spring.profiles.active";
	final private static String PROFILE_PROPERTY_DIR = "profile/profile.properties";
	final private static String COMMA = ",";

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		StringBuilder profilesList = new StringBuilder();
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

		try {
			Resource resource = new ClassPathResource(PROFILE_PROPERTY_DIR);
			Properties props = PropertiesLoaderUtils.loadProperties(resource);

			// gets spring bean profile from spring.profiles.default (web.xml)
			if (context.getEnvironment().getActiveProfiles().length == 0  && servletContext.getInitParameter(DEFAULT_PROFILE) != null) {
				for (String defaultProfile : servletContext.getInitParameter(DEFAULT_PROFILE).split(COMMA)) {
					profilesList.append(defaultProfile).append(COMMA);
				}
			}

			// gets spring bean profile from spring.profiles.active (JAVA_OPTS for example)
			for (String activeProfile : context.getEnvironment().getActiveProfiles()) {
				if (!profilesList.toString().contains(activeProfile)) {
					profilesList.append(activeProfile).append(COMMA);
				}
			}

			// gets spring bean profile from property file
			String propertyProfile = props.getProperty(ACTIVE_PROFILE);
			if (StringUtils.hasLength(propertyProfile) && !profilesList.toString().contains(propertyProfile)) {
				profilesList.append(propertyProfile).append(COMMA);
			}

			// remove the last comma if present in a string
			if (profilesList.lastIndexOf(COMMA) == profilesList.length() - 1) {
				profilesList.deleteCharAt(profilesList.lastIndexOf(COMMA));
			}

			// set resolved spring profiles
			servletContext.setInitParameter(ACTIVE_PROFILE, profilesList.toString());

		} catch (IOException e) {
			logger.error("Failed to load spring profile property", e);
		} finally {
			context.close();
		}
		logger.error("Using profiles: " + profilesList);
	}

}