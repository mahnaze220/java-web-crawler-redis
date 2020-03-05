package com.sample.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Mahnaz
 * @Mar 01, 2020
 */

@EnableSwagger2
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan("com.sample.crawler")
@SpringBootConfiguration
public class WebCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebCrawlerApplication.class, args);
	}

}
