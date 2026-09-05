package com.damalert.ddas.monitoring.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class DeviceKeyServiceTests {
	@Test
	void generatedKeysAreRandomHashedAndRecognizable() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		DeviceKeyService service = new DeviceKeyService(encoder);

		DeviceKeyService.GeneratedDeviceKey first = service.generate();
		DeviceKeyService.GeneratedDeviceKey second = service.generate();

		assertThat(first.plaintext()).startsWith("ddk_").hasSizeGreaterThanOrEqualTo(47);
		assertThat(first.plaintext()).isNotEqualTo(second.plaintext());
		assertThat(first.verifier()).doesNotContain(first.plaintext());
		assertThat(encoder.matches(first.plaintext(), first.verifier())).isTrue();
		assertThat(first.fingerprint()).hasSize(64);
	}
}
