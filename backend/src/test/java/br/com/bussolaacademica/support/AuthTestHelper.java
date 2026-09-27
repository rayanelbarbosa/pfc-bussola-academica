package br.com.bussolaacademica.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Utilitário dos testes de integração: cadastra/loga usuários e devolve o JWT. */
public final class AuthTestHelper {

    public static final String ADMIN_EMAIL = "admin@teste.com";
    public static final String ADMIN_PASSWORD = "Admin12345";
    public static final String DEFAULT_PASSWORD = "Senha1234";

    private AuthTestHelper() {
    }

    public static String uniqueEmail() {
        return "aluna-" + UUID.randomUUID() + "@teste.com";
    }

    public static String registerJson(String name, String email, String password, boolean acceptedTerms) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password
                + "\",\"acceptedTerms\":" + acceptedTerms + "}";
    }

    public static String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    /** Cadastra um estudante novo e devolve o token. */
    public static String registerStudent(MockMvc mockMvc, String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Aluna Teste", email, DEFAULT_PASSWORD, true)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    public static String login(MockMvc mockMvc, String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}
