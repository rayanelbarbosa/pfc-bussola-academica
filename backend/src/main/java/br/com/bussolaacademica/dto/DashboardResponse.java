package br.com.bussolaacademica.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Indicadores do dashboard administrativo (RF15) para um período (7, 30 ou 90 dias).
 * "Previous" é o período imediatamente anterior, de mesmo tamanho, usado para mostrar a variação.
 */
public record DashboardResponse(
        int periodDays,
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
        List<DailyCount> newUsersPerWeek
) {

    /** Contagem de um item (tipo RIASEC ou área). "key" é o código e "label", o rótulo em português. */
    public record CountItem(String key, String label, long count) {
    }

    /** Contagem por dia (ou por semana, iniciando na data informada). */
    public record DailyCount(LocalDate date, long count) {
    }
}
