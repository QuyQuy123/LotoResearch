package com.example.NumberTrendAnalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example"})
@EnableJpaRepositories(basePackages = {"com.example.repository"})
@EntityScan(basePackages = {"com.example.domain"})
public class NumberTrendAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NumberTrendAnalyzerApplication.class, args);
	}

}
