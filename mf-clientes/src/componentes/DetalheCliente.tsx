import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { ClientesApi, ErroApi } from '../api/clientesApi';
import { formatarData, formatarDataHora } from '../formato';
import type { ClienteDetalhe, TipoInteracao, Usuario } from '../tipos';
import { BadgeStatus } from './BadgeStatus';

interface Props {
  api: ClientesApi;
  clienteId: string;
  usuarioId: string;
  nomeDe: (id: string) => string;
  usuarios: Usuario[];
  aoAlterar: () => void;
}

const TIPOS: TipoInteracao[] = ['Ligação', 'E-mail', 'Reunião'];
const ICONE: Record<TipoInteracao, string> = { Ligação: '☎', 'E-mail': '✉', Reunião: '◷' };

/** RF05 + RF06: dados do cliente, histórico de interações e registro de nova interação. */
export function DetalheCliente({ api, clienteId, usuarioId, nomeDe, aoAlterar }: Props) {
  const [detalhe, setDetalhe] = useState<ClienteDetalhe | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [tipo, setTipo] = useState<TipoInteracao>('Ligação');
  const [obs, setObs] = useState('');
  const [errosForm, setErrosForm] = useState<string[]>([]);
  const [enviando, setEnviando] = useState(false);

  const carregar = useCallback(
    (signal?: AbortSignal) =>
      api
        .detalhar(clienteId, signal)
        .then(setDetalhe)
        .catch((e: Error) => e.name !== 'AbortError' && setErro(e.message)),
    [api, clienteId],
  );

  useEffect(() => {
    const controle = new AbortController();
    void carregar(controle.signal);
    return () => controle.abort();
  }, [carregar]);

  async function registrar(e: FormEvent) {
    e.preventDefault();
    if (obs.trim().length < 3) {
      setErrosForm(['obs: deve ter ao menos 3 caracteres']);
      return;
    }
    setEnviando(true);
    try {
      await api.registrarInteracao(clienteId, { tipo, obs: obs.trim(), autorId: usuarioId });
      setObs('');
      setErrosForm([]);
      await carregar();
      aoAlterar();
    } catch (falha) {
      setErrosForm(falha instanceof ErroApi && falha.erros.length ? falha.erros : [(falha as Error).message]);
    } finally {
      setEnviando(false);
    }
  }

  if (erro) return <p className="mfc-erro-caixa">{erro}</p>;
  if (!detalhe) return <p className="mfc-muted">Carregando…</p>;
  const { cliente, interacoes } = detalhe;

  return (
    <div className="mfc-detalhe">
      <dl className="mfc-detalhe__dados">
        <div><dt>Empresa</dt><dd>{cliente.empresa}</dd></div>
        <div><dt>Status</dt><dd><BadgeStatus status={cliente.status} /></dd></div>
        <div><dt>E-mail</dt><dd>{cliente.email}</dd></div>
        <div><dt>Telefone</dt><dd>{cliente.telefone || '—'}</dd></div>
        <div><dt>Responsável</dt><dd>{nomeDe(cliente.responsavelId)}</dd></div>
        <div><dt>Cliente desde</dt><dd>{formatarData(cliente.criadoEm)}</dd></div>
      </dl>

      <h3>Interações ({interacoes.length})</h3>
      {!cliente.excluido && (
        <form className="mfc-interacao-form" onSubmit={registrar}>
          <div className="mfc-segmentos" role="radiogroup" aria-label="Tipo de interação">
            {TIPOS.map((t) => (
              <button key={t} type="button" role="radio" aria-checked={tipo === t}
                className={`mfc-segmento${tipo === t ? ' mfc-segmento--ativo' : ''}`} onClick={() => setTipo(t)}>
                {ICONE[t]} {t}
              </button>
            ))}
          </div>
          <textarea aria-label="Observação" value={obs} onChange={(e) => setObs(e.target.value)} rows={2}
            placeholder="O que foi conversado?" />
          {errosForm.length > 0 && (
            <ul className="mfc-erros" role="alert">{errosForm.map((x) => <li key={x}>{x}</li>)}</ul>
          )}
          <button type="submit" className="mfc-btn mfc-btn--primario" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Registrar interação'}
          </button>
        </form>
      )}
      {interacoes.length === 0 ? (
        <p className="mfc-muted">Nenhuma interação registrada ainda.</p>
      ) : (
        <ol className="mfc-linha-tempo">
          {interacoes.map((i) => (
            <li key={i.id}>
              <span className={`mfc-linha-tempo__icone mfc-tipo--${i.tipo === 'Ligação' ? 'ligacao' : i.tipo === 'E-mail' ? 'email' : 'reuniao'}`}>
                {ICONE[i.tipo]}
              </span>
              <div>
                <p className="mfc-linha-tempo__meta">
                  <strong>{i.tipo}</strong> · {formatarDataHora(i.data)} · {nomeDe(i.autorId)}
                </p>
                <p>{i.obs}</p>
              </div>
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}
