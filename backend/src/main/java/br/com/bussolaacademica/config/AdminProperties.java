package br.com.bussolaacademica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Administrador inicial (app.admin.*), criado na inicialização se ainda não existir. */
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String name, String email, String password) {
}
