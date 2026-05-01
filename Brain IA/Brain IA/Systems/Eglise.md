# Systeme d'Eglise

#eglise #religion #rang

## Hierarchie des rangs d'eglise
| Rang | Description |
|------|-------------|
| LAIQUE | Aucun rang (defaut) |
| PRETRE | Premier rang religieux |
| ARCHEVEQUE | Rang intermediaire |
| CARDINAL | Haut rang |
| PAPE | Rang supreme (1 seul) |

## Obtention
- Via items **Benediction** (consommables)
- Via commande admin `/es church promote <joueur>`
- Le Pape est unique (1 seul par serveur)

## Affichage
- Prefixe dans le nameplate : `[Rang Eglise]`
- Combine avec le rang de noblesse

## Items
| Item | Effet |
|------|-------|
| Benediction_Pretre | → PRETRE |
| Benediction_Archeveque | → ARCHEVEQUE |
| Benediction_Cardinal | → CARDINAL |
| (Pas de benediction Pape) | Admin uniquement |

## Fichiers cles
- `titles/church/ChurchManager.java` - Gestion des rangs
- `titles/church/commands/ChurchCommand.java` - Commandes
- `titles/church/commands/ChurchPromoteCommand.java` - Promotion

## Pages detaillees
- [[Eglise/Rangs et Foi]] - 7 rangs, foi, systeme d'acolytes
- [[Eglise/Benedictions]] - 3 types de benedictions

## Liens
- [[Systems/Noblesse]] - Combine avec la noblesse dans le nameplate
- [[Items/Consommables]] - Benedictions
