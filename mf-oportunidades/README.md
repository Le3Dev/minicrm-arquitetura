# MF Oportunidades — Mini CRM

Microfrontend do **Funil de vendas** do Mini CRM, em **React 19 + Vite + Module Federation**. É um *remote*: o shell (host) carrega `remoteEntry.js` e monta o componente `mfOportunidades/OportunidadesApp`. Também roda sozinho, para desenvolvimento e demonstração.

| | URL |
| --- | --- |
| MF publicado (Vercel) | `https://minicrm-arquitetura-1xxh.vercel.app` |
| Remote entry | `https://minicrm-arquitetura-1xxh.vercel.app/remoteEntry.js` |

O MF **nunca chama o microsserviço direto**: todas as chamadas vão para o **AWS API Gateway** (header `x-api-key`), que encaminha ao **BFF**, que fala com o MS Oportunidades (Azure SQL) e o MS Clientes.

## O que a tela faz

| Função | Requisito do Mini CRM | Rota do BFF |
| --- | --- | --- |
| Quadro com as colunas Prospecção, Contato, Proposta e Fechado | RF08 | `GET /oportunidades` |
| Quantidade e soma dos valores no topo de cada coluna, e total em negociação | RF19 | mesma |
| Nova oportunidade escolhendo o cliente | RF07 | `POST /oportunidades` (+ `GET /clientes` para a lista) |
| Mover de coluna, por botões ou arrastando o cartão | RF09 | `PATCH /oportunidades/{id}` |
| Ao fechar, pergunta ganho ou perdido; se perdido, o motivo (Preço, Concorrente, Sem verba, Sem resposta) | RF29 | mesma, com `resultado` e `motivoPerda` |
| Reabrir uma fechada (manda `reabrir: true`, volta para Proposta) | 5.2 | mesma |
| Editar (só abertas) e excluir (não vale para ganhas) | — | `PUT` e `DELETE /oportunidades/{id}` |
| Filtro por cliente e por responsável (o BFF aplica o escopo do usuário, RF15) | RF15 | `?clienteId=` e `?responsavelId=` |
| Erros do servidor mostrados na tela, campo a campo | 5.5 | — |

Ao fechar como **ganha**, a tela avisa "Oportunidade ganha, o cliente será ativado": o MS Oportunidades publica `OportunidadeGanha` no Service Bus, a Function consome e o MS Clientes passa o cliente para Ativo alguns segundos depois.

## Como o shell usa

```ts
// vite.config.ts do shell
federation({
  name: 'shell',
  remotes: {
    mfOportunidades: {
      type: 'module',
      name: 'mfOportunidades',
      entry: 'https://minicrm-arquitetura-1xxh.vercel.app/remoteEntry.js',
    },
  },
  shared: { react: { singleton: true }, 'react-dom': { singleton: true } },
});
```

```tsx
// rota /funil do shell
const OportunidadesApp = lazy(() => import('mfOportunidades/OportunidadesApp'));

<Suspense fallback={<p>Carregando…</p>}>
  <OportunidadesApp apiUrl={GATEWAY_URL} apiKey={GATEWAY_KEY} usuarioId={usuarioLogado.id} usuarios={USUARIOS} />
</Suspense>
```

| Prop | Obrigatória | Uso |
| --- | --- | --- |
| `apiUrl` | sim | URL do API Gateway (ou do BFF, em desenvolvimento) |
| `apiKey` | não | API key do usage plan do Gateway (`x-api-key`) |
| `usuarioId` | não | Usuário logado: vai em `?usuarioId=` para o BFF calcular o escopo (RF15); padrão `u3` |
| `usuarios` | não | Lista `{ id, nome }` para os seletores de responsável; padrão = os 8 usuários da massa do Mini CRM |

## Estrutura

```
src/
├── OportunidadesApp.tsx     componente exposto (tela inteira)
├── main.tsx                 modo isolado
├── api/oportunidadesApi.ts  cliente HTTP (gateway, API key, erros)
├── componentes/             ColunaFunil, CartaoOportunidade, FormOportunidade, FormFechamento, Modal
├── estilos/                 CSS isolado sob .mfo (mesmos tokens do MF Clientes)
└── testes/                  servidor falso do BFF para os testes
```

## Tecnologias

React 19, TypeScript 5.9, Vite 7, `@module-federation/vite`, Vitest, Testing Library, Vercel.

## Como rodar localmente

```bash
npm install
cp .env.example .env.local     # VITE_API_URL = URL do BFF local (ex.: http://localhost:3000)
npm run dev                    # http://localhost:5002
npm test                       # testes de componente e do cliente HTTP
npm run build && npm run preview
```

## Deploy na Vercel

Importe o repositório na Vercel com **Root Directory = `mf-oportunidades`**, framework Vite, e as variáveis `VITE_API_URL` (URL do stage do API Gateway; antes disso, a do BFF), `VITE_API_KEY` e `VITE_USUARIO_ID=u3`. O `vercel.json` libera CORS para o shell baixar o `remoteEntry.js`. Confira que `https://minicrm-arquitetura-1xxh.vercel.app/remoteEntry.js` abre no navegador: essa é a URL que vai no Shell.

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
