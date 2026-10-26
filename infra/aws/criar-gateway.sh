#!/usr/bin/env bash
# Cria o AWS API Gateway do Mini CRM na frente do BFF: REST API + stage "prod" + API key + usage plan
# com throttling (10 req/s, rajada 20, 5.000 req/dia).
#
# Pré-requisitos: AWS CLI v2 configurado (aws configure) e a URL pública do BFF.
# Uso: BFF_URL=https://bff.<id>.brazilsouth.azurecontainerapps.io ./infra/aws/criar-gateway.sh
set -euo pipefail
: "${BFF_URL:?Defina BFF_URL com a URL pública do BFF (sem barra no final)}"
REGIAO="${AWS_REGION:-us-east-1}"
NOME="${NOME_API:-minicrm-gateway}"
PASTA="$(cd "$(dirname "$0")" && pwd)"

DEF=$(mktemp --suffix=.yaml)
sed "s#__BFF_URL__#${BFF_URL%/}#g" "$PASTA/api-gateway.openapi.yaml" > "$DEF"

echo "→ Importando a REST API..."
API_ID=$(aws apigateway import-rest-api --region "$REGIAO" --parameters endpointConfigurationTypes=REGIONAL \
  --body "fileb://$DEF" --query id --output text)
aws apigateway update-rest-api --region "$REGIAO" --rest-api-id "$API_ID" \
  --patch-operations op=replace,path=/name,value="$NOME" >/dev/null

echo "→ Publicando o stage prod..."
aws apigateway create-deployment --region "$REGIAO" --rest-api-id "$API_ID" --stage-name prod >/dev/null

echo "→ Criando API key e usage plan (throttling)..."
KEY_ID=$(aws apigateway create-api-key --region "$REGIAO" --name "$NOME-front" --enabled --query id --output text)
KEY_VALOR=$(aws apigateway get-api-key --region "$REGIAO" --api-key "$KEY_ID" --include-value --query value --output text)
PLANO_ID=$(aws apigateway create-usage-plan --region "$REGIAO" --name "$NOME-plano" \
  --throttle burstLimit=20,rateLimit=10 --quota limit=5000,period=DAY \
  --api-stages apiId="$API_ID",stage=prod --query id --output text)
aws apigateway create-usage-plan-key --region "$REGIAO" --usage-plan-id "$PLANO_ID" --key-id "$KEY_ID" --key-type API_KEY >/dev/null

URL="https://$API_ID.execute-api.$REGIAO.amazonaws.com/prod"
echo
echo "✔ API Gateway pronto"
echo "  URL:     $URL"
echo "  API key: $KEY_VALOR   (use no front como VITE_API_KEY; não commite)"
echo
echo "Teste (deve dar 403 sem chave e 200 com chave):"
echo "  curl -i $URL/aggregated-data"
echo "  curl -i -H 'x-api-key: $KEY_VALOR' $URL/aggregated-data"
rm -f "$DEF"
