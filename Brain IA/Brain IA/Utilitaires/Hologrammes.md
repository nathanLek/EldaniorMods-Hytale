# Systeme de Hologrammes

#hologramme #affichage #arene #classement

## Vue d'ensemble
Texte flottant en 3D dans le monde, utilise pour afficher du lore, des classements d'arene ou des informations de gameplay. Hytale ne supportant pas nativement les hologrammes, le systeme utilise un **hack ProjectileComponent + Nameplate**.

## Architecture technique
- Chaque hologramme est une entite invisible avec un `ProjectileComponent` et un `Nameplate`
- Support **multi-lignes** : espacement vertical de 0.3 unites entre chaque ligne
- Chaque ligne = une entite separee

## HologramManager
- CRUD complet (create, delete, list)
- Persistence dans `holograms.properties` (dossier `eldanior_data/`)
- `nextId` auto-incremente pour garantir l'unicite
- Spawn des hologrammes au chargement du monde

## HologramData
Modele de donnees :
- `id` — identifiant unique
- `lines` — liste de lignes de texte
- `x`, `y`, `z` — position dans le monde
- `world` — nom du monde

## DynamicHologramManager
Hologrammes auto-mis a jour pour les classements d'arene :
- Timer de refresh toutes les **60 secondes**
- Mapping `arenaParcelId → holoId` pour lier un hologramme a une arene
- `autoCreateForAllArenas()` — cree automatiquement un hologramme pour chaque arene existante au demarrage
- Affiche le top players (kills/deaths) de chaque arene

## Presets narratifs (HologramCommand)
Des presets de texte predecoupes pour decorer le spawn :
- `WELCOME_LINES` — texte d'accueil
- `CLASSES_LINES` — presentation des classes
- `GUILDE_LINES` — presentation des guildes

## Commandes
```
/es hologram create <nom>      → Creer a la position actuelle
/es hologram delete <nom>      → Supprimer
/es hologram list              → Lister tous les hologrammes
/es hologram preset <preset>   → Appliquer un preset narratif
```

## Fichiers cles
- `hologram/HologramManager.java` — CRUD + persistence
- `hologram/HologramData.java` — modele de donnees
- `hologram/DynamicHologramManager.java` — hologrammes dynamiques (arenes)
- `hologram/HologramCommand.java` — commande /es hologram + presets

## Liens
- [[Systems/Territoires]] - Arenes liees aux hologrammes dynamiques
- [[Systems/Classements]] - Donnees affichees par les hologrammes
- [[Architecture/Persistence]] - holograms.properties
