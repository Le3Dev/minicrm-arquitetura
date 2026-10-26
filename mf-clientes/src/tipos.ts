/** Contratos das respostas que chegam pelo BFF (vindas do MS Clientes). */

export type StatusCliente = 'Prospect' | 'Ativo' | 'Inativo';
export type TipoInteracao = 'Ligação' | 'E-mail' | 'Reunião';

export interface Cliente {
  id: string;
  nome: string;
  empresa: string;
  email: string;
  telefone: string;
  status: StatusCliente;
  responsavelId: string;
  excluido: boolean;
  totalInteracoes: number;
  ultimaInteracaoEm: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Interacao {
  id: string;
  tipo: TipoInteracao;
  obs: string;
  data: string;
  autorId: string;
}

export interface RespostaClientes {
  itens: Cliente[];
  paginacao: { pagina: number; porPagina: number; total: number; totalPaginas: number };
  filtros: { q: string; status: string; responsaveis: string[] };
}

export interface ClienteDetalhe {
  cliente: Cliente;
  interacoes: Interacao[];
}

export interface NovoCliente {
  nome: string;
  empresa: string;
  email: string;
  telefone: string;
  responsavelId: string;
}

export interface Usuario {
  id: string;
  nome: string;
}

export interface FiltrosClientes {
  q: string;
  status: '' | StatusCliente;
  responsavelId: string;
  pagina: number;
}
