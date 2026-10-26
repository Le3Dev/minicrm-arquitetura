import type { Usuario } from './tipos';

/**
 * Usuários da massa inicial do Mini CRM (api/src/data/mock-data.json do projeto original).
 * O shell pode passar outra lista pela prop `usuarios`.
 */
export const USUARIOS_PADRAO: Usuario[] = [
  { id: 'u3', nome: 'Rui Alves' },
  { id: 'u1', nome: 'Ana Costa' },
  { id: 'u6', nome: 'Caio Bezerra' },
  { id: 'u2', nome: 'Pedro Dias' },
  { id: 'u5', nome: 'Larissa Ferraz' },
];
