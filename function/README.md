# Azure Function — Mini CRM

Função serverless em **Java 21** com dois gatilhos:

| Gatilho | Nome | O que faz |
| --- | --- | --- |
| **HTTP** `POST /api/indicadores` | `indicadores` | Recebe clientes e oportunidades do BFF e devolve os indicadores do dashboard: clientes por status, funil por etapa, valor em negociação, valor ganho, taxa de conversão, motivos de perda e ranking de vendedores (RF11, RF19, RF29, RF31). |
| **Service Bus**, fila `oportunidade-ganha` | `oportunidadeGanha` | Consome o evento `OportunidadeGanha` publicado pelo MS Oportunidades e chama o MS Clientes para passar o cliente a **Ativo** (RF30). |

| | URL |
| --- | --- |
| Function App | `https://<function-app>.azurewebsites.net` |
| Swagger da rota HTTP | `https://<function-app>.azurewebsites.net/api/docs` |
| Eventos (AsyncAPI) | `https://<function-app>.azurewebsites.net/api/eventos` |
| Saúde | `https://<function-app>.azurewebsites.net/api/health` |

## Onde ela fica na solução

```
                    GET /aggregated-data
Front → API Gateway → BFF ─────────────────► POST /api/indicadores        (HTTP Trigger)
                       │
                       └► MS Oportunidades ─► Service Bus "oportunidade-ganha" ─► oportunidadeGanha (Service Bus Trigger)
                                                                                     │ POST /clientes/{id}/ativacao
                                                                                     ▼
                                                                                MS Clientes
```

## Arquitetura do código

```
br.pjbl.minicrm.function
├── domain/                          Java puro
│   ├── CalculadoraDeIndicadores       regra dos indicadores
│   ├── OportunidadeGanhaEvento        contrato e validação do evento
│   └── Etapa, *Resumo, exceções
├── application/                     vertical slices
│   ├── calcularindicadores/           slice da rota HTTP
│   └── ativarcliente/                 slice do evento + porta ClientesGateway
├── infrastructure/
│   ├── ClientesHttpGateway            java.net.http → MS Clientes
│   ├── Json, Configuracao
└── functions/                       gatilhos (@FunctionName) = entrada e raiz de composição
    ├── IndicadoresFunction
    ├── OportunidadeGanhaFunction
    └── DocumentacaoFunction           /api/docs, /api/eventos, /api/health
src/main/resources/docs/
├── openapi.yaml                     Swagger da rota HTTP
└── asyncapi.yaml                    contrato do evento OportunidadeGanha
```

O `ArquiteturaTest` (ArchUnit) garante que `domain` e `application` são Java puro, que o SDK do Azure só aparece em `functions` e que as slices não se conhecem.

### Como o evento é tratado

| Resposta do MS Clientes | Resultado | Mensagem |
| --- | --- | --- |
| 200 `ATIVADO` ou `JA_ATIVO` | cliente Ativo | concluída |
| 200 `REPETIDO` | evento já processado, nada muda (idempotência) | concluída |
| 200 `IGNORADO_EXCLUIDO` | cliente excluído não volta a Ativo | concluída |
| 404 | pendência no log (warning) | concluída |
| rede, timeout, 5xx, 409 `CONCORRENCIA` | exceção | o Service Bus entrega de novo; depois de `maxDeliveryCount`, dead-letter |
| mensagem malformada | erro no log | concluída (repetir não adianta) |

## Tecnologias

Java 21, Azure Functions (Java library 3.1, plano Flex Consumption), Azure Service Bus (Basic), Jackson, `java.net.http`, JUnit 5, AssertJ, Mockito, ArchUnit, OpenAPI 3, AsyncAPI 3.

## Como rodar localmente

Pré-requisitos: JDK 21, Maven 3.9, [Azure Functions Core Tools v4](https://learn.microsoft.com/azure/azure-functions/functions-run-local) e o [Azurite](https://learn.microsoft.com/azure/storage/common/storage-use-azurite). O gatilho do Service Bus precisa de um namespace real.

```bash
cp local.settings.example.json local.settings.json   # preencha ServiceBusConnection e MS_CLIENTES_URL
mvn clean package
mvn azure-functions:run                              # http://localhost:7071/api/docs
```

Teste da rota HTTP (localmente a chave não é exigida):

```bash
curl -X POST http://localhost:7071/api/indicadores -H 'content-type: application/json' \
  -d '{"clientes":[{"id":"c1","status":"Ativo"}],"oportunidades":[{"id":"1","etapa":"Fechado","resultado":"ganho","valor":1000,"responsavelId":"u2"}]}'
```

Publicar um `OportunidadeGanha` de teste sem o MS Oportunidades: no portal, **Service Bus → Filas → oportunidade-ganha → Service Bus Explorer → Send messages**, colando o exemplo de `asyncapi.yaml` com o `clienteId` de um cliente real.

Testes: `mvn verify`.

## Deploy

```bash
az login
mvn clean package azure-functions:deploy -DfunctionAppName=<nome> -DfunctionResourceGroup=rg-minicrm
```

Configurações da Function App:

| Nome | Valor |
| --- | --- |
| `ServiceBusConnection` | connection string da política **Listen** da fila |
| `MS_CLIENTES_URL` | URL pública do MS Clientes no Container Apps |

A rota `indicadores` usa `authLevel = FUNCTION`: o BFF manda a chave no header `x-functions-key` (portal → Function App → App keys). As rotas de documentação e saúde são anônimas.

## Contrato com o grupo

- **MS Oportunidades** publica na fila `oportunidade-ganha` exatamente o formato de `asyncapi.yaml`, com um `eventId` (UUID) novo por fechamento.
- **BFF** chama `POST /api/indicadores` com `{ clientes, oportunidades }` e o header `x-functions-key`.

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
