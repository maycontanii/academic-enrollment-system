package com.enrollment.academic.shared.security;

/**
 * Fine-grained action authorities the backend understands. Coarse profiles (ADMIN, STUDENT) are a
 * Keycloak-side composition concern and never appear here — the backend reasons only about actions.
 * The {@code adm_*} constants below are the admin variants that bypass per-student ownership.
 */
public final class Authorities {

    public static final String ADM_CREATE_ENROLLMENT = "adm_create_enrollment";
    public static final String ADM_READ_ENROLLMENT = "adm_read_enrollment";
    public static final String ADM_CONFIRM_ENROLLMENT = "adm_confirm_enrollment";
    public static final String ADM_CANCEL_ENROLLMENT = "adm_cancel_enrollment";

    private Authorities() {
    }
}
