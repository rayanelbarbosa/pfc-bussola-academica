package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.DashboardResponse;
import br.com.bussolaacademica.dto.DashboardResponse.CountItem;
import br.com.bussolaacademica.dto.DashboardResponse.DailyCount;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Regra de negócio do dashboard administrativo (RF15).
 *
 * <ul>
 *   <li>Padrão (sem filtro): todo o período, do primeiro cadastro ou teste até hoje.</li>
 *   <li>Período rápido: 7, 30 ou 90 dias, contados até hoje (inclusive).</li>
 *   <li>Período personalizado: data inicial e final (inclusive); para um dia só, as duas iguais. A inicial não pode
 *       ser depois da final, a final não pode ser no futuro e o período tem no máximo {@value #MAX_RANGE_DAYS} dias.</li>
 *   <li>Variação: compara o período com o período anterior de mesmo tamanho.</li>
 *   <li>Perfil mais comum: o tipo RIASEC que mais apareceu como perfil principal nos testes do período
 *       (em caso de empate no teste, cada tipo empatado conta 1).</li>
 *   <li>Área mais escolhida: a área de interesse mais selecionada nos testes do período.</li>
 *   <li>Empate no "mais comum": vale a ordem oficial (R, I, A, S, E, C ou a ordem das áreas).</li>
 * </ul>
 */
@Service
public class DashboardService {

    public static final Set<Integer> ALLOWED_PERIODS = Set.of(7, 30, 90);

    /** Limite do período personalizado (um ano), para o gráfico por dia não crescer sem controle. */
    public static final int MAX_RANGE_DAYS = 366;

    private final UserRepository userRepository;
    private final VocationalTestResultRepository resultRepository;
    private final Clock clock;

    public DashboardService(UserRepository userRepository, VocationalTestResultRepository resultRepository, Clock clock) {
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
        this.clock = clock;
    }

    /** Indicadores de todo o período: do primeiro cadastro ou teste até hoje (só hoje, se ainda não há dados). */
    @Transactional(readOnly = true)
    public DashboardResponse metricsAllTime() {
        LocalDate today = LocalDate.now(clock);
        LocalDate first = Stream.of(
                        userRepository.findFirstByOrderByCreatedAtAsc().map(User::getCreatedAt),
                        resultRepository.findFirstByOrderByCreatedAtAsc().map(VocationalTestResult::getCreatedAt))
                .flatMap(Optional::stream)
                .map(LocalDateTime::toLocalDate)
                .min(Comparator.naturalOrder())
                .filter(day -> day.isBefore(today))
                .orElse(today);
        return compute(first, today, true);
    }

    /** Indicadores dos últimos 7, 30 ou 90 dias, contados até hoje (inclusive). */
    @Transactional(readOnly = true)
    public DashboardResponse metrics(int days) {
        if (!ALLOWED_PERIODS.contains(days)) {
            throw new IllegalArgumentException("Período inválido: use 7, 30 ou 90 dias");
        }
        LocalDate today = LocalDate.now(clock);
        return compute(today.minusDays(days - 1L), today, false);
    }

    /** Indicadores de um período escolhido no calendário (datas inclusive; um dia só quando as duas são iguais). */
    @Transactional(readOnly = true)
    public DashboardResponse metrics(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Informe a data inicial e a data final do período");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da data final");
        }
        if (to.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("A data final não pode ser no futuro");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("O período pode ter no máximo " + MAX_RANGE_DAYS + " dias");
        }
        return compute(from, to, false);
    }

    private DashboardResponse compute(LocalDate from, LocalDate to, boolean allTime) {
        int days = (int) ChronoUnit.DAYS.between(from, to) + 1;
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        LocalDateTime previousStart = start.minusDays(days);

        List<VocationalTestResult> results = resultRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
        List<User> newUsers = userRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);

        List<CountItem> profiles = profileDistribution(results);
        List<CountItem> areas = areaDistribution(results);

        return new DashboardResponse(
                days,
                from,
                to,
                allTime,
                userRepository.count(),
                newUsers.size(),
                userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(previousStart, start),
                resultRepository.count(),
                results.size(),
                resultRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(previousStart, start),
                mostCommon(profiles),
                mostCommon(areas),
                perDay(results.stream().map(VocationalTestResult::getCreatedAt).toList(), from, to),
                profiles,
                areas,
                perWeek(newUsers.stream().map(User::getCreatedAt).toList(), from, to)
        );
    }

    private static List<CountItem> profileDistribution(List<VocationalTestResult> results) {
        Map<RiasecCategory, Long> counts = new EnumMap<>(RiasecCategory.class);
        Arrays.stream(RiasecCategory.values()).forEach(category -> counts.put(category, 0L));
        results.forEach(result -> result.getTopCategories().forEach(category -> counts.merge(category, 1L, Long::sum)));
        return counts.entrySet().stream()
                .map(entry -> new CountItem(entry.getKey().name(), entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }

    private static List<CountItem> areaDistribution(List<VocationalTestResult> results) {
        Map<InterestArea, Long> counts = new EnumMap<>(InterestArea.class);
        Arrays.stream(InterestArea.values()).forEach(area -> counts.put(area, 0L));
        results.forEach(result -> result.getInterestAreas().forEach(area -> counts.merge(area, 1L, Long::sum)));
        return counts.entrySet().stream()
                .map(entry -> new CountItem(entry.getKey().name(), entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }

    /** O item de maior contagem (o primeiro na ordem oficial em caso de empate); nulo se nada foi contado. */
    static CountItem mostCommon(List<CountItem> items) {
        return items.stream()
                .filter(item -> item.count() > 0)
                .max(Comparator.comparingLong(CountItem::count).thenComparing(item -> -items.indexOf(item)))
                .orElse(null);
    }

    /** Uma entrada por dia do período, inclusive os dias sem nenhum teste (contagem zero). */
    static List<DailyCount> perDay(List<LocalDateTime> moments, LocalDate start, LocalDate end) {
        Map<LocalDate, Long> counts = new TreeMap<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            counts.put(day, 0L);
        }
        moments.forEach(moment -> counts.computeIfPresent(moment.toLocalDate(), (day, count) -> count + 1));
        return counts.entrySet().stream().map(entry -> new DailyCount(entry.getKey(), entry.getValue())).toList();
    }

    /** Uma entrada por semana (iniciando na segunda-feira) que cruza o período. */
    static List<DailyCount> perWeek(List<LocalDateTime> moments, LocalDate start, LocalDate end) {
        Map<LocalDate, Long> counts = new TreeMap<>();
        LocalDate firstMonday = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (LocalDate week = firstMonday; !week.isAfter(end); week = week.plusWeeks(1)) {
            counts.put(week, 0L);
        }
        moments.forEach(moment -> counts.computeIfPresent(
                moment.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), (week, count) -> count + 1));
        List<DailyCount> weeks = new ArrayList<>();
        counts.forEach((week, count) -> weeks.add(new DailyCount(week, count)));
        return weeks;
    }
}
