# Decrets Royaux

#noblesse #decrets #items #promotion

## Fonctionnement
Le Roi distribue des **items Decret** depuis l'onglet Territoires du SystemScreen. Chaque Decret est un item consommable qui **force le rang** du joueur cible.

## Items Decret
| Decret | Donne par | Max | Item JSON |
|--------|-----------|-----|-----------|
| Decret Marquis | Roi | 4 | `Decret_Marquis.json` |
| Decret Duc | Roi | 3 | `Decret_Duc.json` |
| Decret Comte | Roi, Marquis | 2 | `Decret_Comte.json` |
| Decret Baron | Roi, Marquis, Duc | 1 | `Decret_Baron.json` |
| Decret Chevalier | Tous les nobles | illimite | `Decret_Chevalier.json` |

## Distribution par rang
| Rang | Peut donner |
|------|-----------|
| **Roi** | 4 Marquis + 3 Ducs + 2 Comtes + 1 Baron |
| **Marquis** | 1 Comte + 2 Barons |
| **Duc** | 1 Baron |

## Flow
1. Le noble ouvre l'onglet **Territoires**
2. Section **Decrets** avec boutons pour chaque type
3. Clic sur un bouton → l'item Decret apparait dans le hotbar du joueur
4. Le noble donne l'item au joueur cible
5. Le joueur cible consomme l'item → son rang change

## Tracking
- Les compteurs de decrets donnes sont suivis dans le GUI
- Le GUI affiche combien de Decrets restants par type

## Fichiers cles
- `gui/tabs/TerritoiresTab.java` — boutons Decrets
- `skills/interaction/ConsumableItemStatsInteraction.java` — consommation (force le rang)
- `src/main/resources/Server/Item/Items/Food/Decret_*.json` — definitions JSON

## Liens
- [[Rangs et Hierarchie]] - Les 7 rangs
- [[../Noblesse]] - Vue d'ensemble
