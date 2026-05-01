# Outils Admin Manquants

#admin #basse #commandes #outils

## Commandes admin a ajouter

### Territoire & Location
| Commande | Description | Priorite |
|----------|------------|----------|
| `/es admin evictall` | Expulser tous les locataires expires | Haute |
| `/es admin repairparcels` | Reparer les hierarchies parent-enfant cassees | Moyenne |
| `/es admin orphancheck` | Lister les parcelles orphelines | Moyenne |
| `/es admin reassign <parcel> <parent>` | Reassigner une parcelle a un parent | Basse |

### Economie & Audit
| Commande | Description | Priorite |
|----------|------------|----------|
| `/es admin taxaudit <player>` | Historique des taxes payees par un joueur | Haute |
| `/es admin transactions <parcel>` | Log des transactions d'une parcelle | Haute |
| `/es admin economy reset <parcel>` | Reset la tresorerie d'une parcelle | Moyenne |
| `/es admin economy inject <parcel> <amount>` | Injecter de l'or dans une tresorerie | Basse |

### Debug
| Commande | Description | Priorite |
|----------|------------|----------|
| `/es debug on/off` | Toggle le mode debug (logs verbose) | Haute |
| `/es debug effects` | Valider tous les mappings skill→effet | Haute |
| `/es debug stats <player>` | Afficher tous les stats calcules d'un joueur | Moyenne |

### Backup
| Commande | Description | Priorite |
|----------|------------|----------|
| `/es admin backup` | Sauvegarder toutes les donnees manuellement | Haute |
| `/es admin restore <timestamp>` | Restaurer depuis un backup | Moyenne |

## Transaction logging
Actuellement, aucune transaction n'est loguee. Propositions :
- Fichier `transactions.log` avec format : `[timestamp] [type] [player] [amount] [parcel] [details]`
- Rotation des logs (1 fichier par jour)
- Accessible via commande admin

## Priorite globale
**BASSE** — Utile pour la gestion, pas bloquant pour le gameplay

## Liens
- [[Features/Auto-Eviction Locations]] - Necessite des outils d'eviction
- [[Architecture/Persistence et Backup]] - Systeme de backup
- [[Architecture/Gestion Erreurs]] - Mode debug
- [[Commandes/Liste des commandes]] - Commandes existantes
