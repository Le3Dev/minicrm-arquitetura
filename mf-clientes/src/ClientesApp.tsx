import { useCallback, useEffect, useMemo, useState } from 'react';
import { ClientesApi } from './api/clientesApi';
import { BadgeStatus } from './componentes/BadgeStatus';
import { DetalheCliente } from './componentes/DetalheCliente';
import { FormCliente } from './componentes/FormCliente';
import { Modal } from './componentes/Modal';
import { Paginacao } from './componentes/Paginacao';
import './estilos/mf-clientes.css';
import { formatarData, iniciais } from './formato';
import type { Cliente, FiltrosClientes, NovoCliente, RespostaClientes, StatusCliente, Usuario } from './tipos';
import { USUARIOS_PADRAO } from './usuarios';

export interface ClientesAppProps {
  /** URL do API Gateway (produção) ou do BFF (desenvolvimento). */
  apiUrl: string;
  /** API key do AWS API Gateway (header x-api-key). */
  apiKey?: string;
  /** Usuário logado no shell; vai para o BFF calcular o escopo (RF15) e como autor das interações. */
  usuarioId?: string;
  /** Usuários para o seletor de responsável; padrão = massa inicial do Mini CRM. */
  usuarios?: Usuario[];
}

const FILTROS_INICIAIS: FiltrosClientes = { q: '', status: '', responsavelId: '', pagina: 1 };
const STATUS: ('' | StatusCliente)[] = ['', 'Prospect', 'Ativo'];

/**
 * Microfrontend de Clientes do Mini CRM (exposto como "mfClientes/ClientesApp").
 * Fala só com o BFF, através do API Gateway: nunca com o MS Clientes direto.
 */
export default function ClientesApp({ apiUrl, apiKey, usuarioId = 'u3', usuarios = USUARIOS_PADRAO }: ClientesAppProps) {
  const api = useMemo(() => new ClientesApi({ apiUrl, apiKey, usuarioId }), [apiUrl, apiKey, usuarioId]);
  const nomeDe = useCallback((id: string) => usuarios.find((u) => u.id === id)?.nome ?? id, [usuarios]);

  const [filtros, setFiltros] = useState<FiltrosClientes>(FILTROS_INICIAIS);
  const [busca, setBusca] = useState('');
  const [resposta, setResposta] = useState<RespostaClientes | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [versao, setVersao] = useState(0);

  const [novoAberto, setNovoAberto] = useState(false);
  const [editando, setEditando] = useState<Cliente | null>(null);
  const [excluindo, setExcluindo] = useState<Cliente | null>(null);
  const [detalheId, setDetalheId] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const recarregar = useCallback(() => setVersao((v) => v + 1), []);

  // Busca só dispara quando o usuário para de digitar (RF04).
  useEffect(() => {
    const t = setTimeout(() => setFiltros((f) => (f.q === busca ? f : { ...f, q: busca, pagina: 1 })), 350);
    return () => clearTimeout(t);
  }, [busca]);

  useEffect(() => {
    const controle = new AbortController();
    setCarregando(true);
    api
      .pesquisar(filtros, controle.signal)
      .then((r) => {
        setResposta(r);
        setErro(null);
      })
      .catch((e: Error) => e.name !== 'AbortError' && setErro(e.message))
      .finally(() => !controle.signal.aborted && setCarregando(false));
    return () => controle.abort();
  }, [api, filtros, versao]);

  useEffect(() => {
    if (!aviso) return;
    const t = setTimeout(() => setAviso(null), 3500);
    return () => clearTimeout(t);
  }, [aviso]);

  const mudarFiltro = (mudanca: Partial<FiltrosClientes>) => setFiltros((f) => ({ ...f, ...mudanca, pagina: mudanca.pagina ?? 1 }));

  async function cadastrar(dados: NovoCliente) {
    const criado = await api.cadastrar(dados);
    setNovoAberto(false);
    setAviso(`${criado.nome} cadastrado como Prospect.`);
    recarregar();
  }

  async function salvarEdicao(dados: NovoCliente) {
    if (!editando) return;
    await api.alterar(editando.id, dados);
    setEditando(null);
    setAviso('Alterações salvas.');
    recarregar();
  }

  async function confirmarExclusao() {
    if (!excluindo) return;
    try {
      await api.excluir(excluindo.id);
      setAviso(`${excluindo.nome} excluído (ficou Inativo e saiu da lista).`);
      recarregar();
    } catch (e) {
      setAviso((e as Error).message);
    } finally {
      setExcluindo(null);
    }
  }

  const clienteDoDetalhe = resposta?.itens.find((c) => c.id === detalheId);

  return (
    <section className="mfc" aria-label="Clientes">
      <header className="mfc-topo">
        <div>
          <h1>Clientes</h1>
          <p className="mfc-muted">Carteira, histórico de interações e status de cada cliente.</p>
        </div>
        <button type="button" className="mfc-btn mfc-btn--primario" onClick={() => setNovoAberto(true)}>
          + Novo cliente
        </button>
      </header>

      <div className="mfc-filtros">
        <input
          type="search"
          aria-label="Buscar por nome ou empresa"
          placeholder="Buscar por nome ou empresa…"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <div className="mfc-segmentos" role="group" aria-label="Filtrar por status">
          {STATUS.map((s) => (
            <button
              key={s || 'todos'}
              type="button"
              aria-pressed={filtros.status === s}
              className={`mfc-segmento${filtros.status === s ? ' mfc-segmento--ativo' : ''}`}
              onClick={() => mudarFiltro({ status: s })}
            >
              {s || 'Todos'}
            </button>
          ))}
        </div>
        <select aria-label="Filtrar por responsável" value={filtros.responsavelId} onChange={(e) => mudarFiltro({ responsavelId: e.target.value })}>
          <option value="">Todos os responsáveis</option>
          {usuarios.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nome}
            </option>
          ))}
        </select>
      </div>

      {erro && (
        <div className="mfc-erro-caixa" role="alert">
          {erro}{' '}
          <button type="button" className="mfc-btn" onClick={recarregar}>
            Tentar de novo
          </button>
        </div>
      )}

      <div className="mfc-cartao" aria-busy={carregando}>
        <table className="mfc-tabela">
          <thead>
            <tr>
              <th>Cliente</th>
              <th>Status</th>
              <th>Responsável</th>
              <th>Interações</th>
              <th>Desde</th>
              <th aria-label="Ações" />
            </tr>
          </thead>
          <tbody>
            {resposta?.itens.map((c) => (
              <tr key={c.id}>
                <td>
                  <button type="button" className="mfc-cliente" onClick={() => setDetalheId(c.id)} aria-label={`Ver detalhes de ${c.nome}`}>
                    <span className="mfc-avatar" aria-hidden="true">{iniciais(c.nome)}</span>
                    <span>
                      <strong>{c.nome}</strong>
                      <small>{c.empresa} · {c.email}</small>
                    </span>
                  </button>
                </td>
                <td><BadgeStatus status={c.status} /></td>
                <td>{nomeDe(c.responsavelId)}</td>
                <td>{c.totalInteracoes}</td>
                <td>{formatarData(c.criadoEm)}</td>
                <td className="mfc-tabela__acoes">
                  <button type="button" className="mfc-btn mfc-btn--fantasma" onClick={() => setEditando(c)} aria-label={`Editar ${c.nome}`}>
                    Editar
                  </button>
                  <button type="button" className="mfc-btn mfc-btn--fantasma mfc-btn--perigo" onClick={() => setExcluindo(c)} aria-label={`Excluir ${c.nome}`}>
                    Excluir
                  </button>
                </td>
              </tr>
            ))}
            {resposta && resposta.itens.length === 0 && !carregando && (
              <tr>
                <td colSpan={6} className="mfc-vazio">Nenhum cliente encontrado com esses filtros.</td>
              </tr>
            )}
            {!resposta && carregando && (
              <tr>
                <td colSpan={6} className="mfc-vazio">Carregando…</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {resposta && (
        <Paginacao
          pagina={resposta.paginacao.pagina}
          totalPaginas={resposta.paginacao.totalPaginas}
          total={resposta.paginacao.total}
          aoMudar={(pagina) => mudarFiltro({ pagina })}
        />
      )}

      <Modal titulo="Novo cliente" aberto={novoAberto} aoFechar={() => setNovoAberto(false)}>
        <FormCliente usuarios={usuarios} aoSalvar={cadastrar} aoCancelar={() => setNovoAberto(false)} />
      </Modal>

      <Modal titulo={`Editar ${editando?.nome ?? ''}`} aberto={!!editando} aoFechar={() => setEditando(null)}>
        {editando && <FormCliente inicial={editando} usuarios={usuarios} aoSalvar={salvarEdicao} aoCancelar={() => setEditando(null)} />}
      </Modal>

      <Modal titulo="Excluir cliente" aberto={!!excluindo} aoFechar={() => setExcluindo(null)} largura={420}>
        <p>
          <strong>{excluindo?.nome}</strong> vai ficar <strong>Inativo</strong> e sair das listagens. O histórico é preservado.
        </p>
        <footer className="mfc-form__acoes">
          <button type="button" className="mfc-btn" onClick={() => setExcluindo(null)}>Cancelar</button>
          <button type="button" className="mfc-btn mfc-btn--perigo-cheio" onClick={confirmarExclusao}>Excluir</button>
        </footer>
      </Modal>

      <Modal titulo={clienteDoDetalhe?.nome ?? 'Cliente'} aberto={!!detalheId} aoFechar={() => setDetalheId(null)} largura={640}>
        {detalheId && (
          <DetalheCliente api={api} clienteId={detalheId} usuarioId={usuarioId} usuarios={usuarios} nomeDe={nomeDe} aoAlterar={recarregar} />
        )}
      </Modal>

      {aviso && (
        <div className="mfc-aviso" role="status">
          {aviso}
        </div>
      )}
    </section>
  );
}
