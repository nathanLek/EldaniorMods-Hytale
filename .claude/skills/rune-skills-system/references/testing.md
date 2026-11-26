# Valider une rune et rendre compte

## Avant de déployer
`./gradlew jar` écrit dans `UserData/Mods` : **le jar de la partie en cours**. Si `pgrep -fl HytaleServer.jar` montre un serveur `--singleplayer`, ne lancer ni `jar` ni `runServer` (NoClassDefFoundError, freezes) : s'arrêter à `compileJava` + `check` et demander à l'utilisateur de quitter le jeu.

## Performance des visuels
`SpellFx.fx` / `arc` / `shake` / `sound2dNear` trouvent les joueurs via la liste du monde (`playersNear`), jamais par une sphère d'entités : ne pas réintroduire `getAllEntitiesInSphere` sur un grand rayon dans une boucle de particules.

## Vérifications automatiques (toujours)
1. `./gradlew compileJava -q` : aucune erreur.
2. `python3 scripts/runes/rune_tool.py check <Id>` : 0 erreur. Vérifie l'item, la qualité, les icônes, la racine et les interactions,
   les types d'interaction (vanilla ou enregistrés), le sort `RuneSpell` (classe + enregistrement), particules, sons, animations,
   classes, parchemin, loot, coût/recharge par rapport à la rareté, systèmes de particules qui bouclent, chemins de modèles, trails.
3. `./gradlew jar -q` : le mod part dans `UserData/Mods`.
4. Chargement des assets sur serveur headless :
   ```bash
   (./gradlew runServer -q < /dev/null > /tmp/runserver.out 2>&1 &)
   # attendre "Hytale Server Booted" dans /tmp/runserver.out (~10-60 s)
   grep -iE "Failed to validate|Asset validation FAILED|Exception|<Id>" /tmp/runserver.out
   pkill -f HytaleServer.jar
   ```
   Bruit connu, sans rapport : `Launch_Projectile_Spell` (deprecated),
   `skill_page_* ... incorrect format`, `rune_page_* ... incorrect format` / `Unused key: Id` (convention de nommage des parchemins).
   Le serveur écrit dans `run/` (logs, monde) : le signaler.

## Test en jeu (fait par l'utilisateur ; donner une checklist)
- `/es runes` → équiper la rune ; parchemin : l'utiliser et vérifier que la rune arrive dans le sac.
- Animation et particules d'incantation visibles ; coût de mana et recharge corrects.
- Effet principal sur un monstre et sur un allié (groupe) : l'allié doit être épargné.
- Visibilité des particules de jour, contre le ciel, et de loin.
- Terrain : modifié puis restauré après le délai ; rien dans une ville ni une parcelle.
- Mort de la cible, déconnexion pendant le sort, relancement pendant la recharge.
- Logs du monde solo : `~/Library/Application Support/Hytale/UserData/Saves/<Monde>/logs/*_server.log` (rechercher `Exception`, `[RuneSpell]`).

## Rapport (format)
1. Ce qui a été créé/modifié (liste de fichiers).
2. Valeurs : type, rareté, mana, recharge, dégâts/effets, portée, durée, terrain.
3. Vérifié : compilation / `check` / chargement des assets (dire lesquels ont réellement été faits).
4. Non vérifié : rendu en jeu, sensation, équilibrage → checklist ci-dessus.
5. Limitations techniques rencontrées et alternatives choisies.
