package org.example.controller;

import org.example.dto.DeskRequest;
import org.example.entity.Desk;
import org.example.entity.Office;
import org.example.repository.DeskRepository;
import org.example.repository.OfficeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeskController.class)
class DeskControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DeskRepository deskRepo;

    @MockBean
    private OfficeRepository officeRepo;

    @Test
    @DisplayName("GET /api/desks/{id} повертає 200 і тіло, якщо є")
    void getDeskFound() throws Exception {
        Office office = new Office();
        office.setId(1L);
        office.setName("HQ");
        office.setCity("Kyiv");
        office.setAddress("Some str");

        Desk desk = new Desk();
        desk.setId(10L);
        desk.setName("Test Desk");
        desk.setFloor(2);
        desk.setOffice(office);

        Mockito.when(deskRepo.findById(10L)).thenReturn(Optional.of(desk));

        mvc.perform(get("/api/desks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Desk"))
                .andExpect(jsonPath("$.office.id").value(1));
    }

    @Test
    @DisplayName("GET /api/desks/{id} повертає 404, якщо нема")
    void getDeskNotFound() throws Exception {
        Mockito.when(deskRepo.findById(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/desks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/desks створює Desk, якщо office існує")
    void createDeskSuccess() throws Exception {
        Office office = new Office();
        office.setId(5L);

        Mockito.when(officeRepo.findById(5L)).thenReturn(Optional.of(office));

        String json = """
            {
              "name": "New Desk",
              "floor": 3,
              "officeId": 5
            }
            """;

        Desk saved = new Desk();
        saved.setId(20L);
        saved.setName("New Desk");
        saved.setFloor(3);
        saved.setOffice(office);

        Mockito.when(deskRepo.save(Mockito.any(Desk.class))).thenReturn(saved);

        mvc.perform(post("/api/desks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.floor").value(3));
    }

    @Test
    @DisplayName("POST /api/desks повертає 404, якщо officeId не знайдено")
    void createDeskOfficeNotFound() throws Exception {
        Mockito.when(officeRepo.findById(999L)).thenReturn(Optional.empty());

        String json = """
            {
              "name": "NoOffice",
              "floor": 1,
              "officeId": 999
            }
            """;

        mvc.perform(post("/api/desks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/desks повертає 400 при невідповідній валідації")
    void createDeskValidationError() throws Exception {
        // пропустимо поле "name"
        String json = """
            {
              "floor": 2,
              "officeId": 1
            }
            """;

        mvc.perform(post("/api/desks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @DisplayName("PUT /api/desks/{id} оновлює Desk успішно")
    void updateDeskSuccess() throws Exception {
        Office office = new Office();
        office.setId(2L);
        Desk existing = new Desk();
        existing.setId(30L);
        existing.setOffice(office);

        Mockito.when(deskRepo.findById(30L)).thenReturn(Optional.of(existing));
        Mockito.when(officeRepo.findById(2L)).thenReturn(Optional.of(office));
        Mockito.when(deskRepo.save(Mockito.any(Desk.class))).thenAnswer(i -> i.getArgument(0));

        String json = """
            {
              "name": "Updated",
              "floor": 4,
              "officeId": 2
            }
            """;

        mvc.perform(put("/api/desks/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.floor").value(4));
    }

    @Test
    @DisplayName("PUT /api/desks/{id} – 404, якщо Desk не знайдено")
    void updateDeskNotFoundDesk() throws Exception {
        Mockito.when(deskRepo.findById(777L)).thenReturn(Optional.empty());

        String json = """
            {
              "name": "X",
              "floor": 1,
              "officeId": 1
            }
            """;

        mvc.perform(put("/api/desks/777")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/desks/{id} – 404, якщо officeId неіснуючий")
    void updateDeskNotFoundOffice() throws Exception {
        Office office = new Office();
        office.setId(3L);
        Desk existing = new Desk();
        existing.setId(40L);
        existing.setOffice(office);

        Mockito.when(deskRepo.findById(40L)).thenReturn(Optional.of(existing));
        Mockito.when(officeRepo.findById(999L)).thenReturn(Optional.empty());

        String json = """
            {
              "name": "Name",
              "floor": 2,
              "officeId": 999
            }
            """;

        mvc.perform(put("/api/desks/40")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/desks/{id} – успішне видалення")
    void deleteDeskSuccess() throws Exception {
        Mockito.when(deskRepo.existsById(50L)).thenReturn(true);

        mvc.perform(delete("/api/desks/50"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/desks/{id} – 404, якщо нема такого Desk")
    void deleteDeskNotFound() throws Exception {
        Mockito.when(deskRepo.existsById(88L)).thenReturn(false);

        mvc.perform(delete("/api/desks/88"))
                .andExpect(status().isNotFound());
    }
}
