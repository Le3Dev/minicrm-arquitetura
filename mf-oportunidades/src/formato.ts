const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const data = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' });

export const formatarValor = (valor: number): string => moeda.format(valor);

/** Datas sem hora ("2026-12-15") são lidas como dia local, sem deslocar por fuso. */
export const formatarData = (iso: string | null | undefined): string => (iso ? data.format(new Date(`${iso.slice(0, 10)}T00:00:00`)) : '—');
