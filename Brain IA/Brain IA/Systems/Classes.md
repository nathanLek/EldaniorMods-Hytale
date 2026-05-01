# Systeme de Classes

#classes #evolution #gacha

## Hierarchie
```
Novice (Lvl 20)
├── Guerrier ──→ 27 evolutions Tier 1 ──→ 77 evolutions Tier 2
├── Mage ──→ 27 evolutions Tier 1 ──→ 76 evolutions Tier 2
├── Assassin ──→ 27 evolutions Tier 1 ──→ 77 evolutions Tier 2
├── Archer ──→ 15 evolutions Tier 1 ──→ 43 evolutions Tier 2
└── Marchand ──→ 15 evolutions Tier 1 ──→ 15 evolutions Tier 2
```

## Seuils de niveau
| Palier | Niveau requis | Choix proposes |
|--------|--------------|----------------|
| Classe de base | 20 | 5 (toutes les bases) |
| Evolution Tier 1 | 180 | 3 (gacha pondere) |
| Evolution Tier 2 | 400 | 1 (gacha pondere) |

## Systeme de Gacha
Les evolutions sont tirees au sort avec des poids par rarete :
- **Common** : 50% 
- **Rare** : 33%
- **Epic** : 6.6%
- **Unique** : 0.5%
- **Legendary** : 0.125%
- **Divine** : 0.02%

## Relances
- 2 relances max par personnage (global, pas par evolution)
- Item **Parchemin de Relance** : ajoute +1 relance
- Admin : relances illimitees

## Sauvegarde des choix
Quand le joueur ferme la fenetre sans choisir, les 3 propositions sont sauvegardees dans `PlayerLevelData.savedEvolutionChoices` et reproposees au retour.

## Rarete des evolutions
- COMMON → evolue en **RARE**
- RARE → evolue en **EPIC**
- EPIC → evolue en **UNIQUE**
- UNIQUE → reste UNIQUE
- LEGENDARY → reste LEGENDARY
- DIVINE → 1 seule evolution (Demi-Dieu, Demi-Dragon, etc.)

## Fichiers cles
- `classes/ClassManager.java` - Registre de toutes les classes
- `classes/gui/OpenClassSelectionInteraction.java` - Gacha + Selection Tool
- `classes/gui/ClassSelectionScreen.java` - Ecran de choix avec reroll
- `classes/gui/ClassEvolutionIntroScreen.java` - Ecran d'intro evolution
- `classes/definitions/` - Definitions des classes par famille
- `classes/definitions/*/400/` - Evolutions Tier 2

## Liens
- [[Systems/Skills]] - Competences passives par classe
- [[Systems/Consommables]] - Parchemin de Relance
