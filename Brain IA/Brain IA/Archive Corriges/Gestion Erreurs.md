# Gestion des Erreurs

#architecture #moyenne #logging #qualite #CORRIGE

> **CORRIGE** le 2026-05-01 — EldaniorLogger.java cree avec info/warn/error/debug, 67 catches migres, debug print → EldaniorLogger.debug().

## Etat actuel (RESOLU)

| Pattern | Fichiers | Probleme |
|---------|----------|----------|
| `catch (Exception ignored) {}` | 30+ | Masque completement l'erreur |
| `System.err.println(msg)` | ~10 | Pas de stacktrace |
| `e.printStackTrace()` | ~5 | Bruit, pas structure |

## Debug output en production
- `TreasureChestInteractEvent.java` ligne 171 : `System.out.println("[DEBUG ELDANIOR]...")`

## Correction proposee
Creer un `EldaniorLogger` centralise :
```java
public final class EldaniorLogger {
    private static boolean debugMode = false;
    public static void info(String msg) { ... }
    public static void warn(String msg) { ... }
    public static void error(String msg, Throwable e) { ... }
    public static void debug(String msg) { if (debugMode) ... }
}
```

## Plan de migration
1. Remplacer `System.out.println("[DEBUG")` → `EldaniorLogger.debug()`
2. Remplacer `catch (Exception ignored)` → `EldaniorLogger.error()` + gestion
3. Remplacer `System.err.println()` → `EldaniorLogger.warn/error()`
4. Ajouter commande `/es debug toggle`

## Liens
- [[../Bugs/Exceptions Silencieuses]] - 30+ exceptions masquees
- [[../Bugs/Null Reference Classes]] - Null masques par les catch