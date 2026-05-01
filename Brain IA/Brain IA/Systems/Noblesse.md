# Systeme de Noblesse

#noblesse #feodal #rang

## Hierarchie des rangs

| Rang | Couleur | Dignite | Max/Royaume | Chevaliers | Gere |
|------|---------|---------|-------------|------------|------|
| **Roi** | §c Rouge | 100 | 1 | 10 | Royaume + Famille |
| **Marquis** | §6 Or | 75 | 4 | 3 (1 Comte + 2 Barons) | Territoire + Famille |
| **Duc** | §5 Violet | 50 | 3 | 1 (1 Baron) | Territoire + Famille |
| **Comte** | §9 Bleu | 30 | 2 | 2 | Ville + Guilde |
| **Baron** | §a Vert | 15 | 1 | 1 | - |
| **Chevalier** | §f Blanc | 5 | illimite | 0 | - |
| **Roturier** | §7 Gris | 0 | - | 0 | - |

## Decrets Royaux
Le Roi peut donner des **items Decret** depuis l'onglet Territoires :
- Decret Marquis (4 max)
- Decret Duc (3 max)
- Decret Comte (2 max)
- Decret Baron (1 max)

Le Marquis peut donner : 1 Decret Comte + 2 Decrets Baron
Le Duc peut donner : 1 Decret Baron

L'item est consomme par le joueur cible pour devenir noble.

## Nameplate
Format : `[PK] [Rang] [Eglise] Nom Von Famille`
Exemple : `§5[Duc] §f Shinoo_ §7Von §6Drakenhart`

## Systeme de Dignite
- Chaque rang a une dignite de base
- La dignite active l'**Aura de Dignite** (ralentit les mobs proches)
- Item **Essence de Dignite** (+1 dignite, Divine, 0.02% en coffre legendaire)

## Fichiers cles
- `titles/nobility/NobilityRank.java` - Enum des rangs
- `titles/nobility/NobilityManager.java` - Gestion promotions et compteurs
- `titles/nobility/PlayerNameplateSystem.java` - Affichage des noms
- `titles/nobility/systems/DignityAuraSystem.java` - Aura de dignite

## Pages detaillees
- [[Noblesse/Rangs et Hierarchie]] - Les 7 rangs, chaine feodale
- [[Noblesse/Decrets Royaux]] - Items de promotion
- [[Noblesse/Dignite et Aura]] - Dignite et aura noble

## Liens
- [[Systems/Territoires]] - Gestion des territoires par les nobles
- [[Systems/Familles]] - Familles nobles
- [[Systems/Economie]] - Impots et tresorerie
