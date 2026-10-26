import type { Cliente, Interacao } from '../tipos';

/** Imita o BFF em memória para os testes do MF (mesmos formatos do MS Clientes). */
export function criarServidorFalso() {
  const clientes: Cliente[] = [];
  const interacoes = new Map<string, Interacao[]>();
  const chamadas: { metodo: string; url: URL; headers: Record<string, string>; corpo?: unknown }[] = [];
  let seq = 0;

  const json = (status: number, corpo?: unknown) =>
    new Response(corpo === undefined ? null : JSON.stringify(corpo), { status, headers: { 'content-type': 'application/json' } });

  function novo(dados: Partial<Cliente>): Cliente {
    const c: Cliente = {
      id: `c${++seq}`, nome: '', empresa: '', email: '', telefone: '', status: 'Prospect', responsavelId: 'u2', excluido: false,
      totalInteracoes: 0, ultimaInteracaoEm: null, criadoEm: '2026-11-02T13:00:00Z', atualizadoEm: '2026-11-02T13:00:00Z', ...dados,
    };
    clientes.push(c);
    return c;
  }

  const fetchFn: typeof fetch = async (entrada, init) => {
    const url = new URL(String(entrada));
    const metodo = init?.method ?? 'GET';
    const corpo = init?.body ? JSON.parse(String(init.body)) : undefined;
    chamadas.push({ metodo, url, headers: (init?.headers ?? {}) as Record<string, string>, corpo });
    const todas = url.pathname.split('/').filter(Boolean);
    const partes = todas.slice(todas.indexOf('clientes'));

    if (metodo === 'GET' && partes.length === 1) {
      const q = (url.searchParams.get('q') ?? '').toLowerCase();
      const status = url.searchParams.get('status');
      const lista = clientes
        .filter((c) => !c.excluido)
        .filter((c) => !q || `${c.nome} ${c.empresa}`.toLowerCase().includes(q))
        .filter((c) => !status || c.status === status);
      return json(200, { itens: lista, paginacao: { pagina: 1, porPagina: 20, total: lista.length, totalPaginas: lista.length ? 1 : 0 }, filtros: { q, status: status ?? '', responsaveis: [] } });
    }
    if (metodo === 'POST' && partes.length === 1) {
      if (!corpo.email.includes('@')) return json(400, { status: 400, codigo: 'VALIDACAO', erro: 'Cliente inválido', erros: ['email: formato inválido'] });
      return json(201, novo(corpo));
    }
    const cliente = clientes.find((c) => c.id === partes[1]);
    if (!cliente) return json(404, { status: 404, codigo: 'NAO_ENCONTRADO', erro: 'Cliente não encontrado', erros: [] });
    if (metodo === 'GET') return json(200, { cliente, interacoes: interacoes.get(cliente.id) ?? [] });
    if (metodo === 'PATCH') return json(200, Object.assign(cliente, corpo));
    if (metodo === 'DELETE') {
      Object.assign(cliente, { excluido: true, status: 'Inativo' });
      return json(204);
    }
    if (metodo === 'POST' && partes[2] === 'interacoes') {
      const i: Interacao = { id: `i${++seq}`, tipo: corpo.tipo, obs: corpo.obs, autorId: corpo.autorId, data: '2026-11-03T10:00:00Z' };
      interacoes.set(cliente.id, [i, ...(interacoes.get(cliente.id) ?? [])]);
      cliente.totalInteracoes++;
      return json(201, i);
    }
    return json(405);
  };

  return { clientes, chamadas, novo, fetchFn };
}
