# Configuration des Stats

#config #stats #combat #formules

## 21 Stats du joueur

### Stats d'attributs (investies par le joueur)
| Stat | Base | Ratio | Cap | Description |
|------|------|-------|-----|-------------|
| MAX_HEALTH | 100 | VIT × 5 | - | Points de vie max |
| MAX_MANA | 50 | INT × 3 | - | Mana max |
| PHYSICAL_DAMAGE | 0 | STR × 0.5 | - | Degats physiques bonus |
| PHYSICAL_DEFENSE | 0 | END × 0.3 | - | Reduction degats |
| MAGIC_DAMAGE | 0 | INT × 0.5 | - | Degats magiques bonus |
| CRIT_CHANCE | 1 | LCK × 0.15 | 80% | Chance de critique |
| CRIT_DAMAGE | 150 | STR × 0.5 | - | Multiplicateur critique (%) |
| DODGE_CHANCE | 1 | AGL × 0.1 | 60% | Chance d'esquive |
| ATTACK_SPEED | 0 | AGL × 0.05 | 50% | Vitesse d'attaque bonus (%) |
| LOOT_QUALITY | 0 | LCK × 0.1 | 100% | Bonus qualite loot |

### Stats de mouvement
| Stat | Description |
|------|-------------|
| MOVEMENT_SPEED | Vitesse de deplacement |
| MOVEMENT_JUMP | Hauteur de saut |

### Stats mecaniques
| Stat | Description |
|------|-------------|
| HEALTH_REGEN | Regeneration HP par tick |
| MANA_REGEN | Regeneration Mana par tick |
| DETECTION_RANGE | Portee de detection (radar) |
| THREAT_AWARENESS | Conscience des menaces |

## Formules de calcul
```
finalStat = baseStat + (attribute × ratio) + classBonus + titleBonus + passiveBonus
finalStat = min(finalStat, cap)  // si cap existe
```

## Application
`StatCalculator.updatePlayerStats()` recalcule toutes les stats et les applique via `EntityStatMap`.

## Fichiers cles
- `config/configs/StatConfig.java` - Toutes les formules
- `Leveling/utils/StatCalculator.java` - Application des stats

## Liens
- [[Systems/Classes]] - Bonus de classe
- [[Systems/Skills]] - Bonus passifs
