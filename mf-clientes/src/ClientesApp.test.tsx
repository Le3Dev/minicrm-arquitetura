import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ClientesApp from './ClientesApp';
import { criarServidorFalso } from './testes/servidorFalso';

describe('ClientesApp (microfrontend)', () => {
  let servidor: ReturnType<typeof criarServidorFalso>;

  beforeEach(() => {
    servidor = criarServidorFalso();
    servidor.novo({ nome: 'Mariana Souza', empresa: 'Acme Ltda', email: 'mariana@acme.com', status: 'Ativo' });
    servidor.novo({ nome: 'João Pereira', empresa: 'Café Brasil', email: 'joao@cafe.com' });
    vi.stubGlobal('fetch', servidor.fetchFn);
  });

  afterEach(() => vi.unstubAllGlobals());

  const renderizar = () => render(<ClientesApp apiUrl="https://gateway.exemplo/prod" apiKey="chave-123" usuarioId="u2" />);

  it('lista os clientes chamando o gateway com API key e usuário', async () => {
    renderizar();

    expect(await screen.findByText('Mariana Souza')).toBeInTheDocument();
    expect(screen.getByText('João Pereira')).toBeInTheDocument();
    const chamada = servidor.chamadas[0];
    expect(chamada.url.origin + chamada.url.pathname).toBe('https://gateway.exemplo/prod/clientes');
    expect(chamada.url.searchParams.get('usuarioId')).toBe('u2');
    expect(chamada.headers['x-api-key']).toBe('chave-123');
  });

  it('filtra por status', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('Mariana Souza');

    await usuario.click(within(screen.getByRole('group', { name: 'Filtrar por status' })).getByRole('button', { name: 'Ativo' }));

    await waitFor(() => expect(screen.queryByText('João Pereira')).not.toBeInTheDocument());
    expect(servidor.chamadas.at(-1)!.url.searchParams.get('status')).toBe('Ativo');
  });

  it('cadastra um cliente e mostra os erros de validação antes de enviar', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('Mariana Souza');

    await usuario.click(screen.getByRole('button', { name: '+ Novo cliente' }));
    const dialogo = screen.getByRole('dialog', { name: 'Novo cliente' });
    await usuario.click(within(dialogo).getByRole('button', { name: 'Cadastrar cliente' }));
    expect(within(dialogo).getByRole('alert')).toHaveTextContent('nome: deve ter ao menos 3 caracteres');

    await usuario.type(within(dialogo).getByLabelText('Nome'), 'Bruno Alves');
    await usuario.type(within(dialogo).getByLabelText('Empresa'), 'TechSul');
    await usuario.type(within(dialogo).getByLabelText('E-mail'), 'bruno@techsul.com');
    await usuario.click(within(dialogo).getByRole('button', { name: 'Cadastrar cliente' }));

    expect(await screen.findByText('Bruno Alves')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('Bruno Alves cadastrado como Prospect.');
    const post = servidor.chamadas.find((c) => c.metodo === 'POST')!;
    expect(post.corpo).toMatchObject({ nome: 'Bruno Alves', responsavelId: 'u3' });
  });

  it('exclui com confirmação e o cliente some da lista', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('João Pereira');

    await usuario.click(screen.getByRole('button', { name: 'Excluir João Pereira' }));
    await usuario.click(within(screen.getByRole('dialog', { name: 'Excluir cliente' })).getByRole('button', { name: 'Excluir' }));

    await waitFor(() => expect(screen.queryByText('João Pereira')).not.toBeInTheDocument());
    expect(servidor.chamadas.some((c) => c.metodo === 'DELETE' && c.url.pathname.endsWith('/clientes/c2'))).toBe(true);
  });

  it('abre o detalhe e registra uma interação', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await usuario.click(await screen.findByRole('button', { name: 'Ver detalhes de Mariana Souza' }));

    const dialogo = await screen.findByRole('dialog', { name: 'Mariana Souza' });
    expect(await within(dialogo).findByText('Nenhuma interação registrada ainda.')).toBeInTheDocument();
    await usuario.click(within(dialogo).getByRole('radio', { name: /Reunião/ }));
    await usuario.type(within(dialogo).getByLabelText('Observação'), 'Apresentei a proposta');
    await usuario.click(within(dialogo).getByRole('button', { name: 'Registrar interação' }));

    expect(await within(dialogo).findByText('Apresentei a proposta')).toBeInTheDocument();
    const post = servidor.chamadas.find((c) => c.url.pathname.endsWith('/interacoes'))!;
    expect(post.corpo).toEqual({ tipo: 'Reunião', obs: 'Apresentei a proposta', autorId: 'u2' });
  });

  it('mostra mensagem clara quando o gateway recusa a API key', async () => {
    vi.stubGlobal('fetch', async () => new Response(JSON.stringify({ message: 'Forbidden' }), { status: 403 }));
    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado pelo API Gateway');
  });
});
