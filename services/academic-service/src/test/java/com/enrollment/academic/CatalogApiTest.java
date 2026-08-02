package com.enrollment.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Test
    void studentCrudValidationAndConflict() throws Exception {
        String body = "{\"name\":\"Ana Silva\",\"email\":\"ana@x.com\",\"document\":\"1\"}";

        // create -> 201
        String created = mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(om.readTree(created).get("id").asText());

        // get -> 200
        mvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@x.com"));

        // list -> paged
        mvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // duplicate email -> 409 (standardized envelope)
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.type").value("conflict"))
                .andExpect(jsonPath("$.error.code").value("student.email.duplicate"));

        // invalid -> 400 validation envelope
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("validation_error"))
                .andExpect(jsonPath("$.error.details").isArray());

        // missing -> 404
        mvc.perform(get("/api/students/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.type").value("not_found"));
    }

    @Test
    void catalogHierarchyAndOpenClose() throws Exception {
        UUID courseId = createId("/api/courses", "{\"name\":\"Computer Science\"}");
        UUID subjectId = createId("/api/subjects",
                "{\"name\":\"Algorithms\",\"courseId\":\"" + courseId + "\"}");
        UUID classId = createId("/api/classes",
                "{\"subjectId\":\"" + subjectId + "\",\"label\":\"2026.1 - A\",\"seatLimit\":30}");

        // class defaults to CLOSED; open it
        mvc.perform(post("/api/classes/{id}/open", classId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.seatsUsed").value(0));

        // browse open classes by subject
        mvc.perform(get("/api/classes").param("subjectId", subjectId.toString()).param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].label").value("2026.1 - A"));

        // subject referencing a missing course -> 404
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"courseId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("course.not_found"));

        // invalid class (seatLimit <= 0) -> 400
        mvc.perform(post("/api/classes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"" + subjectId + "\",\"label\":\"B\",\"seatLimit\":0}"))
                .andExpect(status().isBadRequest());
    }

    private UUID createId(String path, String json) throws Exception {
        String content = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(om.readTree(content).get("id").asText());
    }
}
