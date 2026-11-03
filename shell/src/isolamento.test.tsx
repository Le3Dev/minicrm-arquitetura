import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { dadosDe } from './testes/dadosFalsos';

// O MF Oportunidades "cai": o resto do Shell precisa continuar funcionando.
vi.mock('mfOportunidades/OportunidadesApp', () => ({
  default: () => {
    throw new Error('remoteEntry indisponível');
  },
}));

describe('Shell: um MF que cai não derruba o resto', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    vi.stubGlobal('fetch', async () =>
      new Response(JSON.stringify(dadosDe({ id: 'u3', nome: 'Rui Alves', perfil: 'Admin' }, ['u3'])), { status: 200, headers: { 'content-type': 'application/json' } }),
    );
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('mostra o erro só na área do MF e mantém menu, seletor e as outras telas', async () => {
    const usuario = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/funil']}>
        <App config={{ apiUrl: 'https://gw' }} />
      </MemoryRouter>,
    );

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar o MF Oportunidades.');
    expect(screen.getByLabelText('Entrar como')).toBeInTheDocument();

    await usuario.click(screen.getByRole('link', { name: 'Clientes' }));
    expect(await screen.findByTestId('mf-clientes')).toBeInTheDocument();

    await usuario.click(screen.getByRole('link', { name: 'Dashboard' }));
    expect(await screen.findByLabelText('Ranking de vendedores')).toBeInTheDocument();
  });
});
