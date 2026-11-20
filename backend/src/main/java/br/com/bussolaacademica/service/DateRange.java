package br.com.bussolaacademica.service;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Período escolhido no calendário das listas do admin (usuários e auditoria): data inicial e final, as duas inclusive.
 * Para um dia só, as duas datas são iguais. Sem nenhuma data, a lista não é filtrada por período.
 */
public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Informe a data inicial e a data final do período");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da data final");
        }
    }

    /** Vazio quando nenhuma data foi informada; erro quando só uma veio ou a ordem está invertida. */
    public static Optional<DateRange> of(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return Optional.empty();
        }
        return Optional.of(new DateRange(from, to));
    }

    /** Filtro "do início do primeiro dia até antes do dia seguinte ao último" sobre um campo de data e hora. */
    public <T> Specification<T> on(String attribute) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.<LocalDateTime>get(attribute), start),
                cb.lessThan(root.<LocalDateTime>get(attribute), end));
    }
}
