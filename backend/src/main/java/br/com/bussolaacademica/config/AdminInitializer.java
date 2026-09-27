package br.com.bussolaacademica.config;

import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Cria o administrador inicial a partir das variáveis ADMIN_EMAIL e ADMIN_PASSWORD
 * (nunca com senha fixa no código). Se o e-mail já existir, não faz nada.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AdminInitializer(AdminProperties properties, UserRepository userRepository,
                            PasswordEncoder passwordEncoder, Clock clock) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (isBlank(properties.email()) || isBlank(properties.password())) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD não definidos: administrador inicial não será criado.");
            return;
        }
        String email = properties.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        String name = isBlank(properties.name()) ? "Administrador" : properties.name().trim();
        userRepository.save(new User(name, email, passwordEncoder.encode(properties.password()), Role.ADMIN,
                LocalDateTime.now(clock), AuthService.CURRENT_TERMS_VERSION));
        log.info("Administrador inicial criado: {}", email);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
