import { describe, expect, it } from 'vitest';
import { ErroApi, OportunidadesApi } from './oportunidadesApi';

describe('OportunidadesApi', () => {
  it('monta a URL do funil sem parâmetros vazios e com o usuário', async () => {
    const urls: string[] = [];
    const api = new OportunidadesApi({
      apiUrl: 'https://gw.exemplo/prod/',
      usuarioId: 'u1',
      fetchFn: async (u) => {
        urls.push(String(u));
        return new Response(JSON.stringify({ etapas: [] }), { status: 200 });
      },
    });

    await api.funil({ clienteId: 'c1', responsavelId: '' });

    expect(urls[0]).toBe('https://gw.exemplo/prod/oportunidades?clienteId=c1&usuarioId=u1');
  });

  it('usa os verbos certos para mover, editar e excluir', async () => {
    const chamadas: string[] = [];
    const api = new OportunidadesApi({
      apiUrl: 'https://gw',
      fetchFn: async (u, init) => {
        chamadas.push(`${init?.method} ${new URL(String(u)).pathname}`);
        return new Response(init?.method === 'DELETE' ? null : '{}', { status: init?.method === 'DELETE' ? 204 : 200 });
      },
    });

    await api.mover(7, { etapa: 'Fechado', resultado: 'ganho' });
    await api.atualizar(7, { titulo: 'Novo', valor: 10, responsavelId: 'u2' });
    await expect(api.excluir(7)).resolves.toBeUndefined();

    expect(chamadas).toEqual(['PATCH /oportunidades/7', 'PUT /oportunidades/7', 'DELETE /oportunidades/7']);
  });

  it('transforma erro do servidor em ErroApi com a lista de erros', async () => {
    const api = new OportunidadesApi({
      apiUrl: 'https://gw',
      fetchFn: async () =>
        new Response(JSON.stringify({ status: 400, codigo: 'VALIDACAO', erro: 'Dados inválidos', erros: ['motivoPerda: obrigatório ao perder'] }), { status: 400 }),
    });

    await expect(api.mover(1, { etapa: 'Fechado', resultado: 'perdido' })).rejects.toMatchObject({
      status: 400,
      codigo: 'VALIDACAO',
      erros: ['motivoPerda: obrigatório ao perder'],
    });
  });

  it('trata falha de rede e API key recusada', async () => {
    const fora = new OportunidadesApi({ apiUrl: 'https://gw', fetchFn: async () => Promise.reject(new TypeError('fetch failed')) });
    await expect(fora.excluir(1)).rejects.toBeInstanceOf(ErroApi);

    const negado = new OportunidadesApi({ apiUrl: 'https://gw', fetchFn: async () => new Response('{"message":"Forbidden"}', { status: 403 }) });
    await expect(negado.funil({ clienteId: '', responsavelId: '' })).rejects.toThrow('Acesso negado pelo API Gateway');
  });
});
