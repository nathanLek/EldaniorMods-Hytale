# Eldanior System - Evolutions & Optimisations

> Second cerveau dedie aux **ameliorations, bugs, optimisations et features manquantes** du plugin Eldanior.
> Lie au cerveau principal : [[Plugin Hytale ( Eldanior )]]

---

## Issues Ouvertes

### Bugs restants
- [[Bugs/Ressources Manquantes]] - 29 assets references manquants (modeles, textures, icones)

### Optimisations restantes
- [[Optimisations/Validation Effets Visuels]] - 50+ skills sans mapping d'effet + assets non verifies

### Features Manquantes
- [[Features/Auto-Eviction Locations]] - Pas d'expulsion automatique des locataires expires
- [[Features/Regeneration Fermes]] - Les parcelles FARM n'ont pas de regeneration de blocs
- [[Features/Application PvP Zones]] - Le flag PvP existe mais pas de notifications entree/sortie
- [[Features/Taxes Hebdomadaires]] - collectWeeklyTaxes() est vide
- [[Features/Effets Manquants Skills]] - ~87 skills sans effet visuel associe

### Architecture restante
- [[Architecture/Threading et Synchronisation]] - Revue generale (principales race conditions corrigees)
- [[Architecture/Persistence et Backup]] - Backup atomique, versioning (hierarchies deja persistees)

### Balance restante
- [[Balance/Economie Taxes]] - Inconsistance entre ParcelEconomyManager et TerritoiresTab

### Admin & GUI
- [[Admin/Outils Admin Manquants]] - Commandes admin supplementaires
- [[GUI/Ameliorations Interface]] - Boutons manquants, infos incompletes

---

## Statistiques
| Categorie | Ouvertes | Corrigees | Total |
|-----------|----------|-----------|-------|
| Bugs & Securite | 1 | 9 | 10 |
| Performance | 1 | 3 | 4 |
| Features | 5 | 0 | 5 |
| Architecture | 2 | 3 | 5 |
| Balance | 1 | 2 | 3 |
| Admin & GUI | 2 | 0 | 2 |
| **TOTAL** | **12 ouvertes** | **16 corrigees** | **28** |

---

## 21 Corrections Appliquees (2026-05-01)
> Detail dans [[Archive Corriges/]] — 16 pages archivees

1. Persist hierarchies Noblesse/Eglise/Familles
2. Exploit duplication Trade (synchronized TRADE_LOCK)
3. Memory leaks (PlayerPositionTracker, Timer, RateLimiter cleanup)
4. Shutdown complet (cancelAllTrades, cancelAllDuels, PersistenceManager.shutdown)
5. UUIDExtractor centralise (46 fichiers refactores)
6. Fix Decret_Marquis.json (Model + Texture)
7. Exceptions silencieuses → EldaniorLogger (67 catches, 38 fichiers)
8. Race conditions (TRADE_LOCK, DUEL_LOCK, PARTY_LOCK)
9. Index O(1) GuildManager (tagIndex + nameIndex)
10. Exploit duplication Shop (buyListing atomique, unmodifiableList)
11. Null Reference Classes (fallback Novice)
12. Permission admin centralisee (ADMIN_PERMISSION, 11 fichiers)
13. Level cap 500 + XP multiplier cap 2.0x
14. Dodge plancher 2% minimum
15. Duel history persistence (CODEC)
16. Validation noms guilde (regex, longueur, doublons)
17. Timer leaks → SCHEDULER partage
18. Recursive deleteParcel batch save
19. Validation parent parcelles (VALID_CHILDREN)
20. Rate limiter (RateLimiter.java)
21. AdminTab permission check

---
#evolutions #optimisation #roadmap #eldanior