package com.damalert.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.damalert.alert.entity.*;

public interface AlertZoneRepository extends JpaRepository<AlertZone, AlertZoneId> { }