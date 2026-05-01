# Mort et Perte d'XP

#leveling #mort #penalite #pvp

## Penalite de mort (PvE)
- Le joueur perd **10% de son XP actuelle** a la mort contre un mob
- L'XP perdue ne descend jamais en dessous de 0

## Penalite de mort (PvP)
- Le joueur tue par un autre joueur perd **20% de son XP actuelle**
- L'XP perdue est **transferee** au tueur

## Multiplicateur PvP
```java
double multiplier = 1.0 + (levelGap * 0.02);
xpAmount = (int) Math.max(1, Math.round(xpAmount * multiplier));
```
- Si la victime est plus haut niveau que le tueur → bonus XP
- Ecart de 50 niveaux → multiplicateur 2.0x (200%)
- **Pas de plafond** actuellement (voir [[../../Balance/Formule XP]])

## Protection anti-farm
- `lastVictimUUID` track le dernier joueur tue
- Pas de cooldown explicite entre kills du meme joueur (probleme potentiel)

## XP des mobs
- Chaque famille de mob a un XP de base
- Multiplie par le niveau du mob
- Bonus de premiere decouverte pour les coffres

## Fichiers cles
- `Leveling/systems/DeathXPSystem.java` — calcul perte/gain XP
- `config/Player/PlayerLevelData.java` — `removeExperiencePercent()`

## Liens
- [[Formule XP et Niveaux]] - Formule de base
- [[../Leveling]] - Vue d'ensemble