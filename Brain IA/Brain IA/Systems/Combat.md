# Systeme de Combat

#combat #degats #formules #defense

## Flow de calcul des degats (CombatStatsSystem)

```
1. ANTI-PVP ZONE → Si dans une zone PvP desactive, annuler
2. ESQUIVE → dodgeChance / 100 → si reussi, Dodge_Left/Right + annuler
3. ATTAQUE → pour chaque skill passif actif :
   a. Verifier le mana (cout > 0 ? assez de mana ?)
   b. Executer onAttack() du skill
   c. Appliquer l'effet visuel (SkillEffectConfig)
4. BONUS TITRE → bonus degats vs type de mob specifique
5. DEFENSE → pour chaque skill passif defensif :
   a. Executer onDefend() du skill
   b. Appliquer l'effet visuel
6. BONUS TITRE DEFENSE → reduction degats vs type de mob
7. DEGATS FINAUX appliques
```

## Formules de base

### Degats physiques
```
degatsBase = arme.damage + (STR × 0.5)
degatsCrit = degatsBase × (1 + CRIT_DAMAGE/100) si critique
degatsFinaux = degatsCrit × (1 - defense_reduction)
```

### Esquive
```
dodgeChance = base(1%) + (AGL × 0.1) + bonus_passifs
// Malus si ennemi > 5 niveaux au-dessus :
penalite = (ecart_niveau - 5) × 5%  // max 90% de malus
dodgeChance_final = dodgeChance × (1 - penalite)
```

### Critique
```
critChance = base(1%) + (LCK × 0.15) + bonus_passifs  // cap 80%
critDamage = base(150%) + (STR × 0.5) + bonus_passifs
```

## Protection PvP par zone
- Verifie la parcelle de la victime
- Si `parcel.isPvpEnabled() == false` → degats annules
- Seules les **villes** peuvent toggle le PvP

## Mana
```
maxMana = 50 + (INT × 3)
```
- Les skills avec `manaCost > 0` consomment du mana quand ils proc
- Si pas assez de mana → le skill est skip
- Regeneration naturelle via `GlobalRegenSystem`

## Systemes ECS associes
| Systeme | Role |
|---------|------|
| CombatStatsSystem | Calcul principal degats/defense |
| CombatTrackerSystem | Suivi attaquant/victime |
| GlobalRegenSystem | Regen HP/Mana/Stamina |
| FallDamageSystem | Degats de chute |
| DuelProtectionSystem | Anti-mort en duel |

## Fichiers cles
- `Leveling/systems/CombatStatsSystem.java` - Calcul principal
- `Leveling/systems/CombatTrackerSystem.java` - Tracking
- `Leveling/systems/GlobalRegenSystem.java` - Regeneration
- `config/configs/StatConfig.java` - Formules stats

## Pages detaillees
- [[Combat/Formules de Degats]] - Calcul complet des degats
- [[Combat/Esquive et Critique]] - Dodge, crit, soft caps
- [[Combat/Mana et Couts Passifs]] - Systeme de mana et skills
- [[Combat/Zone PvP]] - Protection PvP par zone

## Liens
- [[Config/StatConfig]] - Toutes les formules
- [[Systems/Skills]] - Skills passifs en combat
- [[Utilitaires/Effets Visuels]] - Effets declenchés en combat
- [[Systems/Duels]] - Protection en duel