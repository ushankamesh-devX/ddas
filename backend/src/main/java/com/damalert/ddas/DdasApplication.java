package com.damalert.ddas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan({ "com.damalert.ddas", "com.damalert.alert.entity", "com.damalert.notification.entity" })
@EnableJpaRepositories({ "com.damalert.ddas", "com.damalert.alert.repository", "com.damalert.notification.repository" })
public class DdasApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdasApplication.class, args);
	}

}
