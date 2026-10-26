import { describe, expect, it } from 'vitest';
import { ClientesApi, ErroApi } from './clientesApi';

describe('ClientesApi', () => {
  it('monta a URL sem parâmetros vazios e sem barra dupla', async () => {
    const urls: string[] = [];
    const api = new ClientesApi({
      apiUrl: 'https://gw.exemplo/prod/',
      fetchFn: async (u) => {
        urls.push(String(u));
        return new Response(JSON.stringify({ itens: [] }), { status: 200 });
      },
    });

    await api.pesquisar({ q: 'acme', status: '', responsavelId: '', pagina: 2 });

    expect(urls[0]).toBe('https://gw.exemplo/prod/clientes?q=acme&pagina=2');
  });

  it('transforma erro do servidor em ErroApi com a lista de erros', async () => {
    const api = new ClientesApi({
      apiUrl: 'https://gw',
      fetchFn: async () =>
        new Response(JSON.stringify({ status: 400, codigo: 'VALIDACAO', erro: 'Cliente inválido', erros: ['email: formato inválido'] }), { status: 400 }),
    });

    await expect(api.cadastrar({ nome: 'X', empresa: 'Y', email: 'z', telefone: '', responsavelId: 'u2' })).rejects.toMatchObject({
      status: 400,
      codigo: 'VALIDACAO',
      erros: ['email: formato inválido'],
    });
  });

  it('trata falha de rede e resposta 204', async () => {
    const fora = new ClientesApi({ apiUrl: 'https://gw', fetchFn: async () => Promise.reject(new TypeError('fetch failed')) });
    await expect(fora.excluir('c1')).rejects.toBeInstanceOf(ErroApi);

    const ok = new ClientesApi({ apiUrl: 'https://gw', fetchFn: async () => new Response(null, { status: 204 }) });
    await expect(ok.excluir('c1')).resolves.toBeUndefined();
  });
});
