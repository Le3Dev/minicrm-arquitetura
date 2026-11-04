import { lazy, useState } from 'react';
import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { SeletorUsuario } from './componentes/SeletorUsuario';
import { config as configPadrao, type Config } from './config';
import './estilos/shell.css';
import Dashboard from './paginas/Dashboard';
import PaginaMf from './paginas/PaginaMf';
import { USUARIO_PADRAO, USUARIOS } from './usuarios';

// Os microfrontends vêm da rede em tempo de execução (Module Federation).
const ClientesApp = lazy(() => import('mfClientes/ClientesApp'));
const OportunidadesApp = lazy(() => import('mfOportunidades/OportunidadesApp'));

const CHAVE = 'minicrm.usuarioId';

function usuarioSalvo(): string {
  try {
    const salvo = sessionStorage.getItem(CHAVE);
    if (salvo && USUARIOS.some((u) => u.id === salvo)) return salvo;
  } catch {
    // sessionStorage pode estar bloqueado; usa o padrão
  }
  return USUARIO_PADRAO;
}

export default function App({ config = configPadrao }: { config?: Config }) {
  const [usuarioId, setUsuarioId] = useState(usuarioSalvo);

  function trocarUsuario(id: string) {
    setUsuarioId(id);
    try {
      sessionStorage.setItem(CHAVE, id);
    } catch {
      // ignora
    }
  }

  return (
    <div className="sh">
      <header className="sh-topo">
        <span className="sh-marca">Mini CRM</span>
        <nav aria-label="Menu principal" className="sh-menu">
          <NavLink to="/" end>
            Dashboard
          </NavLink>
          <NavLink to="/clientes">Clientes</NavLink>
          <NavLink to="/funil">Funil</NavLink>
        </nav>
        <SeletorUsuario usuarios={USUARIOS} usuarioId={usuarioId} aoMudar={trocarUsuario} />
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Dashboard config={config} usuarioId={usuarioId} usuarios={USUARIOS} />} />
          <Route path="/clientes" element={<PaginaMf nome="o MF Clientes" Remote={ClientesApp} config={config} usuarioId={usuarioId} usuarios={USUARIOS} />} />
          <Route path="/funil" element={<PaginaMf nome="o MF Oportunidades" Remote={OportunidadesApp} config={config} usuarioId={usuarioId} usuarios={USUARIOS} />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
