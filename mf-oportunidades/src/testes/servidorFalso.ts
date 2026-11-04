import type { ClienteResumo, Etapa, MotivoPerda, Oportunidade, Resultado } from '../tipos';
import { ETAPAS } from '../tipos';

/** Imita o BFF em memória para os testes do MF (mesmos formatos do MS Oportunidades e do MS Clientes). */
export function criarServidorFalso() {
  const clientes: ClienteResumo[] = [
    { id: 'c1', nome: 'Mariana Souza', empresa: 'Acme Ltda', status: 'Prospect' },
    { id: 'c2', nome: 'João Pereira', empresa: 'Café Brasil', status: 'Ativo' },
  ];
  const oportunidades: Oportunidade[] = [];
  const chamadas: { metodo: string; url: URL; headers: Record<string, string>; corpo?: Record<string, unknown> }[] = [];
  let seq = 0;

  const json = (status: number, corpo?: unknown) =>
    new Response(corpo === undefined ? null : JSON.stringify(corpo), { status, headers: { 'content-type': 'application/json' } });
  const erro = (status: number, codigo: string, mensagem: string, erros: string[] = [mensagem]) => json(status, { status, codigo, erro: mensagem, erros });

  function nova(dados: Partial<Oportunidade>): Oportunidade {
    const o: Oportunidade = {
      id: ++seq, clienteId: 'c1', titulo: '', etapa: 'Prospecção', resultado: null, motivoPerda: null, valor: 1000, dataPrevista: null,
      responsavelId: 'u2', criadaEm: '2026-11-02T13:00:00Z', atualizada: '2026-11-02T13:00:00Z', ...dados,
    };
    oportunidades.unshift(o);
    return o;
  }

  function montarFunil(lista: Oportunidade[]) {
    const etapas = ETAPAS.map((etapa) => {
      const itens = lista.filter((o) => o.etapa === etapa);
      return { etapa, quantidade: itens.length, valorTotal: itens.reduce((s, o) => s + o.valor, 0), itens };
    });
    const emNegociacao = etapas.filter((c) => c.etapa !== 'Fechado').reduce((s, c) => s + c.valorTotal, 0);
    return { etapas, quantidade: lista.length, valorTotal: lista.reduce((s, o) => s + o.valor, 0), valorEmNegociacao: emNegociacao };
  }

  const fetchFn: typeof fetch = async (entrada, init) => {
    const url = new URL(String(entrada));
    const metodo = init?.method ?? 'GET';
    const corpo = init?.body ? (JSON.parse(String(init.body)) as Record<string, unknown>) : undefined;
    chamadas.push({ metodo, url, headers: (init?.headers ?? {}) as Record<string, string>, corpo });
    const partes = url.pathname.split('/').filter(Boolean);

    if (partes.at(-1) === 'clientes' && metodo === 'GET') return json(200, { itens: clientes });

    const base = partes.indexOf('oportunidades');
    if (base < 0) return json(404);
    const idTexto = partes[base + 1];

    if (!idTexto && metodo === 'GET') {
      const clienteId = url.searchParams.get('clienteId');
      const responsavel = url.searchParams.get('responsavelId');
      const filtradas = oportunidades.filter((o) => (!clienteId || o.clienteId === clienteId) && (!responsavel || responsavel.split(',').includes(o.responsavelId)));
      return json(200, montarFunil(filtradas));
    }
    if (!idTexto && metodo === 'POST') {
      const c = corpo as { clienteId: string; titulo: string; valor: number; responsavelId: string; dataPrevista?: string };
      if (!c.titulo || c.titulo.length < 3) return erro(400, 'VALIDACAO', 'Dados inválidos', ['titulo: deve ter entre 3 e 160 caracteres']);
      return json(201, nova({ clienteId: c.clienteId, titulo: c.titulo, valor: c.valor, responsavelId: c.responsavelId, dataPrevista: c.dataPrevista ?? null }));
    }

    const o = oportunidades.find((x) => x.id === Number(idTexto));
    if (!o) return erro(404, 'NAO_ENCONTRADO', `Oportunidade ${idTexto} não encontrada`);
    if (metodo === 'GET') return json(200, o);
    if (metodo === 'PUT') {
      if (o.etapa === 'Fechado') return erro(400, 'MOVIMENTACAO_INVALIDA', 'Oportunidade fechada não pode ser alterada; reabra antes de editar');
      return json(200, Object.assign(o, corpo));
    }
    if (metodo === 'DELETE') {
      if (o.resultado === 'ganho') return erro(400, 'MOVIMENTACAO_INVALIDA', 'Oportunidade fechada como ganha não pode ser excluída');
      oportunidades.splice(oportunidades.indexOf(o), 1);
      return json(204);
    }
    if (metodo === 'PATCH') {
      const m = corpo as { etapa: Etapa; resultado?: Resultado; motivoPerda?: MotivoPerda; reabrir?: boolean };
      if (o.etapa === 'Fechado' && !m.reabrir) return erro(400, 'MOVIMENTACAO_INVALIDA', 'Oportunidade fechada só sai do Fechado com reabrir = true');
      if (m.etapa === 'Fechado') {
        if (!m.resultado) return erro(400, 'VALIDACAO', 'Dados inválidos', ['resultado: obrigatório ao fechar (ganho ou perdido)']);
        if (m.resultado === 'perdido' && !m.motivoPerda) return erro(400, 'VALIDACAO', 'Dados inválidos', ['motivoPerda: obrigatório ao perder']);
        return json(200, Object.assign(o, { etapa: 'Fechado', resultado: m.resultado, motivoPerda: m.motivoPerda ?? null }));
      }
      return json(200, Object.assign(o, { etapa: m.etapa, resultado: null, motivoPerda: null }));
    }
    return json(405);
  };

  return { clientes, oportunidades, chamadas, nova, fetchFn };
}
