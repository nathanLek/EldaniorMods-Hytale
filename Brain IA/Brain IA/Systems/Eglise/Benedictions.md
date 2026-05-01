# Benedictions

#eglise #benedictions #items #consommables

## Items de Benediction
3 types de benedictions disponibles via le systeme d'eglise :

| Item | Rang requis | Effet |
|------|------------|-------|
| **Benediction Pretre** | Pretre+ | Force le rang Pretre |
| **Benediction Cardinal** | Cardinal+ | Force le rang Cardinal |
| **Benediction Archeveque** | Archeveque+ | Force le rang Archeveque |

## Fonctionnement
1. Un membre du clerge cree une benediction depuis le GUI
2. La benediction est un **item consommable**
3. Le joueur cible consomme l'item
4. Son rang d'eglise est **force** au rang de la benediction
5. Fonctionne via `ConsumableItemStatsInteraction` (type CHURCH_RANK)

## Lien avec la Noblesse
- Un joueur peut etre noble ET membre du clerge
- Le nameplate affiche les deux : `[Eglise] [Noblesse] Nom Von Famille`
- Les deux hierarchies sont **independantes**

## Fichiers cles
- `src/main/resources/Server/Item/Items/Food/Benediction_*.json` — items
- `skills/interaction/ConsumableItemStatsInteraction.java` — consommation

## Liens
- [[Rangs et Foi]] - Hierarchie ecclesiastique
- [[../Eglise]] - Vue d'ensemble
- [[../Noblesse]] - Parallele avec la noblesse