# Recursive deleteParcel et Batch Save

#optimisation #moyenne #performance #io #CORRIGE

> **CORRIGE** le 2026-05-01 — deleteParcelRecursive() + un seul save() a la fin.

## Probleme (RESOLU)
`ParcelManager.deleteParcel()` appelle `save()` a **chaque niveau de recursion**. Supprimer un Royaume avec ses enfants provoque des dizaines d'ecritures disque.

**Fichier** : `territory/ParcelManager.java` lignes 69-76

### Scenario
- Royaume → 3 Territoires → 6 Villes → 30 Parcelles
- deleteParcel("kingdom") → **40 appels a save()** (1 par noeud)
- Chaque save() ecrit tout le fichier `parcels.properties`

## Correction
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

## Autres patterns similaires
- `GuildManager.disbandGuild()` → potentiel ConcurrentModificationException en iterant les membres
- Actions batch sur les parcelles (assignation de guilde, toggle PvP) → pourraient beneficier d'un save batch

## Priorite
**MOYENNE** — Performance I/O, pas bloquant pour peu de parcelles

## Liens
- [[Bugs/Shutdown et Disconnect]] - Save au shutdown
- [[Architecture/Persistence et Backup]] - Systeme de sauvegarde