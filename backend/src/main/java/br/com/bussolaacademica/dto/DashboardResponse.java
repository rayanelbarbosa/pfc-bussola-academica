package br.com.bussolaacademica.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Indicadores do dashboard administrativo (RF15): todo o período (padrão), 7, 30 ou 90 dias, ou datas do calendário.
 * "startDate" e "endDate" são o primeiro e o último dia do período (inclusive). "allTime" indica o padrão (todo o
 * período, sem filtro): aí a comparação com o período anterior não se aplica.
 * "Previous" é o período imediatamente anterior, de mesmo tamanho, usado para mostrar a variação.
 */
public record DashboardResponse(
        int periodDays,
        LocalDate startDate,
        LocalDate endDate,
        boolean allTime,
        long totalUsers,
        long newUsers,
        long newUsersPrevious,
        long totalTests,
        long tests,
        long testsPrevious,
        CountItem mostCommonProfile,
        CountItem mostChosenArea,
        List<DailyCount> testsPerDay,
        List<CountItem> profileDistribution,
        List<CountItem> areaDistribution,
        List<DailyCount> newUsersPerDay
) {

    /** Contagem de um item (tipo RIASEC ou área). "key" é o código e "label", o rótulo em português. */
    public record CountItem(String key, String label, long count) {
    }

    /** Contagem por dia (o front agrupa por mês quando o período passa de 31 dias). */
    public record DailyCount(LocalDate date, long count) {
    }
}
