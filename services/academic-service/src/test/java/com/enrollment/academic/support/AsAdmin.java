package com.enrollment.academic.support;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs a test as an administrator: the full set of fine-grained {@code adm_*} action authorities the
 * controllers require. The admin variant of each action is also what lets {@link
 * com.enrollment.academic.shared.security.AccessGuard} skip per-student ownership — no coarse role.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithMockUser(authorities = {
        "adm_create_student", "adm_read_student", "adm_update_student", "adm_delete_student",
        "adm_create_course", "adm_read_course", "adm_update_course", "adm_delete_course",
        "adm_create_subject", "adm_read_subject", "adm_update_subject", "adm_delete_subject",
        "adm_create_class", "adm_read_class", "adm_update_class", "adm_delete_class",
        "adm_open_class", "adm_close_class",
        "adm_create_enrollment", "adm_read_enrollment", "adm_confirm_enrollment", "adm_cancel_enrollment"
})
public @interface AsAdmin {
}
