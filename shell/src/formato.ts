const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const percentual = new Intl.NumberFormat('pt-BR', { style: 'percent', maximumFractionDigits: 0 });

export const formatarValor = (v: number): string => moeda.format(v);
export const formatarPercentual = (v: number): string => percentual.format(v);
