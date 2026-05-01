# Esquive et Combat

#balance #moyenne #combat #dodge #CORRIGE

> **CORRIGE** le 2026-05-01 :
> - Plancher d'esquive 2% minimum dans CombatStatsSystem

## Problemes identifies (RESOLUS)

### 1. Dodge a 0% avec grand ecart de niveau
**Fichier** : `Leveling/systems/CombatStatsSystem.java` ligne 118
- Le multiplicateur de penalite de niveau peut reduire le dodge a quasi 0%
- Un joueur avec 50% d'esquive contre un ennemi +50 niveaux au dessus → ~0% dodge
- Pas de plancher minimum

### 2. Cout en mana des passifs (race condition)
**Fichier** : `Leveling/systems/CombatStatsSystem.java` lignes 190-211
- Plusieurs passifs consomment du mana sequentiellement
- Si 2 attaques rapides → le mana peut etre consomme 2 fois avant la mise a jour

### 3. Pas de types de degats
- Tous les degats sont traites de maniere identique (physique, magique, feu, poison...)
- Pas de resistance specifique par type

## Corrections proposees

### Plancher d'esquive
```java
float dodgeChance = calculateDodge(agility, level);
dodgeChance *= levelPenaltyMultiplier;
dodgeChance = Math.max(2.0f, dodgeChance); // Minimum 2% d'esquive
```

### Reservation atomique de mana
```java
// Reserver le mana de tous les passifs AVANT de les activer
int totalManaCost = activePassives.stream()
    .mapToInt(IPassiveCombatSkill::getManaCost)
    .sum();

if (currentMana >= totalManaCost) {
    currentMana -= totalManaCost;
    // Puis activer tous les passifs
}
```

## Priorite
**MOYENNE** — Balance de combat

## Liens
- [[Systems/Combat]] - Systeme de combat
- [[Systems/Skills]] - Competences passives
- [[Formule XP]] - Scaling general