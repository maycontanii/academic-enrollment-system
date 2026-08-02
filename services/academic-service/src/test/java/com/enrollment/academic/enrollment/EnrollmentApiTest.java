package com.enrollment.academic.enrollment;

import com.enrollment.academic.AbstractIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentApiTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void enrollmentRulesAndQueries() throws Exception {
        UUID courseId = createId("/api/courses", "{\"name\":\"Computer Science\"}");
        UUID subjectId = createId("/api/subjects", "{\"name\":\"Algorithms\",\"courseId\":\"" + courseId + "\"}");
        UUID openClass = createId("/api/classes",
                "{\"subjectId\":\"" + subjectId + "\",\"label\":\"2026.1 - A\",\"seatLimit\":30}");
        mvc.perform(post("/api/classes/{id}/open", openClass)).andExpect(status().isOk());
        UUID closedClass = createId("/api/classes",
                "{\"subjectId\":\"" + subjectId + "\",\"label\":\"2026.1 - B\",\"seatLimit\":30}");
        UUID student = createId("/api/students", "{\"name\":\"Ana Silva\",\"email\":\"ana@x.com\"}");

        String enroll = "{\"studentId\":\"" + student + "\",\"classId\":\"" + openClass + "\"}";

        // enroll in an open class -> 201 PENDING
        mvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON).content(enroll))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // duplicate active -> 409
        mvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON).content(enroll))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("enrollment.duplicate"));

        // closed class -> 422 business error
        mvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"" + student + "\",\"classId\":\"" + closedClass + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.type").value("business_error"))
                .andExpect(jsonPath("$.error.code").value("class.not_open"));

        // unknown student -> 404
        mvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"" + UUID.randomUUID() + "\",\"classId\":\"" + openClass + "\"}"))
                .andExpect(status().isNotFound());

        // query by student
        mvc.perform(get("/api/enrollments").param("studentId", student.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].classId").value(openClass.toString()));

        // query by class
        mvc.perform(get("/api/enrollments").param("classId", openClass.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private UUID createId(String path, String json) throws Exception {
        String content = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(om.readTree(content).get("id").asText());
    }
}
