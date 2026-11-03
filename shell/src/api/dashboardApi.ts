import type { Config } from '../config';

export interface Falha {
  servico: string;
  erro: string;
}

export interface EtapaIndicador {
  etapa: string;
  quantidade: number;
  valorTotal: number;
}

export interface Indicadores {
  clientes?: { total: number; porStatus: Record<string, number> };
  oportunidades?: {
    total: number;
    valorEmNegociacao: number;
    valorGanho: number;
    ganhas: number;
    perdidas: number;
    taxaConversao: number;
    porEtapa: EtapaIndicador[];
    motivosPerda: Record<string, number>;
  };
  ranking?: { responsavelId: string; valorGanho: number; oportunidadesGanhas: number; oportunidadesPerdidas: number }[];
}

/** Resposta de GET /aggregated-data do BFF. */
export interface DadosAgregados {
  usuario: { id: string; nome: string; perfil: string };
  escopo: string[];
  clientes: { total: number; itens: unknown[] };
  funil: unknown[];
  indicadores: Indicadores;
  falhas: Falha[];
}

export class ErroDashboard extends Error {}

/** Busca os dados do dashboard no BFF (através do API Gateway). */
export async function buscarDadosAgregados(config: Config, usuarioId: string, signal?: AbortSignal, fetchFn: typeof fetch = fetch): Promise<DadosAgregados> {
  const headers: Record<string, string> = { accept: 'application/json' };
  if (config.apiKey) headers['x-api-key'] = config.apiKey;
  let resposta: Response;
  try {
    resposta = await fetchFn(`${config.apiUrl}/aggregated-data?usuarioId=${encodeURIComponent(usuarioId)}`, { headers, signal });
  } catch (erro) {
    if ((erro as Error).name === 'AbortError') throw erro;
    throw new ErroDashboard('Não foi possível falar com o servidor. Verifique a conexão.');
  }
  if (resposta.status === 403 && !(resposta.headers.get('content-type') ?? '').includes('json')) {
    throw new ErroDashboard('Acesso negado pelo API Gateway (confira a API key).');
  }
  const texto = await resposta.text();
  let corpo: unknown = null;
  try {
    corpo = texto ? JSON.parse(texto) : null;
  } catch {
    corpo = null;
  }
  if (!resposta.ok) {
    const c = (corpo ?? {}) as { erro?: string; message?: string };
    throw new ErroDashboard(c.erro ?? c.message ?? `Erro ${resposta.status}`);
  }
  return corpo as DadosAgregados;
}
