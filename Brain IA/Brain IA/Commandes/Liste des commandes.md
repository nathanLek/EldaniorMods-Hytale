# Liste des Commandes

#commandes #admin #gameplay

## Commande racine : `/es`

### Leveling (Admin)
| Commande | Description |
|----------|-------------|
| `/es addxp <joueur> <montant>` | Donner de l'XP |
| `/es setlevel <joueur> <niveau>` | Definir le niveau (1 = reset complet) |

### Classes
| Commande | Description |
|----------|-------------|
| `/es classinfo <joueur>` | Info classe du joueur |
| `/es setclass <joueur> <classe>` | Forcer une classe |

### Skills
| Commande | Description |
|----------|-------------|
| `/es getrelic` | Donner une relique aleatoire |
| `/es withdraw` | Retirer un skill |

### Titres
| Commande | Description |
|----------|-------------|
| `/es titlelist <joueur>` | Lister les titres |
| `/es titleadmin grant <joueur> <titleId>` | Donner un titre |
| `/es titleadmin remove <joueur> <titleId>` | Retirer un titre |

### Noblesse
| Commande | Description |
|----------|-------------|
| `/es rank promote <joueur>` | Promouvoir en noblesse |
| `/es rank demote <joueur>` | Retrograder |
| `/es kingdom _` | Info royaume |
| `/es nstatus setvice <joueur>` | Definir vice de famille |

### Eglise
| Commande | Description |
|----------|-------------|
| `/es church status <joueur>` | Info eglise |
| `/es church promote <joueur>` | Promouvoir |
| `/es church demote <joueur>` | Retrograder |

### Famille
| Commande | Description |
|----------|-------------|
| `/es familyset <joueur> <familyId>` | Assigner famille |
| `/es family _` | Info famille |

### Guilde
| Commande | Description |
|----------|-------------|
| `/es guildcreate <nom> <tag>` | Creer une guilde |
| `/es guild invite/kick/promote/demote/info/leave/accept/decline <arg>` | Gestion |
| `/es guilddisband` | Dissoudre |

### Groupe
| Commande | Description |
|----------|-------------|
| `/es party create/invite/kick/leave/disband/accept/decline/list <arg>` | Gestion groupe |

### Duel
| Commande | Description |
|----------|-------------|
| `/es duel accept/decline` | Repondre a un defi |

### Echange
| Commande | Description |
|----------|-------------|
| `/es trade accept/decline/cancel` | Gestion echange |

### Shop
| Commande | Description |
|----------|-------------|
| `/es sell` | Vendre un item |

### Parcelles
| Commande | Description |
|----------|-------------|
| `/es parcel pos1 _` | Position 1 |
| `/es parcel pos2 _` | Position 2 |
| `/es parcel create <TYPE> <NOM>` | Creer (Selection Tool ou pos) |
| `/es parcel delete <ID> _` | Supprimer |
| `/es parcel info _ _` | Info zone actuelle |
| `/es parcel invite <joueur> _` | Ajouter membre |
| `/es parcel kick <joueur> _` | Retirer membre |
| `/es parcel setperm <ROLE:PERM> <true/false>` | Permissions |
| `/es parcel list _ _` | Lister |
| `/es parcel sell <prix> _` | Mettre en vente |
| `/es parcel buy _ _` | Acheter |
| `/es parcel setprice <prix> _` | Prix de vente |
| `/es parcel setrent <prix> _` | Prix de location |
| `/es parcel assign <familyId> _` | Assigner famille |
| `/es parcel assignguild <guildId> _` | Assigner guilde |

### Hologrammes
| Commande | Description |
|----------|-------------|
| `/es hologram create <nom>` | Creer un hologramme a la position actuelle |
| `/es hologram delete <nom>` | Supprimer un hologramme |
| `/es hologram list` | Lister les hologrammes |
| `/es hologram preset <preset>` | Appliquer un preset narratif (Welcome, Classes, Guilde...) |

### Coffres au tresor
| Commande | Description |
|----------|-------------|
| `/es treasure delete <id>` | Supprimer un coffre au tresor |
| `/es treasure config` | Configurer les coffres au tresor |

### Interface
| Commande | Description |
|----------|-------------|
| `/es system` | Ouvrir le menu principal (joueur) |
| `/es admin` | Ouvrir l'interface d'administration (OP uniquement) |

## Liens
- [[Architecture/GUI SystemScreen]] - Interface graphique
- [[Systems/Territoires]] - Commandes parcelles
