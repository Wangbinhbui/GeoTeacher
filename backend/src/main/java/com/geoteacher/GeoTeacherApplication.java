package com.geoteacher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GeoTeacherApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeoTeacherApplication.class, args);
	}

}
