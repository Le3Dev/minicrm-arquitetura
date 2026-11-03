import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';
import { dadosDe } from './testes/dadosFalsos';

const config = { apiUrl: 'https://gateway.exemplo/prod', apiKey: 'chave-123' };

describe('Shell', () => {
  let chamadas: { url: URL; headers: Record<string, string> }[];

  beforeEach(() => {
    sessionStorage.clear();
    chamadas = [];
    vi.stubGlobal('fetch', async (entrada: string, init?: RequestInit) => {
      const url = new URL(entrada);
      chamadas.push({ url, headers: (init?.headers ?? {}) as Record<string, string> });
      const id = url.searchParams.get('usuarioId');
      const corpo =
        id === 'u1'
          ? dadosDe({ id: 'u1', nome: 'Ana Costa', perfil: 'Gerente' }, ['u1', 'u2', 'u5', 'u4'])
          : dadosDe({ id: 'u3', nome: 'Rui Alves', perfil: 'Admin' }, ['u1', 'u2', 'u3', 'u4', 'u5', 'u6', 'u7', 'u8']);
      return new Response(JSON.stringify(corpo), { status: 200, headers: { 'content-type': 'application/json' } });
    });
  });

  afterEach(() => vi.unstubAllGlobals());

  const renderizar = (rota = '/') =>
    render(
      <MemoryRouter initialEntries={[rota]}>
        <App config={config} />
      </MemoryRouter>,
    );

  it('mostra o dashboard do Admin chamando /aggregated-data com API key e usuarioId', async () => {
    renderizar();

    expect(await screen.findByText(/Rui Alves \(Admin\) · enxerga 8 responsáveis/)).toBeInTheDocument();
    expect(chamadas[0].url.origin + chamadas[0].url.pathname).toBe('https://gateway.exemplo/prod/aggregated-data');
    expect(chamadas[0].url.searchParams.get('usuarioId')).toBe('u3');
    expect(chamadas[0].headers['x-api-key']).toBe('chave-123');
    expect(within(screen.getByLabelText('Clientes')).getByText('28')).toBeInTheDocument();
    expect(screen.getByLabelText('Valor em negociação')).toHaveTextContent(/401\.000,00/);
    expect(screen.getByLabelText('Taxa de conversão')).toHaveTextContent('50%');
    const ranking = screen.getByLabelText('Ranking de vendedores');
    expect(within(ranking).getByText('Pedro Dias')).toBeInTheDocument();
    expect(within(ranking).getByText('Larissa Ferraz')).toBeInTheDocument();
  });

  it('trocar o usuário recarrega o dashboard com o novo escopo (RF15)', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText(/Rui Alves/);

    await usuario.selectOptions(screen.getByLabelText('Entrar como'), 'u1');

    expect(await screen.findByText(/Ana Costa \(Gerente\) · enxerga 4 responsáveis/)).toBeInTheDocument();
    expect(chamadas.at(-1)!.url.searchParams.get('usuarioId')).toBe('u1');
  });

  it('o seletor tem os 8 usuários da massa', () => {
    renderizar();
    const opcoes = within(screen.getByLabelText('Entrar como')).getAllByRole('option');
    expect(opcoes.map((o) => o.textContent)).toEqual([
      'Rui Alves (Admin)', 'Ana Costa (Gerente)', 'Caio Bezerra (Gerente)', 'Pedro Dias (Vendedor)',
      'Larissa Ferraz (Vendedor)', 'Bia Melo (Vendedor)', 'Otavio Pinto (Vendedor)', 'Marina Rocha (Vendedor)',
    ]);
  });

  it('monta o MF Clientes e o MF Oportunidades com as props e o usuário escolhido', async () => {
    const usuario = userEvent.setup();
    renderizar('/clientes');

    expect(await screen.findByTestId('mf-clientes')).toHaveTextContent('clientes api=https://gateway.exemplo/prod key=chave-123 usuario=u3');

    await usuario.selectOptions(screen.getByLabelText('Entrar como'), 'u2');
    await waitFor(() => expect(screen.getByTestId('mf-clientes')).toHaveTextContent('usuario=u2'));

    await usuario.click(screen.getByRole('link', { name: 'Funil' }));
    expect(await screen.findByTestId('mf-oportunidades')).toHaveTextContent('oportunidades api=https://gateway.exemplo/prod key=chave-123 usuario=u2');
  });

  it('avisa quando um serviço do BFF falhou, sem esconder o resto', async () => {
    vi.stubGlobal('fetch', async () =>
      new Response(
        JSON.stringify(dadosDe({ id: 'u3', nome: 'Rui Alves', perfil: 'Admin' }, ['u3'], { falhas: [{ servico: 'ms-oportunidades', erro: 'timeout' }] })),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    );
    renderizar();

    expect(await screen.findByRole('status')).toHaveTextContent('ms-oportunidades (timeout)');
    expect(screen.getByLabelText('Clientes')).toBeInTheDocument();
  });

  it('mostra mensagem clara quando o gateway recusa a API key', async () => {
    vi.stubGlobal('fetch', async () => new Response(JSON.stringify({ message: 'Forbidden' }), { status: 403 }));
    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado pelo API Gateway');
  });

  it('mostra o erro do BFF e permite tentar de novo', async () => {
    const usuario = userEvent.setup();
    vi.stubGlobal('fetch', async () =>
      new Response(JSON.stringify({ status: 403, codigo: 'USUARIO_DESCONHECIDO', erro: 'Usuário desconhecido: u99', erros: [] }), {
        status: 403,
        headers: { 'content-type': 'application/json' },
      }),
    );
    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent('Usuário desconhecido: u99');
    expect(within(screen.getByRole('alert')).getByRole('button', { name: 'Tentar de novo' })).toBeInTheDocument();
    await usuario.click(within(screen.getByRole('alert')).getByRole('button', { name: 'Tentar de novo' }));
  });
});
