# Memory Leaks

#bug #critique #memoire #performance #CORRIGE

> **CORRIGE** le 2026-05-01 :
> - PlayerPositionTracker : 3 maps nettoyees au disconnect
> - Caches GUI : verifie — toutes les listes sont clear() a chaque refresh, pas de vrai leak
> - QuestTab.playerDisplayCaches : deja nettoye via cleanupPlayer() au disconnect
> - Timer leaks consumables : remplace par EldaniorLogger.SCHEDULER (ScheduledExecutorService partage)
> - RateLimiter : cleanup au disconnect
> - **TOUT RESOLU**

## Probleme
Plusieurs maps statiques **grossissent indefiniment** car les entrees ne sont jamais supprimees quand un joueur se deconnecte.

---

## 1. PlayerPositionTracker — 3 maps sans cleanup
**Fichier** : `config/Player/PlayerPositionTracker.java` lignes 20-22
```java
public static final Map<UUID, Vector3d> PLAYER_POSITIONS = new ConcurrentHashMap<>();
public static final Map<UUID, Integer> PLAYER_LEVELS = new ConcurrentHashMap<>();
public static final Map<UUID, Integer> PLAYER_DIGNITY = new ConcurrentHashMap<>();
```

**Probleme** : Ces maps sont alimentees a chaque connexion/tick mais **jamais nettoyees** lors de la deconnexion.

**Impact** : Avec 100 joueurs/jour sur un serveur qui tourne 1 semaine sans restart → 700 entrees inutiles en memoire.

**Fichier disconnect** : `EldaniorSystem.java` lignes 188-198 — gere Party, Trade, Parcel, QuestTab mais **PAS PlayerPositionTracker**.

### Correction
```java
// Dans EldaniorSystem.java, handler PlayerDisconnectEvent (ligne 188)
PlayerPositionTracker.PLAYER_POSITIONS.remove(uuid);
PlayerPositionTracker.PLAYER_LEVELS.remove(uuid);
PlayerPositionTracker.PLAYER_DIGNITY.remove(uuid);
```

---

## 2. Caches GUI — 5 caches statiques sans nettoyage
| Fichier | Cache | Ligne |
|---------|-------|-------|
| `gui/tabs/QuestTab.java` | `playerDisplayCaches` | 26 |
| `gui/tabs/GuildeTab.java` | `cachedInviteNames` | 24 |
| `gui/tabs/DuelTab.java` | `cachedPlayerNames` | 24 |
| `gui/tabs/FamilleTab.java` | `cachedFamilyIds` | 24 |
| `gui/tabs/FamilleTab.java` | `cachedInviteNames` | 25 |
| `gui/tabs/AdminTab.java` | `cachedPlayerNames` | 26 |
| `gui/tabs/BlackMarketTab.java` | `cachedPlayerNames` | 24 |
| `gui/tabs/EchangesTab.java` | `cachedPlayerNames` | 21 |
| `gui/tabs/GroupeTab.java` | `cachedInviteNames` | 30 |
| `gui/tabs/ProprietesTab.java` | `cachedOwnedIds`, `cachedAvailableIds`, `cachedInviteNames` | 27-29 |
| `gui/tabs/TerritoiresTab.java` | `cachedTerrIds` | 23 |

**Probleme** : Ces caches statiques accumulent des donnees par joueur mais ne sont jamais vides. Seul `QuestTab.cleanupPlayer(uuid)` est appele au disconnect (ligne 197 dans EldaniorSystem), les autres non.

### Correction
Ajouter dans le handler disconnect :
```java
GuildeTab.cleanupPlayer(uuid);
DuelTab.cleanupPlayer(uuid);
FamilleTab.cleanupPlayer(uuid);
```

---

## 3. Timer Leaks — Consumables
**Fichiers** :
- `skills/interaction/ConsumableItemMoneyInteraction.java` ligne 60
- `skills/interaction/ConsumableItemStatsInteraction.java` ligne 74
- `skills/interaction/ConsumableItemSkillInteraction.java` ligne 117

**Pattern problematique** :
```java
new Timer().schedule(new TimerTask() {
    @Override public void run() { /* action */ }
}, 500);
```

**Probleme** : Chaque consommation d'item cree un **nouveau Timer** (= nouveau thread). Avec 50 joueurs qui consomment des items → 50 threads Timer inutiles accumules.

### Correction
```java
// Utiliser un ScheduledExecutorService partage
private static final ScheduledExecutorService SCHEDULER = 
    Executors.newSingleThreadScheduledExecutor();

// Au lieu de new Timer()
SCHEDULER.schedule(() -> { /* action */ }, 500, TimeUnit.MILLISECONDS);
```

---

## 4. Collections mutables exposees publiquement
**Fichier** : `shop/ShopManager.java` lignes 36, 57
```java
public static List<ShopListing> getListings() { return listings; }
public static List<ShopListing> getBlackMarketListings() { return blackMarketListings; }
```

**Probleme** : Retourne une reference directe → un appelant peut `.clear()` la liste entiere.

### Correction
```java
public static List<ShopListing> getListings() { 
    return Collections.unmodifiableList(listings); 
}
```

---

## Priorite
**CRITIQUE** — Fuite memoire progressive, degrade les performances sur le long terme

## Liens
- [[Architecture/Threading et Synchronisation]] - Static state non synchronise
- [[Features/Auto-Eviction Locations]] - Aussi un timer manquant