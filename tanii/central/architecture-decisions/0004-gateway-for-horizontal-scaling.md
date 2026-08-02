# ADR-0004 — Minimal gateway for a scalable, port-less service

Status: Accepted

## Context

To demonstrate the seat race across processes, the API service is scaled (`docker compose up --scale academic-service=2`). A service that publishes a fixed host port cannot scale — the second replica collides on the port. But the browser needs one stable API URL.

## Decision

`academic-service` **binds no host port**. A minimal **nginx reverse proxy** (`academic-gateway`) publishes the stable port and forwards to the replicas via Docker's embedded DNS.

## Rationale

- The scaled service can run N replicas with no port conflict.
- The browser keeps a single URL while the service scales behind it.
- The actual seat race is contended on the **RabbitMQ finalize queue** (every replica consumes), so HTTP load-balancing is incidental — the proxy exists for a stable entry, not for the race.

## Consequences

- One extra tiny service (nginx) in the local stack.
- A full API gateway (e.g. Traefik, which auto-discovers scaled Compose services) is optional polish, not required.
