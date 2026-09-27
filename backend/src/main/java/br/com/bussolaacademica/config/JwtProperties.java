package br.com.bussolaacademica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do token JWT (app.jwt.*). O segredo vem da variável JWT_SECRET
 * e precisa ter pelo menos 32 caracteres (exigência do algoritmo HS256).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
