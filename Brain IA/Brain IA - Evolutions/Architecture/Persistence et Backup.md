# Persistence et Backup

#architecture #moyenne #persistence #donnees

## Probleme
Le systeme de persistence a plusieurs faiblesses qui peuvent mener a la **perte de donnees**.

## Issues identifiees

### 1. Pas de backup automatique
- `PersistenceManager.java` ecrit directement dans les fichiers `.properties`
- Si le serveur crash pendant l'ecriture → fichier corrompu
- Pas de copie de sauvegarde avant ecriture

### 2. Pas de versioning des donnees
- Les fichiers `.properties` n'ont pas de champ version
- Si la structure change (ajout de champ) → anciennes sauvegardes incompatibles
- Pas de migration automatique

### 3. Donnees transientes perdues au restart
**Fichier** : `config/Player/PlayerLevelData.java` lignes 66-88
| Champ transient | Impact de la perte |
|----------------|-------------------|
| `lastPvPKillTime` | Reset du cooldown PvP |
| `duelHistory` | Historique perdu |
| `lastDamageTakenTime` | Reset du timer de regen |
| `cooldowns` | Tous les cooldowns reset |

### 4. Donnees de quetes en memoire seule
**Fichier** : `quest/QuestManager.java` ligne 225
- Les cooldowns de quetes journalieres sont stockes en memoire
- Perdu au restart → les joueurs peuvent refaire les quetes journalieres immediatement

### 5. Assertions au lieu de null checks
**Fichier** : `TreasureChestInteractEvent.java` lignes 47, 130
```java
assert world != null;  // Desactive en production !
assert pRef != null;    // Ne protege rien
```

## Corrections proposees

### Backup avant ecriture
```java
public static void saveAll() {
    // 1. Creer un backup du fichier actuel
    Path dataFile = Paths.get(DATA_DIR, "parcels.properties");
    Path backupFile = Paths.get(DATA_DIR, "parcels.properties.bak");
    if (Files.exists(dataFile)) {
        Files.copy(dataFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
    }
    
    // 2. Ecrire dans un fichier temporaire
    Path tempFile = Paths.get(DATA_DIR, "parcels.properties.tmp");
    writeToFile(tempFile);
    
    // 3. Remplacer atomiquement
    Files.move(tempFile, dataFile, StandardCopyOption.ATOMIC_MOVE);
}
```

### Versioning des donnees
```properties
# En tete de chaque fichier .properties
_version=2
_savedAt=2026-04-30T15:00:00
```

### Persister les cooldowns
```java
// Sauvegarder les cooldowns dans le properties du joueur
public void saveCooldowns(Properties props, String prefix) {
    for (Map.Entry<String, Long> cd : cooldowns.entrySet()) {
        props.setProperty(prefix + "cooldown." + cd.getKey(), 
            String.valueOf(cd.getValue()));
    }
}
```

## Priorite
**MOYENNE** — Risque de perte de donnees important mais rare

## Liens
- [[../Brain IA/Architecture/Persistence]] - Documentation actuelle
- [[Threading et Synchronisation]] - Race conditions pendant la sauvegarde
