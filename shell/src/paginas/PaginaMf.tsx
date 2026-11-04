import { Suspense, type ComponentType, type LazyExoticComponent } from 'react';
import type { Config } from '../config';
import { ErrorBoundary } from '../componentes/ErrorBoundary';
import type { Usuario } from '../usuarios';

interface PropsMf {
  apiUrl: string;
  apiKey?: string;
  usuarioId?: string;
  usuarios?: { id: string; nome: string }[];
}

interface Props {
  nome: string;
  Remote: LazyExoticComponent<ComponentType<PropsMf>>;
  config: Config;
  usuarioId: string;
  usuarios: Usuario[];
}

/**
 * Monta um microfrontend remoto com Suspense e error boundary: se ele cair, o resto do Shell segue.
 * A `key` garante que o erro de um MF não "gruda" ao navegar para o outro.
 */
export default function PaginaMf({ nome, Remote, config, usuarioId, usuarios }: Props) {
  return (
    <ErrorBoundary key={nome} nome={nome}>
      <Suspense fallback={<p className="sh-vazio">Carregando {nome}…</p>}>
        <Remote apiUrl={config.apiUrl} apiKey={config.apiKey} usuarioId={usuarioId} usuarios={usuarios.map(({ id, nome }) => ({ id, nome }))} />
      </Suspense>
    </ErrorBoundary>
  );
}
