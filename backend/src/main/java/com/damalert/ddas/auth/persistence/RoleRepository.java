package com.damalert.ddas.auth.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.auth.domain.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	Optional<Role> findByCode(String code);
}
