import { useEffect, useState } from 'react';
import { buscarDadosAgregados, type DadosAgregados } from '../api/dashboardApi';
import type { Config } from '../config';
import { formatarPercentual, formatarValor } from '../formato';
import type { Usuario } from '../usuarios';

interface Props {
  config: Config;
  usuarioId: string;
  usuarios: Usuario[];
}

const STATUS = ['Prospect', 'Ativo', 'Inativo'];

/** Dashboard (RF11, RF19, RF29, RF31): alimentado por GET /aggregated-data do BFF. */
export default function Dashboard({ config, usuarioId, usuarios }: Props) {
  const [dados, setDados] = useState<DadosAgregados | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [versao, setVersao] = useState(0);

  useEffect(() => {
    const controle = new AbortController();
    setCarregando(true);
    buscarDadosAgregados(config, usuarioId, controle.signal)
      .then((d) => {
        setDados(d);
        setErro(null);
      })
      .catch((e: Error) => e.name !== 'AbortError' && setErro(e.message))
      .finally(() => !controle.signal.aborted && setCarregando(false));
    return () => controle.abort();
  }, [config, usuarioId, versao]);

  const nomeDe = (id: string) => usuarios.find((u) => u.id === id)?.nome ?? id;
  const ind = dados?.indicadores;
  const op = ind?.oportunidades;
  const semIndicadores = !!dados && !ind?.clientes && !op;

  return (
    <section className="sh-pagina" aria-label="Dashboard" aria-busy={carregando}>
      <header className="sh-pagina__topo">
        <div>
          <h1>Dashboard</h1>
          {dados && (
            <p className="sh-muted">
              {dados.usuario.nome} ({dados.usuario.perfil}) · enxerga {dados.escopo.length} {dados.escopo.length === 1 ? 'responsável' : 'responsáveis'}
            </p>
          )}
        </div>
        <button type="button" className="sh-btn" onClick={() => setVersao((v) => v + 1)}>
          Atualizar
        </button>
      </header>

      {erro && (
        <div className="sh-erro" role="alert">
          {erro}{' '}
          <button type="button" className="sh-btn" onClick={() => setVersao((v) => v + 1)}>
            Tentar de novo
          </button>
        </div>
      )}

      {dados && dados.falhas.length > 0 && (
        <div className="sh-aviso" role="status">
          Alguns serviços não responderam, então parte dos números pode estar incompleta:{' '}
          {dados.falhas.map((f) => `${f.servico} (${f.erro})`).join(', ')}.
        </div>
      )}

      {!dados && carregando && <p className="sh-vazio">Carregando…</p>}

      {dados && (
        <>
          <div className="sh-cartoes">
            <article className="sh-cartao" aria-label="Clientes">
              <h2>Clientes</h2>
              <p className="sh-grande">{ind?.clientes?.total ?? dados.clientes.total}</p>
              <ul className="sh-status">
                {STATUS.map((s) => (
                  <li key={s}>
                    <span>{s}</span> <strong>{ind?.clientes?.porStatus?.[s] ?? 0}</strong>
                  </li>
                ))}
              </ul>
            </article>
            <article className="sh-cartao" aria-label="Valor em negociação">
              <h2>Valor em negociação</h2>
              <p className="sh-grande">{formatarValor(op?.valorEmNegociacao ?? 0)}</p>
              <p className="sh-muted">{op?.total ?? 0} oportunidades no funil</p>
            </article>
            <article className="sh-cartao" aria-label="Taxa de conversão">
              <h2>Taxa de conversão</h2>
              <p className="sh-grande">{formatarPercentual(op?.taxaConversao ?? 0)}</p>
              <p className="sh-muted">
                {op?.ganhas ?? 0} ganhas · {op?.perdidas ?? 0} perdidas
              </p>
            </article>
            <article className="sh-cartao" aria-label="Valor ganho">
              <h2>Valor ganho</h2>
              <p className="sh-grande">{formatarValor(op?.valorGanho ?? 0)}</p>
            </article>
          </div>

          {semIndicadores && <p className="sh-vazio">Indicadores indisponíveis no momento.</p>}

          <div className="sh-duas-colunas">
            <article className="sh-cartao" aria-label="Funil por etapa">
              <h2>Funil por etapa</h2>
              <table className="sh-tabela">
                <thead>
                  <tr>
                    <th>Etapa</th>
                    <th>Qtd.</th>
                    <th>Valor</th>
                  </tr>
                </thead>
                <tbody>
                  {(op?.porEtapa ?? []).map((e) => (
                    <tr key={e.etapa}>
                      <td>{e.etapa}</td>
                      <td>{e.quantidade}</td>
                      <td>{formatarValor(e.valorTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </article>
            <article className="sh-cartao" aria-label="Ranking de vendedores">
              <h2>Ranking de vendedores</h2>
              {(ind?.ranking ?? []).length === 0 ? (
                <p className="sh-vazio">Nenhuma venda fechada ainda.</p>
              ) : (
                <table className="sh-tabela">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Vendedor</th>
                      <th>Valor ganho</th>
                      <th>Ganhas</th>
                      <th>Perdidas</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ind!.ranking!.map((r, i) => (
                      <tr key={r.responsavelId}>
                        <td>{i + 1}</td>
                        <td>{nomeDe(r.responsavelId)}</td>
                        <td>{formatarValor(r.valorGanho)}</td>
                        <td>{r.oportunidadesGanhas}</td>
                        <td>{r.oportunidadesPerdidas}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </article>
          </div>

          {Object.keys(op?.motivosPerda ?? {}).length > 0 && (
            <article className="sh-cartao" aria-label="Motivos de perda">
              <h2>Motivos de perda</h2>
              <ul className="sh-status">
                {Object.entries(op!.motivosPerda).map(([motivo, qtd]) => (
                  <li key={motivo}>
                    <span>{motivo}</span> <strong>{qtd}</strong>
                  </li>
                ))}
              </ul>
            </article>
          )}
        </>
      )}
    </section>
  );
}
