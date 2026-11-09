# Shell — Mini CRM

**Host** do Module Federation e a página que o professor abre: menu, seletor de usuário ("Entrar como") e dashboard. Carrega o **MF Clientes** e o **MF Oportunidades** pela rede, em tempo de execução. Em **React 19 + Vite + `@module-federation/vite` + React Router**.

| | URL |
| --- | --- |
| Shell publicado (Vercel) | `https://shell-zeta-puce.vercel.app` |

Toda chamada de dados vai ao **AWS API Gateway** (header `x-api-key`), que encaminha ao **BFF**. O Shell só chama `GET /aggregated-data`; as demais rotas são chamadas pelos MFs.

## Telas

| Rota | Conteúdo |
| --- | --- |
| `/` | Dashboard (`GET /aggregated-data?usuarioId=`): clientes por status, valor em negociação, taxa de conversão, valor ganho, funil por etapa, ranking de vendedores e motivos de perda. Se um serviço do BFF falhou, mostra um aviso e o que deu para calcular |
| `/clientes` | `<ClientesApp apiUrl apiKey usuarioId usuarios />` do MF Clientes |
| `/funil` | `<OportunidadesApp apiUrl apiKey usuarioId usuarios />` do MF Oportunidades |

**Entrar como:** o seletor do topo tem os 8 usuários da massa. Trocar de usuário muda o `usuarioId` passado ao dashboard e aos MFs, e o BFF recalcula o escopo (RF15): Rui (Admin) vê tudo, Ana (Gerente) vê a equipe dela, Pedro (Vendedor) só os dele.

**Resiliência:** cada MF fica dentro de `Suspense` e de um error boundary. Se um MF não carregar (deploy fora do ar, `remoteEntry.js` indisponível) ou quebrar, só a área dele mostra o erro; menu, seletor e as outras telas continuam funcionando.

## Variáveis de ambiente

| Variável | Local | Nuvem |
| --- | --- | --- |
| `VITE_MF_CLIENTES_URL` | `http://localhost:5001` | Vercel do MF Clientes (sem `/remoteEntry.js`) |
| `VITE_MF_OPORTUNIDADES_URL` | `http://localhost:5002` | Vercel do MF Oportunidades |
| `VITE_API_URL` | `http://localhost:3000` (BFF) | URL do API Gateway |
| `VITE_API_KEY` | vazio | API key do Gateway (segredo) |

As URLs dos remotes entram **no build** (o Vite as lê do ambiente), então mude-as na Vercel e faça um novo deploy.

## Estrutura

```
src/
├── App.tsx                  menu, seletor de usuário e rotas; remotes carregados com lazy()
├── remotes.d.ts             tipos dos módulos remotos
├── api/dashboardApi.ts      GET /aggregated-data (gateway, API key, erros)
├── componentes/             ErrorBoundary, SeletorUsuario
├── paginas/                 Dashboard, PaginaMf (Suspense + error boundary)
├── estilos/shell.css
└── testes/                  remotes falsos (alias só no modo test) e dados de exemplo
```

## Tecnologias

React 19, TypeScript 5.9, Vite 7, `@module-federation/vite`, React Router 7, Vitest, Testing Library, Vercel.

## Como rodar localmente

Suba na ordem: MS Clientes (8080), MS Oportunidades (8081), BFF (3000), MF Clientes (5001), MF Oportunidades (5002) e, por último, o Shell. Para os MFs, use `npm run dev` em cada pasta.

```bash
npm install
cp .env.example .env.local
npm run dev                    # http://localhost:5000
npm test                       # dashboard, seletor de usuário, remotes e isolamento de falhas
npm run build && npm run preview
```

## Deploy na Vercel

Importe o repositório com **Root Directory = `shell`**, framework Vite, e as quatro variáveis da tabela. O `vercel.json` reescreve todas as rotas para `index.html`, para o React Router funcionar ao recarregar a página. A URL do Shell vai em `CORS_ORIGENS` do BFF (quem responde os headers de CORS é o BFF; o Gateway só repassa) e no README do repositório central.

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
