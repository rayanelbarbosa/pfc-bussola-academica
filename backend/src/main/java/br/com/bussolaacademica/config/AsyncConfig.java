package br.com.bussolaacademica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Liga a execução assíncrona (@Async), usada no envio de e-mail. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
