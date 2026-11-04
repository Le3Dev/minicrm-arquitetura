#!/usr/bin/env bash
# Cria na Azure os recursos da pessoa B:
#   - Azure SQL (servidor + banco) para o MS Oportunidades
#   - MS Oportunidades no Container Apps (imagem do Docker Hub, Flyway cria as tabelas)
#   - BFF no Container Apps (imagem do Docker Hub)
#
# Reaproveita o resource group e o ambiente do Container Apps criados por deploy-pessoa-a.sh.
# Pré-requisitos: az login; imagens v1.0.0 já publicadas no Docker Hub (tag v1.0.0 em cada repositório).
#
# Uso (valores secretos vêm do ambiente, nunca do repositório):
#   DOCKERHUB_USUARIO=leandrocanha \
#   SQL_SENHA='<senha forte nova>' \
#   SERVICEBUS_CONNECTION='Endpoint=sb://...' \
#   MS_CLIENTES_URL=https://ms-clientes.<...>.azurecontainerapps.io \
#   FUNCTION_URL=https://func-minicrm-<...>.azurewebsites.net \
#   FUNCTION_KEY='<chave da rota indicadores>' \
#   bash infra/azure/deploy-pessoa-b.sh
set -euo pipefail
: "${DOCKERHUB_USUARIO:?Defina DOCKERHUB_USUARIO}"
: "${SQL_SENHA:?Defina SQL_SENHA (mín. 8 caracteres, com maiúscula, minúscula e número)}"
: "${SERVICEBUS_CONNECTION:?Defina SERVICEBUS_CONNECTION (string Send do Service Bus)}"
: "${MS_CLIENTES_URL:?Defina MS_CLIENTES_URL}"
: "${FUNCTION_URL:?Defina FUNCTION_URL}"
: "${FUNCTION_KEY:?Defina FUNCTION_KEY}"

RG="${RG:-rg-minicrm}"
LOCAL="${LOCAL:-centralus}"
# O SQL às vezes não aceita a mesma região do restante em contas de estudante; se falhar, use LOCAL_SQL=outra região permitida.
LOCAL_SQL="${LOCAL_SQL:-$LOCAL}"
SUFIXO="${SUFIXO:-$RANDOM}"
CAE="${CAE:-cae-minicrm}"
SQL_SERVIDOR="sql-minicrm-$SUFIXO"
SQL_BANCO="minicrm"
SQL_USUARIO="minicrmadmin"
TAG="${TAG:-v1.0.0}"
# Origens liberadas no CORS do BFF (URL do Shell na Vercel; preencha depois e rode de novo só o passo do BFF)
CORS_ORIGENS="${CORS_ORIGENS:-http://localhost:5000}"

az extension add --name containerapp --upgrade -y >/dev/null 2>&1 || true
az provider register -n Microsoft.Sql --wait >/dev/null

echo "→ Azure SQL $SQL_SERVIDOR ($LOCAL_SQL)"
az sql server create -g "$RG" -n "$SQL_SERVIDOR" -l "$LOCAL_SQL" \
  --admin-user "$SQL_USUARIO" --admin-password "$SQL_SENHA" -o none
# 0.0.0.0 = permite serviços da Azure (o Container Apps não tem IP fixo)
az sql server firewall-rule create -g "$RG" -s "$SQL_SERVIDOR" -n AzureServices \
  --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 -o none
az sql db create -g "$RG" -s "$SQL_SERVIDOR" -n "$SQL_BANCO" \
  --edition Basic --service-objective Basic -o none
SQL_URL="jdbc:sqlserver://$SQL_SERVIDOR.database.windows.net:1433;database=$SQL_BANCO;encrypt=true;loginTimeout=30"

echo "→ MS Oportunidades"
az containerapp create -g "$RG" -n ms-oportunidades --environment "$CAE" \
  --image "docker.io/$DOCKERHUB_USUARIO/minicrm-ms-oportunidades:$TAG" \
  --target-port 8080 --ingress external --cpu 0.5 --memory 1.0Gi --min-replicas 1 --max-replicas 2 \
  --secrets sql-senha="$SQL_SENHA" servicebus="$SERVICEBUS_CONNECTION" \
  --env-vars AZURE_SQL_URL="$SQL_URL" AZURE_SQL_USUARIO="$SQL_USUARIO" \
             AZURE_SQL_SENHA=secretref:sql-senha SERVICEBUS_CONNECTION=secretref:servicebus -o none
URL_OPORTUNIDADES="https://$(az containerapp show -g "$RG" -n ms-oportunidades --query properties.configuration.ingress.fqdn -o tsv)"

echo "→ BFF"
az containerapp create -g "$RG" -n bff --environment "$CAE" \
  --image "docker.io/$DOCKERHUB_USUARIO/minicrm-bff:$TAG" \
  --target-port 3000 --ingress external --cpu 0.5 --memory 1.0Gi --min-replicas 1 --max-replicas 2 \
  --secrets function-key="$FUNCTION_KEY" \
  --env-vars PORT=3000 MS_CLIENTES_URL="$MS_CLIENTES_URL" MS_OPORTUNIDADES_URL="$URL_OPORTUNIDADES" \
             FUNCTION_URL="$FUNCTION_URL" FUNCTION_KEY=secretref:function-key \
             CORS_ORIGENS="$CORS_ORIGENS" TIMEOUT_MS=20000 -o none
URL_BFF="https://$(az containerapp show -g "$RG" -n bff --query properties.configuration.ingress.fqdn -o tsv)"

cat <<FIM

✔ Pronto. Endereços (não há segredos aqui):

  MS Oportunidades ... $URL_OPORTUNIDADES   (Swagger: $URL_OPORTUNIDADES/docs)
  BFF ................ $URL_BFF   (Swagger: $URL_BFF/docs)

Testes:
  curl $URL_OPORTUNIDADES/actuator/health
  curl "$URL_BFF/aggregated-data?usuarioId=u1"     # deve vir com "falhas": []

Depois da Vercel, libere o Shell no CORS do BFF:
  az containerapp update -g $RG -n bff --set-env-vars CORS_ORIGENS="https://<shell>.vercel.app"

Gateway da AWS:
  BFF_URL=$URL_BFF bash infra/aws/criar-gateway.sh

Se algo não subir: az containerapp logs não funciona com réplica zerada; use o Log Analytics (ContainerAppConsoleLogs_CL).
FIM
