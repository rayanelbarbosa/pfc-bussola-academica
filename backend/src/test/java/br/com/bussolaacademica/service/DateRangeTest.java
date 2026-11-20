package br.com.bussolaacademica.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 20);

    @Test
    void noDatesMeansNoPeriodFilter() {
        assertThat(DateRange.of(null, null)).isEmpty();
    }

    @Test
    void acceptsARangeOrASingleDay() {
        assertThat(DateRange.of(DAY.minusDays(5), DAY)).contains(new DateRange(DAY.minusDays(5), DAY));
        assertThat(DateRange.of(DAY, DAY)).contains(new DateRange(DAY, DAY));
    }

    @Test
    void rejectsAMissingDateOrInvertedOrder() {
        assertThatThrownBy(() -> DateRange.of(DAY, null)).hasMessageContaining("data inicial e a data final");
        assertThatThrownBy(() -> DateRange.of(null, DAY)).hasMessageContaining("data inicial e a data final");
        assertThatThrownBy(() -> DateRange.of(DAY, DAY.minusDays(1))).hasMessageContaining("depois da data final");
    }
}
