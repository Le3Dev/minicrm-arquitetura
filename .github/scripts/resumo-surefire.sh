#!/usr/bin/env bash
# Soma os relatórios do Surefire e publica um check run "<nome>: testes" no commit,
# com o total de testes por classe (aparece na aba Checks do GitHub).
# Uso: resumo-surefire.sh <nome> <pasta-dos-relatorios>
set -euo pipefail
NOME="$1"; PASTA="$2"
shopt -s nullglob
arquivos=("$PASTA"/TEST-*.xml)
total=0; falhas=0; erros=0; pulados=0; linhas=""
for f in "${arquivos[@]}"; do
  cab=$(grep -m1 -o '<testsuite [^>]*>' "$f")
  attr() { echo "$cab" | grep -o " $1=\"[^\"]*\"" | head -1 | cut -d'"' -f2; }
  n=$(attr tests); fl=$(attr failures); er=$(attr errors); sk=$(attr skipped)
  classe=$(attr name)
  total=$((total+n)); falhas=$((falhas+fl)); erros=$((erros+er)); pulados=$((pulados+${sk:-0}))
  linhas+="| \`${classe##*.}\` | $n | $fl | $er |"$'\n'
done
conclusao=success; [ $((falhas+erros)) -gt 0 ] && conclusao=failure
[ "$total" -eq 0 ] && conclusao=failure
titulo="$total testes, $falhas falhas, $erros erros, $pulados pulados"
resumo=$'| Classe | Testes | Falhas | Erros |\n|---|---|---|---|\n'"$linhas"
echo "$titulo"; echo "$resumo"
echo "## $NOME — $titulo" >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
echo "$resumo" >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
jq -n --arg name "$NOME: testes" --arg sha "$GITHUB_SHA" --arg c "$conclusao" --arg t "$titulo" --arg s "$resumo" \
  '{name:$name, head_sha:$sha, status:"completed", conclusion:$c, output:{title:$t, summary:$s}}' \
  | gh api "repos/$GITHUB_REPOSITORY/check-runs" --input - > /dev/null
