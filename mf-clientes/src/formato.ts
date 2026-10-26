const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
const data = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' });

export const formatarDataHora = (iso: string | null | undefined): string => (iso ? dataHora.format(new Date(iso)) : '—');
export const formatarData = (iso: string | null | undefined): string => (iso ? data.format(new Date(iso)) : '—');

export function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/);
  return ((partes[0]?.[0] ?? '') + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase();
}
