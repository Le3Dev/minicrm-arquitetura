# MF Clientes — Mini CRM

Microfrontend da tela de **Clientes** do Mini CRM, em **React 19 + Vite + Module Federation**. É um *remote*: o shell (host, do aluno B) carrega `remoteEntry.js` e monta o componente `mfClientes/ClientesApp`. Também roda sozinho, para desenvolvimento e demonstração.

| | URL |
| --- | --- |
| MF publicado (Vercel) | `https://<mf-clientes>.vercel.app` |
| Remote entry | `https://<mf-clientes>.vercel.app/remoteEntry.js` |

O MF **nunca chama o microsserviço direto**: todas as chamadas vão para o **AWS API Gateway** (header `x-api-key`), que encaminha ao **BFF**, que fala com o MS Clientes.

## O que a tela faz

| Função | Requisito do Mini CRM |
| --- | --- |
| Lista com busca por nome ou empresa, filtro por status e por responsável, 20 por página | RF04, RF18 |
| Cadastro (nasce Prospect), edição e transferência de responsável | RF03, RF26, RF30 |
| Exclusão lógica com confirmação | RF03 |
| Detalhe com histórico de interações e registro de ligação, e-mail ou reunião | RF05, RF06 |
| Mensagens de erro do servidor exibidas campo a campo | 5.5 |

## Como o shell usa

```ts
// vite.config.ts do shell
federation({
  name: 'shell',
  remotes: {
    mfClientes: {
      type: 'module',
      name: 'mfClientes',
      entry: 'https://<mf-clientes>.vercel.app/remoteEntry.js',
    },
  },
  shared: { react: { singleton: true }, 'react-dom': { singleton: true } },
});
```

```tsx
// rota /clientes do shell
const ClientesApp = lazy(() => import('mfClientes/ClientesApp'));

<Suspense fallback={<p>Carregando…</p>}>
  <ClientesApp apiUrl={GATEWAY_URL} apiKey={GATEWAY_KEY} usuarioId={usuarioLogado.id} />
</Suspense>
```

| Prop | Obrigatória | Uso |
| --- | --- | --- |
| `apiUrl` | sim | URL do API Gateway (ou do BFF, em desenvolvimento) |
| `apiKey` | não | API key do usage plan do Gateway (`x-api-key`) |
| `usuarioId` | não | Usuário logado: vai em `?usuarioId=` para o BFF calcular o escopo (RF15) e é o autor das interações |
| `usuarios` | não | Lista `{ id, nome }` para o seletor de responsável; padrão = usuários da massa do Mini CRM |

## Rotas do BFF que o MF consome

`GET /clientes`, `GET /clientes/{id}`, `POST /clientes`, `PATCH /clientes/{id}`, `DELETE /clientes/{id}` e `POST /clientes/{id}/interacoes`, sempre com `?usuarioId=`. Os formatos são os do MS Clientes (veja o Swagger dele).

## Estrutura

```
src/
├── ClientesApp.tsx        componente exposto (tela inteira)
├── main.tsx               modo isolado
├── api/clientesApi.ts     cliente HTTP (gateway, API key, erros)
├── componentes/           FormCliente, DetalheCliente, Modal, Paginacao, BadgeStatus
├── estilos/               CSS isolado sob .mfc (tokens do design system do Mini CRM)
└── testes/                servidor falso do BFF para os testes
```

## Tecnologias

React 19, TypeScript 5.9, Vite 7, `@module-federation/vite`, Vitest, Testing Library, Vercel.

## Como rodar localmente

```bash
npm install
cp .env.example .env.local     # VITE_API_URL = URL do BFF local (ex.: http://localhost:3000)
npm run dev                    # http://localhost:5001
npm test                       # testes de componente e do cliente HTTP
npm run build && npm run preview
```

## Deploy na Vercel

Importe o repositório na Vercel com **Root Directory = `mf-clientes`**, framework Vite, e as variáveis `VITE_API_URL` (URL do stage do API Gateway), `VITE_API_KEY` e `VITE_USUARIO_ID`. O `vercel.json` libera CORS para o shell baixar o `remoteEntry.js`.

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
