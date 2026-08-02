package com.enrollment.academic.shared.security;

import com.enrollment.academic.catalog.repository.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Enforces "a student acts only on their own enrollments". Ownership is data-dependent, so it lives
 * here rather than in Keycloak. The backend knows only fine-grained action authorities: a caller
 * holding the admin variant of the action (e.g. {@code adm_read_enrollment}) acts system-wide and
 * skips the ownership check — coarse profiles like ADMIN never reach this code. A request with no
 * authentication is a trusted internal/system call.
 */
@Component
public class AccessGuard {

    private final StudentRepository students;

    public AccessGuard(StudentRepository students) {
        this.students = students;
    }

    /**
     * @param studentId      the student the action targets
     * @param adminAuthority the admin action authority that grants a system-wide bypass
     */
    public void requireOwnStudent(UUID studentId, String adminAuthority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || hasAuthority(auth, adminAuthority)) {
            return; // system call or an admin acting on this action
        }
        if (!studentId.equals(currentStudentId(auth))) {
            throw new AccessDeniedException("You can only act on your own enrollments");
        }
    }

    /** Queries: an admin sees the requested scope; a student is forced to their own. */
    public UUID effectiveStudentFilter(UUID requested, String adminAuthority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || hasAuthority(auth, adminAuthority)) {
            return requested;
        }
        return currentStudentId(auth);
    }

    private UUID currentStudentId(Authentication auth) {
        String subject = ((Jwt) auth.getPrincipal()).getSubject();
        return students.findByKeycloakId(subject)
                .orElseThrow(() -> new AccessDeniedException("No student is linked to this account"))
                .getId();
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }
}
