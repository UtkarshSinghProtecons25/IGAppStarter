package com.camelSpring.demoCamel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.camelSpring.demoCamel",
		"firstRoute"
})
public class DemoCamelApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoCamelApplication.class, args);
	}

}
