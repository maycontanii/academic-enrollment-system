-- Link a student to its Keycloak identity, for ownership checks.
ALTER TABLE student ADD COLUMN keycloak_id VARCHAR(255) UNIQUE;
