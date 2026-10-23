# Modifier le terrain sans risque

Toute modification passe par `TerrainRestore` : elle est **temporaire et restaurée** (sauvegardée sur disque, survit à un redémarrage).
Il n'existe pas de modification permanente prévue pour les runes ; en ajouter une exige l'accord explicite de l'utilisateur.

## Recette
```java
TerrainRestore.Restore batch = TerrainRestore.open(world, now + RESTORE_AFTER_MS);   // 1 lot par lancement
world.execute(() -> {                                                                 // ecritures de blocs sur le thread du monde
    try {
        if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return;         // villes, parcelles, arenes
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);                  // null = chunk non charge
        if (type == null || TerrainRestore.isKept(world, type.getId(), x, y, z)) return; // blocs a ne jamais toucher
        TerrainRestore.place(world, batch, x, y, z, type.getId(), "Rock_Basalt", 0);  // TerrainRestore.EMPTY pour creuser
    } catch (Exception e) { EldaniorLogger.error("MonSort: terrain", e); }
});
// fin anticipee : TerrainRestore.restoreNow(world, batch)
```
- `place` reprend l'original d'un précédent propriétaire : poser deux fois sur une même case ne perd pas le bloc d'origine.
  Ne jamais écrire un bloc avec `setBlock` direct dans une zone gérée (le bloc ne serait plus restauré : bug des crocs de Gaia).
- Fluides : `placeFluid` ; ne pas creuser sous l'eau (`WorldBlockUtil.getFluidIndex(...) > 0`).
- Recherche du sol : `top` (FujinWrath), `surface` (BifrostBridge : renvoie `null` si la zone n'est pas chargée).

## Limites de performance
| Rareté | Blocs modifiés max par lancement | Débris (`spawnBlock`) simultanés |
|---|---|---|
| Rare | 50 | 20 |
| Epic | 300 | 80 |
| Unique | 1 500 | 150 |
| Legendaire | 5 000 | 300 |
| Divin | ~15 000 (étalé dans le temps) | 800 |

- Étaler les grosses modifications sur plusieurs ticks (budget par tick) ; regrouper les écritures dans un seul `world.execute`.
- Les débris sont des entités : les supprimer (`SpellFx.remove`) en fin de sort et à l'interruption (`cleanup`).
- Restauration typique : 3 minutes (`RESTORE_AFTER_MS = 180_000`).
