package com.damalert.ddas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
	"com.damalert.ddas",
	"com.damalert.alert.entity",
	"com.damalert.notification.entity"
})
@EnableJpaRepositories(basePackages = {
	"com.damalert.ddas",
	"com.damalert.alert.repository",
	"com.damalert.notification.repository"
})
public class DdasApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdasApplication.class, args);
	}

}
