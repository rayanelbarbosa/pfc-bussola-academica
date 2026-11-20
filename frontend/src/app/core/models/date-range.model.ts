/** Período escolhido no calendário: datas ISO (AAAA-MM-DD), as duas inclusive; as duas nulas = sem filtro de período. */
export interface DateRangeValue {
  from: string | null;
  to: string | null;
}
