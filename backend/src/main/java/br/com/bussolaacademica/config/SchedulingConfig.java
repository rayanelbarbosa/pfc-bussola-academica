package br.com.bussolaacademica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Liga as rotinas agendadas (@Scheduled), como a limpeza diária de dados vencidos. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
