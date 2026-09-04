package com.damalert.ddas.auth.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.auth.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

	Optional<AppUser> findByEmailIgnoreCase(String email);
}
