package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.CareerContent;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Dados enviados pelo administrador para incluir ou alterar uma carreira. */
public record CareerRequest(
        @NotBlank(message = "informe o nome") @Size(max = 100, message = "máximo de 100 caracteres") String name,
        @NotNull(message = "informe a área") InterestArea area,
        @NotNull(message = "informe o tipo principal") RiasecCategory primaryCategory,
        @NotNull(message = "informe o tipo secundário") RiasecCategory secondaryCategory,
        // opcional: 3º tipo do código de Holland, usado só para desempatar
        RiasecCategory tertiaryCategory,
        @NotBlank(message = "informe a descrição") @Size(max = 300, message = "máximo de 300 caracteres") String description,
        // opcional: tarefas do dia a dia mostradas no "Mais sobre a carreira"
        @Size(max = 3, message = "informe no máximo 3 tarefas")
        List<@Size(max = 150, message = "máximo de 150 caracteres por tarefa")
             @Pattern(regexp = "[^|]*", message = "não use o caractere |") String> dailyTasks,
        Boolean active) {

    public CareerContent toContent() {
        return new CareerContent(name.trim(), area, primaryCategory, secondaryCategory, tertiaryCategory, description.trim());
    }

    public List<String> dailyTasksOrEmpty() {
        return dailyTasks == null ? List.of() : dailyTasks;
    }

    public boolean activeOrDefault() {
        return active == null || active;
    }
}
