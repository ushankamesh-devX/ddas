package com.damalert.ddas.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.damalert.ddas.auth.domain.AppUser;
import com.damalert.ddas.auth.domain.RefreshToken;
import com.damalert.ddas.auth.persistence.RefreshTokenRepository;
import com.damalert.ddas.common.security.JwtProperties;

@Service
@Profile("!standalone")
public class TokenService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;
	private final RefreshTokenRepository refreshTokenRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	public TokenService(
		JwtEncoder jwtEncoder,
		JwtProperties properties,
		RefreshTokenRepository refreshTokenRepository
	) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public IssuedTokens issue(AppUser user) {
		Instant now = Instant.now();
		Instant accessTokenExpiry = now.plus(properties.accessTokenTtl());
		List<String> roles = user.getRoles().stream().map(role -> role.getCode()).sorted().toList();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(properties.issuer())
			.issuedAt(now)
			.expiresAt(accessTokenExpiry)
			.subject(user.getId().toString())
			.claim("email", user.getEmail())
			.claim("roles", roles)
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		String refreshTokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
		RefreshToken refreshToken = new RefreshToken(
			UUID.randomUUID(),
			user.getId(),
			hash(refreshTokenValue),
			now.plus(properties.refreshTokenTtl())
		);
		refreshTokenRepository.save(refreshToken);

		return new IssuedTokens(accessToken, refreshTokenValue, properties.accessTokenTtl().toSeconds());
	}

	public String hash(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable.", exception);
		}
	}
}
