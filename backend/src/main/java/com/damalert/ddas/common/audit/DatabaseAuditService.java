package com.damalert.ddas.common.audit;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.damalert.ddas.common.error.RequestIdFilter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("!standalone")
public class DatabaseAuditService implements AuditService {

	private static final String INSERT_SQL = """
		INSERT INTO audit_log (
			dam_id, actor_user_id, action, entity_type, entity_id,
			request_id, source_ip, old_value, new_value
		) VALUES (?, ?, ?, ?, ?, ?, CAST(? AS inet), CAST(? AS jsonb), CAST(? AS jsonb))
		""";

	private final JdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	public DatabaseAuditService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void record(AuditEvent event) {
		HttpServletRequest request = currentRequest();
		String requestId = request == null
			? null
			: (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
		String sourceIp = request == null ? null : request.getRemoteAddr();
		String oldValue = toJson(event.oldValue());
		String newValue = toJson(event.newValue());

		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(INSERT_SQL);
			statement.setObject(1, event.damId());
			statement.setObject(2, event.actorUserId());
			statement.setString(3, event.action());
			statement.setString(4, event.entityType());
			statement.setObject(5, event.entityId());
			setNullableString(statement, 6, requestId);
			setNullableString(statement, 7, sourceIp);
			setNullableString(statement, 8, oldValue);
			setNullableString(statement, 9, newValue);
			return statement;
		});
	}

	private String toJson(Map<String, Object> value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		}
		catch (JacksonException exception) {
			throw new IllegalArgumentException("Audit data is not JSON serializable.", exception);
		}
	}

	private void setNullableString(PreparedStatement statement, int index, String value) throws java.sql.SQLException {
		if (value == null) {
			statement.setNull(index, Types.VARCHAR);
		}
		else {
			statement.setString(index, value);
		}
	}

	private HttpServletRequest currentRequest() {
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
			return attributes.getRequest();
		}
		return null;
	}
}
