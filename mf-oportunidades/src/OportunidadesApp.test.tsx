import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import OportunidadesApp from './OportunidadesApp';
import { criarServidorFalso } from './testes/servidorFalso';

describe('OportunidadesApp (microfrontend)', () => {
  let servidor: ReturnType<typeof criarServidorFalso>;

  beforeEach(() => {
    servidor = criarServidorFalso();
    servidor.nova({ titulo: 'Plano anual', clienteId: 'c1', valor: 18500, etapa: 'Prospecção' });
    servidor.nova({ titulo: 'Licenças extras', clienteId: 'c2', valor: 7000, etapa: 'Proposta', responsavelId: 'u5' });
    vi.stubGlobal('fetch', servidor.fetchFn);
  });

  afterEach(() => vi.unstubAllGlobals());

  const renderizar = () => render(<OportunidadesApp apiUrl="https://gateway.exemplo/prod" apiKey="chave-123" usuarioId="u2" />);
  const coluna = (etapa: string) => screen.getByRole('region', { name: `Etapa ${etapa}` });

  it('mostra as 4 colunas com quantidade e soma, chamando o gateway com API key e usuário', async () => {
    renderizar();

    expect(await screen.findByText('Plano anual')).toBeInTheDocument();
    expect(['Prospecção', 'Contato', 'Proposta', 'Fechado'].map((e) => within(coluna(e)).getByRole('heading').textContent)).toEqual([
      'Prospecção', 'Contato', 'Proposta', 'Fechado',
    ]);
    expect(within(coluna('Prospecção')).getByLabelText('1 oportunidades')).toBeInTheDocument();
    expect(coluna('Prospecção')).toHaveTextContent(/18\.500,00/);
    expect(coluna('Proposta')).toHaveTextContent(/7\.000,00/);
    expect(within(coluna('Contato')).getByText('Nenhuma oportunidade.')).toBeInTheDocument();
    expect(screen.getByText(/25\.500,00/)).toBeInTheDocument();
    const chamada = servidor.chamadas.find((c) => c.url.pathname.endsWith('/oportunidades'))!;
    expect(chamada.url.searchParams.get('usuarioId')).toBe('u2');
    expect(chamada.headers['x-api-key']).toBe('chave-123');
  });

  it('move uma oportunidade de coluna', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('Plano anual');

    await usuario.click(screen.getByRole('button', { name: 'Mover Plano anual para Contato' }));

    await waitFor(() => expect(within(coluna('Contato')).getByText('Plano anual')).toBeInTheDocument());
    expect(within(coluna('Prospecção')).queryByText('Plano anual')).not.toBeInTheDocument();
    const patch = servidor.chamadas.find((c) => c.metodo === 'PATCH')!;
    expect(patch.corpo).toEqual({ etapa: 'Contato' });
  });

  it('fechar como perdido sem motivo mostra o erro e não envia', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await usuario.click(await screen.findByRole('button', { name: 'Fechar Plano anual' }));
    const dialogo = screen.getByRole('dialog', { name: 'Fechar oportunidade' });

    await usuario.click(within(dialogo).getByRole('radio', { name: 'Perdido' }));
    await usuario.click(within(dialogo).getByRole('button', { name: 'Fechar oportunidade' }));

    expect(within(dialogo).getByRole('alert')).toHaveTextContent('motivoPerda: obrigatório ao perder');
    expect(servidor.chamadas.some((c) => c.metodo === 'PATCH')).toBe(false);

    await usuario.selectOptions(within(dialogo).getByLabelText('Motivo da perda'), 'Preço');
    await usuario.click(within(dialogo).getByRole('button', { name: 'Fechar oportunidade' }));

    await waitFor(() => expect(within(coluna('Fechado')).getByText('Plano anual')).toBeInTheDocument());
    expect(within(coluna('Fechado')).getByText('Perdido')).toBeInTheDocument();
    expect(servidor.chamadas.find((c) => c.metodo === 'PATCH')!.corpo).toEqual({ etapa: 'Fechado', resultado: 'perdido', motivoPerda: 'Preço' });
  });

  it('fechar como ganho avisa que o cliente será ativado', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await usuario.click(await screen.findByRole('button', { name: 'Fechar Licenças extras' }));

    await usuario.click(within(screen.getByRole('dialog', { name: 'Fechar oportunidade' })).getByRole('button', { name: 'Fechar oportunidade' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Oportunidade ganha, o cliente será ativado.');
    expect(await within(coluna('Fechado')).findByText('Ganho')).toBeInTheDocument();
    expect(servidor.chamadas.find((c) => c.metodo === 'PATCH')!.corpo).toEqual({ etapa: 'Fechado', resultado: 'ganho' });
    expect(screen.queryByRole('button', { name: 'Excluir Licenças extras' })).not.toBeInTheDocument();
  });

  it('reabre uma fechada mandando reabrir: true', async () => {
    servidor.nova({ titulo: 'Contrato antigo', etapa: 'Fechado', resultado: 'perdido', motivoPerda: 'Sem verba' });
    const usuario = userEvent.setup();
    renderizar();

    await usuario.click(await screen.findByRole('button', { name: 'Reabrir Contrato antigo' }));

    await waitFor(() => expect(within(coluna('Proposta')).getByText('Contrato antigo')).toBeInTheDocument());
    expect(servidor.chamadas.find((c) => c.metodo === 'PATCH')!.corpo).toEqual({ etapa: 'Proposta', reabrir: true });
  });

  it('cria uma oportunidade escolhendo o cliente, validando antes de enviar', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('Plano anual');

    await usuario.click(screen.getByRole('button', { name: '+ Nova oportunidade' }));
    const dialogo = screen.getByRole('dialog', { name: 'Nova oportunidade' });
    await usuario.click(within(dialogo).getByRole('button', { name: 'Criar oportunidade' }));
    expect(within(dialogo).getByRole('alert')).toHaveTextContent('clienteId: escolha um cliente');
    expect(within(dialogo).getByRole('alert')).toHaveTextContent('valor: deve ser maior que zero');

    await usuario.selectOptions(within(dialogo).getByLabelText('Cliente'), 'c2');
    await usuario.type(within(dialogo).getByLabelText('Título'), 'Upgrade de plano');
    await usuario.type(within(dialogo).getByLabelText('Valor estimado (R$)'), '4200');
    await usuario.click(within(dialogo).getByRole('button', { name: 'Criar oportunidade' }));

    expect(await within(coluna('Prospecção')).findByText('Upgrade de plano')).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('Upgrade de plano criada em Prospecção.');
    expect(servidor.chamadas.find((c) => c.metodo === 'POST')!.corpo).toMatchObject({ clienteId: 'c2', titulo: 'Upgrade de plano', valor: 4200, responsavelId: 'u2' });
  });

  it('edita e exclui', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await usuario.click(await screen.findByRole('button', { name: 'Editar Plano anual' }));
    const edicao = screen.getByRole('dialog', { name: 'Editar Plano anual' });
    await usuario.clear(within(edicao).getByLabelText('Título'));
    await usuario.type(within(edicao).getByLabelText('Título'), 'Plano anual v2');
    await usuario.click(within(edicao).getByRole('button', { name: 'Salvar alterações' }));
    expect(await screen.findByText('Plano anual v2')).toBeInTheDocument();

    await usuario.click(screen.getByRole('button', { name: 'Excluir Plano anual v2' }));
    await usuario.click(within(screen.getByRole('dialog', { name: 'Excluir oportunidade' })).getByRole('button', { name: 'Excluir' }));

    await waitFor(() => expect(screen.queryByText('Plano anual v2')).not.toBeInTheDocument());
    expect(servidor.chamadas.some((c) => c.metodo === 'DELETE')).toBe(true);
  });

  it('filtra por responsável pedindo ao BFF', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await screen.findByText('Plano anual');

    await usuario.selectOptions(screen.getByLabelText('Filtrar por responsável'), 'u5');

    await waitFor(() => expect(screen.queryByText('Plano anual')).not.toBeInTheDocument());
    expect(screen.getByText('Licenças extras')).toBeInTheDocument();
    expect(servidor.chamadas.at(-1)!.url.searchParams.get('responsavelId')).toBe('u5');
  });

  it('mostra mensagem clara quando o gateway recusa a API key', async () => {
    vi.stubGlobal('fetch', async () => new Response(JSON.stringify({ message: 'Forbidden' }), { status: 403 }));
    renderizar();

    expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado pelo API Gateway');
  });
});
