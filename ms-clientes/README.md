# MS Clientes — Mini CRM

Microsserviço do domínio **Clientes** do Mini CRM: carteira de clientes e histórico de interações, gravados no **MongoDB Atlas**. CRUD completo documentado com **Swagger**. O status do cliente passa de Prospect para Ativo quando a **Azure Function** consome o evento `OportunidadeGanha` (regra RF30 do Mini CRM).

| | URL |
| --- | --- |
| API na nuvem | `https://<ms-clientes>.azurecontainerapps.io` |
| Swagger | `https://<ms-clientes>.azurecontainerapps.io/docs` |
| OpenAPI (JSON) | `https://<ms-clientes>.azurecontainerapps.io/v3/api-docs` |
| Imagem Docker | `docker.io/<usuario-dockerhub>/minicrm-ms-clientes:v1` |

## Onde ele fica na solução

```
MF Clientes → AWS API Gateway → BFF → MS Clientes → MongoDB Atlas
                                           ↑  POST /clientes/{id}/ativacao
MS Oportunidades → Service Bus (OportunidadeGanha) → Azure Function
```

O front nunca chama este serviço direto: o BFF faz o proxy do CRUD e calcula o escopo do usuário (RF15), mandando a lista de responsáveis visíveis em `responsavelId`.

## Arquitetura do código

**Clean Architecture** em quatro camadas, com a camada de aplicação em **vertical slices** (um pacote por caso de uso):

```
br.pjbl.minicrm.clientes
├── domain/                     Java puro, sem Spring
│   ├── Cliente                   entidade: validações, Prospect/Ativo/Inativo, exclusão lógica, idempotência por evento
│   ├── Interacao, TipoInteracao, StatusCliente
│   ├── ClienteRepository         porta de persistência
│   └── *Exception                erros de domínio
├── application/                um pacote por caso de uso (vertical slice)
│   ├── cadastrarcliente/         Command + Handler
│   ├── buscarcliente/
│   ├── pesquisarclientes/
│   ├── atualizarcliente/
│   ├── excluircliente/
│   ├── registrarinteracao/
│   ├── ativarcliente/            disparado pelo evento OportunidadeGanha
│   └── common/                   views e retentativa
├── infrastructure/
│   ├── mongo/                    Spring Data MongoDB (MongoTemplate + @Version)
│   └── memoria/                  em memória (testes e perfil "memoria")
├── api/                        um controller por caso de uso, DTOs, Swagger, tratador de erros
└── config/                     raiz de composição: liga portas, adaptadores e casos de uso
```

Regras verificadas por **ArchUnit** em `ArquiteturaTest`:

- `domain` depende só de `java.*`; `application` só de `domain` e `java.*`, sem nenhuma classe do Spring.
- `api` e `infrastructure` não se conhecem; só `config` liga as camadas.
- Uma slice não depende de outra (só de `application.common`) e toda slice tem um `Handler`.
- Cada controller usa exatamente um caso de uso.

## Regras de negócio (mesmas da SPEC do Mini CRM)

| Regra | Requisito |
| --- | --- |
| Nome com 3+ caracteres, empresa com 2+, e-mail válido, responsável obrigatório; erros voltam todos juntos | 5.5 |
| Cliente nasce **Prospect** | RF30 |
| Exclusão lógica: vira **Inativo**, some das listagens, histórico preservado | RF03 |
| Busca por nome ou empresa (sem acento), filtros por status e responsável, 20 por página | RF04, RF18 |
| Interação do tipo Ligação, E-mail ou Reunião, observação com 3+ caracteres | RF05 |
| Um responsável por cliente, transferível via PATCH | RF26 |
| **OportunidadeGanha** passa Prospect a **Ativo**; o mesmo evento aplicado duas vezes não muda nada; cliente excluído não volta | RF30 |
| Concorrência otimista (`@Version`) com até 3 retentativas | — |

## Endpoints

| Método | Rota | Uso |
| --- | --- | --- |
| GET | `/clientes?q=&status=&responsavelId=u2,u5&pagina=1&porPagina=20` | Pesquisa paginada |
| GET | `/clientes/{id}` | Cliente + interações (RF06) |
| POST | `/clientes` | Cadastra (201) |
| PATCH | `/clientes/{id}` | Altera / transfere responsável |
| DELETE | `/clientes/{id}` | Exclusão lógica (204) |
| POST | `/clientes/{id}/interacoes` | Registra interação (201) |
| POST | `/clientes/{id}/ativacao` | Chamado pela Function: `{ eventoId, oportunidadeId }` → `{ resultado, cliente }` |
| GET | `/actuator/health` | Saúde |

Erros: `{ status, codigo, erro, erros[] }` — `400 VALIDACAO`, `404 NAO_ENCONTRADO`, `409 CLIENTE_EXCLUIDO`, `409 CONCORRENCIA`.

## Tecnologias

Java 21, Spring Boot 3.5, Spring Data MongoDB, MongoDB Atlas (M0), springdoc-openapi (Swagger UI), Bean Validation, JUnit 5, AssertJ, MockMvc, ArchUnit, Docker, Azure Container Apps, GitHub Actions.

## Como rodar localmente

Pré-requisitos: JDK 21 e Maven 3.9 (ou Docker).

```bash
# sem banco (dados em memória)
mvn spring-boot:run -Dspring-boot.run.profiles=memoria     # http://localhost:8080/docs

# com MongoDB (Atlas ou local)
MONGODB_URI="mongodb+srv://..." mvn spring-boot:run

# tudo em Docker, com MongoDB local
docker compose up --build                                   # http://localhost:8081/docs

# clientes de exemplo
BASE_URL=http://localhost:8080 ./scripts/seed.sh
```

Testes:

```bash
mvn verify                                  # unitários, arquitetura e API
mvn test -Dtest=ArquiteturaTest             # só arquitetura
```

## Docker Hub e deploy

```bash
docker build -t <usuario-dockerhub>/minicrm-ms-clientes:v1 .
docker login
docker push <usuario-dockerhub>/minicrm-ms-clientes:v1
```

O Docker Hub aceita só `usuario/repositorio`, por isso o nome usa hífen em vez de `usuario/minicrm/ms-clientes`. O workflow `.github/workflows/ms-clientes.yml` roda `mvn verify` a cada push e, numa tag `ms-clientes-v*`, publica a imagem (secrets `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN`).

Deploy no Azure Container Apps: `infra/azure/deploy-pessoa-a.sh` na raiz do repositório. Variáveis do container: `MONGODB_URI` (secret), `URL_PUBLICA`. No Atlas, libere o acesso de rede `0.0.0.0/0`, porque o IP de saída do Container Apps não é fixo.

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
