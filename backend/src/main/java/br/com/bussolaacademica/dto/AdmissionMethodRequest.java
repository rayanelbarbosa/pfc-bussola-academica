package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.AdmissionMethodContent;
import br.com.bussolaacademica.model.AdmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/** Dados enviados pelo administrador para incluir ou alterar uma forma de ingresso. */
public record AdmissionMethodRequest(
        @NotBlank(message = "informe o nome") @Size(max = 100, message = "máximo de 100 caracteres") String name,
        @NotNull(message = "informe o tipo") AdmissionType type,
        @NotBlank(message = "informe a frase de resumo") @Size(max = 300, message = "máximo de 300 caracteres") String summary,
        @NotBlank(message = "informe o que é") @Size(max = 1000, message = "máximo de 1000 caracteres") String description,
        @NotBlank(message = "informe como funciona") @Size(max = 1500, message = "máximo de 1500 caracteres") String howItWorks,
        @NotBlank(message = "informe quem pode participar") @Size(max = 1000, message = "máximo de 1000 caracteres") String eligibility,
        @NotBlank(message = "informe quando acontece") @Size(max = 500, message = "máximo de 500 caracteres") String schedule,
        @NotBlank(message = "informe quanto custa") @Size(max = 500, message = "máximo de 500 caracteres") String cost,
        @Size(max = 1000, message = "máximo de 1000 caracteres") String tips,
        @NotBlank(message = "informe o link oficial") @Size(max = 300, message = "máximo de 300 caracteres")
        @URL(message = "link inválido") @Pattern(regexp = "^https?://.+", message = "o link deve começar com http:// ou https://")
        String officialUrl,
        Boolean active) {

    public AdmissionMethodContent toContent() {
        return new AdmissionMethodContent(name.trim(), type, summary.trim(), description.trim(), howItWorks.trim(),
                eligibility.trim(), schedule.trim(), cost.trim(),
                tips == null || tips.isBlank() ? null : tips.trim(), officialUrl.trim());
    }

    public boolean activeOrDefault() {
        return active == null || active;
    }
}
