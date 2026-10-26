import { useState, type FormEvent } from 'react';
import { ErroApi } from '../api/clientesApi';
import type { Cliente, NovoCliente, Usuario } from '../tipos';

interface Props {
  inicial?: Cliente;
  usuarios: Usuario[];
  aoSalvar: (dados: NovoCliente) => Promise<void>;
  aoCancelar: () => void;
}

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/** Mesmas regras da SPEC do Mini CRM (5.5); o MS Clientes valida de novo no servidor. */
export function validarCliente(d: NovoCliente): string[] {
  const erros: string[] = [];
  if (d.nome.trim().length < 3) erros.push('nome: deve ter ao menos 3 caracteres');
  if (d.empresa.trim().length < 2) erros.push('empresa: deve ter ao menos 2 caracteres');
  if (!EMAIL.test(d.email.trim())) erros.push('email: formato inválido');
  if (!d.responsavelId) erros.push('responsavelId: escolha um responsável');
  return erros;
}

export function FormCliente({ inicial, usuarios, aoSalvar, aoCancelar }: Props) {
  const [dados, setDados] = useState<NovoCliente>({
    nome: inicial?.nome ?? '',
    empresa: inicial?.empresa ?? '',
    email: inicial?.email ?? '',
    telefone: inicial?.telefone ?? '',
    responsavelId: inicial?.responsavelId ?? usuarios[0]?.id ?? '',
  });
  const [erros, setErros] = useState<string[]>([]);
  const [salvando, setSalvando] = useState(false);

  const campo = (nome: keyof NovoCliente) => ({
    value: dados[nome],
    onChange: (e: { target: { value: string } }) => setDados((d) => ({ ...d, [nome]: e.target.value })),
  });

  async function enviar(e: FormEvent) {
    e.preventDefault();
    const locais = validarCliente(dados);
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
    <form className="mfc-form" onSubmit={enviar} noValidate>
      <label>
        Nome
        <input {...campo('nome')} placeholder="Mariana Souza" autoFocus />
      </label>
      <label>
        Empresa
        <input {...campo('empresa')} placeholder="Acme Ltda" />
      </label>
      <div className="mfc-form__linha">
        <label>
          E-mail
          <input {...campo('email')} type="email" placeholder="mariana@acme.com.br" />
        </label>
        <label>
          Telefone
          <input {...campo('telefone')} placeholder="(41) 99999-0000" />
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
        <ul className="mfc-erros" role="alert">
          {erros.map((e) => (
            <li key={e}>{e}</li>
          ))}
        </ul>
      )}
      <footer className="mfc-form__acoes">
        <button type="button" className="mfc-btn" onClick={aoCancelar}>
          Cancelar
        </button>
        <button type="submit" className="mfc-btn mfc-btn--primario" disabled={salvando}>
          {salvando ? 'Salvando…' : inicial ? 'Salvar alterações' : 'Cadastrar cliente'}
        </button>
      </footer>
    </form>
  );
}
