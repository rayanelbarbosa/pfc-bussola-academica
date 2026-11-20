package br.com.bussolaacademica.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Clock;
import java.time.LocalDate;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.answers;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Dashboard administrativo (RF15): só administradores, com os indicadores do período. */
@SpringBootTest
@AutoConfigureMockMvc
class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Clock clock;

    @Test
    void adminSeesMetricsAndStudentIsBlocked() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());
        String answers = "{\"answers\":[" + answers(1, 1, 1, 4, 1, 1) + "],\"interestAreas\":[\"EDUCATION\"]}";
        mockMvc.perform(post("/api/vocational-test/results").header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON).content(answers))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());

        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(get("/api/admin/dashboard").param("days", "7").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodDays").value(7))
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.tests", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.testsPerDay", hasSize(7)))
                .andExpect(jsonPath("$.profileDistribution", hasSize(6)))
                .andExpect(jsonPath("$.areaDistribution", hasSize(8)));

        mockMvc.perform(get("/api/admin/dashboard").param("days", "15").header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());

        // sem filtro: todo o período, do primeiro cadastro até hoje
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allTime").value(true))
                .andExpect(jsonPath("$.endDate").value(LocalDate.now(clock).toString()))
                .andExpect(jsonPath("$.newUsers", greaterThanOrEqualTo(2)));

        // período do calendário: um dia só (hoje) e validações
        String today = LocalDate.now(clock).toString(); // mesmo fuso da aplicação (o CI roda em UTC)
        mockMvc.perform(get("/api/admin/dashboard").param("from", today).param("to", today).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodDays").value(1))
                .andExpect(jsonPath("$.startDate").value(today))
                .andExpect(jsonPath("$.endDate").value(today))
                .andExpect(jsonPath("$.tests", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.testsPerDay", hasSize(1)));
        mockMvc.perform(get("/api/admin/dashboard").param("from", today).header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/admin/dashboard").param("from", "2026-11-10").param("to", "2026-11-01")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/admin/dashboard").param("from", "10/11/2026").param("to", today)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }
}
