# Lesson — Keycloak's two hostnames: validate by JWKS, not issuer

## What happened

In Docker Compose, the browser obtains tokens from Keycloak at `http://localhost:8080`, so the token's `iss` is `localhost`. The backend, on the same network, must reach Keycloak at `http://keycloak:8080`. Validating tokens by **issuer** would reject them (host mismatch).

## Why

The same identity provider is reachable under two hostnames — one for the browser (published port) and one for backend-to-backend (Compose service name). Issuer validation ties acceptance to a single host string.

## How to avoid

Validate JWTs via **`jwk-set-uri`** (signature check against the realm's JWKS), pointing at the internal host — not **`issuer-uri`**. Signature validation is independent of which hostname minted the token, so the two-hostname split doesn't break it.
