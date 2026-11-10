package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.CareerContent;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados enviados pelo administrador para incluir ou alterar uma carreira. */
public record CareerRequest(
        @NotBlank(message = "informe o nome") @Size(max = 100, message = "máximo de 100 caracteres") String name,
        @NotNull(message = "informe a área") InterestArea area,
        @NotNull(message = "informe o tipo principal") RiasecCategory primaryCategory,
        @NotNull(message = "informe o tipo secundário") RiasecCategory secondaryCategory,
        @NotBlank(message = "informe a descrição") @Size(max = 300, message = "máximo de 300 caracteres") String description,
        Boolean active) {

    public CareerContent toContent() {
        return new CareerContent(name.trim(), area, primaryCategory, secondaryCategory, description.trim());
    }

    public boolean activeOrDefault() {
        return active == null || active;
    }
}
