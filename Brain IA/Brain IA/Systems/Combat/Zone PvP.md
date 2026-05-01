# Zone PvP

#combat #pvp #territoire #protection

## Fonctionnement
- Chaque parcelle de type **CITY** peut activer/desactiver le PvP
- Les KINGDOM et TERRITORY ne peuvent **pas** modifier le PvP
- Seul l'admin ou le proprietaire de la ville peut toggler

## Verification
Dans `CombatStatsSystem.java`, **avant** le calcul de dodge :
1. Verifier si attaquant ET victime sont des joueurs
2. Recuperer la parcelle la plus petite a la position de la victime
3. Si `pvpEnabled == false` → bloquer les degats

## Limitations actuelles
- Pas de notification "PvP desactive ici" pour l'attaquant
- Pas de notification d'entree/sortie de zone PvP
- Les effets visuels d'attaque se declenchent quand meme
- Les Duels ne sont **pas** affectes par les zones PvP

## Fichiers cles
- `Leveling/systems/CombatStatsSystem.java` — check PvP avant dodge
- `territory/ParcelData.java` — `pvpEnabled` boolean
- `gui/tabs/TerritoiresTab.java` — toggle PvP (lignes 314-321)

## Liens
- [[Formules de Degats]] - Calcul des degats
- [[../Territoires]] - Systeme de parcelles
- [[../Duels]] - Duels (non affectes)
- [[../Combat]] - Vue d'ensemble