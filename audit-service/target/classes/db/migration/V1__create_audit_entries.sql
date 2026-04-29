CREATE SEQUENCE IF NOT EXISTS audit_entries_id_seq START 1 INCREMENT 50;

CREATE TABLE audit_entries (
    id BIGINT PRIMARY KEY DEFAULT nextval('audit_entries_id_seq'),
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    payload TEXT,
    source_service VARCHAR(100)
);

CREATE INDEX idx_audit_event_type ON audit_entries(event_type);
CREATE INDEX idx_audit_entity ON audit_entries(entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_entries(actor);
CREATE INDEX idx_audit_timestamp ON audit_entries(timestamp DESC);
