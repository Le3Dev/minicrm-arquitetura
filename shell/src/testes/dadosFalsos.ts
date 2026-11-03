import type { DadosAgregados } from '../api/dashboardApi';

export function dadosDe(usuario: { id: string; nome: string; perfil: string }, escopo: string[], extra: Partial<DadosAgregados> = {}): DadosAgregados {
  return {
    usuario,
    escopo,
    clientes: { total: 2, itens: [] },
    funil: [],
    indicadores: {
      clientes: { total: 28, porStatus: { Prospect: 10, Ativo: 15, Inativo: 3 } },
      oportunidades: {
        total: 12,
        valorEmNegociacao: 401000,
        valorGanho: 178000,
        ganhas: 4,
        perdidas: 4,
        taxaConversao: 0.5,
        porEtapa: [
          { etapa: 'Prospecção', quantidade: 5, valorTotal: 215000 },
          { etapa: 'Fechado', quantidade: 8, valorTotal: 178000 },
        ],
        motivosPerda: { Preço: 3, 'Sem verba': 1 },
      },
      ranking: [
        { responsavelId: 'u2', valorGanho: 120000, oportunidadesGanhas: 3, oportunidadesPerdidas: 1 },
        { responsavelId: 'u5', valorGanho: 58000, oportunidadesGanhas: 1, oportunidadesPerdidas: 3 },
      ],
    },
    falhas: [],
    ...extra,
  };
}
