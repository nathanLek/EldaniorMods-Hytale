# Esquive et Critique

#combat #dodge #critique #formule

## Esquive (Dodge)
```
Dodge Chance = Agility × 0.5%
```

### Penalite de niveau
Si l'ennemi a 5+ niveaux de plus :
```
Penalty = 1.0 - ((levelGap - 5) × 0.05)
Minimum = 0.1 (max 90% de reduction)
Final Dodge = Dodge Chance × Penalty
```

### Soft Cap
| Seuil | Comportement |
|-------|-------------|
| 0-80% | Normal |
| 80%+ | Rendements decroissants (×0.3) |
| Max | 92% (80 + 12 bonus) |

### Effets visuels
- Dodge reussi → `Dodge_Left` ou `Dodge_Right` (aleatoire)
- Applique sur le joueur qui esquive

## Critique (Critical Hit)
```
Critical Chance = Luck × 0.05%
Critical Multiplier = 2.0x (300% degats totaux)
```

### Soft Cap Critique
| Seuil | Comportement |
|-------|-------------|
| 0-80% | Normal |
| 80%+ | Rendements decroissants |

### Effets visuels
- Coup critique → `Red_Flash` sur la victime

## Fichiers cles
- `Leveling/systems/CombatStatsSystem.java` — dodge lignes 90-130
- `Leveling/systems/LuckSystem.java` — critique

## Liens
- [[Formules de Degats]] - Formule de base
- [[../Combat]] - Vue d'ensemble