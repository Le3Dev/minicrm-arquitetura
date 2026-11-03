import type { EdicaoOportunidade, FiltrosFunil, Movimentacao, NovaOportunidade, Oportunidade, RespostaClientes, RespostaFunil } from '../tipos';

/** Erro devolvido pelo BFF/MS Oportunidades no formato { status, codigo, erro, erros[] }. */
export class ErroApi extends Error {
  constructor(
    readonly status: number,
    readonly codigo: string,
    mensagem: string,
    readonly erros: string[] = [],
  ) {
    super(mensagem);
    this.name = 'ErroApi';
  }
}

export interface ConfigApi {
  /** URL do API Gateway (produção) ou do BFF (desenvolvimento). O MF nunca chama o microsserviço direto. */
  apiUrl: string;
  /** API key do usage plan do AWS API Gateway (header x-api-key). */
  apiKey?: string;
  /** Usuário logado; o BFF usa para calcular o escopo de visualização (RF15). */
  usuarioId?: string;
  fetchFn?: typeof fetch;
}

/** Cliente HTTP das rotas de oportunidades (e da lista de clientes para o formulário) expostas pelo BFF. */
export class OportunidadesApi {
  private readonly base: string;

  constructor(private readonly config: ConfigApi) {
    this.base = config.apiUrl.replace(/\/+$/, '');
  }

  /** RF08, RF19: quadro com as 4 etapas, quantidade e soma por etapa. */
  funil(filtros: FiltrosFunil, signal?: AbortSignal): Promise<RespostaFunil> {
    return this.requisitar('GET', '/oportunidades', { query: { clienteId: filtros.clienteId, responsavelId: filtros.responsavelId }, signal });
  }

  /** Clientes para escolher no formulário (RF07). */
  clientes(signal?: AbortSignal): Promise<RespostaClientes> {
    return this.requisitar('GET', '/clientes', { query: { porPagina: '200' }, signal });
  }

  criar(dados: NovaOportunidade): Promise<Oportunidade> {
    return this.requisitar('POST', '/oportunidades', { corpo: dados });
  }

  atualizar(id: number, dados: EdicaoOportunidade): Promise<Oportunidade> {
    return this.requisitar('PUT', `/oportunidades/${id}`, { corpo: dados });
  }

  /** RF09, RF29: move, fecha (resultado e motivo) ou reabre (reabrir: true). */
  mover(id: number, movimentacao: Movimentacao): Promise<Oportunidade> {
    return this.requisitar('PATCH', `/oportunidades/${id}`, { corpo: movimentacao });
  }

  excluir(id: number): Promise<void> {
    return this.requisitar('DELETE', `/oportunidades/${id}`, {});
  }

  private async requisitar<T>(
    metodo: string,
    caminho: string,
    opcoes: { query?: Record<string, string>; corpo?: unknown; signal?: AbortSignal },
  ): Promise<T> {
    const url = new URL(this.base + caminho);
    const query = { ...opcoes.query, ...(this.config.usuarioId ? { usuarioId: this.config.usuarioId } : {}) };
    Object.entries(query).forEach(([chave, valor]) => {
      if (valor) url.searchParams.set(chave, valor);
    });

    const headers: Record<string, string> = { accept: 'application/json' };
    if (opcoes.corpo !== undefined) headers['content-type'] = 'application/json';
    if (this.config.apiKey) headers['x-api-key'] = this.config.apiKey;

    let resposta: Response;
    try {
      resposta = await (this.config.fetchFn ?? fetch)(url.toString(), {
        method: metodo,
        headers,
        body: opcoes.corpo === undefined ? undefined : JSON.stringify(opcoes.corpo),
        signal: opcoes.signal,
      });
    } catch (erro) {
      if ((erro as Error).name === 'AbortError') throw erro;
      throw new ErroApi(0, 'REDE', 'Não foi possível falar com o servidor. Verifique a conexão.');
    }

    if (resposta.status === 204) return undefined as T;
    const texto = await resposta.text();
    const corpo = texto ? safeJson(texto) : null;
    if (!resposta.ok) {
      const c = (corpo ?? {}) as { codigo?: string; erro?: string; message?: string; erros?: string[] };
      const mensagem =
        resposta.status === 403
          ? 'Acesso negado pelo API Gateway (confira a API key).'
          : (c.erro ?? c.message ?? `Erro ${resposta.status}`);
      throw new ErroApi(resposta.status, c.codigo ?? 'ERRO', mensagem, c.erros ?? []);
    }
    return corpo as T;
  }
}

function safeJson(texto: string): unknown {
  try {
    return JSON.parse(texto);
  } catch {
    return null;
  }
}
