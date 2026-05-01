# Systeme de Leveling & XP

#leveling #xp #niveau #progression

## Progression de niveau
### Formule d'XP requise
```
XP_requis = 100 + 140 × (niveau - 1) + 5 × (niveau - 1)²
```

| Niveau | XP requis | Total cumule |
|--------|----------|--------------|
| 1→2 | 100 | 100 |
| 10→11 | 1665 | ~8 000 |
| 50→51 | 18 995 | ~400 000 |
| 100→101 | 63 895 | ~2 500 000 |
| 200→201 | 238 695 | ~20 000 000 |

### Gain de niveau
- +3 points d'attributs par niveau
- Recalcul automatique des stats via `StatCalculator`

## Sources d'XP
| Source | XP | Condition |
|--------|-----|-----------|
| Mobs | Variable (5-50000) | Selon le type et niveau |
| Quetes | Variable | Recompense de quete |
| Tome d'Experience | 5000 | Item consommable |
| Tome de Niveau | +1 niveau | Item consommable |

## Perte d'XP a la mort
- `DeathXPSystem` retire un % de l'XP courante a la mort
- Le joueur ne peut pas perdre de niveau

## Commandes admin
```
/es addxp <joueur> <montant>     → Donner de l'XP
/es setlevel <joueur> <niveau>   → Definir le niveau
/es setlevel <joueur> 1          → RESET COMPLET du joueur
```

## Reset complet (niveau 1)
Remet a zero :
- Stats (1 partout), points d'attributs (0)
- Classe → Novice, skills oublies
- Titres, kills, PvP, coffres, argent (1000 Or)
- Noblesse → Roturier, famille videe
- Eglise → Laique, guilde videe
- Quetes et cooldowns effaces
- Relances d'evolution remises a 0

## Fichiers cles
- `config/Player/PlayerLevelData.java` - Donnees joueur (~30 champs)
- `Leveling/systems/DeathXPSystem.java` - Perte d'XP a la mort
- `Leveling/commands/AddXPCommand.java` - Commande addxp
- `Leveling/commands/SetLevelCommand.java` - Commande setlevel + reset
- `Leveling/utils/StatCalculator.java` - Recalcul des stats

## Pages detaillees
- [[Leveling/Formule XP et Niveaux]] - Formule, sources d'XP, seuils de classe
- [[Leveling/Mort et Perte XP]] - Penalites PvE et PvP, multiplicateur
- [[Leveling/Distribution Attributs]] - 6 attributs, bonus de classe, soft caps

## Liens
- [[Config/StatConfig]] - Formules des stats
- [[Systems/Classes]] - Evolution de classe par niveau
- [[Mobs/Configuration Mobs]] - XP des mobs