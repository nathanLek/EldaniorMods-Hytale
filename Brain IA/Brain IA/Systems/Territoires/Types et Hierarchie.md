# Types de Parcelles et Hierarchie

#territoire #types #hierarchie #parcelle

## Hierarchie
```
KINGDOM (Royaume)
  └── TERRITORY (Territoire)
       └── CITY (Ville)
            ├── PLOT (Parcelle)
            ├── HOUSING (Logement)
            │    └── ROOM (Chambre)
            └── FARM (Zone de Recolte)
```

## Details par type

### KINGDOM (Royaume)
- Plus grand type de territoire
- Gere par le **Roi** et sa famille
- Contient des Territoires et Villes
- Champs : `familyId`, `treasury`

### TERRITORY (Territoire)
- Subdivision du Royaume
- Gere par un **Marquis** ou **Duc** + famille
- Contient des Villes

### CITY (Ville)
- Zone urbaine
- Geree par un **Comte** + guilde
- Seul type qui peut **toggler le PvP**
- Champs : `guildId`, `pvpEnabled`

### PLOT (Parcelle)
- Petit terrain personnel (32x32 blocs par defaut)
- Pour les joueurs individuels
- Achetable/louable

### HOUSING (Logement)
- Taille maison, residentiel prive
- Peut contenir des ROOM

### ROOM (Chambre)
- Plus petit espace interieur
- A l'interieur d'un HOUSING

### FARM (Zone de Recolte)
- Zone agricole/ressources
- Blocs cassables par tous
- Regeneration prevue (non implementee)

## Fichier cle
- `territory/ParcelType.java` — enum des 7 types

## Liens
- [[Permissions et Roles]] - Qui peut faire quoi
- [[Achat et Location]] - Economie des parcelles
- [[../Territoires]] - Vue d'ensemble
