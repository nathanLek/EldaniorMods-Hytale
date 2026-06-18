# Persistence et Backup

#architecture #moyenne #persistence #donnees

## 3 Mecanismes de sauvegarde

### 1. EntityStore (CODEC)
- Composants attaches aux entites (PlayerLevelData, MobLevelData, PlayerChestData)
- Sauvegarde automatique par Hytale via les CODECs
- 36+ champs pour PlayerLevelData

### 2. Properties Files (PersistenceManager)
- Guildes → `guilds.properties`
- Familles → `families.properties`
- Classements → `classements.properties`
- Shop → `shop.properties`
- Autosave toutes les 5 minutes via Timer

### 3. Parcelles (ParcelManager)
- `parcels.properties` — 27 champs par parcelle
- Sauvegarde separee de PersistenceManager
- 2 corrections automatiques au chargement (rental corrupted)

## Problemes identifies

### Pas de backup avant ecriture
- Si crash pendant l'ecriture → fichier corrompu
- Pas de `.bak` ou `.tmp` + rename atomique

### Pas de versioning
- Les fichiers n'ont pas de champ `_version`
- Changement de structure = anciennes sauvegardes incompatibles

### Donnees transientes perdues
| Champ | Fichier | Impact |
|-------|---------|--------|
| `lastPvPKillTime` | PlayerLevelData | Cooldown PvP reset |
| `duelHistory` | PlayerLevelData | Historique perdu |
| `cooldowns` | PlayerLevelData | Cooldowns reset |
| Quetes actives | QuestManager | Progression perdue |
| ~~Hierarchies (Roi, Pape)~~ | ~~NobilityManager, ChurchManager~~ | **CORRIGE** — persiste via `saveHierarchies()`/`loadHierarchies()` dans `hierarchies.properties` |

### ~~Timer non annule~~ — CORRIGE
- `autoSaveTimer` a desormais un `cancel()` dans la methode `shutdown()`

## Correction proposee
- Ecriture atomique (fichier temp → rename)
- Backup `.bak` avant chaque ecriture
- Champ `_version` pour migration
- `cancel()` du timer au shutdown

## Fichiers cles
- `persistence/PersistenceManager.java` — sauvegarde centralisee
- `territory/ParcelManager.java` — sauvegarde parcelles
- `config/Player/PlayerLevelData.java` — CODEC joueur

## Liens
- [[Persistence]] - Documentation originale
- [[../Bugs/Persistence Hierarchies]] - Donnees non persistees
- [[../Bugs/Shutdown et Disconnect]] - Shutdown incomplet
