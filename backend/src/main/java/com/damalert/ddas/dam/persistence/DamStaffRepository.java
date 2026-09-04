package com.damalert.ddas.dam.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.damalert.ddas.dam.domain.DamStaff;
import com.damalert.ddas.dam.domain.DamStaffId;

public interface DamStaffRepository extends JpaRepository<DamStaff, DamStaffId> {

	@Query("select staff from DamStaff staff where staff.id.userId = :userId")
	List<DamStaff> findAllByUserId(@Param("userId") UUID userId);

	@Query("select staff from DamStaff staff where staff.id.damId = :damId order by staff.assignedAt")
	List<DamStaff> findAllByDamId(@Param("damId") UUID damId);

	@Query("select staff from DamStaff staff where staff.id.damId = :damId and staff.id.userId = :userId")
	Optional<DamStaff> findByDamIdAndUserId(@Param("damId") UUID damId, @Param("userId") UUID userId);
}
