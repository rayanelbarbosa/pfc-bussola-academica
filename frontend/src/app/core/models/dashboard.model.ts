/** Indicadores do dashboard administrativo (espelha DashboardResponse do back-end). */
export interface CountItem {
  key: string;
  label: string;
  count: number;
}

export interface DailyCount {
  date: string;
  count: number;
}

export interface Dashboard {
  periodDays: number;
  /** Primeiro e último dia do período (AAAA-MM-DD), inclusive. */
  startDate: string;
  endDate: string;
  /** true no padrão (todo o período, sem filtro): não há período anterior para comparar. */
  allTime: boolean;
  totalUsers: number;
  newUsers: number;
  newUsersPrevious: number;
  totalTests: number;
  tests: number;
  testsPrevious: number;
  mostCommonProfile: CountItem | null;
  mostChosenArea: CountItem | null;
  testsPerDay: DailyCount[];
  profileDistribution: CountItem[];
  areaDistribution: CountItem[];
  newUsersPerDay: DailyCount[];
}

export const DASHBOARD_PERIODS = [7, 30, 90] as const;
