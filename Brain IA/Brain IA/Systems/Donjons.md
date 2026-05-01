# Systeme de Donjons

#donjon #instance #portal #boss

## Acces
- Via un **portail** dans le monde
- Necessite une **Cle de Donjon** (`PortalKey_Dungeon_V1`)
- La cle se trouve dans les coffres au tresor (5% donjon, 0.02% default)

## Instance
- Le donjon est une **instance separee** (Dungeon_V1)
- Configurations dans `Server/Instances/Dungeon_V1/`
- Inclut : InstanceData, ChunkStorage, TreasureChestConfig

## Coffres de donjon
- 2 types : **Donjon** (haut tier) et **Donjon Common** (bas tier)
- Loot : armures Cobalt→Prisma, armes, skills Common→Divine
- Voir [[Config/LootTables]] pour les probabilites

## Boss (4 tiers)
| Tier | Niveau | XP | Difficulte |
|------|--------|-----|------------|
| Tier 1 | 300 | 5 000 | Normal |
| Tier 2 | 500 | 10 000 | Difficile |
| Tier 3 | 750 | 25 000 | Tres difficile |
| Tier 4 | 999 | 50 000 | Extreme |

## Fichiers cles
- `Server/PortalTypes/Dungeon_V1.json` - Config du portail
- `Server/Instances/Dungeon_V1/` - Donnees de l'instance
- `config/configs/LootTableConfig.java` - Tables DUNGEON_CHEST et DUNGEON_COMMON_CHEST

## Liens
- [[Config/LootTables]] - Loot des coffres de donjon
- [[Mobs/Configuration Mobs]] - Boss tiers
- [[Systems/Coffres au tresor]] - Systeme de coffres