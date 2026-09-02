-- Dam Disaster Alert System V1
-- PostgreSQL + PostGIS
-- Canonical initial schema
-- Apply through Flyway. Do not manually edit shared databases.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL UNIQUE,
    phone VARCHAR(32),
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    account_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (account_status IN ('ACTIVE','DISABLED','LOCKED','PENDING')),
    preferred_language VARCHAR(16) NOT NULL DEFAULT 'en',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE dam (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    description TEXT,
    operational_state VARCHAR(24) NOT NULL DEFAULT 'NORMAL'
        CHECK (operational_state IN ('NORMAL','WATCH','WARNING','EMERGENCY')),
    public_status_message TEXT,
    area GEOMETRY(POLYGON, 4326),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dam_area_gist ON dam USING GIST(area);

CREATE TABLE dam_staff (
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_code VARCHAR(64) NOT NULL
        CHECK (role_code IN ('DAM_ADMIN','DAM_ENGINEER','DAM_OPERATOR','FIELD_OFFICER')),
    can_trigger_emergency BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (dam_id, user_id)
);

CREATE INDEX idx_dam_staff_user ON dam_staff(user_id);

CREATE TABLE household (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    primary_user_id UUID NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE RESTRICT,
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE RESTRICT,
    home_location GEOMETRY(POINT, 4326),
    address_text VARCHAR(500),
    emergency_phone VARCHAR(32),
    consent_emergency_contact BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_household_dam ON household(dam_id);
CREATE INDEX idx_household_home_gist ON household USING GIST(home_location);

CREATE TABLE household_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID NOT NULL REFERENCES household(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    age_group VARCHAR(24) NOT NULL
        CHECK (age_group IN ('INFANT','CHILD','ADULT','OLDER_ADULT','UNKNOWN')),
    relationship VARCHAR(80),
    assistance_category VARCHAR(40) NOT NULL DEFAULT 'NONE'
        CHECK (assistance_category IN ('NONE','MOBILITY','MEDICAL','PREGNANCY','CHILD_ASSISTANCE','OTHER')),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_household_member_household ON household_member(household_id);

CREATE TABLE device_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    platform VARCHAR(16) NOT NULL CHECK (platform IN ('ANDROID','IOS')),
    push_token TEXT NOT NULL,
    device_label VARCHAR(160),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (platform, push_token)
);

CREATE INDEX idx_device_token_user_active ON device_token(user_id, is_active);


CREATE TABLE iot_device (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    device_uid VARCHAR(160) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    device_type VARCHAR(40) NOT NULL
        CHECK (device_type IN ('GATEWAY','SENSOR_NODE','GATE_CONTROLLER','WEATHER_STATION','OTHER')),
    auth_method VARCHAR(24) NOT NULL DEFAULT 'DEVICE_KEY'
        CHECK (auth_method IN ('DEVICE_KEY','MTLS')),
    device_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (device_status IN ('ACTIVE','DISABLED','REVOKED')),
    firmware_version VARCHAR(100),
    last_connected_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    revoked_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_iot_device_dam_status
    ON iot_device(dam_id, device_status);

CREATE TABLE iot_device_credential (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES iot_device(id) ON DELETE CASCADE,
    credential_type VARCHAR(24) NOT NULL DEFAULT 'DEVICE_KEY'
        CHECK (credential_type IN ('DEVICE_KEY','MTLS')),
    key_prefix VARCHAR(32),
    credential_fingerprint VARCHAR(128),
    secret_verifier VARCHAR(500),
    broker_credential_ref VARCHAR(255),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','ROTATED','REVOKED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ,
    rotated_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_iot_credential_device
    ON iot_device_credential(device_id, status);

CREATE UNIQUE INDEX uq_iot_device_single_active_credential
    ON iot_device_credential(device_id)
    WHERE status = 'ACTIVE';

CREATE TABLE sensor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(180) NOT NULL,
    sensor_type VARCHAR(48) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    location GEOMETRY(POINT, 4326),
    visibility VARCHAR(24) NOT NULL DEFAULT 'PRIVATE'
        CHECK (visibility IN ('PRIVATE','PUBLIC_SUMMARY','PUBLIC')),
    expose_exact_location BOOLEAN NOT NULL DEFAULT FALSE,
    warning_threshold NUMERIC(18,6),
    critical_threshold NUMERIC(18,6),
    threshold_direction VARCHAR(8) NOT NULL DEFAULT 'HIGH'
        CHECK (threshold_direction IN ('HIGH','LOW')),
    sensor_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (sensor_status IN ('ACTIVE','DISABLED','MAINTENANCE','OFFLINE')),
    last_seen_at TIMESTAMPTZ,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, code)
);

CREATE INDEX idx_sensor_dam ON sensor(dam_id);
CREATE INDEX idx_sensor_location_gist ON sensor USING GIST(location);
CREATE INDEX idx_sensor_public ON sensor(dam_id, visibility) WHERE visibility <> 'PRIVATE';


CREATE TABLE iot_device_sensor (
    device_id UUID NOT NULL REFERENCES iot_device(id) ON DELETE CASCADE,
    sensor_id UUID NOT NULL REFERENCES sensor(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (device_id, sensor_id)
);

CREATE UNIQUE INDEX uq_sensor_single_iot_device
    ON iot_device_sensor(sensor_id);

CREATE INDEX idx_iot_device_sensor_device
    ON iot_device_sensor(device_id);

CREATE TABLE sensor_reading (
    id BIGSERIAL PRIMARY KEY,
    sensor_id UUID NOT NULL REFERENCES sensor(id) ON DELETE CASCADE,
    measured_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    value NUMERIC(20,8) NOT NULL,
    quality VARCHAR(24) NOT NULL DEFAULT 'GOOD'
        CHECK (quality IN ('GOOD','SUSPECT','BAD','UNKNOWN')),
    external_message_id VARCHAR(160),
    raw_payload JSONB,
    UNIQUE (sensor_id, external_message_id)
);

CREATE INDEX idx_sensor_reading_sensor_time
    ON sensor_reading(sensor_id, measured_at DESC);

CREATE INDEX idx_sensor_reading_measured_at
    ON sensor_reading(measured_at DESC);

CREATE TABLE dam_gate (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(180) NOT NULL,
    location GEOMETRY(POINT, 4326),
    gate_status VARCHAR(24) NOT NULL DEFAULT 'UNKNOWN'
        CHECK (gate_status IN ('OPEN','CLOSED','PARTIAL','UNKNOWN','MAINTENANCE')),
    opening_percent NUMERIC(5,2)
        CHECK (opening_percent IS NULL OR (opening_percent >= 0 AND opening_percent <= 100)),
    visibility VARCHAR(24) NOT NULL DEFAULT 'PRIVATE'
        CHECK (visibility IN ('PRIVATE','PUBLIC_SUMMARY','PUBLIC')),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, code)
);

CREATE INDEX idx_dam_gate_dam ON dam_gate(dam_id);
CREATE INDEX idx_dam_gate_location_gist ON dam_gate USING GIST(location);

CREATE TABLE risk_zone (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(180) NOT NULL,
    severity VARCHAR(24) NOT NULL
        CHECK (severity IN ('MONITORING','WARNING','HIGH_RISK','CRITICAL')),
    geometry GEOMETRY(POLYGON, 4326) NOT NULL,
    evacuation_required BOOLEAN NOT NULL DEFAULT FALSE,
    public_visible BOOLEAN NOT NULL DEFAULT TRUE,
    instructions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, code)
);

CREATE INDEX idx_risk_zone_dam ON risk_zone(dam_id);
CREATE INDEX idx_risk_zone_geometry_gist ON risk_zone USING GIST(geometry);

CREATE TABLE safe_location (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(180) NOT NULL,
    location GEOMETRY(POINT, 4326) NOT NULL,
    capacity INTEGER CHECK (capacity IS NULL OR capacity >= 0),
    current_occupancy INTEGER CHECK (current_occupancy IS NULL OR current_occupancy >= 0),
    status VARCHAR(24) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE','NEAR_CAPACITY','FULL','CLOSED')),
    contact_number VARCHAR(32),
    facilities JSONB NOT NULL DEFAULT '[]'::jsonb,
    public_visible BOOLEAN NOT NULL DEFAULT TRUE,
    instructions TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, code)
);

CREATE INDEX idx_safe_location_dam ON safe_location(dam_id);
CREATE INDEX idx_safe_location_location_gist ON safe_location USING GIST(location);

CREATE TABLE evacuation_route (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(180) NOT NULL,
    from_zone_id UUID NOT NULL REFERENCES risk_zone(id) ON DELETE RESTRICT,
    safe_location_id UUID NOT NULL REFERENCES safe_location(id) ON DELETE RESTRICT,
    geometry GEOMETRY(LINESTRING, 4326) NOT NULL,
    route_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (route_status IN ('ACTIVE','BLOCKED','CLOSED')),
    public_visible BOOLEAN NOT NULL DEFAULT TRUE,
    instructions TEXT,
    priority SMALLINT NOT NULL DEFAULT 1 CHECK (priority >= 1),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, code)
);

CREATE INDEX idx_evacuation_route_dam ON evacuation_route(dam_id);
CREATE INDEX idx_evacuation_route_zone ON evacuation_route(from_zone_id);
CREATE INDEX idx_evacuation_route_geometry_gist ON evacuation_route USING GIST(geometry);

CREATE TABLE dam_emergency_state (
    dam_id UUID PRIMARY KEY REFERENCES dam(id) ON DELETE CASCADE,
    state VARCHAR(24) NOT NULL DEFAULT 'INACTIVE'
        CHECK (state IN ('INACTIVE','ACTIVE')),
    activated_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    activated_at TIMESTAMPTZ,
    cleared_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    cleared_at TIMESTAMPTZ,
    reason TEXT,
    idempotency_key VARCHAR(100),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, idempotency_key)
);

CREATE TABLE alert (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    severity VARCHAR(24) NOT NULL
        CHECK (severity IN ('INFO','WATCH','WARNING','EVACUATE','EMERGENCY')),
    title VARCHAR(220) NOT NULL,
    message TEXT NOT NULL,
    recommended_action TEXT,
    evacuation_required BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','CANCELLED','EXPIRED')),
    created_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    cancelled_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    cancelled_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    idempotency_key VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (dam_id, idempotency_key)
);

CREATE INDEX idx_alert_dam_created ON alert(dam_id, created_at DESC);
CREATE INDEX idx_alert_active ON alert(dam_id, status) WHERE status = 'ACTIVE';

CREATE TABLE alert_zone (
    alert_id UUID NOT NULL REFERENCES alert(id) ON DELETE CASCADE,
    risk_zone_id UUID NOT NULL REFERENCES risk_zone(id) ON DELETE RESTRICT,
    PRIMARY KEY (alert_id, risk_zone_id)
);

CREATE INDEX idx_alert_zone_zone ON alert_zone(risk_zone_id);

CREATE TABLE alert_recipient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id UUID NOT NULL REFERENCES alert(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    delivery_status VARCHAR(24) NOT NULL DEFAULT 'PENDING'
        CHECK (delivery_status IN ('PENDING','ATTEMPTED','SENT','FAILED','NO_DEVICE')),
    first_attempt_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    opened_at TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (alert_id, user_id)
);

CREATE INDEX idx_alert_recipient_user_created ON alert_recipient(user_id, created_at DESC);
CREATE INDEX idx_alert_recipient_alert_status ON alert_recipient(alert_id, delivery_status);

CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id UUID REFERENCES alert(id) ON DELETE CASCADE,
    recipient_id UUID REFERENCES alert_recipient(id) ON DELETE CASCADE,
    channel VARCHAR(24) NOT NULL
        CHECK (channel IN ('PUSH','SMS','EMAIL','OTHER')),
    payload JSONB NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED','DEAD')),
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_notification_outbox_pending
    ON notification_outbox(status, next_attempt_at)
    WHERE status IN ('PENDING','FAILED');

CREATE TABLE citizen_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    reporter_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    category VARCHAR(40) NOT NULL
        CHECK (category IN ('CRACK','FLOODING','LANDSLIDE','ROAD_BLOCKED','GATE_ISSUE','SUSPICIOUS_ACTIVITY','OTHER')),
    description TEXT NOT NULL,
    location GEOMETRY(POINT, 4326),
    moderation_status VARCHAR(24) NOT NULL DEFAULT 'SUBMITTED'
        CHECK (moderation_status IN ('SUBMITTED','UNDER_REVIEW','VERIFIED','REJECTED','PUBLIC')),
    reviewed_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMPTZ,
    review_note TEXT,
    idempotency_key VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (reporter_user_id, idempotency_key)
);

CREATE INDEX idx_citizen_report_dam_created ON citizen_report(dam_id, created_at DESC);
CREATE INDEX idx_citizen_report_public ON citizen_report(dam_id, moderation_status)
    WHERE moderation_status = 'PUBLIC';
CREATE INDEX idx_citizen_report_location_gist ON citizen_report USING GIST(location);

CREATE TABLE report_image (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES citizen_report(id) ON DELETE CASCADE,
    object_key VARCHAR(700) NOT NULL UNIQUE,
    content_type VARCHAR(120) NOT NULL,
    byte_size BIGINT NOT NULL CHECK (byte_size > 0),
    checksum_sha256 VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_report_image_report ON report_image(report_id);

CREATE TABLE news (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dam_id UUID NOT NULL REFERENCES dam(id) ON DELETE CASCADE,
    title VARCHAR(220) NOT NULL,
    summary VARCHAR(500),
    content TEXT NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    created_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    published_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_news_dam_published ON news(dam_id, published_at DESC);
CREATE INDEX idx_news_public ON news(dam_id, status, published_at DESC)
    WHERE status = 'PUBLISHED';

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    dam_id UUID REFERENCES dam(id) ON DELETE SET NULL,
    actor_user_id UUID REFERENCES app_user(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    request_id VARCHAR(100),
    source_ip INET,
    old_value JSONB,
    new_value JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_dam_time ON audit_log(dam_id, occurred_at DESC);
CREATE INDEX idx_audit_actor_time ON audit_log(actor_user_id, occurred_at DESC);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);

-- Seed stable global roles.
INSERT INTO role (code, description) VALUES
('SUPER_ADMIN', 'Platform-level administrator'),
('DAM_ADMIN', 'Dam administrator'),
('DAM_ENGINEER', 'Dam engineer'),
('DAM_OPERATOR', 'Dam control-room operator'),
('FIELD_OFFICER', 'Field operations officer'),
('CIVILIAN', 'Civilian mobile application user')
ON CONFLICT (code) DO NOTHING;
