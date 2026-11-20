package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.DashboardResponse;
import br.com.bussolaacademica.dto.DashboardResponse.CountItem;
import br.com.bussolaacademica.dto.DashboardResponse.DailyCount;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    /** "Hoje" fixo: sábado, 07/11/2026. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-07T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private UserRepository userRepository;

    @Mock
    private VocationalTestResultRepository resultRepository;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(userRepository, resultRepository, CLOCK);
    }

    private static VocationalTestResult result(LocalDateTime when, List<RiasecCategory> top, List<InterestArea> areas) {
        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        for (RiasecCategory category : RiasecCategory.values()) {
            scores.put(category, top.contains(category) ? 10 : 4);
        }
        return new VocationalTestResult(null, when, scores, top, areas, 20);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 15, 365})
    void rejectsPeriodsOtherThan7_30_90(int days) {
        assertThatThrownBy(() -> service.metrics(days)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void computesBigNumbersDistributionsAndVariation() {
        LocalDateTime today = LocalDateTime.of(2026, 11, 7, 10, 0);
        List<VocationalTestResult> results = List.of(
                result(today, List.of(RiasecCategory.SOCIAL), List.of(InterestArea.HEALTH, InterestArea.EDUCATION)),
                result(today.minusDays(1), List.of(RiasecCategory.SOCIAL, RiasecCategory.ARTISTIC), List.of(InterestArea.HEALTH)),
                result(today.minusDays(6), List.of(RiasecCategory.REALISTIC), List.of(InterestArea.ENGINEERING)));
        User user = new User("Ana", "ana@teste.com", "hash", Role.STUDENT, today.minusDays(2), "1.0");
        when(resultRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 11, 8, 0, 0))).thenReturn(results);
        when(userRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of(user));
        when(userRepository.count()).thenReturn(10L);
        when(resultRepository.count()).thenReturn(25L);
        when(resultRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(1L);
        when(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(4L);

        DashboardResponse dashboard = service.metrics(7);

        assertThat(dashboard.periodDays()).isEqualTo(7);
        assertThat(dashboard.allTime()).isFalse();
        assertThat(dashboard.startDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(dashboard.endDate()).isEqualTo(LocalDate.of(2026, 11, 7));
        assertThat(dashboard.totalUsers()).isEqualTo(10);
        assertThat(dashboard.newUsers()).isEqualTo(1);
        assertThat(dashboard.newUsersPrevious()).isEqualTo(4);
        assertThat(dashboard.tests()).isEqualTo(3);
        assertThat(dashboard.testsPrevious()).isEqualTo(1);
        assertThat(dashboard.mostCommonProfile()).isEqualTo(new CountItem("SOCIAL", "Social", 2));
        assertThat(dashboard.mostChosenArea()).isEqualTo(new CountItem("HEALTH", "Saúde", 2));
        assertThat(dashboard.testsPerDay()).hasSize(7);
        assertThat(dashboard.testsPerDay().get(6)).isEqualTo(new DailyCount(LocalDate.of(2026, 11, 7), 1));
        assertThat(dashboard.testsPerDay().get(0)).isEqualTo(new DailyCount(LocalDate.of(2026, 11, 1), 1));
        assertThat(dashboard.profileDistribution()).hasSize(6);
        assertThat(dashboard.areaDistribution()).hasSize(8);
    }

    @Test
    void usesTheCalendarRangeAndComparesWithThePreviousRangeOfSameSize() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 10);
        List<VocationalTestResult> results = List.of(
                result(LocalDateTime.of(2026, 10, 1, 8, 0), List.of(RiasecCategory.INVESTIGATIVE), List.of(InterestArea.HEALTH)),
                result(LocalDateTime.of(2026, 10, 10, 23, 0), List.of(RiasecCategory.INVESTIGATIVE), List.of(InterestArea.HEALTH)));
        when(resultRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 11, 0, 0))).thenReturn(results);
        when(userRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(resultRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                LocalDateTime.of(2026, 9, 21, 0, 0), LocalDateTime.of(2026, 10, 1, 0, 0))).thenReturn(5L);

        DashboardResponse dashboard = service.metrics(from, to);

        assertThat(dashboard.periodDays()).isEqualTo(10);
        assertThat(dashboard.startDate()).isEqualTo(from);
        assertThat(dashboard.endDate()).isEqualTo(to);
        assertThat(dashboard.tests()).isEqualTo(2);
        assertThat(dashboard.testsPrevious()).isEqualTo(5);
        assertThat(dashboard.testsPerDay()).hasSize(10);
        assertThat(dashboard.testsPerDay().get(9)).isEqualTo(new DailyCount(to, 1));
    }

    @Test
    void allTimeStartsOnTheFirstSignUpOrTestAndHasNoComparison() {
        User first = new User("Bia", "bia@teste.com", "hash", Role.STUDENT, LocalDateTime.of(2026, 6, 1, 9, 0), "1.0");
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(first));
        when(resultRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(
                result(LocalDateTime.of(2026, 6, 3, 9, 0), List.of(RiasecCategory.SOCIAL), List.of(InterestArea.HEALTH))));
        when(resultRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(userRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of(first));

        DashboardResponse dashboard = service.metricsAllTime();

        assertThat(dashboard.allTime()).isTrue();
        assertThat(dashboard.startDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(dashboard.endDate()).isEqualTo(LocalDate.of(2026, 11, 7));
        assertThat(dashboard.newUsers()).isEqualTo(1);
        assertThat(dashboard.newUsersPerDay()).hasSize(160).contains(new DailyCount(LocalDate.of(2026, 6, 1), 1));
    }

    @Test
    void allTimeWithoutDataIsJustToday() {
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());
        when(resultRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());
        when(resultRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());
        when(userRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());

        DashboardResponse dashboard = service.metricsAllTime();

        assertThat(dashboard.periodDays()).isEqualTo(1);
        assertThat(dashboard.startDate()).isEqualTo(LocalDate.of(2026, 11, 7));
    }

    @Test
    void acceptsASingleDayWhenBothDatesAreEqual() {
        LocalDate day = LocalDate.of(2026, 11, 3);
        when(resultRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                LocalDateTime.of(2026, 11, 3, 0, 0), LocalDateTime.of(2026, 11, 4, 0, 0))).thenReturn(List.of());
        when(userRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(List.of());

        DashboardResponse dashboard = service.metrics(day, day);

        assertThat(dashboard.periodDays()).isEqualTo(1);
        assertThat(dashboard.testsPerDay()).containsExactly(new DailyCount(day, 0));
    }

    @Test
    void rejectsInvalidCalendarRanges() {
        LocalDate today = LocalDate.of(2026, 11, 7);
        assertThatThrownBy(() -> service.metrics(null, today)).hasMessageContaining("data inicial e a data final");
        assertThatThrownBy(() -> service.metrics(today, null)).hasMessageContaining("data inicial e a data final");
        assertThatThrownBy(() -> service.metrics(today, today.minusDays(1))).hasMessageContaining("depois da data final");
        assertThatThrownBy(() -> service.metrics(today, today.plusDays(1))).hasMessageContaining("futuro");
        assertThatThrownBy(() -> service.metrics(today.minusDays(366), today)).hasMessageContaining("no máximo 366 dias");
    }

    @Test
    void mostCommonIsNullWithoutDataAndKeepsOfficialOrderOnTies() {
        assertThat(DashboardService.mostCommon(List.of(new CountItem("A", "A", 0)))).isNull();
        assertThat(DashboardService.mostCommon(List.of(new CountItem("R", "Realista", 3), new CountItem("I", "Investigativo", 3))))
                .extracting(CountItem::key).isEqualTo("R");
    }
}
