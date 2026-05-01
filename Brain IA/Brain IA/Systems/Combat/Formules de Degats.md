# Formules de Degats

#combat #formule #degats #stats

## Formule principale
```
Degats = Base Damage + (Attacker STR × 0.072) × (Critical Multiplier ou 1.0)
       - (Victim END × 0.05)
Final  = Maximum(Calcule, 1)
```

## Etapes de calcul (CombatStatsSystem)
1. Verifier zone PvP (si joueur vs joueur)
2. Calculer le dodge (esquive)
3. Calculer les degats bruts (STR × ratio)
4. Appliquer les passifs d'attaque (onAttack)
5. Appliquer les passifs de defense (onDefend)
6. Appliquer les bonus de titre (vs type de mob)
7. Appliquer les effets visuels (SkillEffectConfig)
8. Minimum 1 degat

## Stats impliquees
| Stat | Ratio | Role |
|------|-------|------|
| Strength | ×0.072 | Degats physiques |
| Endurance | ×0.05 | Reduction de degats |
| Intelligence | ×3.33 | Mana max (pour skills) |
| Luck | ×0.05 | Chance de critique |

## Fichier cle
- `Leveling/systems/CombatStatsSystem.java` — lignes 42-86

## Liens
- [[Esquive et Critique]] - Dodge et crit
- [[Mana et Couts Passifs]] - Consommation mana
- [[Zone PvP]] - Protection PvP
- [[../Combat]] - Vue d'ensemble
