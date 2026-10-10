package com.kaminskyi.deskbooking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private long deskId;
    private long officeId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin@test.local", "admin123");
        userToken = register("john@test.com", "John Doe");

        officeId = postJson("/api/offices", adminToken,
                Map.of("name", "HQ", "city", "Bratislava", "address", "Main 1"))
                .get("id").asLong();
        deskId = postJson("/api/desks", adminToken,
                Map.of("name", "A-1", "floor", 1, "officeId", officeId))
                .get("id").asLong();
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mvc.perform(get("/api/offices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "john@test.com", "password", "wrong-pass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithSameEmailTwiceReturns409() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "john@test.com", "password", "secret123", "fullName", "Copy"))))
                .andExpect(status().isConflict());
    }

    @Test
    void userCantCreateOffice() throws Exception {
        mvc.perform(post("/api/offices")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "X", "city", "Y", "address", "Z"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsAreReturned() throws Exception {
        mvc.perform(post("/api/desks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("floor", 1, "officeId", officeId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void fullBookingFlow() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        mvc.perform(get("/api/desks/available")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                        .param("officeId", String.valueOf(officeId))
                        .param("date", tomorrow))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        long bookingId = postJson("/api/bookings", userToken, Map.of("deskId", deskId, "date", tomorrow))
                .get("id").asLong();

        // desk is taken now
        mvc.perform(get("/api/desks/available")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                        .param("officeId", String.valueOf(officeId))
                        .param("date", tomorrow))
                .andExpect(jsonPath("$.length()").value(0));

        String otherToken = register("anna@test.com", "Anna Smith");
        mvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("deskId", deskId, "date", tomorrow))))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/bookings/my").header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deskName").value("A-1"));

        mvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // after cancel anna can take it
        mvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("deskId", deskId, "date", tomorrow))))
                .andExpect(status().isCreated());
    }

    @Test
    void cantDeleteOfficeWithDesks() throws Exception {
        mvc.perform(delete("/api/offices/" + officeId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownDeskReturns404() throws Exception {
        mvc.perform(get("/api/desks/123456").header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Desk with id 123456 not found"));
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private String register(String email, String name) throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "secret123", "fullName", name))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private JsonNode postJson(String url, String token, Object body) throws Exception {
        String response = mvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
