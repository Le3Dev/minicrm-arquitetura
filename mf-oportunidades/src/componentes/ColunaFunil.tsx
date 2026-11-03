import { useState, type ReactNode } from 'react';
import { formatarValor } from '../formato';
import type { Etapa } from '../tipos';

interface Props {
  etapa: Etapa;
  quantidade: number;
  valorTotal: number;
  aoSoltar: (id: number) => void;
  children: ReactNode;
}

/** RF19: quantidade e soma dos valores no topo de cada coluna. Aceita cartões arrastados (RF09). */
export function ColunaFunil({ etapa, quantidade, valorTotal, aoSoltar, children }: Props) {
  const [sobre, setSobre] = useState(false);
  return (
    <section
      className={`mfo-coluna${sobre ? ' mfo-coluna--alvo' : ''}`}
      aria-label={`Etapa ${etapa}`}
      onDragOver={(e) => {
        e.preventDefault();
        setSobre(true);
      }}
      onDragLeave={() => setSobre(false)}
      onDrop={(e) => {
        e.preventDefault();
        setSobre(false);
        const id = Number(e.dataTransfer.getData('text/plain'));
        if (id) aoSoltar(id);
      }}
    >
      <header className="mfo-coluna__topo">
        <h2>{etapa}</h2>
        <p>
          <span className="mfo-contagem" aria-label={`${quantidade} oportunidades`}>
            {quantidade}
          </span>{' '}
          <span className="mfo-total">{formatarValor(valorTotal)}</span>
        </p>
      </header>
      <div className="mfo-coluna__itens">{children}</div>
    </section>
  );
}
