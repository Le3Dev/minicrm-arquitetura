# Documentação de arquitetura

| Artefato | Arquivo | Fonte editável |
| --- | --- | --- |
| C4 — Contexto | `c4/1-contexto.png` | `c4/1-contexto.mmd` |
| C4 — Containers | `c4/2-containers.png` | `c4/2-containers.mmd` |
| C4 — Componentes do MS Clientes | `c4/3-componentes-ms-clientes.png` | `c4/3-componentes-ms-clientes.mmd` |
| C4 — Componentes do BFF | `c4/4-componentes-bff.png` | `c4/4-componentes-bff.mmd` |
| Sequência do evento OportunidadeGanha | `c4/5-sequencia-evento.png` | `c4/5-sequencia-evento.mmd` |
| Canvas de arquitetura | `canvas/canvas.png` | `canvas/canvas.html` |
| Contrato do evento (AsyncAPI) | [asyncapi.yaml no minicrm-function](https://github.com/Le3Dev/minicrm-function/blob/main/src/main/resources/docs/asyncapi.yaml) | — |

Nos diagramas, **azul** = componentes da dupla A e **laranja** = componentes do aluno B.

Para regenerar as imagens: `npx @mermaid-js/mermaid-cli -i c4/2-containers.mmd -o c4/2-containers.png -b white -s 2`.
