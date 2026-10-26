import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import ClientesApp from './ClientesApp';

/** Modo isolado: o MF roda sozinho (útil para desenvolver e para mostrar no vídeo). */
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ClientesApp
      apiUrl={import.meta.env.VITE_API_URL ?? 'http://localhost:3000'}
      apiKey={import.meta.env.VITE_API_KEY || undefined}
      usuarioId={import.meta.env.VITE_USUARIO_ID || 'u3'}
    />
  </StrictMode>,
);
