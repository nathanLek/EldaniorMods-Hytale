# Distribution des Attributs

#leveling #attributs #stats #profil

## 6 Attributs principaux

| Attribut | Abreviation | Effets |
|----------|-------------|--------|
| **Strength** | STR | Degats physiques (×0.072), degats d'attaque |
| **Vitality** | VIT | Points de vie (×0.5), survie |
| **Intelligence** | INT | Mana max (×3.33), regen mana |
| **Endurance** | END | Reduction degats (×0.05), stamina |
| **Agility** | AGL | Esquive (×0.5%), vitesse (×0.0005), saut (×0.004) |
| **Luck** | LCK | Critique (×0.05%), loot (×0.005%) |

## Points d'attribut
- Gagnes a chaque level up
- Distribuables via l'onglet **Profil** du SystemScreen
- Boutons **+1** et **+5** pour chaque attribut
- Non redistribuables (sauf reset admin)

## Bonus de classe
Chaque classe donne des bonus de stats fixes :
| Classe | STR | VIT | INT | END | AGL | LCK |
|--------|-----|-----|-----|-----|-----|-----|
| Warrior | +10 | +10 | +4 | +4 | +6 | +2 |
| Assassin | +6 | +4 | +2 | +2 | +14 | +8 |
| Mage | +2 | +4 | +20 | +4 | +4 | +2 |
| Archer | +4 | +4 | +4 | +2 | +12 | +10 |
| Merchant | +4 | +2 | +4 | +4 | +4 | +20 |

## Calcul final d'une stat
```
Stat totale = Points investis + Bonus de classe + Bonus de titre + Bonus de skills passifs
```

## Soft Caps (StatConfig)
| Stat derivee | Cap | Au-dela |
|-------------|-----|---------|
| Dodge | 80% | ×0.3 diminishing |
| Critique | 80% | ×0.3 diminishing |
| Vitesse | 3.0 | ×0.3 diminishing |
| Detection range | 100 | Hard cap |

## Fichiers cles
- `config/configs/StatConfig.java` — 21 stats, ratios, caps
- `config/Player/PlayerLevelData.java` — stockage des points
- `gui/SystemScreen.java` — boutons d'attribution

## Liens
- [[Formule XP et Niveaux]] - Gain de points
- [[../Combat]] - Impact sur le combat
- [[../Leveling]] - Vue d'ensemble
