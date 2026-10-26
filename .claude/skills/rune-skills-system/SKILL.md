---
name: rune-skills-system
description: Rune_Skills_System — conçoit, équilibre et implémente les compétences de rune (sorts actifs) du mod Eldanior pour Hytale - 12 types (Feu, Glace, Terre, Air, Foudre, Ténèbres, Lumière, Temps, Espace, Combat, Spatial, Dragonic), 7 raretés (Common à Divin), parchemin, loot, icônes, animations, particules, sons, effets de statut, terrain. À utiliser pour créer une rune à partir d'une idée, proposer ou équilibrer une compétence, l'intégrer au projet (JSON + Java RuneSpell), vérifier les runes existantes ou chercher des assets vanilla.
---

# Rune_Skills_System

Fabrique de compétences de rune du mod Eldanior (plugin serveur Hytale, Java 25, `server_version ~0.7.x`).
Une rune = un item à bloc `Ability` équipé dans les slots de compétence ; elle s'apprend avec un parchemin trouvé dans le loot.

## 1. Choisir le mode

| L'utilisateur dit… | Mode | Livrable |
|---|---|---|
| « propose », « imagine », « idées de sorts », « équilibre » | **Conception** | fiche `scripts/runes/specs/<Id>.json` + résumé, **aucun code** |
| « crée », « ajoute », « implémente », « intègre » | **Implémentation** | fiche + assets + Java + vérifications + rapport |

En cas de doute, commencer par la conception et la présenter avant de coder.

## 2. Règles d'or (non négociables)

1. **Vérifier avant d'utiliser** : tout id de particule, son, animation, effet, bloc ou modèle se vérifie avec
   `python3 scripts/runes/rune_tool.py assets <type> <regex>` (mod + `Assets.zip` vanilla). Toute méthode Java se vérifie dans
   le code (`SpellFx`, `TerrainRestore`) ou dans le jar serveur (`javap`). Ne jamais supposer une API Minecraft.
2. **Coût et recharge viennent de la rareté** (`scripts/runes/rune_rules.json`). Une exception exige `overrides.reason` dans la fiche.
3. **La rareté ≠ les dégâts** : elle mesure importance, complexité, portée, mise en scène et impact gameplay (voir `references/rarity-balance.md`).
4. **Pas de doublon** : consulter `references/library.md` ; deux sorts du même type doivent différer par leur mécanique, pas par la couleur ou les chiffres.
5. **Honnêteté** : « compilé », « assets validés au chargement » et « testé en jeu » sont trois niveaux distincts ; ne jamais annoncer le troisième sans retour de l'utilisateur.
6. **Pas de nouveau Divin** : ne jamais proposer de nouvelle compétence de rareté Divin (décision utilisateur).
7. **Une icône par rune** : chaque rune a son emblème dédié dans `rune_icons.py` (champ `emblem` de la fiche), jamais l'emblème générique du type ; prévisualiser en 128 et 64 px.
8. **Ne rien casser** : `rune_tool.py new` n'écrase rien sans `--force` ; ne pas modifier les sorts divins existants sans demande.

## 3. Processus (implémentation)

1. **Cadrer** : type, rareté, rôle (dégâts / contrôle / mobilité / soutien / défense / invocation / zone), fantasy en une phrase.
   Lire `references/categories.md` (identité du type, frontières Espace/Spatial et Air/Foudre), `references/library.md` (cases à remplir, doublons) et `references/ideas.md` (pistes de l'utilisateur).
2. **Concevoir** : copier `templates/spec.json` vers `scripts/runes/specs/<Id>.json` et remplir. Calibrer avec le budget de puissance
   de `references/rarity-balance.md`. Choisir l'animation (`assets anims Staff|Spellbook|Sword|Battleaxe|Daggers|Item`).
3. **Chercher les assets** : particules, sons, effets, caméra (`references/visual-fx.md` pour les pièges vérifiés).
   Créer des particules du mod seulement si rien ne convient (dossier `Server/Particles/Spell/<Type>/`).
4. **Générer** : `python3 scripts/runes/rune_tool.py new scripts/runes/specs/<Id>.json` crée l'item, le parchemin, les interactions
   (incantation → `RuneSpell`), 3 images, la classe Java, et insère `RuneSpells`, `RuneManager` (classes + parchemin) et le loot.
5. **Coder l'effet** dans `src/main/java/com/eldanior/system/skills/runes/spells/<Classe>.java` (interface `RuneSpell`, voir
   `references/architecture.md` et l'exemple `Bourrasque.java`). Terrain : `references/world-edit.md`. Cycle et cas limites : `references/lifecycle.md`.
6. **Valider** (`references/testing.md`) : `./gradlew compileJava` → `rune_tool.py check <Id>` → `./gradlew jar` → serveur headless
   (chargement des assets) → checklist de test en jeu à donner à l'utilisateur.
7. **Rapporter** : fichiers créés/modifiés, valeurs (mana, recharge, dégâts…), ce qui est vérifié et ce qui reste à tester en jeu,
   limitations techniques. Ajouter la rune à `references/library.md`.

## 4. Commandes

```bash
python3 scripts/runes/rune_tool.py rules                         # table des raretés, loot, types
python3 scripts/runes/rune_tool.py new scripts/runes/specs/X.json [--dry-run] [--force]
python3 scripts/runes/rune_tool.py check [Rune_X ...]            # 0 erreur attendu (code retour 1 sinon)
python3 scripts/runes/rune_tool.py assets particles|sounds|models|trails|effects|camera|damage|beams|blocks|interactions <regex>
python3 scripts/runes/rune_tool.py assets anims <JeuAnimations>   # ex. Staff, Spellbook, Sword
python3 scripts/runes/rune_tool.py icons Rune_X [--emblem Puits]  # régénère seulement les 3 images (fiche : champ "emblem")
python3 scripts/runes/rune_icons.py <Type> <Rareté> <dossier> [Emblème]  # aperçu des icônes
./gradlew compileJava && ./gradlew jar                           # le jar part dans UserData/Mods (chargé par le jeu)
```

## 5. Références

- `references/architecture.md` — fichiers d'une rune, moteur `RuneSpell`, API `SpellFx`/`TerrainRestore`, branchements, sorts divins historiques.
- `references/categories.md` — les 12 types : identité, mécaniques, palette, assets conseillés, frontières.
- `references/rarity-balance.md` — table des raretés, budget de puissance, fourchettes chiffrées, exceptions, quantités cibles.
- `references/visual-fx.md` — animations, particules, trails, modèles, caméra : ce qui marche et les pièges vérifiés.
- `references/world-edit.md` — modifications du terrain sûres et restaurées.
- `references/lifecycle.md` — cycle d'exécution, mana/recharge, interruptions, déconnexions, cibles invalides.
- `references/testing.md` — validation et rapport.
- `references/library.md` — catalogue des runes existantes et cases à remplir.
- `references/ideas.md` — banque d'idées de l'utilisateur (pistes, pas des commandes) : la consulter pour proposer des sorts.
- `templates/spec.json` — fiche de conception commentée.
