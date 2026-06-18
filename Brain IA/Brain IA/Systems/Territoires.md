# Systeme de Territoires & Parcelles

#territoire #parcelle #immobilier #protection

## Types de zones (12 types)

| Type | Cree par | Dans | Couleur |
|------|----------|------|---------|
| **KINGDOM** | Admin | - (top level) | Or #FFD700 |
| **GRAND_TERRITORY** | Admin/Noble | Kingdom | - |
| **TERRITORY** | Admin/Noble | Kingdom ou Grand Territory | Bleu #3498DB |
| **CITY** | Admin/Noble | Territory | Vert #2ECC71 |
| **PLOT** | Admin | City | Gris #aabbcc |
| **HOUSING** | Admin | City | Violet #9B59B6 |
| **ROOM** | Admin | Housing | Rose #E91E63 |
| **FARM** | Admin | City/Territory | Marron #8B4513 |
| **FOREST** | Admin | Territory/Grand Territory | - |
| **ARENA** | Admin | Territory/Grand Territory | - |
| **DUNGEON** | Admin | Territory/Grand Territory | - |
| **MINE** | Admin | Territory/Grand Territory | - |

## Hierarchie de propriete
```
Royaume → proprio = Famille Royale
  Grand Territoire → proprio = Famille noble (prix : 100M)
    Territoire Marquis → proprio = Famille du Marquis (prix : 30M)
      Ville → proprio = Guilde du Comte
        Plot/Housing → proprio = Ville (par defaut) ou Joueur (achat/location)
      Arene → PvP avec leaderboards (ArenaManager)
      Donjon → dungeonRank (defaut "E"), regenDelaySec (defaut 300s)
      Mine / Foret / Ferme → zones de ressources
```

## Types speciaux

### GRAND_TERRITORY
- Niveau intermediaire entre Kingdom et Territory
- Prix famille : 100 000 000
- Peut contenir : TERRITORY, CITY, ARENA, DUNGEON, MINE, FARM, FOREST, PLOT, HOUSING

### DUNGEON
- Champ `dungeonRank` (defaut "E") — rang de difficulte
- Champ `regenDelaySec` (defaut 300s) — delai de regeneration
- Champs `firstDiscovererName` / `firstDiscovererUUID` — premier explorateur

### ARENA (ArenaManager)
- Leaderboards par arene (kills/deaths par joueur, top players)
- Persistence dans `arena_stats.properties`
- `DuelProtectionSystem` gere aussi la protection anti-mort en arene

## Creation
1. Selectionner une zone avec le **Selection Tool** de Hytale
2. `/es parcel create <TYPE> <NOM>`
3. La parcelle est creee SANS proprietaire
4. Le parent est detecte automatiquement (coin + centre de selection)

## Protection
### Villes / Territoires / Royaumes
- **INTERACT** et **ENTER** : autorises pour tous
- **BUILD** et **BREAK** : bloques (sauf membres)

### Plots / Housing / Room
- Protection complete : rien autorise pour les non-membres
- Permissions configurables par role (OWNER, OFFICER, MEMBER, VISITOR)

### PvP
- Toggle par **ville** uniquement (bouton dans gestion territoire)
- Si desactive : degats entre joueurs annules dans la zone
- Admin peut toggle sur n'importe quel type

## Achat / Location

### Achat
- Le joueur achete un plot/housing → devient OWNER
- 12% de taxe sur la transaction
- Le joueur peut revendre (remet en vente, reste proprio jusqu'a l'achat)

### Location
- 7 jours renouvelable
- Le locataire a les permissions OWNER sur la parcelle
- Le proprio original n'a AUCUN acces pendant la location
- Bouton PROLONGER (paie le loyer + 7j)
- Bouton QUITTER (resilie la location)
- Si le joueur a achete, il peut mettre en location

## Onglets GUI

### Onglet Territoires (Nobles/Admin)
- Carte par territoire avec stats HUD style objectifs
- Tresorerie, population, sous-zones
- Boutons : PvP, Impots, Transferer, Protection, Famille, Guilde, Invasion, Decrets

### Onglet Proprietes (Tous)
- **Mes biens** : liste des plots/housing possedes/loues
- Panneau deroulant avec detail + boutons de gestion
- **Marche immobilier** : offres en vente/location

## Notifications
- **Royaume / Territoire / Ville** → Grand titre au centre (comme level up)
- **Plot / Housing / Room / Farm** → Petite notification en bas a droite
- Sortie ville → pas de message territoire (deja dedans)

## Commandes
```
/es parcel pos1 _          → Position 1
/es parcel pos2 _          → Position 2
/es parcel create TYPE NOM → Creer (Selection Tool ou pos1/pos2)
/es parcel delete ID _     → Supprimer
/es parcel info _ _        → Info zone actuelle
/es parcel invite JOUEUR _ → Ajouter membre
/es parcel kick JOUEUR _   → Retirer membre
/es parcel setperm R:P V   → Modifier permission
/es parcel list _ _        → Lister parcelles
/es parcel sell PRIX _     → Mettre en vente
/es parcel buy _ _         → Acheter
/es parcel setprice PRIX _ → Configurer prix vente
/es parcel setrent PRIX _  → Configurer prix location
/es parcel assign FAMILY _ → Assigner famille
/es parcel assignguild G _ → Assigner guilde (villes)
```

## Fichiers cles
- `territory/ParcelData.java` - Modele de donnees
- `territory/ParcelManager.java` - Registre + persistence + lookup
- `territory/ParcelEconomyManager.java` - Taxes et distribution
- `territory/commands/ParcelCommand.java` - Commandes
- `territory/events/Parcel*Event.java` - Protection blocs
- `territory/systems/ParcelRangeSystem.java` - Detection entree/sortie

## Pages detaillees
- [[Territoires/Types et Hierarchie]] - 12 types, arbre hierarchique
- [[Territoires/Permissions et Roles]] - Qui peut faire quoi, cas location
- [[Territoires/Achat et Location]] - Achat, location, taxes, renouvellement

## Liens
- [[Systems/Economie]] - Systeme de taxes et impots
- [[Systems/Noblesse]] - Qui gere quoi
- [[Systems/Familles]] - Familles nobles associees
- [[Systems/Guildes]] - Guildes associees aux villes
