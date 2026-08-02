package com.enrollment.academic;

import com.enrollment.academic.catalog.domain.Student;
import com.enrollment.academic.catalog.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The authorization contract: no token is rejected, fine-grained roles gate each action, and a
 * student may act only on their own enrollments (ownership resolved by the Keycloak subject).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest extends AbstractIntegrationTest {

    private static final List<GrantedAuthority> ADMIN_AUTHORITIES = List.of(
            new SimpleGrantedAuthority("adm_create_student"), new SimpleGrantedAuthority("adm_read_student"),
            new SimpleGrantedAuthority("adm_create_course"), new SimpleGrantedAuthority("adm_create_subject"),
            new SimpleGrantedAuthority("adm_create_class"), new SimpleGrantedAuthority("adm_open_class"),
            new SimpleGrantedAuthority("adm_create_enrollment"), new SimpleGrantedAuthority("adm_read_enrollment"));

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired StudentRepository students;

    @Test
    void noTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentRoleCannotManageCatalog() throws Exception {
        mvc.perform(post("/api/students")
                        .with(student("sub-x", "student_create_enrollment"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"x@x.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWithFineGrainedRoleCanCreateStudent() throws Exception {
        mvc.perform(post("/api/students")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@x.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void meResolvesTheAlreadyLinkedStudent() throws Exception {
        UUID anaId = adminCreate("/api/students", "{\"name\":\"Ana\",\"email\":\"ana@x.com\"}");
        link(anaId, "sub-ana");

        mvc.perform(get("/api/students/me").with(studentToken("sub-ana", "ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(anaId.toString()))
                .andExpect(jsonPath("$.linked").value(true));
    }

    @Test
    void meLinksAnExistingStudentByEmailOnFirstLogin() throws Exception {
        UUID anaId = adminCreate("/api/students", "{\"name\":\"Ana\",\"email\":\"ana@x.com\"}");

        // First login: not linked yet, but matched by the token's email and linked.
        mvc.perform(get("/api/students/me").with(studentToken("sub-ana", "ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(anaId.toString()))
                .andExpect(jsonPath("$.linked").value(true));
    }

    @Test
    void meMaterializesAStudentFromTheTokenWhenNoneExists() throws Exception {
        mvc.perform(get("/api/students/me").with(studentToken("sub-new", "new@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@x.com"))
                .andExpect(jsonPath("$.linked").value(true));
    }

    @Test
    void meIsNotFoundForANonStudentWithoutALink() throws Exception {
        // A caller without the STUDENT role is never auto-provisioned.
        mvc.perform(get("/api/students/me").with(student("sub-admin", "adm_read_student")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("student.not_found"));
    }

    @Test
    void studentActsOnlyOnOwnEnrollment() throws Exception {
        UUID courseId = adminCreate("/api/courses", "{\"name\":\"Computer Science\"}");
        UUID subjectId = adminCreate("/api/subjects", "{\"name\":\"Algorithms\",\"courseId\":\"" + courseId + "\"}");
        UUID classId = adminCreate("/api/classes",
                "{\"subjectId\":\"" + subjectId + "\",\"label\":\"2026.1 - A\",\"seatLimit\":30}");
        mvc.perform(post("/api/classes/{id}/open", classId).with(admin())).andExpect(status().isOk());

        UUID anaId = adminCreate("/api/students", "{\"name\":\"Ana\",\"email\":\"ana@x.com\"}");
        UUID bobId = adminCreate("/api/students", "{\"name\":\"Bob\",\"email\":\"bob@x.com\"}");
        link(anaId, "sub-ana");
        link(bobId, "sub-bob");

        // Ana enrolls herself -> 201
        UUID enrollmentId = idOf(mvc.perform(post("/api/enrollments")
                        .with(student("sub-ana", "student_create_enrollment"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"" + anaId + "\",\"classId\":\"" + classId + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        // Ana enrolling Bob -> 403 (not her own)
        mvc.perform(post("/api/enrollments")
                        .with(student("sub-ana", "student_create_enrollment"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"" + bobId + "\",\"classId\":\"" + classId + "\"}"))
                .andExpect(status().isForbidden());

        // Ana reads her own enrollment -> 200
        mvc.perform(get("/api/enrollments/{id}", enrollmentId)
                        .with(student("sub-ana", "student_read_enrollment")))
                .andExpect(status().isOk());

        // Bob reads Ana's enrollment -> 403
        mvc.perform(get("/api/enrollments/{id}", enrollmentId)
                        .with(student("sub-bob", "student_read_enrollment")))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("admin-sub")).authorities(ADMIN_AUTHORITIES);
    }

    private static RequestPostProcessor student(String subject, String... authorities) {
        List<GrantedAuthority> granted = java.util.Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new).map(a -> (GrantedAuthority) a).toList();
        return jwt().jwt(j -> j.subject(subject)).authorities(granted);
    }

    /** A realistic student token: subject + email claim + the STUDENT realm role (drives JIT linking). */
    private static RequestPostProcessor studentToken(String subject, String email) {
        return jwt().jwt(j -> j.subject(subject)
                .claim("email", email)
                .claim("name", email)
                .claim("realm_access", java.util.Map.of("roles", java.util.List.of("STUDENT"))));
    }

    private void link(UUID studentId, String keycloakSubject) {
        Student s = students.findById(studentId).orElseThrow();
        s.setKeycloakId(keycloakSubject);
        students.save(s);
    }

    private UUID adminCreate(String path, String json) throws Exception {
        return idOf(mvc.perform(post(path).with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private UUID idOf(String body) throws Exception {
        return UUID.fromString(om.readTree(body).get("id").asText());
    }
}
