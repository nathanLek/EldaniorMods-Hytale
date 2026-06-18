# Directive Patrick — Implémenter les paliers de progression Craft

## Contexte
Le système de craft a déjà 10 skills (CRAFT_CUISINE, CRAFT_FONDERIE, etc.) avec un compteur de procs (0 → 10 000 dans `PlayerLevelData.skillProcs`). On veut ajouter des **paliers de maîtrise** et un **boost de vitesse continu**.

## Spec validée

### Paliers
| Palier | Procs requis | Titre | Bonus |
|--------|-------------|-------|-------|
| Novice | 0 | - | Accès au bench uniquement |
| Apprenti | 500 | "Apprenti [Métier]" | - |
| Compagnon | 2 000 | "Compagnon [Métier]" | Double craft 5% |
| Expert | 5 000 | "Expert [Métier]" | Double craft 8% |
| Maître | 10 000 | "Maître [Métier]" | Double craft 10% |

### Vitesse de craft continue
- Progression linéaire de x1.0 (0 procs) à x2.0 (10 000 procs)
- Formule : `speedMultiplier = 1.0 + (procs / 10000.0)`
- **ATTENTION** : Sabrina doit d'abord confirmer si l'API Hytale permet de modifier la vitesse de craft. Attends son rapport avant d'implémenter cette partie. Si c'est pas possible, on trouvera une alternative.

### Titres génériques
Les 9 métiers avec 4 paliers titrés = 36 titres :
- Cuisine : Apprenti Cuisinier, Compagnon Cuisinier, Expert Cuisinier, Maître Cuisinier
- Fonderie : Apprenti Fondeur, Compagnon Fondeur, Expert Fondeur, Maître Fondeur
- Armurerie : Apprenti Armurier, Compagnon Armurier, Expert Armurier, Maître Armurier
- Forge d'Armes : Apprenti Forgeron, Compagnon Forgeron, Expert Forgeron, Maître Forgeron
- Tannerie : Apprenti Tanneur, Compagnon Tanneur, Expert Tanneur, Maître Tanneur
- Alchimie : Apprenti Alchimiste, Compagnon Alchimiste, Expert Alchimiste, Maître Alchimiste
- Scierie : Apprenti Bûcheron, Compagnon Bûcheron, Expert Bûcheron, Maître Bûcheron
- Agriculture : Apprenti Fermier, Compagnon Fermier, Expert Fermier, Maître Fermier
- Recyclage : Apprenti Recycleur, Compagnon Recycleur, Expert Recycleur, Maître Recycleur

### Double craft
Quand un joueur craft au palier Compagnon+, il a une chance (5/8/10%) de recevoir le double de l'item crafté. Implémente ça dans `CraftingProgressionSystem.onPlayerCraft()`.

## Fichiers à modifier
1. **`CraftingProgressionSystem.java`** (`src/main/java/com/eldanior/system/Leveling/systems/`) — ajouter la logique de paliers + double craft
2. **`PlayerLevelData.java`** — ajouter une méthode `getCraftTier(String skillId)` qui retourne le palier (enum ou int) basé sur les procs
3. **Titres** — ajouter les 36 titres dans le système de titres existant (`src/main/java/com/eldanior/system/titles/`) et les débloquer automatiquement au passage de palier
4. **Localisation** — ajouter les noms de titres dans `src/main/resources/Server/Languages/en-US/server.lang`

## Fichiers de référence (à lire avant de coder)
- `src/main/java/com/eldanior/system/Leveling/systems/CraftingProgressionSystem.java` — système actuel
- `src/main/java/com/eldanior/system/Leveling/systems/CraftingRestrictionSystem.java` — restriction benchs
- `src/main/java/com/eldanior/system/skills/skillsInteraction/PassiveSkill.java` — enum des skills craft (lignes 564-573)
- `src/main/java/com/eldanior/system/config/Player/PlayerLevelData.java` — skillProcs (ligne 103, 183-202)
- `Brain IA/Brain IA/Processus/Creer un skill.md` — process officiel

## Contraintes
- JSON dans `src/main/resources/`, JAMAIS dans `build/`
- Compile avec `./gradlew build` après chaque modification
- Ne touche PAS à la vitesse de craft tant que Sabrina n'a pas confirmé l'API
- Le skill ARTISANAT (passe-partout) ne donne PAS de progression — il ouvre juste les benchs
