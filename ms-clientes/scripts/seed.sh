#!/usr/bin/env bash
# Cadastra clientes de exemplo pela API (útil antes de gravar o vídeo).
# Uso: BASE_URL=https://<ms-clientes> ./scripts/seed.sh     (padrão: http://localhost:8080)
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8080}"
cadastrar() {
  curl -fsS -X POST "$BASE_URL/clientes" -H 'content-type: application/json' \
    -d "{\"nome\":\"$1\",\"empresa\":\"$2\",\"email\":\"$3\",\"telefone\":\"$4\",\"responsavelId\":\"$5\"}" >/dev/null
  echo "✔ $1 ($2)"
}
cadastrar "Mariana Souza"   "Acme Ltda"          "mariana@acme.com.br"     "(41) 99811-2030" "u2"
cadastrar "João Pereira"    "Café Brasil"        "joao@cafebrasil.com"     "(41) 99722-1144" "u2"
cadastrar "Ana Lima"        "Lima Engenharia"    "ana@limaeng.com.br"      "(11) 98123-4567" "u5"
cadastrar "Bruno Alves"     "TechSul Sistemas"   "bruno@techsul.com"       "(51) 99654-3210" "u5"
cadastrar "Carla Mendes"    "Mendes Logística"   "carla@mendeslog.com.br"  "(41) 99900-1122" "u1"
cadastrar "Diego Ramos"     "Ramos & Filhos"     "diego@ramosfilhos.com"   "(21) 99777-8899" "u2"
cadastrar "Fernanda Castro" "Castro Alimentos"   "fernanda@castro.com.br"  "(31) 98888-0001" "u5"
cadastrar "Gustavo Prado"   "Prado Construtora"  "gustavo@prado.com.br"    "(41) 99101-2020" "u1"
