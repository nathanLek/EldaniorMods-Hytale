# Persistence des donnees

#persistence #sauvegarde #properties

## Mecanismes

### 1. EntityStore (Hytale natif)
Donnees **par entite**, sauvegardees automatiquement par Hytale dans le monde.
- `PlayerLevelData` → toutes les stats joueur
- `PlayerChestData` → coffres visites
- `MobLevelData` → niveau des mobs
- Utilise des **Codecs** (BuilderCodec) pour la serialisation

### 2. Properties (fichiers .properties)
Donnees **globales**, sauvegardees manuellement dans `eldanior_data/`.
- `guilds.properties` → guildes
- `families.properties` → familles nobles
- `classements.properties` → classements
- `shop.properties` → boutique
- `blackmarket.properties` → marche noir
- `pending_earnings.properties` → gains en attente (joueurs absents, shop earnings)
- `holograms.properties` → persistence des hologrammes
- `arena_stats.properties` → stats des arenes (leaderboards)
- `hierarchies.properties` → hierarchies (Roi, Pape) — corrige, desormais persiste
- Autosave toutes les **5 minutes**

### 3. Parcelles (fichier separe)
- `parcels.properties` → dans le dossier du plugin (`getDataDirectory()`)
- Sauvegarde manuelle a chaque modification
- Format : `parcelId.champ=valeur`

## Autosave
```java
Timer autoSave = toutes les 5 minutes
→ saveGuilds()
→ saveFamilies()
→ saveClassements()
→ saveShop()
→ ParcelEconomyManager.collectWeeklyTaxes()
→ ParcelManager.saveAll()
```

## Shutdown
`EldaniorSystem.shutdown()` :
```java
PersistenceManager.saveAll()
ParcelManager.saveAll()
```

## Reset (Admin)
Boutons dans l'onglet Admin :
- Reset Guildes
- Reset Familles
- Reset Parcelles
- Reset Shop
- Reset Classements
- Reset Complet (tout)

## Fichiers cles
- `persistence/PersistenceManager.java` - Save/load guildes, familles, classements, shop
- `territory/ParcelManager.java` - Save/load parcelles
- `config/Player/PlayerLevelData.java` - Codec joueur (~30 champs)

## Liens
- [[Architecture/ECS Systems]] - Donnees alimentees par les systemes
- [[Architecture/GUI SystemScreen]] - Donnees affichees
