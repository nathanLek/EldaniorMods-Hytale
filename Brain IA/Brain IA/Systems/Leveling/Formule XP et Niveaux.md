# Formule XP et Niveaux

#leveling #xp #formule #niveau

## Formule d'XP requise par niveau
```
XP requise = 100 + 140 × (level - 1) + 5 × (level - 1)²
```

| Niveau | XP requise | XP cumule |
|--------|-----------|-----------|
| 1 | 100 | 100 |
| 10 | 1,665 | ~8,000 |
| 50 | 18,945 | ~300,000 |
| 100 | 62,945 | ~2,000,000 |
| 200 | 236,945 | ~16,000,000 |
| 500 | ~1,500,000 | ~250,000,000 |

## Sources d'XP
| Source | XP | Conditions |
|--------|-----|-----------|
| Mob kill | Variable | XP du mob × multiplicateur |
| Quete | rewardXP × difficulte | A la completion |
| Duel (victoire) | 10% XP du perdant | Transfere du perdant |
| Coffre au tresor | Bonus XP | Premiere decouverte |

## Level up
- Le level up se produit quand `experience >= getRequiredExperience()`
- L'excedent d'XP est conserve pour le niveau suivant
- Pas de cap de niveau actuellement (voir [[../../Balance/Formule XP]])

## Points d'attribut
- Chaque level up donne des **points d'attribut** a distribuer
- 6 stats : STR, VIT, INT, END, AGL, LCK
- Distribuables via l'onglet Profil (+1 ou +5)

## Seuils de classe
| Seuil | Evenement |
|-------|-----------|
| Niveau 1 | Choix de classe tier 1 (gacha) |
| Niveau 180 | Evolution tier 2 (gacha) |
| Niveau 400 | Evolution tier 2+ (1 seul choix) |

## Fichiers cles
- `config/Player/PlayerLevelData.java` — `addExperience()`, `getRequiredExperience()`
- `Leveling/systems/DeathXPSystem.java` — XP des mobs et PvP

## Liens
- [[Mort et Perte XP]] - Penalite de mort
- [[Distribution Attributs]] - Points d'attribut
- [[../Leveling]] - Vue d'ensemble