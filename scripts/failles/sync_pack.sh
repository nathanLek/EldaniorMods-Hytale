#!/bin/zsh
# Failles-donjons : recopie les instances retouchees en jeu (Mods/EldaniorFailles) dans le projet, pour les livrer avec le mod.
# Le mod installe ces modeles au premier lancement sur une autre machine (failles/FaillePack), sans jamais ecraser des retouches.
# A lancer partie fermee (le monde d'edition doit etre sauvegarde : panneau admin › Failles › « Terminer l'edition »).
SRC="$HOME/Library/Application Support/Hytale/UserData/Mods/EldaniorFailles/Server/Instances"
DST="$(cd "$(dirname "$0")/../.." && pwd)/src/main/resources/failles-pack/Server/Instances"
[ -d "$SRC" ] || { echo "Rien a recopier : $SRC n'existe pas (lancer le jeu une fois avec le mod)."; exit 1; }
for d in "$SRC"/*/; do
  n=$(basename "$d")
  rsync -a --delete --exclude '*.bak' --exclude 'instance.bson' "$d" "$DST/$n/"
  echo "recopie : $n ($(ls "$DST/$n/chunks" 2>/dev/null | wc -l | tr -d ' ') region(s))"
done
