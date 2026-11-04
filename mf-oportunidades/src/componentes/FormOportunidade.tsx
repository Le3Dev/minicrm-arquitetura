import { useState, type FormEvent } from 'react';
import { ErroApi } from '../api/oportunidadesApi';
import type { ClienteResumo, Oportunidade, Usuario } from '../tipos';

export interface DadosFormulario {
  clienteId: string;
  titulo: string;
  valor: string;
  dataPrevista: string;
  responsavelId: string;
}

interface Props {
  inicial?: Oportunidade;
  clientes: ClienteResumo[];
  usuarios: Usuario[];
  usuarioId: string;
  aoSalvar: (dados: DadosFormulario) => Promise<void>;
  aoCancelar: () => void;
}

/** Mesmas regras da SPEC do Mini CRM (RF07); o MS Oportunidades valida de novo no servidor. */
export function validarOportunidade(d: DadosFormulario): string[] {
  const erros: string[] = [];
  if (!d.clienteId) erros.push('clienteId: escolha um cliente');
  if (d.titulo.trim().length < 3) erros.push('titulo: deve ter ao menos 3 caracteres');
  if (!(Number(d.valor) > 0)) erros.push('valor: deve ser maior que zero');
  if (!d.responsavelId) erros.push('responsavelId: escolha um responsável');
  return erros;
}

export function FormOportunidade({ inicial, clientes, usuarios, usuarioId, aoSalvar, aoCancelar }: Props) {
  const [dados, setDados] = useState<DadosFormulario>({
    clienteId: inicial?.clienteId ?? '',
    titulo: inicial?.titulo ?? '',
    valor: inicial ? String(inicial.valor) : '',
    dataPrevista: inicial?.dataPrevista?.slice(0, 10) ?? '',
    responsavelId: inicial?.responsavelId ?? (usuarios.some((u) => u.id === usuarioId) ? usuarioId : (usuarios[0]?.id ?? '')),
  });
  const [erros, setErros] = useState<string[]>([]);
  const [salvando, setSalvando] = useState(false);

  const campo = (nome: keyof DadosFormulario) => ({
    value: dados[nome],
    onChange: (e: { target: { value: string } }) => setDados((d) => ({ ...d, [nome]: e.target.value })),
  });

  async function enviar(e: FormEvent) {
    e.preventDefault();
    const locais = validarOportunidade(dados);
    setErros(locais);
    if (locais.length) return;
    setSalvando(true);
    try {
      await aoSalvar(dados);
    } catch (erro) {
      setErros(erro instanceof ErroApi && erro.erros.length ? erro.erros : [(erro as Error).message]);
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form className="mfo-form" onSubmit={enviar} noValidate>
      <label>
        Cliente
        <select {...campo('clienteId')} disabled={!!inicial}>
          <option value="">Escolha um cliente…</option>
          {clientes.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome} — {c.empresa}
            </option>
          ))}
        </select>
      </label>
      <label>
        Título
        <input {...campo('titulo')} placeholder="Plano anual — 20 licenças" autoFocus />
      </label>
      <div className="mfo-form__linha">
        <label>
          Valor estimado (R$)
          <input {...campo('valor')} type="number" min="0" step="0.01" placeholder="18500" />
        </label>
        <label>
          Data prevista
          <input {...campo('dataPrevista')} type="date" />
        </label>
      </div>
      <label>
        Responsável
        <select {...campo('responsavelId')}>
          {usuarios.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nome}
            </option>
          ))}
        </select>
      </label>
      {erros.length > 0 && (
        <ul className="mfo-erros" role="alert">
          {erros.map((e) => (
            <li key={e}>{e}</li>
          ))}
        </ul>
      )}
      <footer className="mfo-form__acoes">
        <button type="button" className="mfo-btn" onClick={aoCancelar}>
          Cancelar
        </button>
        <button type="submit" className="mfo-btn mfo-btn--primario" disabled={salvando}>
          {salvando ? 'Salvando…' : inicial ? 'Salvar alterações' : 'Criar oportunidade'}
        </button>
      </footer>
    </form>
  );
}
