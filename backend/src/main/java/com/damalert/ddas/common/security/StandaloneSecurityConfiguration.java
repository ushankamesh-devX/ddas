package com.damalert.ddas.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("standalone")
public class StandaloneSecurityConfiguration {

	@Bean
	SecurityFilterChain standaloneSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.anyRequest().denyAll());
		return http.build();
	}
}
