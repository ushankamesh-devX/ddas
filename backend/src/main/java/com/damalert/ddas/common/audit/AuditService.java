package com.damalert.ddas.common.audit;

public interface AuditService {

	void record(AuditEvent event);
}
