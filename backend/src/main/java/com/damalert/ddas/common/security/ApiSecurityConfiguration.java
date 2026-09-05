package com.damalert.ddas.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@Profile("!standalone")
@EnableConfigurationProperties(JwtProperties.class)
public class ApiSecurityConfiguration {

	@Bean
	SecurityFilterChain apiSecurityFilterChain(
		HttpSecurity http,
		ApiAuthenticationEntryPoint authenticationEntryPoint,
		ApiAccessDeniedHandler accessDeniedHandler
	) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
				// Public read-only product surface consumed by the mobile app without a session.
				// Each public endpoint is still responsible for filtering to explicitly public data.
				.requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(Customizer.withDefaults())
				.authenticationEntryPoint(authenticationEntryPoint))
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecretKey jwtSecretKey(JwtProperties properties) {
		byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
		if (secret.length < 32) {
			throw new IllegalStateException("JWT_SECRET must be at least 32 bytes.");
		}
		return new SecretKeySpec(secret, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey secretKey) {
		return NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey secretKey, JwtProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
		return decoder;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(org.springframework.core.env.Environment environment) {
		String configuredOrigins = environment.getProperty("app.security.cors.allowed-origins", "http://localhost:3000");
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(configuredOrigins.split(",")).map(String::trim).toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Request-Id"));
		configuration.setExposedHeaders(List.of("X-Request-Id"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
