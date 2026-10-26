# MS Oportunidades — Mini CRM (esqueleto para o aluno B)

Microsserviço do **funil de vendas** (Prospecção → Contato → Proposta → Fechado), gravado no **Azure SQL**. Quando uma oportunidade é fechada como **ganha**, ele publica o evento `OportunidadeGanha` no **Azure Service Bus**; a Azure Function consome e o MS Clientes passa o cliente para Ativo (RF30).

> **Situação:** esqueleto funcional criado pela dupla A. Domínio completo do funil, duas slices prontas (criar e mover), adaptadores de Azure SQL e Service Bus, migração Flyway e testes (unitários, ArchUnit e API com H2). O aluno B completa as slices marcadas como **TODO** e o resto da entrega dele.

| | URL |
| --- | --- |
| Swagger | `https://<ms-oportunidades>.azurecontainerapps.io/docs` |
| Imagem Docker | `docker.io/<usuario-dockerhub>/minicrm-ms-oportunidades:v1` |

## Já pronto

| Item | Onde |
| --- | --- |
| Regras do funil da SPEC 5.2: fechar exige resultado; perdido exige motivo da lista fixa; sair do Fechado exige `reabrir`; retrocesso livre | `domain/Oportunidade.java` + `OportunidadeTest` |
| Slice **CriarOportunidade** (`POST /oportunidades`) | `application/criaroportunidade` |
| Slice **MoverOportunidade** (`PATCH /oportunidades/{id}`), que publica `OportunidadeGanha` ao fechar como ganho | `application/moveroportunidade` |
| Azure SQL com JPA, `@Version` (concorrência otimista) e migração Flyway `V1__oportunidades.sql` | `infrastructure/jpa`, `resources/db/migration` |
| Publicação no Service Bus no formato exato do `asyncapi.yaml` (sem `SERVICEBUS_CONNECTION`, vai só para o log) | `infrastructure/eventos` |
| ArchUnit: camadas, Java puro em domain/application, slices independentes | `ArquiteturaTest` |
| CI com `mvn verify` e publicação da imagem em tag `ms-oportunidades-v*` | `.github/workflows/ms-oportunidades.yml` |

## TODO (aluno B)

- [ ] Slice **ListarFunil** — `GET /oportunidades?responsavelId=u2,u5&clienteId=` com as 4 etapas, contagem e soma (RF08, RF19). Ampliar `OportunidadeRepository.listar` com filtros.
- [ ] Slice **BuscarOportunidade** — `GET /oportunidades/{id}`.
- [ ] Slice **AtualizarOportunidade** — `PUT /oportunidades/{id}` (título, valor, data prevista, responsável), só para oportunidades abertas.
- [ ] Slice **ExcluirOportunidade** — `DELETE /oportunidades/{id}`, só se não estiver fechada como ganha.
- [ ] (Opcional) Padrão **Outbox**: gravar o evento numa tabela na mesma transação e publicar depois, para não perder evento se o Service Bus cair.
- [ ] Registrar cada nova slice em `config/CasosDeUsoConfig.java` e criar o teste dela (o ArchUnit cobra as regras sozinho).

Cada slice segue o mesmo molde: `XxxCommand` + `XxxHandler` (Java puro) em `application/xxx/`, um `XxxController` em `api/` e o teste em `src/test`.

## Rodar localmente

```bash
mvn verify                                    # testes com H2 em modo SQL Server
AZURE_SQL_URL=... AZURE_SQL_USUARIO=... AZURE_SQL_SENHA=... mvn spring-boot:run
```

## Alunos

Pedro Henrique Z. Costa · Lincoln Neto · Nicolas Tancon · Leandro Canha
