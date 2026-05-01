# Threading et Synchronisation

#architecture #moyenne #threading #concurrence

## Probleme
Plusieurs managers utilisent `ConcurrentHashMap` mais effectuent des operations **multi-etapes non atomiques** (check-then-act).

## Managers concernes
| Manager | Maps statiques | Synchronise ? |
|---------|---------------|---------------|
| PartyManager | playerParty, pendingInvites | Non |
| GuildManager | guilds, playerGuildMap, pendingInvites | Non |
| TradeManager | activeSessions, pendingInvites | Non |
| DuelManager | activeDuels, pendingDuels | Non |
| QuestManager | playerQuests, cooldowns | Non |
| ParcelManager | parcels | Non |
| ClassementManager | leaderboards | Non |
| ShopManager | listings, blackMarketListings | Non |

## Race condition GUI vs Save
Le `PersistenceManager` autosave toutes les 5 minutes. Si un handler GUI modifie des donnees pendant la sauvegarde → donnees partiellement ecrites.

## Static mutable fields
```java
// SystemScreen.java — indices partages entre tous les joueurs
private static int priceIdx = 0;   // MUTABLE STATIC
private static int rentIdx = 0;    // MUTABLE STATIC
```

## Correction proposee
- `synchronized` blocks pour les operations multi-etapes
- Snapshot des donnees avant sauvegarde
- `cancel()` du Timer au shutdown

## Liens
- [[../Bugs/Race Conditions]] - Cas concrets
- [[Persistence et Backup]] - Sauvegarde des donnees