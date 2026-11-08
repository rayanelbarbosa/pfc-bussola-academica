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
        return new VocationalTestResult(null, when, scores, top, areas);
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
        when(resultRepository.findByCreatedAtGreaterThanEqual(LocalDateTime.of(2026, 11, 1, 0, 0))).thenReturn(results);
        when(userRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of(user));
        when(userRepository.count()).thenReturn(10L);
        when(resultRepository.count()).thenReturn(25L);
        when(resultRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(1L);
        when(userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(any(), any())).thenReturn(4L);

        DashboardResponse dashboard = service.metrics(7);

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
    void mostCommonIsNullWithoutDataAndKeepsOfficialOrderOnTies() {
        assertThat(DashboardService.mostCommon(List.of(new CountItem("A", "A", 0)))).isNull();
        assertThat(DashboardService.mostCommon(List.of(new CountItem("R", "Realista", 3), new CountItem("I", "Investigativo", 3))))
                .extracting(CountItem::key).isEqualTo("R");
    }

    @Test
    void groupsNewUsersByWeekStartingOnMonday() {
        List<DailyCount> weeks = DashboardService.perWeek(
                List.of(LocalDateTime.of(2026, 11, 3, 9, 0), LocalDateTime.of(2026, 11, 7, 9, 0), LocalDateTime.of(2026, 10, 28, 9, 0)),
                LocalDate.of(2026, 10, 27), LocalDate.of(2026, 11, 7));

        assertThat(weeks).containsExactly(
                new DailyCount(LocalDate.of(2026, 10, 26), 1),
                new DailyCount(LocalDate.of(2026, 11, 2), 2));
    }
}
