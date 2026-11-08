package br.com.bussolaacademica.security;

import br.com.bussolaacademica.config.JwtProperties;
import br.com.bussolaacademica.model.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Gera o JWT entregue ao front-end após o login ou o cadastro.
 * Cada token tem um identificador único (jti), usado para revogá-lo no logout, e a versão do
 * Termo/Política que o usuário tinha aceitado quando o token foi gerado (claim "terms").
 */
@Service
public class TokenService {

    public static final String ROLE_CLAIM = "role";
    public static final String EMAIL_CLAIM = "email";
    public static final String NAME_CLAIM = "name";
    public static final String TERMS_CLAIM = "terms";
    private static final String ISSUER = "bussola-academica";

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public String generate(User user) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim(EMAIL_CLAIM, user.getEmail())
                .claim(NAME_CLAIM, user.getName())
                .claim(ROLE_CLAIM, user.getRole().name())
                .claim(TERMS_CLAIM, user.getTermsVersion())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return properties.expirationMinutes() * 60;
    }
}
