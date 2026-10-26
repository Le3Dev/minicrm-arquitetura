interface Props {
  pagina: number;
  totalPaginas: number;
  total: number;
  aoMudar: (pagina: number) => void;
}

/** RF18: 20 por página; os filtros continuam valendo ao trocar de página. */
export function Paginacao({ pagina, totalPaginas, total, aoMudar }: Props) {
  if (totalPaginas <= 1) {
    return <p className="mfc-paginacao mfc-muted">{total} {total === 1 ? 'cliente' : 'clientes'}</p>;
  }
  return (
    <nav className="mfc-paginacao" aria-label="Paginação">
      <span className="mfc-muted">{total} clientes</span>
      <button type="button" className="mfc-btn" disabled={pagina <= 1} onClick={() => aoMudar(pagina - 1)}>
        ← Anterior
      </button>
      <span>
        Página {pagina} de {totalPaginas}
      </span>
      <button type="button" className="mfc-btn" disabled={pagina >= totalPaginas} onClick={() => aoMudar(pagina + 1)}>
        Próxima →
      </button>
    </nav>
  );
}
