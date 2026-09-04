package com.damalert.ddas.auth.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false)
	private String description;

	protected Role() {
	}

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}
}
