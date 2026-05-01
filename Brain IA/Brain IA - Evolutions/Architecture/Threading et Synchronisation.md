# Threading et Synchronisation

#architecture #moyenne #threading #concurrence

## Probleme
Plusieurs managers utilisent `ConcurrentHashMap` mais effectuent des operations **multi-etapes non atomiques**. Le ConcurrentHashMap protege les operations individuelles mais pas les sequences check-then-act.

## Managers concernes

### 1. Global Static State
| Manager | Maps statiques | Synchronise ? |
|---------|---------------|---------------|
| `PartyManager` | playerParty, pendingInvites | Non |
| `GuildManager` | guilds, playerGuildMap, pendingInvites | Non |
| `TradeManager` | activeSessions, pendingInvites | Non |
| `DuelManager` | activeDuels, pendingDuels | Non |
| `QuestManager` | playerQuests, cooldowns | Non |
| `ParcelManager` | parcels | Non |
| `ClassementManager` | leaderboards | Non |

### 2. Timer non annule
**Fichier** : `persistence/PersistenceManager.java` lignes 18, 30-36
```java
private static java.util.Timer autoSaveTimer;
autoSaveTimer = new java.util.Timer("EldaniorAutoSave", true);
// Pas de cancel() dans shutdown → le daemon thread peut persister
```

### 3. Race condition GUI vs Save
Le `PersistenceManager` autosave toutes les 5 minutes. Si un handler GUI modifie des donnees pendant la sauvegarde, les donnees peuvent etre partiellement ecrites.

## Corrections proposees

### Synchronisation des operations critiques
```java
// Pattern recommande pour les operations multi-etapes
private static final Object tradeLock = new Object();

public static boolean sendInvite(UUID sender, UUID target) {
    synchronized (tradeLock) {
        if (isInTrade(sender) || isInTrade(target)) return false;
        pendingInvites.put(target, sender);
        return true;
    }
}
```

### Annulation du Timer
```java
// Dans EldaniorSystem.onDisable()
public static void shutdown() {
    if (autoSaveTimer != null) {
        autoSaveTimer.cancel();
        autoSaveTimer = null;
    }
    saveAll(); // Derniere sauvegarde
}
```

### Sauvegarde thread-safe
```java
public static void saveAll() {
    synchronized (saveLock) {
        // Serialiser une snapshot des donnees
        Map<String, ParcelData> snapshot = new HashMap<>(parcels);
        // Ecrire la snapshot (pas les donnees live)
        writeToFile(snapshot);
    }
}
```

## Priorite
**MOYENNE** — Problemes rares mais critiques quand ils surviennent

## Liens
- [[../Bugs/Race Conditions]] - Cas concrets de race conditions
- [[Persistence et Backup]] - Sauvegarde des donnees
