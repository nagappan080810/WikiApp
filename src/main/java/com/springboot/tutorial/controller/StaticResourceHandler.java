package com.springboot.tutorial.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

@Configuration
public class StaticResourceHandler extends WebMvcAutoConfiguration {
    
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    	System.out.println("!!!!added resource handlers");
//        registry.addResourceHandler("**/images/**").addResourceLocations("classpath:/static/images/*");
//    }
//    
	@Configuration
	public static class ImagesConfiguration implements ResourceLoaderAware {

		@Autowired
		private ResourceProperties resourceProperties = new ResourceProperties();
		
		private ResourceLoader resourceLoader;

		@Bean
		public SimpleUrlHandlerMapping imagesHandlerMapping() {
			SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
			mapping.setOrder(Integer.MIN_VALUE + 1);
			mapping.setUrlMap(Collections.singletonMap("**/images/**",
					imagesRequestHandler()));
			return mapping;
		}

		@Bean
		public ResourceHttpRequestHandler imagesRequestHandler() {
			ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
			List<Resource> resources = new ArrayList<Resource>();
			resources.add(this.resourceLoader.getResource("classpath:/static/"));
			requestHandler
					.setLocations(resources);
			
			return requestHandler;
		}

		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}

	}
}
