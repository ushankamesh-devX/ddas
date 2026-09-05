package com.damalert.ddas.monitoring.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!standalone")
public class DeviceKeyService {
	private final SecureRandom random = new SecureRandom();
	private final PasswordEncoder passwordEncoder;

	public DeviceKeyService(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public GeneratedDeviceKey generate() {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String key = "ddk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return new GeneratedDeviceKey(key, key.substring(0, 12), fingerprint(key), passwordEncoder.encode(key));
	}

	private String fingerprint(String value) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable.", ex);
		}
	}

	public record GeneratedDeviceKey(String plaintext, String prefix, String fingerprint, String verifier) { }
}
