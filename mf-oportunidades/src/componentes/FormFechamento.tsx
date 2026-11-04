import { useState, type FormEvent } from 'react';
import { ErroApi } from '../api/oportunidadesApi';
import { MOTIVOS, type MotivoPerda, type Resultado } from '../tipos';

interface Props {
  titulo: string;
  aoConfirmar: (resultado: Resultado, motivoPerda?: MotivoPerda) => Promise<void>;
  aoCancelar: () => void;
}

/** RF29: ao fechar, pergunta ganho ou perdido; se perdido, o motivo é obrigatório. */
export function FormFechamento({ titulo, aoConfirmar, aoCancelar }: Props) {
  const [resultado, setResultado] = useState<Resultado>('ganho');
  const [motivo, setMotivo] = useState<MotivoPerda | ''>('');
  const [erros, setErros] = useState<string[]>([]);
  const [salvando, setSalvando] = useState(false);

  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (resultado === 'perdido' && !motivo) {
      setErros(['motivoPerda: obrigatório ao perder (Preço, Concorrente, Sem verba, Sem resposta)']);
      return;
    }
    setErros([]);
    setSalvando(true);
    try {
      await aoConfirmar(resultado, resultado === 'perdido' ? (motivo as MotivoPerda) : undefined);
    } catch (erro) {
      setErros(erro instanceof ErroApi && erro.erros.length ? erro.erros : [(erro as Error).message]);
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form className="mfo-form" onSubmit={enviar} noValidate>
      <p>
        Como <strong>{titulo}</strong> terminou?
      </p>
      <div className="mfo-segmentos" role="radiogroup" aria-label="Resultado">
        {(['ganho', 'perdido'] as Resultado[]).map((r) => (
          <label key={r} className={`mfo-segmento${resultado === r ? ' mfo-segmento--ativo' : ''}`}>
            <input type="radio" name="resultado" value={r} checked={resultado === r} onChange={() => setResultado(r)} />
            {r === 'ganho' ? 'Ganho' : 'Perdido'}
          </label>
        ))}
      </div>
      {resultado === 'perdido' && (
        <label>
          Motivo da perda
          <select value={motivo} onChange={(e) => setMotivo(e.target.value as MotivoPerda | '')}>
            <option value="">Escolha um motivo…</option>
            {MOTIVOS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </label>
      )}
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
          {salvando ? 'Fechando…' : 'Fechar oportunidade'}
        </button>
      </footer>
    </form>
  );
}
