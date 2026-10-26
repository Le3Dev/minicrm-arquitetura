#!/usr/bin/env bash
# Cria na Azure os recursos da dupla A:
#   - Resource group
#   - Service Bus (Basic) + fila oportunidade-ganha + políticas Send (MS Oportunidades) e Listen (Function)
#   - Ambiente do Container Apps + MS Clientes (imagem do Docker Hub)
#   - Storage + Function App Java 21 (Flex Consumption) com as configurações
#
# Pré-requisitos: az login; imagem do MS Clientes já publicada no Docker Hub; connection string do MongoDB Atlas.
# Uso:
#   DOCKERHUB_USUARIO=meuusuario MONGODB_URI="mongodb+srv://..." ./infra/azure/deploy-pessoa-a.sh
set -euo pipefail
: "${DOCKERHUB_USUARIO:?Defina DOCKERHUB_USUARIO}"
: "${MONGODB_URI:?Defina MONGODB_URI (connection string do Atlas)}"

RG="${RG:-rg-minicrm}"
LOCAL="${LOCAL:-brazilsouth}"
SUFIXO="${SUFIXO:-$RANDOM}"
SB="sb-minicrm-$SUFIXO"
FILA="oportunidade-ganha"
CAE="cae-minicrm"
MS_CLIENTES="ms-clientes"
STORAGE="stminicrm$SUFIXO"
FUNC="func-minicrm-$SUFIXO"
TAG="${TAG:-ms-clientes-v1.0.0}"

az extension add --name containerapp --upgrade -y >/dev/null 2>&1 || true
az provider register -n Microsoft.App --wait >/dev/null
az provider register -n Microsoft.OperationalInsights --wait >/dev/null

echo "→ Resource group $RG ($LOCAL)"
az group create -n "$RG" -l "$LOCAL" -o none

echo "→ Service Bus $SB + fila $FILA"
az servicebus namespace create -g "$RG" -n "$SB" -l "$LOCAL" --sku Basic -o none
az servicebus queue create -g "$RG" --namespace-name "$SB" -n "$FILA" --max-delivery-count 5 -o none
az servicebus queue authorization-rule create -g "$RG" --namespace-name "$SB" --queue-name "$FILA" -n ms-oportunidades-send --rights Send -o none
az servicebus queue authorization-rule create -g "$RG" --namespace-name "$SB" --queue-name "$FILA" -n function-listen --rights Listen -o none
SB_SEND=$(az servicebus queue authorization-rule keys list -g "$RG" --namespace-name "$SB" --queue-name "$FILA" -n ms-oportunidades-send --query primaryConnectionString -o tsv)
SB_LISTEN=$(az servicebus queue authorization-rule keys list -g "$RG" --namespace-name "$SB" --queue-name "$FILA" -n function-listen --query primaryConnectionString -o tsv)

echo "→ Ambiente do Container Apps + MS Clientes"
az containerapp env create -g "$RG" -n "$CAE" -l "$LOCAL" -o none
az containerapp create -g "$RG" -n "$MS_CLIENTES" --environment "$CAE" \
  --image "docker.io/$DOCKERHUB_USUARIO/minicrm-ms-clientes:$TAG" \
  --target-port 8080 --ingress external --cpu 0.5 --memory 1.0Gi --min-replicas 0 --max-replicas 2 \
  --secrets mongodb-uri="$MONGODB_URI" \
  --env-vars MONGODB_URI=secretref:mongodb-uri -o none
URL_CLIENTES="https://$(az containerapp show -g "$RG" -n "$MS_CLIENTES" --query properties.configuration.ingress.fqdn -o tsv)"
az containerapp update -g "$RG" -n "$MS_CLIENTES" --set-env-vars URL_PUBLICA="$URL_CLIENTES" -o none

echo "→ Function App $FUNC (Java 21, Flex Consumption)"
az storage account create -g "$RG" -n "$STORAGE" -l "$LOCAL" --sku Standard_LRS -o none
az functionapp create -g "$RG" -n "$FUNC" --storage-account "$STORAGE" --flexconsumption-location "$LOCAL" \
  --runtime java --runtime-version 21 -o none
az functionapp config appsettings set -g "$RG" -n "$FUNC" --settings \
  ServiceBusConnection="$SB_LISTEN" MS_CLIENTES_URL="$URL_CLIENTES" -o none

cat <<FIM

✔ Recursos criados. Guarde estes valores (não commite):

  MS Clientes ............ $URL_CLIENTES   (Swagger: $URL_CLIENTES/docs)
  Function App ........... $FUNC
  Service Bus (Send) ..... para o aluno B usar no MS Oportunidades (SERVICEBUS_CONNECTION):
    $SB_SEND

Próximos passos:
  1. Publicar a Function:   cd function && mvn clean package azure-functions:deploy -DfunctionAppName=$FUNC -DfunctionResourceGroup=$RG
  2. Pegar a chave da rota indicadores para o BFF:
       az functionapp function keys list -g $RG -n $FUNC --function-name indicadores --query default -o tsv
  3. No Atlas: Network Access → Add IP → 0.0.0.0/0 (o IP de saída do Container Apps não é fixo).
  4. Quando o BFF estiver no ar: BFF_URL=<url> ./infra/aws/criar-gateway.sh
FIM
