import { formatarData, formatarValor } from '../formato';
import { ETAPAS_ABERTAS, type Etapa, type Oportunidade } from '../tipos';

interface Props {
  oportunidade: Oportunidade;
  nomeCliente: string;
  nomeResponsavel: string;
  aoMover: (destino: Etapa) => void;
  aoFechar: () => void;
  aoReabrir: () => void;
  aoEditar: () => void;
  aoExcluir: () => void;
}

/** Cartão do quadro. Aberta: move entre as etapas e fecha. Fechada: só reabre (ou exclui, se perdida). */
export function CartaoOportunidade({ oportunidade: o, nomeCliente, nomeResponsavel, aoMover, aoFechar, aoReabrir, aoEditar, aoExcluir }: Props) {
  const fechada = o.etapa === 'Fechado';
  const vizinhas = ETAPAS_ABERTAS.filter((e) => e !== o.etapa);
  return (
    <article
      className={`mfo-cartao-op${fechada ? ' mfo-cartao-op--fechada' : ''}`}
      draggable={!fechada}
      onDragStart={(e) => e.dataTransfer.setData('text/plain', String(o.id))}
      aria-label={`Oportunidade ${o.titulo}`}
    >
      <header>
        <strong>{o.titulo}</strong>
        <small>{nomeCliente}</small>
      </header>
      <p className="mfo-valor">{formatarValor(o.valor)}</p>
      <p className="mfo-muted">
        {nomeResponsavel} · previsto {formatarData(o.dataPrevista)}
      </p>
      {fechada && (
        <p>
          <span className={`mfo-badge mfo-badge--${o.resultado}`}>{o.resultado === 'ganho' ? 'Ganho' : 'Perdido'}</span>
          {o.motivoPerda && <small className="mfo-muted"> {o.motivoPerda}</small>}
        </p>
      )}
      <footer className="mfo-acoes">
        {!fechada &&
          vizinhas.map((destino) => (
            <button key={destino} type="button" className="mfo-btn" aria-label={`Mover ${o.titulo} para ${destino}`} onClick={() => aoMover(destino)}>
              {ETAPAS_ABERTAS.indexOf(destino) < ETAPAS_ABERTAS.indexOf(o.etapa) ? `← ${destino}` : `${destino} →`}
            </button>
          ))}
        {!fechada && (
          <button type="button" className="mfo-btn mfo-btn--primario" aria-label={`Fechar ${o.titulo}`} onClick={aoFechar}>
            Fechar
          </button>
        )}
        {fechada && (
          <button type="button" className="mfo-btn" aria-label={`Reabrir ${o.titulo}`} onClick={aoReabrir}>
            Reabrir
          </button>
        )}
        {!fechada && (
          <button type="button" className="mfo-btn mfo-btn--fantasma" aria-label={`Editar ${o.titulo}`} onClick={aoEditar}>
            Editar
          </button>
        )}
        {o.resultado !== 'ganho' && (
          <button type="button" className="mfo-btn mfo-btn--fantasma mfo-btn--perigo" aria-label={`Excluir ${o.titulo}`} onClick={aoExcluir}>
            Excluir
          </button>
        )}
      </footer>
    </article>
  );
}
