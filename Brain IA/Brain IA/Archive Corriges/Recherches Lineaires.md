# Recherches Lineaires O(n)

#optimisation #haute #performance #indexation #CORRIGE

> **CORRIGE** le 2026-05-01 — GuildManager : tagIndex + nameIndex O(1) ajoutes, disbandGuild nettoie les index, ConcurrentModificationException fixe avec new ArrayList<>().

## Probleme (RESOLU)
Plusieurs managers parcourent **toute** leur collection pour trouver un element par nom ou tag. Avec 100+ guildes ou titres, ces recherches deviennent couteuses, surtout dans les chemins chauds (appeles souvent).

## Cas identifies

### 1. GuildManager — getByTag() et getByName()
**Fichier** : `guild/GuildManager.java` lignes 32-44
```java
public static Guild getByTag(String tag) {
    for (Guild guild : guilds.values()) {  // O(n) a chaque appel
        if (guild.getTag().equalsIgnoreCase(tag)) return guild;
    }
    return null;
}
```
**Frequence** : Appele a chaque affichage de nameplate, commande guild, GUI

### 2. TitleManager — recherche par nom
**Pattern** similaire avec iteration sur toute la collection

### 3. ClassManager — recherche par nom
**Pattern** similaire pour les lookups de classes

## Correction proposee
```java
public class GuildManager {
    private static final Map<String, Guild> guilds = new ConcurrentHashMap<>();
    
    // NOUVEAUX index inverses
    private static final Map<String, Guild> tagIndex = new ConcurrentHashMap<>();
    private static final Map<String, Guild> nameIndex = new ConcurrentHashMap<>();
    
    public static Guild createGuild(String name, String tag, UUID founder, String fName) {
        Guild guild = new Guild(...);
        guilds.put(guild.getId(), guild);
        tagIndex.put(tag.toLowerCase(), guild);       // O(1) insertion
        nameIndex.put(name.toLowerCase(), guild);     // O(1) insertion
        return guild;
    }
    
    public static Guild getByTag(String tag) {
        return tagIndex.get(tag.toLowerCase());  // O(1) au lieu de O(n)
    }
    
    public static Guild getByName(String name) {
        return nameIndex.get(name.toLowerCase());  // O(1) au lieu de O(n)
    }
    
    // Mettre a jour les index lors de suppression
    public static void removeGuild(String id) {
        Guild guild = guilds.remove(id);
        if (guild != null) {
            tagIndex.remove(guild.getTag().toLowerCase());
            nameIndex.remove(guild.getName().toLowerCase());
        }
    }
}
```

## Impact
| Scenario | Avant | Apres |
|----------|-------|-------|
| 50 guildes, lookup par tag | O(50) | O(1) |
| 200 guildes, lookup par nom | O(200) | O(1) |
| 100 joueurs avec nameplate | O(50) x 100 = 5000 ops | O(1) x 100 = 100 ops |

## Priorite
**HAUTE** — Scale mal avec beaucoup de joueurs/guildes

## Liens
- [[Systems/Guildes]] - Systeme de guildes
- [[Systems/Titres]] - Systeme de titres