import { useCallback, useEffect, useMemo, useState } from 'react';
import { OportunidadesApi } from './api/oportunidadesApi';
import { CartaoOportunidade } from './componentes/CartaoOportunidade';
import { ColunaFunil } from './componentes/ColunaFunil';
import { FormFechamento } from './componentes/FormFechamento';
import { FormOportunidade, type DadosFormulario } from './componentes/FormOportunidade';
import { Modal } from './componentes/Modal';
import './estilos/mf-oportunidades.css';
import { formatarValor } from './formato';
import type { ClienteResumo, Etapa, FiltrosFunil, MotivoPerda, Oportunidade, Resultado, RespostaFunil, Usuario } from './tipos';
import { USUARIOS_PADRAO } from './usuarios';

export interface OportunidadesAppProps {
  /** URL do API Gateway (produção) ou do BFF (desenvolvimento). */
  apiUrl: string;
  /** API key do AWS API Gateway (header x-api-key). */
  apiKey?: string;
  /** Usuário logado no shell; vai para o BFF calcular o escopo (RF15). */
  usuarioId?: string;
  /** Usuários para os seletores de responsável; padrão = massa inicial do Mini CRM. */
  usuarios?: Usuario[];
}

const FILTROS_INICIAIS: FiltrosFunil = { clienteId: '', responsavelId: '' };

/**
 * Microfrontend do Funil de Oportunidades (exposto como "mfOportunidades/OportunidadesApp").
 * Fala só com o BFF, através do API Gateway: nunca com o MS Oportunidades direto.
 */
export default function OportunidadesApp({ apiUrl, apiKey, usuarioId = 'u3', usuarios = USUARIOS_PADRAO }: OportunidadesAppProps) {
  const api = useMemo(() => new OportunidadesApi({ apiUrl, apiKey, usuarioId }), [apiUrl, apiKey, usuarioId]);
  const nomeUsuario = useCallback((id: string) => usuarios.find((u) => u.id === id)?.nome ?? id, [usuarios]);

  const [filtros, setFiltros] = useState<FiltrosFunil>(FILTROS_INICIAIS);
  const [funil, setFunil] = useState<RespostaFunil | null>(null);
  const [clientes, setClientes] = useState<ClienteResumo[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [versao, setVersao] = useState(0);

  const [novaAberta, setNovaAberta] = useState(false);
  const [editando, setEditando] = useState<Oportunidade | null>(null);
  const [fechando, setFechando] = useState<Oportunidade | null>(null);
  const [excluindo, setExcluindo] = useState<Oportunidade | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const recarregar = useCallback(() => setVersao((v) => v + 1), []);

  useEffect(() => {
    const controle = new AbortController();
    setCarregando(true);
    api
      .funil(filtros, controle.signal)
      .then((r) => {
        setFunil(r);
        setErro(null);
      })
      .catch((e: Error) => e.name !== 'AbortError' && setErro(e.message))
      .finally(() => !controle.signal.aborted && setCarregando(false));
    return () => controle.abort();
  }, [api, filtros, versao]);

  // A lista de clientes só alimenta o formulário e os nomes nos cartões; se falhar, o quadro continua funcionando.
  useEffect(() => {
    const controle = new AbortController();
    api
      .clientes(controle.signal)
      .then((r) => setClientes(r.itens))
      .catch(() => undefined);
    return () => controle.abort();
  }, [api]);

  useEffect(() => {
    if (!aviso) return;
    const t = setTimeout(() => setAviso(null), 4500);
    return () => clearTimeout(t);
  }, [aviso]);

  const nomeCliente = useCallback((id: string) => clientes.find((c) => c.id === id)?.nome ?? id, [clientes]);
  const todas = useMemo(() => funil?.etapas.flatMap((c) => c.itens) ?? [], [funil]);

  async function executar(acao: () => Promise<unknown>, sucesso: string) {
    try {
      await acao();
      setAviso(sucesso);
      recarregar();
    } catch (e) {
      setAviso((e as Error).message);
    }
  }

  const mover = (o: Oportunidade, destino: Etapa) => executar(() => api.mover(o.id, { etapa: destino }), `${o.titulo} movida para ${destino}.`);

  async function criar(d: DadosFormulario) {
    const criada = await api.criar({
      clienteId: d.clienteId,
      titulo: d.titulo.trim(),
      valor: Number(d.valor),
      dataPrevista: d.dataPrevista || undefined,
      responsavelId: d.responsavelId,
    });
    setNovaAberta(false);
    setAviso(`${criada.titulo} criada em Prospecção.`);
    recarregar();
  }

  async function salvarEdicao(d: DadosFormulario) {
    if (!editando) return;
    await api.atualizar(editando.id, {
      titulo: d.titulo.trim(),
      valor: Number(d.valor),
      dataPrevista: d.dataPrevista || undefined,
      responsavelId: d.responsavelId,
    });
    setEditando(null);
    setAviso('Alterações salvas.');
    recarregar();
  }

  async function fechar(resultado: Resultado, motivoPerda?: MotivoPerda) {
    if (!fechando) return;
    await api.mover(fechando.id, { etapa: 'Fechado', resultado, motivoPerda });
    setAviso(resultado === 'ganho' ? 'Oportunidade ganha, o cliente será ativado.' : `Oportunidade perdida (${motivoPerda}).`);
    setFechando(null);
    recarregar();
  }

  async function confirmarExclusao() {
    if (!excluindo) return;
    const alvo = excluindo;
    setExcluindo(null);
    await executar(() => api.excluir(alvo.id), `${alvo.titulo} excluída.`);
  }

  /** RF09: soltar um cartão numa coluna move; soltar em Fechado pergunta ganho ou perdido. */
  function aoSoltar(id: number, etapa: Etapa) {
    const o = todas.find((x) => x.id === id);
    if (!o || o.etapa === 'Fechado' || o.etapa === etapa) return;
    if (etapa === 'Fechado') setFechando(o);
    else void mover(o, etapa);
  }

  return (
    <section className="mfo" aria-label="Funil de oportunidades">
      <header className="mfo-topo">
        <div>
          <h1>Funil de vendas</h1>
          <p className="mfo-muted">Prospecção, contato, proposta e fechamento das oportunidades.</p>
        </div>
        <button type="button" className="mfo-btn mfo-btn--primario" onClick={() => setNovaAberta(true)}>
          + Nova oportunidade
        </button>
      </header>

      <div className="mfo-filtros">
        <select aria-label="Filtrar por cliente" value={filtros.clienteId} onChange={(e) => setFiltros((f) => ({ ...f, clienteId: e.target.value }))}>
          <option value="">Todos os clientes</option>
          {clientes.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome}
            </option>
          ))}
        </select>
        <select aria-label="Filtrar por responsável" value={filtros.responsavelId} onChange={(e) => setFiltros((f) => ({ ...f, responsavelId: e.target.value }))}>
          <option value="">Todos os responsáveis</option>
          {usuarios.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nome}
            </option>
          ))}
        </select>
      </div>

      {funil && (
        <p className="mfo-resumo">
          <span className="mfo-muted">Em negociação</span> <strong>{formatarValor(funil.valorEmNegociacao)}</strong>
          <span className="mfo-muted">
            {funil.quantidade} {funil.quantidade === 1 ? 'oportunidade' : 'oportunidades'}
          </span>
        </p>
      )}

      {erro && (
        <div className="mfo-erro-caixa" role="alert">
          {erro}{' '}
          <button type="button" className="mfo-btn" onClick={recarregar}>
            Tentar de novo
          </button>
        </div>
      )}

      <div className="mfo-quadro" aria-busy={carregando}>
        {funil?.etapas.map((coluna) => (
          <ColunaFunil key={coluna.etapa} etapa={coluna.etapa} quantidade={coluna.quantidade} valorTotal={coluna.valorTotal} aoSoltar={(id) => aoSoltar(id, coluna.etapa)}>
            {coluna.itens.map((o) => (
              <CartaoOportunidade
                key={o.id}
                oportunidade={o}
                nomeCliente={nomeCliente(o.clienteId)}
                nomeResponsavel={nomeUsuario(o.responsavelId)}
                aoMover={(destino) => void mover(o, destino)}
                aoFechar={() => setFechando(o)}
                aoReabrir={() => void executar(() => api.mover(o.id, { etapa: 'Proposta', reabrir: true }), `${o.titulo} reaberta em Proposta.`)}
                aoEditar={() => setEditando(o)}
                aoExcluir={() => setExcluindo(o)}
              />
            ))}
            {coluna.itens.length === 0 && !carregando && <p className="mfo-vazio">Nenhuma oportunidade.</p>}
          </ColunaFunil>
        ))}
        {!funil && carregando && <p className="mfo-vazio">Carregando…</p>}
      </div>

      <Modal titulo="Nova oportunidade" aberto={novaAberta} aoFechar={() => setNovaAberta(false)}>
        <FormOportunidade clientes={clientes} usuarios={usuarios} usuarioId={usuarioId} aoSalvar={criar} aoCancelar={() => setNovaAberta(false)} />
      </Modal>

      <Modal titulo={`Editar ${editando?.titulo ?? ''}`} aberto={!!editando} aoFechar={() => setEditando(null)}>
        {editando && (
          <FormOportunidade inicial={editando} clientes={clientes} usuarios={usuarios} usuarioId={usuarioId} aoSalvar={salvarEdicao} aoCancelar={() => setEditando(null)} />
        )}
      </Modal>

      <Modal titulo="Fechar oportunidade" aberto={!!fechando} aoFechar={() => setFechando(null)} largura={440}>
        {fechando && <FormFechamento titulo={fechando.titulo} aoConfirmar={fechar} aoCancelar={() => setFechando(null)} />}
      </Modal>

      <Modal titulo="Excluir oportunidade" aberto={!!excluindo} aoFechar={() => setExcluindo(null)} largura={420}>
        <p>
          <strong>{excluindo?.titulo}</strong> será excluída definitivamente.
        </p>
        <footer className="mfo-form__acoes">
          <button type="button" className="mfo-btn" onClick={() => setExcluindo(null)}>
            Cancelar
          </button>
          <button type="button" className="mfo-btn mfo-btn--perigo-cheio" onClick={() => void confirmarExclusao()}>
            Excluir
          </button>
        </footer>
      </Modal>

      {aviso && (
        <div className="mfo-aviso" role="status">
          {aviso}
        </div>
      )}
    </section>
  );
}
