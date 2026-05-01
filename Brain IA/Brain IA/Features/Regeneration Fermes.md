# Regeneration des Fermes

#feature #haute #territoire #farm

## Etat actuel
- Le type `FARM` existe dans `ParcelType.java` (ligne 10)
- Les blocs sont cassables par tous (permissions INTERACT + BREAK pour tous)
- **Aucune regeneration implementee** — une fois les blocs casses, la ferme est vide pour toujours

## Comportement attendu
1. Un joueur casse un bloc de recolte dans une parcelle FARM
2. Le bloc disparait normalement
3. Apres X minutes, le bloc **reapparait** a sa position d'origine
4. La ferme est donc une ressource renouvelable

## Implementation proposee

### 1. Stockage de l'etat initial
```java
// Au moment de la creation de la parcelle FARM
// Sauvegarder tous les blocs dans la zone
public class FarmBlockRegistry {
    // parcelId → liste de blocs originaux
    private static final Map<String, List<FarmBlock>> farmBlocks = new HashMap<>();
    
    public static void registerFarm(String parcelId, BlockPos min, BlockPos max) {
        List<FarmBlock> blocks = new ArrayList<>();
        for (BlockPos pos : iterateBlocks(min, max)) {
            BlockState state = world.getBlockState(pos);
            if (isHarvestable(state)) {
                blocks.add(new FarmBlock(pos, state));
            }
        }
        farmBlocks.put(parcelId, blocks);
    }
}
```

### 2. Detection de blocs casses
```java
// Dans ParcelBreakBlockEvent, si type == FARM
public void onBreakBlock(BreakBlockEvent event) {
    ParcelData parcel = ParcelManager.getSmallestParcelAt(pos);
    if (parcel != null && parcel.getType() == ParcelType.FARM) {
        FarmBlockRegistry.markBroken(parcel.getId(), pos, System.currentTimeMillis());
    }
}
```

### 3. Systeme de regeneration (tick)
```java
public class FarmRegenerationSystem extends EntityTickingSystem {
    private static final long REGEN_DELAY = 5 * 60 * 1000; // 5 minutes
    
    @Override
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        for (var entry : FarmBlockRegistry.getBrokenBlocks().entrySet()) {
            for (FarmBlock block : entry.getValue()) {
                if (now - block.getBrokenTime() >= REGEN_DELAY) {
                    world.setBlockState(block.getPos(), block.getOriginalState());
                    FarmBlockRegistry.markRestored(entry.getKey(), block.getPos());
                }
            }
        }
    }
}
```

### 4. Configuration
| Parametre | Valeur par defaut | Description |
|-----------|-------------------|-------------|
| `REGEN_DELAY` | 5 minutes | Temps avant regeneration |
| `HARVESTABLE_BLOCKS` | Ble, carotte, etc. | Blocs qui se regenerent |
| `MAX_BLOCKS_PER_FARM` | 500 | Limite pour la performance |

## Fichiers a creer/modifier
- `territory/farm/FarmBlockRegistry.java` — nouveau registre
- `territory/farm/FarmBlock.java` — modele (pos, state, brokenTime)
- `territory/farm/FarmRegenerationSystem.java` — nouveau system ECS
- `territory/events/ParcelBreakBlockEvent.java` — ajouter detection FARM
- `EldaniorSystem.java` — enregistrer le system

## Priorite
**HAUTE** — Les fermes sont inutiles sans regeneration

## Liens
- [[Systems/Territoires]] - Types de parcelles
- [[Architecture/ECS Systems]] - Systemes tick
