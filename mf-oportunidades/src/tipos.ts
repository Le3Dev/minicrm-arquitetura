/** Contratos das respostas que chegam pelo BFF (vindas do MS Oportunidades e do MS Clientes). */

export type Etapa = 'Prospecção' | 'Contato' | 'Proposta' | 'Fechado';
export type Resultado = 'ganho' | 'perdido';
export type MotivoPerda = 'Preço' | 'Concorrente' | 'Sem verba' | 'Sem resposta';

export interface Oportunidade {
  id: number;
  clienteId: string;
  titulo: string;
  etapa: Etapa;
  resultado: Resultado | null;
  motivoPerda: MotivoPerda | null;
  valor: number;
  dataPrevista: string | null;
  responsavelId: string;
  criadaEm: string;
  atualizada: string;
}

export interface ColunaFunil {
  etapa: Etapa;
  quantidade: number;
  valorTotal: number;
  itens: Oportunidade[];
}

export interface RespostaFunil {
  etapas: ColunaFunil[];
  quantidade: number;
  valorTotal: number;
  valorEmNegociacao: number;
}

export interface ClienteResumo {
  id: string;
  nome: string;
  empresa: string;
  status: string;
}

export interface RespostaClientes {
  itens: ClienteResumo[];
}

export interface NovaOportunidade {
  clienteId: string;
  titulo: string;
  valor: number;
  dataPrevista?: string;
  responsavelId: string;
}

export interface EdicaoOportunidade {
  titulo: string;
  valor: number;
  dataPrevista?: string;
  responsavelId: string;
}

export interface Movimentacao {
  etapa: Etapa;
  resultado?: Resultado;
  motivoPerda?: MotivoPerda;
  reabrir?: boolean;
}

export interface Usuario {
  id: string;
  nome: string;
}

export interface FiltrosFunil {
  clienteId: string;
  responsavelId: string;
}

export const ETAPAS: Etapa[] = ['Prospecção', 'Contato', 'Proposta', 'Fechado'];
export const ETAPAS_ABERTAS: Etapa[] = ['Prospecção', 'Contato', 'Proposta'];
/** RF29: lista fixa de motivos de perda. */
export const MOTIVOS: MotivoPerda[] = ['Preço', 'Concorrente', 'Sem verba', 'Sem resposta'];
