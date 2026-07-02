#!/usr/bin/env bash
# build-index.sh — Genere un index compact du repo Eldanior dans .claude/index/
# But : eviter que les agents ré-explorent le repo (Grep/Glob/Read massifs) a chaque tache.
# Les agents lisent ces fichiers texte legers au lieu de scanner 1600+ fichiers.
#
# Usage : ./scripts/build-index.sh   (lance aussi automatiquement au SessionStart via hook)
set -euo pipefail

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/.." && pwd)}"
cd "$ROOT"
OUT=".claude/index"
mkdir -p "$OUT"
SRC="src/main/java/com/eldanior/system"
RES="src/main/resources"
LANG="$RES/Server/Languages/en-US/server.lang"

# 1) Carte des classes Java : chemin relatif de chaque .java (localisation instantanee)
find "$SRC" -name "*.java" | sed "s|^$SRC/||" | sort > "$OUT/classes.txt"

# 2) Skills passifs : ID enum | Nom | (impl) depuis PassiveSkill.java
PS="$SRC/skills/skillsInteraction/PassiveSkill.java"
if [ -f "$PS" ]; then
  grep -oE '[A-Z][A-Z0-9_]+\("[A-Z0-9_]+",[[:space:]]*"[^"]*"' "$PS" \
    | sed -E 's/^[A-Z0-9_]+\("([A-Z0-9_]+)",[[:space:]]*"([^"]*)"/\1 | \2/' \
    | sort -u > "$OUT/skills-passives.txt" || true
fi

# 3) Definitions de classes : id | ClassType | tier | fichier
{
  find "$SRC/classes/definitions" -name "*.java" | while read -r f; do
    id=$(grep -oE 'super\("[a-z0-9_]+"' "$f" | head -1 | sed -E 's/super\("([a-z0-9_]+)"/\1/')
    ctype=$(grep -oE 'ClassType\.[A-Z]+' "$f" | head -1 | sed 's/ClassType\.//')
    tier=$(grep -oE ',\s*[0-9]{3},\s*(true|false)' "$f" | head -1 | grep -oE '[0-9]{3}' | head -1)
    rel=${f#"$SRC"/}
    printf '%s | %s | %s | %s\n' "${id:-?}" "${ctype:-?}" "${tier:-?}" "$rel"
  done
} | sort > "$OUT/class-defs.txt" || true

# 4) Cles de langue (pour detecter les manquantes/orphelines)
if [ -f "$LANG" ]; then
  grep -oE '^[a-zA-Z0-9_.]+=' "$LANG" | sed 's/=$//' | sort -u > "$OUT/lang-keys.txt" || true
fi

# 5) Managers / Systems / Tabs (points d'entree architecturaux)
grep -rlE 'class .*(Manager|System|Tab|Screen|Command)\b' "$SRC" --include=*.java 2>/dev/null \
  | sed "s|^$SRC/||" | sort > "$OUT/entrypoints.txt" || true

# 6) Résumé chiffré + mode d'emploi
JCOUNT=$(wc -l < "$OUT/classes.txt" | tr -d ' ')
SCOUNT=$( [ -f "$OUT/skills-passives.txt" ] && wc -l < "$OUT/skills-passives.txt" | tr -d ' ' || echo 0)
CCOUNT=$( [ -f "$OUT/class-defs.txt" ] && wc -l < "$OUT/class-defs.txt" | tr -d ' ' || echo 0)
LCOUNT=$( [ -f "$OUT/lang-keys.txt" ] && wc -l < "$OUT/lang-keys.txt" | tr -d ' ' || echo 0)
JSONCOUNT=$(find "$RES" -name "*.json" | wc -l | tr -d ' ')

cat > "$OUT/SUMMARY.md" <<EOF
# Index Eldanior — genere le $(date '+%Y-%m-%d %H:%M') par scripts/build-index.sh

NE PAS EDITER A LA MAIN. Regenere au SessionStart et via ./scripts/build-index.sh.

## Chiffres
- Classes Java : $JCOUNT
- Skills passifs (enum) : $SCOUNT
- Definitions de classes : $CCOUNT
- Cles de langue en-US : $LCOUNT
- Ressources JSON : $JSONCOUNT

## Fichiers d'index (a grep AU LIEU d'explorer le repo)
- classes.txt        — chemin de chaque classe Java (localiser sans Glob)
- entrypoints.txt    — Managers / Systems / Tabs / Screens / Commands
- skills-passives.txt — ID_ENUM | Nom  (verifier existence/doublon avant creation)
- class-defs.txt     — id | type | tier | fichier
- lang-keys.txt      — cles de traduction existantes

## Exemples
- Localiser une classe : grep -i guildmanager .claude/index/classes.txt
- Verifier un skill avant d'en creer un : grep -i "phoenix" .claude/index/skills-passives.txt
- Verifier une cle de langue : grep "skill_page_fly" .claude/index/lang-keys.txt
EOF

echo "Index regenere dans $OUT/ ($JCOUNT classes, $SCOUNT skills, $CCOUNT class-defs, $LCOUNT lang-keys)"
