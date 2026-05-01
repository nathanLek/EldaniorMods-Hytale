# Formule XP et Level Cap

#balance #moyenne #leveling #xp #CORRIGE

> **CORRIGE** le 2026-05-01 :
> - MAX_LEVEL = 500 ajoute dans PlayerLevelData
> - Cap multiplicateur XP PvP : min 0.2x, max 2.0x dans DeathXPSystem

## Problemes identifies (RESOLUS)

### 1. Pas de cap de niveau
**Fichier** : `config/Player/PlayerLevelData.java` lignes 106-113
```java
while (this.experience >= getRequiredExperience()) {
    // Boucle infinie si assez d'XP — aucun MAX_LEVEL
}
```
- A haut niveau, une seule action peut declencher 10+ level-ups
- Pas de protection contre l'overflow

### 2. Scaling XP PvP non borne
**Fichier** : `Leveling/systems/DeathXPSystem.java` lignes 138-146
```java
double multiplier = 1.0 + (levelGap * 0.02); // Pas de plafond !
```
- Ecart de 100 niveaux → multiplicateur 3.0x (300% XP)
- Encourage le farm de joueurs haut niveau

### 3. Penalite de mort fixe
- 10% ou 20% de l'XP requise, peu importe le niveau
- Negligeable a haut niveau

## Corrections proposees

### Level cap
```java
public static final int MAX_LEVEL = 500;

public void addExperience(int amount) {
    if (level >= MAX_LEVEL) return;
    experience += amount;
    while (experience >= getRequiredExperience() && level < MAX_LEVEL) {
        experience -= getRequiredExperience();
        level++;
    }
}
```

### Cap multiplicateur XP PvP
```java
double multiplier = Math.min(2.0, 1.0 + (levelGap * 0.02)); // Max 200%
```

### Penalite de mort progressive
| Niveau | Penalite |
|--------|----------|
| 1-100 | 5% XP |
| 100-200 | 10% XP |
| 200-400 | 15% XP |
| 400-500 | 20% XP |

## Priorite
**MOYENNE** — Balance, pas de crash

## Liens
- [[Systems/Leveling]] - Systeme de leveling
- [[Systems/Combat]] - Systeme de combat