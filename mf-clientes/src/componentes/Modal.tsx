import { useEffect, useId, type ReactNode } from 'react';

interface Props {
  titulo: string;
  aberto: boolean;
  aoFechar: () => void;
  children: ReactNode;
  largura?: number;
}

export function Modal({ titulo, aberto, aoFechar, children, largura = 520 }: Props) {
  const idTitulo = useId();
  useEffect(() => {
    if (!aberto) return;
    const tecla = (e: KeyboardEvent) => e.key === 'Escape' && aoFechar();
    window.addEventListener('keydown', tecla);
    return () => window.removeEventListener('keydown', tecla);
  }, [aberto, aoFechar]);

  if (!aberto) return null;
  return (
    <div className="mfc-overlay" onMouseDown={(e) => e.target === e.currentTarget && aoFechar()}>
      <div className="mfc-modal" role="dialog" aria-modal="true" aria-labelledby={idTitulo} style={{ maxWidth: largura }}>
        <header className="mfc-modal__topo">
          <h2 id={idTitulo}>{titulo}</h2>
          <button type="button" className="mfc-btn mfc-btn--fantasma" aria-label="Fechar" onClick={aoFechar}>
            ✕
          </button>
        </header>
        <div className="mfc-modal__corpo">{children}</div>
      </div>
    </div>
  );
}
