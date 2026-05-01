# Validation des Entrees

#architecture #moyenne #securite #validation

## Probleme
Plusieurs commandes et API publiques acceptent des entrees utilisateur **sans validation**, ce qui peut causer des bugs, des injections ou des etats corrompus.

## Cas identifies

### 1. Noms de Guilde non valides
**Fichier** : `guild/GuildManager.java` lignes 18-24
```java
String id = name.toLowerCase().replace(" ", "_");  // Pas de validation !
```
- Pas de limite de longueur (nom de 1000 caracteres ?)
- Pas de filtrage de caracteres speciaux (`<script>`, `§`, codes couleur)
- Pas de verification de doublons avant creation
- Tags non valides (espaces, symboles)

### 2. Commandes sans trim()
**Fichier** : `quest/QuestManager.java` lignes 319-329
```java
for (String entry : data.split("\\|")) {
    String[] parts = entry.split("=");
    if (parts.length == 2) {  // OK, mais pas de trim()
```
- Des espaces dans les donnees sauvegardees → echec silencieux de parsing

### 3. ParcelManager — createParcel() sans validation parent
**Fichier** : `territory/ParcelManager.java` lignes 59-66
- Accepte n'importe quel `parentId` sans verifier que le parent existe
- Un parentId invalide cree une parcelle orpheline
- Pas de validation que le type enfant est coherent avec le parent

### 4. Pas de rate limiting
- Aucune commande n'a de cooldown d'utilisation
- Un joueur peut spam `/es trade invite` 100 fois/seconde
- Pas de protection anti-flood sur les commandes economiques

## Corrections proposees

### Validation des noms
```java
public static Guild createGuild(String name, String tag, UUID founder, String fName) {
    // Validation du nom
    name = name.trim();
    if (name.length() < 3 || name.length() > 24) {
        throw new IllegalArgumentException("Nom de guilde: 3-24 caracteres");
    }
    if (!name.matches("[a-zA-Z0-9_ -]+")) {
        throw new IllegalArgumentException("Caracteres invalides dans le nom");
    }
    
    // Validation du tag
    tag = tag.trim().toUpperCase();
    if (tag.length() < 2 || tag.length() > 5) {
        throw new IllegalArgumentException("Tag: 2-5 caracteres");
    }
    
    // Verification doublon
    if (getByName(name) != null || getByTag(tag) != null) {
        throw new IllegalArgumentException("Nom ou tag deja utilise");
    }
    
    // ...
}
```

### Rate limiting global
```java
public class RateLimiter {
    private static final Map<UUID, Map<String, Long>> lastAction = new ConcurrentHashMap<>();
    private static final long DEFAULT_COOLDOWN = 1000; // 1 seconde
    
    public static boolean canExecute(UUID player, String action) {
        return canExecute(player, action, DEFAULT_COOLDOWN);
    }
    
    public static boolean canExecute(UUID player, String action, long cooldownMs) {
        long now = System.currentTimeMillis();
        Map<String, Long> actions = lastAction.computeIfAbsent(player, k -> new HashMap<>());
        Long last = actions.get(action);
        if (last != null && now - last < cooldownMs) return false;
        actions.put(action, now);
        return true;
    }
}
```

### Validation parent-enfant
```java
private static final Map<ParcelType, Set<ParcelType>> VALID_CHILDREN = Map.of(
    ParcelType.KINGDOM, Set.of(ParcelType.TERRITORY),
    ParcelType.TERRITORY, Set.of(ParcelType.CITY),
    ParcelType.CITY, Set.of(ParcelType.PLOT, ParcelType.HOUSING, ParcelType.FARM),
    ParcelType.HOUSING, Set.of(ParcelType.ROOM)
);

public static ParcelData createParcel(String parentId, ParcelType type, ...) {
    if (parentId != null) {
        ParcelData parent = getParcel(parentId);
        if (parent == null) throw new IllegalArgumentException("Parent inexistant: " + parentId);
        if (!VALID_CHILDREN.getOrDefault(parent.getType(), Set.of()).contains(type)) {
            throw new IllegalArgumentException(type + " ne peut pas etre enfant de " + parent.getType());
        }
    }
    // ...
}
```

## Priorite
**MOYENNE** — Securite et robustesse

## Liens
- [[../Brain IA/Commandes/Liste des commandes]] - Toutes les commandes
- [[../Brain IA/Systems/Guildes]] - Creation de guildes
- [[../Brain IA/Systems/Territoires]] - Hierarchie des parcelles
