package br.com.bussolaacademica.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
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

    @Test
    void adminSeesMetricsAndStudentIsBlocked() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());
        String answers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + (id == 7 || id == 8 ? 5 : 2) + "}")
                .collect(Collectors.joining(",", "{\"answers\":[", "],\"interestAreas\":[\"EDUCATION\"]}"));
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
    }
}
