# Null Reference Classes

#bug #haute #crash #classes #CORRIGE

> **CORRIGE** le 2026-05-01 — ClassManager.get() retourne Novice en fallback si l'ID n'existe pas, avec message d'erreur.

## Probleme (RESOLU)
`ClassManager.get(String id)` retourne `null` si l'ID de classe n'existe pas. Ce resultat est utilise **sans verification** dans plusieurs chemins critiques.

## Points d'impact
| Fichier | Ligne | Contexte |
|---------|-------|----------|
| `CombatStatsSystem.java` | 178, 248 | Calcul des stats de combat |
| `StatCalculator.java` | 41 | Calcul des stats du joueur |
| `PlayerLevelData.java` | 324 | Activation des skills passifs |

## Scenario de crash
1. Un joueur a une classe sauvegardee dans ses donnees (ex: `"warrior_blade_400"`)
2. Le code de cette classe est supprime/renomme dans une mise a jour
3. `ClassManager.get("warrior_blade_400")` retourne `null`
4. `NullPointerException` cascade dans le systeme de combat
5. Le joueur ne peut plus combattre

## Correction proposee
```java
// Dans ClassManager.java
public static PlayerClass get(String id) {
    PlayerClass cls = classes.get(id);
    if (cls == null) {
        System.err.println("[Eldanior] Classe introuvable: " + id + " — fallback vers Novice");
        return classes.get("novice"); // Fallback securise
    }
    return cls;
}
```

## Autres null dangereux
- `GuildManager.getGuild(id)` — pas de fallback
- `ParcelManager.getParcel(id)` — utilise sans check dans economy
- `QuestManager` — UUID null apres reflection echouee, continue avec `List.of()`

## Priorite
**HAUTE** — Crash en production si une classe est supprimee

## Liens
- [[Systems/Classes]] - Systeme de classes
- [[Exceptions Silencieuses]] - Les null sont masques par les catch ignored
