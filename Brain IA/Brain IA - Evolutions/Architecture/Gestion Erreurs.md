# Gestion des Erreurs

#architecture #moyenne #logging #qualite

## Probleme
Le codebase utilise **3 patterns differents** pour gerer les erreurs, sans coherence :

| Pattern | Fichiers | Probleme |
|---------|----------|----------|
| `catch (Exception ignored) {}` | 30+ | Masque completement l'erreur |
| `System.err.println(msg)` | ~10 | Pas de stacktrace, cause perdue |
| `e.printStackTrace()` | ~5 | Bruit dans la console, pas structure |

## Debug output en production
**Fichier** : `TreasureChestInteractEvent.java` ligne 171
```java
System.out.println("[DEBUG ELDANIOR] Ajout item: " + stacks.get(idx).getItemId());
```
→ Spam console en production, revele des details d'implementation

## Correction proposee

### Logger centralise
```java
// Nouveau : utils/EldaniorLogger.java
public final class EldaniorLogger {
    private static final String PREFIX = "[Eldanior] ";
    private static boolean debugMode = false;
    
    public static void info(String msg) {
        System.out.println(PREFIX + msg);
    }
    
    public static void warn(String msg) {
        System.err.println(PREFIX + "WARN: " + msg);
    }
    
    public static void error(String msg, Throwable e) {
        System.err.println(PREFIX + "ERROR: " + msg);
        if (e != null) e.printStackTrace();
    }
    
    public static void debug(String msg) {
        if (debugMode) {
            System.out.println(PREFIX + "DEBUG: " + msg);
        }
    }
    
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }
}
```

### Migration progressive
1. **Phase 1** : Remplacer tous les `System.out.println("[DEBUG")` par `EldaniorLogger.debug()`
2. **Phase 2** : Remplacer les `catch (Exception ignored)` par `EldaniorLogger.error()` + gestion
3. **Phase 3** : Remplacer les `System.err.println()` par `EldaniorLogger.warn/error()`
4. **Phase 4** : Ajouter commande `/es debug toggle` pour activer le mode debug en jeu

### Commande admin debug
```
/es debug on    → Active les logs debug
/es debug off   → Desactive
/es debug level  → Affiche le niveau actuel
```

## Priorite
**MOYENNE** — Necessite un effort significatif mais ameliore enormement la maintenabilite

## Liens
- [[../Bugs/Exceptions Silencieuses]] - 30+ exceptions masquees
- [[../Bugs/Null Reference Classes]] - Null masques par les catch
