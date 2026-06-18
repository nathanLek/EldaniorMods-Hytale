# Directive Sabrina — Vérification API Craft/Bench Hytale

## Contexte
On implémente un système de progression craft avec **boost de vitesse continu** (x1.0 → x2.0 sur 10 000 procs). Avant de coder, on a besoin de savoir ce que l'API Hytale permet.

## Ta mission
Cherche dans la doc MCP hytale-docs ET dans les assets/code du projet :

1. **Peut-on modifier la vitesse/durée de craft d'un bench ?** (durée d'animation, cooldown entre crafts, craft speed multiplier, stat native)
2. **Peut-on modifier la quantité produite par un craft ?** (pour implémenter "double craft" — chance de produire 2x l'item)
3. **Quels événements craft sont disponibles ?** On utilise déjà `PlayerCraftEvent` — y a-t-il des events Pre/Post, bench open/close, ou d'autres hooks ?
4. **Y a-t-il un système de craft speed natif dans le moteur ?** (stat, config, modifier sur le joueur ou le bench)

## Où chercher dans le code existant
- `src/main/java/com/eldanior/system/Leveling/systems/CraftingProgressionSystem.java` — utilise `PlayerCraftEvent`
- `src/main/java/com/eldanior/system/Leveling/systems/CraftingRestrictionSystem.java` — utilise `BenchBlock`, `UseBlockEvent.Pre`
- Imports Hytale : `com.hypixel.hytale.builtin.crafting.*`, `PlayerCraftEvent`, `BenchBlock`
- Cherche aussi dans les assets : `run/mods/Hytale/` pour des configs de bench/recette

## Livrable attendu
Un rapport structuré en français :
- Pour chaque question : OUI/NON + comment (classe, méthode, signature) ou alternative si NON
- Sources citées (page doc MCP ou fichier:ligne)
- Si la vitesse de craft n'est pas modifiable, propose des alternatives techniques (cooldown custom, skip animation, batch craft)
