export type Perfil = 'Admin' | 'Gerente' | 'Vendedor';

export interface Usuario {
  id: string;
  nome: string;
  perfil: Perfil;
}

/** Os 8 usuários da massa do Mini-CRM. O escopo de cada um (RF15) é calculado pelo BFF. */
export const USUARIOS: Usuario[] = [
  { id: 'u3', nome: 'Rui Alves', perfil: 'Admin' },
  { id: 'u1', nome: 'Ana Costa', perfil: 'Gerente' },
  { id: 'u6', nome: 'Caio Bezerra', perfil: 'Gerente' },
  { id: 'u2', nome: 'Pedro Dias', perfil: 'Vendedor' },
  { id: 'u5', nome: 'Larissa Ferraz', perfil: 'Vendedor' },
  { id: 'u4', nome: 'Bia Melo', perfil: 'Vendedor' },
  { id: 'u7', nome: 'Otavio Pinto', perfil: 'Vendedor' },
  { id: 'u8', nome: 'Marina Rocha', perfil: 'Vendedor' },
];

export const USUARIO_PADRAO = 'u3';
