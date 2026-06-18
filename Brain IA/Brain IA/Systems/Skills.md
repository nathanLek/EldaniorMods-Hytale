# Systeme de Competences

#skills #passifs #combat #effets

## Vue d'ensemble
- **325+ competences passives** reparties en 7 rarites + categories speciales (Craft, Family, Dignity Aura)
- Chaque competence a un **effet visuel Hytale** associe (214 mappings)
- Les competences sont innees (classe) ou apprises (parchemins/consommables)

## Rarites
| Rarete | Nombre de skills | Couleur |
|--------|-----------------|---------|
| Common | ~50 | Blanc |
| Uncommon | ~50 | Vert |
| Rare | ~50 | Bleu |
| Epic | ~50 | Violet |
| Unique | ~40 | Rouge |
| Legendary | ~30 | Or |
| Divine | ~25 | Cyan |
| Family | ~9 | Special |
| Craft | 10 | Metier |
| Dignity Aura | 5 | Prestige |

## Categories
- **Attack** : Frappes, lames, pression, chasse
- **Defense** : Peau, parade, bouclier, resolve
- **Agilite** : Esquive, reflexes, vitesse
- **Chance** : Critique, omen, esquive miraculeuse
- **Magique** : Mana, arcane, sort
- **Regeneration** : Vie, vampirisme, souffle
- **Resistance** : Fortification, bulwark, immortalite
- **Vie** : Constitution, coeur, sang
- **Endurance** : Poumons, frenzy, forteresse
- **Detection** : Vision, pistage, sens
- **Maitrise** : Epee, hache, dague, arc, lance
- **Craft** : Competences de fabrication par metier (10 skills)

## Effets visuels (SkillEffectConfig)
Chaque skill peut declencher un effet Hytale automatiquement :

| Effet Hytale | Type | Skills associes |
|--------------|------|-----------------|
| `Red_Flash` | Sur victime | Frappes critiques (19 skills) |
| `Stoneskin` | Sur joueur | Peaux de pierre/fer/diamant (31 skills) |
| `Dodge_Left/Right` | Sur joueur | Parades, esquives (18 skills) |
| `Immune` | Sur joueur | Invincibilite, aegis (11 skills) |
| `Intangible_Dark` | Sur joueur | Lames du neant, vitesse sombre (10 skills) |
| `Burn` | Sur victime | Chasse, lame sanglante (3 skills) |
| `Stun` | Sur victime | Pression ecrasante (3 skills) |
| `Poison_T1` | Sur victime | Estocade empoisonnee |
| `Mana_Drain` | Sur victime | Drain d'esprit, vampirisme (7 skills) |
| `Flame_Staff_Burn` | Sur victime | Sorts arcaniques (7 skills) |
| `Battleaxe_Whirlwind` | Sur joueur | Maitrise hache, frenzy (6 skills) |
| `Dagger_Dash/Pounce/Signature` | Sur joueur | Maitrise dague, precision (14 skills) |

## Apprentissage
1. **Inne** : chaque classe a 2-3 skills passifs automatiques
2. **Parchemin** : item consommable qui apprend un skill
3. **Evolution** : un parchemin superieur remplace l'inferieur (voir mecanique ci-dessous)

### Mecanique d'evolution (ConsumableItemSkillInteraction)
Lors de la consommation d'un parchemin de skill :
1. Si le joueur possede deja la **version superieure** du skill → blocage, le parchemin n'est pas consomme
2. Si le joueur possede une **version inferieure** → `removeSkill` puis `learnSkill` (evolution propre)
3. Sinon → apprentissage normal

## Skills Craft (categorie Metier)
10 skills de fabrication couvrant les metiers du mod :

| Enum | Classe Java | Metier |
|------|-------------|--------|
| `CRAFT_AGRICULTURE` | CraftAgriculture | Agriculture |
| `CRAFT_ALCHIMIE` | CraftAlchimie | Alchimie |
| `CRAFT_ARMURERIE` | CraftArmurerie | Armurerie |
| `CRAFT_ARTISAN_BASE` | CraftArtisanBase | Artisanat de base |
| `CRAFT_CUISINE` | CraftCuisine | Cuisine |
| `CRAFT_FONDERIE` | CraftFonderie | Fonderie |
| `CRAFT_FORGE_ARMES` | CraftForgeArmes | Forge d'armes |
| `CRAFT_RECYCLAGE` | CraftRecyclage | Recyclage |
| `CRAFT_SCIERIE` | CraftScierie | Scierie |
| `CRAFT_TANNERIE` | CraftTannerie | Tannerie |

Lies aux restrictions de fabrication via `CraftingRestrictionSystem`.

## Dignity Aura (skills de progression)
5 niveaux : `DIGNITY_AURA_1` a `DIGNITY_AURA_5`
- Implementes via `DignityAuraPassive.java`
- **Pas de fichier JSON** dans `PassiveSkill/` : logique directe dans le code
- Lies au systeme de rang/dignite du joueur

## Skills speciaux
| Enum | Rarete | Categorie | Notes |
|------|--------|-----------|-------|
| `DETECTIONOFVITALPOINTS` | Epique | Attack | Detection points vitaux ennemis |
| `TONOSQUIVE` | Unique | Agilite | Tonneau d'esquive |
| `VOL` | Legendaire | Magique | Vrai vol — gere par `FlySystem.java` (1 mana/s) |
| `ARTISANAT` | Rare | Maitrise | Bonus artisanat general |
| `SEISMIC_STRIKE` | Epique | Attack | Frappe sismique AOE |

## Constructeurs de l'enum PassiveSkill
3 constructeurs surcharges :
```java
PassiveSkill(String id, String name, String desc, IPassiveCombatSkill logic)
PassiveSkill(String id, String name, String desc, int manaCost, IPassiveCombatSkill logic)
PassiveSkill(String id, String name, String desc, int manaCost, float cooldownSeconds, IPassiveCombatSkill logic)
```

## SkillModel (Java Record)
`SkillModel` est un **Java Record** (pas une classe). Champs :

| Champ | Type | Role |
|-------|------|------|
| `skillId` | String | Identifiant unique |
| `catalystId` | String | Item consommable lie au sort actif |
| `displayName` | String | Nom affiche |
| `requiredClass` | String | Classe requise |
| `manaCost` | int | Cout en mana |
| `cooldown` | float | Temps de recharge (s) |
| `castTime` | float | Temps d'incantation |
| `damage` | float | Degats de base |
| `range` | float | Portee |
| `duration` | float | Duree de l'effet |
| `levelUp` | String | Skill de niveau superieur (evolution) |
| `levelDown` | String | Skill de niveau inferieur (blocage) |

Le champ `catalystId` lie un item consommable a un sort actif.

## Systemes annexes lies aux skills
| Systeme | Fichier | Role |
|---------|---------|------|
| FlySystem | `FlySystem.java` | Vol (skill VOL), consomme 1 mana/s |
| DetectionSystem | `DetectionSystem.java` | Radar ennemi, rayon de menace |
| InvisibilityManager | `InvisibilityManager.java` | Gestion invisibilite |
| MorphFlightSystem | `MorphFlightSystem.java` | Vol en morph (dragon) — distinct de FlySystem |
| MovementTrackingSystem | `MovementTrackingSystem.java` | Suivi deplacements pour skills reactifs |

## Interactions consommables
3 types d'interactions pour les items consommables :
1. **ConsumableItemSkillInteraction** — Apprentissage/evolution de skill
2. **ConsumableItemStatsInteraction** — Modification de stats
3. **ConsumableItemMoneyInteraction** — Ajout/retrait de monnaie

## Anomalies connues
- **`skill_page_morph_dragon.json`** existe en JSON mais aucun enum `MORPH_DRAGON` correspondant (orphelin potentiel)

## Fichiers cles
- `skills/skillsInteraction/PassiveSkill.java` - Enum de tous les skills (3 constructeurs)
- `skills/skills/passives/` - Implementation de chaque skill
- `skills/skills/passives/DignityAuraPassive.java` - 5 niveaux Dignity Aura
- `config/Effects/SkillEffectConfig.java` - Mapping skill → effet visuel
- `config/Effects/EffectsManager.java` - Application des effets Hytale
- `skills/interaction/ConsumableItemSkillInteraction.java` - Parchemins + evolution
- `skills/interaction/ConsumableItemStatsInteraction.java` - Consommables de stats
- `skills/interaction/ConsumableItemMoneyInteraction.java` - Consommables monetaires

## Pages detaillees
- [[Skills/Categories de Skills]] - Les 12 categories detaillees
- [[Skills/Rarites et Distribution]] - 10 niveaux de rarete + categories speciales
- [[Skills/Apprentissage de Skills]] - Inne, parchemin, evolution
- [[Skills/Sorts Actifs]] - Sorts actifs (Spellbooks, mecanique charge/lancer, liste par classe)

## Liens
- [[Systems/Classes]] - Skills innes par classe
- [[Systems/Consommables]] - Parchemins de competence
