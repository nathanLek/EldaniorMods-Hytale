# Exceptions Silencieuses

#bug #critique #debug #qualite #CORRIGE

> **CORRIGE** le 2026-05-01 — EldaniorLogger.java cree, 67 catches remplaces dans 38 fichiers, debug print → EldaniorLogger.debug().

## Probleme (RESOLU)
**30+ fichiers** contiennent le pattern `catch (Exception ignored) {}` qui avale silencieusement les erreurs, rendant le debug impossible et masquant des crashes potentiels.

## Fichiers affectes (liste non exhaustive)
| Fichier | Lignes | Contexte |
|---------|--------|----------|
| `PersistenceManager.java` | 183, 293 | Sauvegarde/chargement |
| `CombatStatsSystem.java` | 69, 150 | Calcul de combat |
| `DuelManager.java` | 184 | Fin de duel |
| `DignityAuraSystem.java` | 226, 233, 258 | Aura de dignite |
| `ParcelBreakBlockEvent.java` | 54 | Protection parcelles |
| `ParcelInteractEvent.java` | 54 | Interaction parcelles |
| `ParcelPlaceBlockEvent.java` | 54 | Protection parcelles |
| `ParcelRangeSystem.java` | 117 | Detection de zone |
| `TreasureChestInteractEvent.java` | 126 | Coffres au tresor |
| `TreasureChestRangeSystem.java` | 91, 152 | Detection coffres |
| `PartyHud.java` | 201 | HUD groupe |
| `PartyHudUpdateSystem.java` | 26 | Update HUD |
| `SkillEffectConfig.java` | 356 | Application effets |
| `QuestManager.java` | 170 | UUID extraction |

## Impact
- Erreurs de persistence masquees → perte de donnees silencieuse
- Effets visuels qui ne se declenchent jamais sans aucun message
- Combat buggue sans savoir pourquoi
- Parcelles non protegees sans erreur visible

## Correction proposee
```java
// AVANT (mauvais)
try {
    Field uuidF = PlayerRef.class.getDeclaredField("uuid");
    uuidF.setAccessible(true);
    playerUUID = (UUID) uuidF.get(pRef);
} catch (Exception ignored) {}

// APRES (correct)
try {
    playerUUID = UUIDExtractor.getUUID(pRef);
} catch (Exception e) {
    System.err.println("[Eldanior] Erreur extraction UUID: " + e.getMessage());
    return; // Sortir proprement au lieu de continuer avec null
}
```

## Plan d'action
1. Creer un `Logger` centralise (ou utiliser System.err avec prefix `[Eldanior]`)
2. Remplacer tous les `catch (Exception ignored)` par un log + gestion appropriee
3. Identifier les cas ou `return` est necessaire vs continuer avec une valeur par defaut

## Priorite
**CRITIQUE** — Rend le debugging impossible en production

## Liens
- [[Null Reference Classes]] - Lie aux null non detectes
- [[Architecture/Gestion Erreurs]] - Standardisation du logging
