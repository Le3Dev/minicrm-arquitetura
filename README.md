# Mini CRM — Arquitetura distribuída (PJBL)

Evolução do [Mini CRM](https://github.com/PedroZCosta/Mini-CRM) para uma arquitetura distribuída com **microfrontends, API Gateway, BFF, microsserviços, eventos e serverless**, aplicando **Clean Architecture** e **Vertical Slice**.

![Containers](docs/c4/2-containers.png)

## Componentes

| Pasta | Componente | Tecnologia | Dono | Situação |
| --- | --- | --- | --- | --- |
| [`ms-clientes`](ms-clientes) | MS Clientes (MongoDB Atlas) | Java 21, Spring Boot 3.5 | A | pronto, 33 testes |
| [`function`](function) | Azure Function: `/api/indicadores` (HTTP) + `oportunidade-ganha` (Service Bus) | Java 21 | A | pronto, 29 testes |
| [`mf-clientes`](mf-clientes) | Microfrontend de Clientes (remote) | React 19, Vite, Module Federation | A | pronto, 9 testes |
| [`infra/aws`](infra/aws) | AWS API Gateway (API key, throttling, CORS) | OpenAPI + AWS CLI | A | script pronto |
| [`infra/azure`](infra/azure) | Service Bus, Container Apps, Function App | Azure CLI | A | script pronto |
| [`ms-oportunidades`](ms-oportunidades) | MS Oportunidades / funil (Azure SQL) | Java 21, Spring Boot 3.5 | B | esqueleto com 2 slices + TODO |
| `bff` | BFF (`GET /aggregated-data` + proxy) | Node.js | B | a fazer |
| `shell`, `mf-oportunidades` | Shell host e MF do funil | React | B | a fazer |
| [`docs`](docs) | C4 (contexto, containers, componentes), sequência do evento, Canvas | Mermaid | A | pronto |

## Fluxo

1. O usuário abre o **Shell** (Vercel), que carrega os microfrontends.
2. Toda chamada vai ao **AWS API Gateway** com `x-api-key`, que encaminha ao **BFF**.
3. O BFF faz o proxy do CRUD para o **MS Clientes** (MongoDB) e o **MS Oportunidades** (Azure SQL).
4. `GET /aggregated-data`: o BFF busca clientes e oportunidades e manda à **Azure Function** (`/api/indicadores`), devolvendo tudo num único JSON.
5. Ao fechar uma oportunidade como **ganha**, o MS Oportunidades publica `OportunidadeGanha` no **Service Bus**; a Function consome e chama o MS Clientes, que passa o cliente para **Ativo** (RF30), de forma idempotente.

![Sequência do evento](docs/c4/5-sequencia-evento.png)

## Contratos entre as partes

| De → Para | Contrato |
| --- | --- |
| Front → Gateway → BFF | Rotas `/clientes/*`, `/oportunidades/*`, `/aggregated-data`, sempre com `?usuarioId=` |
| BFF → MS Clientes | Swagger em `/docs` (`responsavelId` aceita lista para o escopo do RF15) |
| BFF → Function | `POST /api/indicadores` `{ clientes, oportunidades }` + header `x-functions-key` — `function/src/main/resources/docs/openapi.yaml` |
| MS Oportunidades → Function | Evento `OportunidadeGanha` na fila `oportunidade-ganha` — `function/src/main/resources/docs/asyncapi.yaml` |
| Function → MS Clientes | `POST /clientes/{id}/ativacao` `{ eventoId, oportunidadeId }` |

## Integração contínua

Cada pasta tem seu workflow em `.github/workflows`, disparado só quando ela muda. Os serviços Java publicam o resumo dos testes como um check no commit (aba **Checks**). Em uma tag `ms-clientes-v1.0.0` (ou `ms-oportunidades-v…`), a imagem vai para o Docker Hub (secrets `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN`).

## Links da entrega (preencher após o deploy)

| Item | URL |
| --- | --- |
| Front (Shell) | |
| MF Clientes | |
| API Gateway | |
| Swagger BFF | |
| Swagger MS Clientes | |
| Swagger MS Oportunidades | |
| Swagger / Eventos da Function | |
| Docker Hub | |
| Vídeo (YouTube, não listado) | |

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
