# Systeme de Competences

#skills #passifs #combat #effets

## Vue d'ensemble
- **301 competences passives** reparties en 7 rarites
- Chaque competence a un **effet visuel Hytale** associe (214 mappings)
- Les competences sont innees (classe) ou apprises (parchemins)

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
3. **Evolution** : un parchemin superieur remplace l'inferieur

## Fichiers cles
- `skills/skillsInteraction/PassiveSkill.java` - Enum de tous les skills
- `skills/skills/passives/` - Implementation de chaque skill
- `config/Effects/SkillEffectConfig.java` - Mapping skill → effet visuel
- `config/Effects/EffectsManager.java` - Application des effets Hytale
- `skills/interaction/ConsumableItemSkillInteraction.java` - Consommation parchemins

## Pages detaillees
- [[Skills/Categories de Skills]] - Les 11 categories detaillees
- [[Skills/Rarites et Distribution]] - 8 niveaux de rarete + family skills
- [[Skills/Apprentissage de Skills]] - Inne, parchemin, evolution

## Liens
- [[Systems/Classes]] - Skills innes par classe
- [[Systems/Consommables]] - Parchemins de competence
