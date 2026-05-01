# Shutdown et Disconnect Incomplets

#bug #haute #shutdown #disconnect #cleanup #CORRIGE

> **PARTIELLEMENT CORRIGE** le 2026-05-01 :
> - Shutdown : TradeManager.cancelAllTrades(), DuelManager.cancelAllDuels(), PersistenceManager.shutdown() ajoutes
> - Disconnect : PlayerPositionTracker (3 maps) nettoye au disconnect
> - Reste a faire : caches GUI tabs (AdminTab, BlackMarketTab, etc.)

## 1. Shutdown — Seulement 2 managers sur 11 sauvegardes
**Fichier** : `EldaniorSystem.java` lignes 71-76

### Etat actuel
```java
// onDisable()
PersistenceManager.saveAll();  // Sauve guildes, familles, classements, shop
ParcelManager.saveAll();        // Sauve parcelles
// ... c'est tout
```

### Managers NON sauvegardes au shutdown
| Manager | Donnees perdues |
|---------|----------------|
| DuelManager | Duels actifs non annules proprement |
| TradeManager | Trades actifs — items potentiellement perdus |
| QuestManager | Quetes actives en cours |
| NobilityManager | Hierarchie complete (voir [[Persistence Hierarchies]]) |
| ChurchManager | Hierarchie complete |
| PartyManager | OK — ephemere, pas besoin de sauvegarder |

### Timer NON annule
```java
// PersistenceManager.java ligne 30
autoSaveTimer = new java.util.Timer("EldaniorAutoSave", true);
// Jamais cancel() → le thread daemon peut persister
```

### Correction
```java
// Dans onDisable()
public void onDisable() {
    // 1. Annuler les sessions actives
    TradeManager.cancelAllTrades();  // Rendre les items
    DuelManager.cancelAllDuels();
    
    // 2. Sauvegarder tout
    PersistenceManager.saveAll();
    ParcelManager.saveAll();
    NobilityManager.save();   // NOUVEAU
    ChurchManager.save();     // NOUVEAU
    
    // 3. Annuler le timer
    PersistenceManager.shutdown(); // cancel() le timer
}
```

---

## 2. Disconnect — 4 handlers mais 3 manquants
**Fichier** : `EldaniorSystem.java` lignes 188-198

### Handlers presents
| Handler | Ligne | Action |
|---------|-------|--------|
| PartyManager.handleDisconnect(uuid) | 193 | Quitte le groupe |
| TradeManager.handleDisconnect(uuid) | 194 | Annule le trade, rend items |
| ParcelRangeSystem.handleDisconnect(uuid) | 195 | Nettoie le tracking de zone |
| QuestTab.cleanupPlayer(uuid) | 197 | Nettoie le cache quetes |

### Handlers MANQUANTS
| Manager | Donnees a nettoyer |
|---------|-------------------|
| **DuelManager** | Duel actif → doit etre annule, adversaire notifie |
| **PlayerPositionTracker** | 3 maps (POSITIONS, LEVELS, DIGNITY) |
| **GuildeTab** | cachedInviteNames |
| **DuelTab** | cachedPlayerNames |
| **FamilleTab** | cachedFamilyIds + cachedInviteNames |

### Correction
```java
// Ajouter dans le handler disconnect (ligne 196)
DuelManager.handleDisconnect(uuid);
PlayerPositionTracker.PLAYER_POSITIONS.remove(uuid);
PlayerPositionTracker.PLAYER_LEVELS.remove(uuid);
PlayerPositionTracker.PLAYER_DIGNITY.remove(uuid);
GuildeTab.cleanupPlayer(uuid);
DuelTab.cleanupPlayer(uuid);
FamilleTab.cleanupPlayer(uuid);
```

---

## 3. deleteParcel recursif — save() appele N fois
**Fichier** : `territory/ParcelManager.java` lignes 69-76
```java
public static void deleteParcel(String id) {
    List<String> children = getChildrenOf(id);
    for (String childId : children) {
        deleteParcel(childId);  // Recursion
    }
    parcels.remove(id);
    save();  // Appele a CHAQUE niveau de recursion !
}
```

**Impact** : Supprimer un Royaume avec 5 Territoires, 10 Villes, 50 Parcelles → 66 appels a `save()` (ecritures disque).

### Correction
```java
public static void deleteParcel(String id) {
    deleteParcelRecursive(id);
    save();  // Un seul save a la fin
}

private static void deleteParcelRecursive(String id) {
    for (String childId : getChildrenOf(id)) {
        deleteParcelRecursive(childId);
    }
    parcels.remove(id);
}
```

---

## Priorite
**HAUTE** — Perte de donnees possible au shutdown/disconnect

## Liens
- [[Persistence Hierarchies]] - Donnees non persistees
- [[Memory Leaks]] - Caches non nettoyes au disconnect
- [[Architecture/Persistence et Backup]] - Systeme de sauvegarde