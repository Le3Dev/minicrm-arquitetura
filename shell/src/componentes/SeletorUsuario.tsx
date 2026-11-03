import type { Usuario } from '../usuarios';

interface Props {
  usuarios: Usuario[];
  usuarioId: string;
  aoMudar: (id: string) => void;
}

/** "Entrar como": troca o usuário simulado e, com ele, o escopo de visualização (RF15). */
export function SeletorUsuario({ usuarios, usuarioId, aoMudar }: Props) {
  return (
    <label className="sh-seletor">
      <span>Entrar como</span>
      <select value={usuarioId} onChange={(e) => aoMudar(e.target.value)}>
        {usuarios.map((u) => (
          <option key={u.id} value={u.id}>
            {u.nome} ({u.perfil})
          </option>
        ))}
      </select>
    </label>
  );
}
