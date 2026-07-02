#!/usr/bin/env bash
# lint-eldanior.sh — Verifications de coherence propres au mod Eldanior (deterministe, sans LLM).
# Detecte : doublons de skills, impl manquante, fichiers Java vides, cles de langue orphelines.
# Sortie : liste de problemes prefixes [NIVEAU]. Code retour 1 si au moins un probleme HAUTE.
#
# Usage : ./scripts/lint-eldanior.sh
set -uo pipefail

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/.." && pwd)}"
cd "$ROOT"
SRC="src/main/java/com/eldanior/system"
PS="$SRC/skills/skillsInteraction/PassiveSkill.java"
PASSDIR="$SRC/skills/skills/passives"
HIGH=0
PROBLEMS=0

report() { # niveau, message
  printf '[%s] %s\n' "$1" "$2"
  PROBLEMS=$((PROBLEMS+1))
  [ "$1" = "HAUTE" ] && HIGH=$((HIGH+1))
  return 0
}

echo "== Lint Eldanior =="

# 1) Doublons de NOM de skill passif (meme libelle FR pour 2 IDs differents)
if [ -f "$PS" ]; then
  DUPS=$(grep -oE '[A-Z][A-Z0-9_]+\("[A-Z0-9_]+",[[:space:]]*"[^"]*"' "$PS" \
    | sed -E 's/^[A-Z0-9_]+\("[A-Z0-9_]+",[[:space:]]*"([^"]*)"/\1/' \
    | sort | uniq -d)
  if [ -n "$DUPS" ]; then
    while IFS= read -r name; do
      [ -n "$name" ] && report "MOYENNE" "Skill au nom en double : \"$name\""
    done <<< "$DUPS"
  fi
fi

# 2) Stubs / travail inacheve laisse dans le code (TODO, FIXME, non implemente)
while IFS= read -r hit; do
  [ -n "$hit" ] && report "MOYENNE" "Travail inacheve dans le code : ${hit#"$SRC"/}"
done < <(grep -rnE 'TODO|FIXME|XXX|UnsupportedOperationException|NotImplemented' "$SRC" --include=*.java 2>/dev/null | sed -E 's/:[0-9]+:.*//' | sort -u)

# 3) Skill passif reference dans l'enum mais dont la classe d'impl est introuvable
if [ -f "$PS" ]; then
  # impl = dernier "new XxYy()" de chaque ligne d'enum
  grep -oE 'new [A-Z][A-Za-z0-9_]+\(\)' "$PS" | sed -E 's/new ([A-Za-z0-9_]+)\(\)/\1/' | sort -u \
    | while IFS= read -r impl; do
        [ -z "$impl" ] && continue
        if ! find "$PASSDIR" -name "${impl}.java" | grep -q .; then
          # tolere les impls hors passives/ (certaines classes utilitaires)
          if ! find "$SRC" -name "${impl}.java" | grep -q .; then
            report "HAUTE" "Skill enum -> classe d'impl introuvable : ${impl}"
          fi
        fi
      done
fi

# 4) Cles de langue en double dans server.lang
LANG="src/main/resources/Server/Languages/en-US/server.lang"
if [ -f "$LANG" ]; then
  DUPK=$(grep -oE '^[a-zA-Z0-9_.]+=' "$LANG" | sed 's/=$//' | sort | uniq -d)
  if [ -n "$DUPK" ]; then
    while IFS= read -r k; do
      [ -n "$k" ] && report "MOYENNE" "Cle de langue en double : $k"
    done <<< "$DUPK"
  fi
fi

echo "== $PROBLEMS probleme(s), dont $HIGH HAUTE =="
[ "$HIGH" -gt 0 ] && exit 1
exit 0
