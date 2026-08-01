-- One PostgreSQL instance, two databases (one per bounded context).
-- Runs once, on first container start, as the superuser.
CREATE DATABASE academicdb;
CREATE DATABASE notificationsdb;
