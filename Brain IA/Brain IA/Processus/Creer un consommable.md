# Processus : Creer un Item Consommable

#processus #item #creation #guide

## 3 etapes

### Etape 1 : Creer le JSON de l'item
Fichier : `Server/Item/Items/Food/<MonItem>.json`
```json
{
  "Id": "MonItem",
  "TranslationProperties": {
    "Name": "Mon Item",
    "Description": "[Clic F]: Consommer (+1 Force)"
  },
  "Icon": "Icons/ItemsGenerated/Food_Egg.png",
  "Quality": "Epic",
  "Categories": ["Eldanior_System.Consommables"],
  "Tags": { "Type": ["Item", "Consumable"] },
  "MaxStack": 1,
  "PlayerAnimationsId": "Item",
  "Model": "Items/Consumables/Food/Egg.blockymodel",
  "Texture": "Items/Consumables/Consomable_Stats_Force_One.png",
  "IconProperties": {
    "Scale": 0.76,
    "Rotation": [135, 135, 0],
    "Translation": [-1, 5]
  },
  "Interactions": {
    "Use": "Root_ConsumableItemStatsInteraction"
  }
}
```

### Etape 2 : Enregistrer dans StatsItemRegistry
Fichier : `skills/interaction/StatsItemRegistry.java`

**Effet simple :**
```java
register("MonItem", new StatsItemEffect("Mon Item", StatType.STRENGTH, 1));
```

**Effet multiple :**
```java
register("MonElixir", StatsItemEffect.builder("Mon Elixir")
    .add(StatType.STRENGTH, 3)
    .add(StatType.ENDURANCE, 3)
    .build());
```

**Effet rang (noblesse/eglise) :**
```java
register("MonDecret", StatsItemEffect.rank("Mon Decret", StatType.NOBILITY_RANK, "DUC"));
```

### Etape 3 (optionnel) : Ajouter dans les tables de loot
Fichier : `config/configs/LootTableConfig.java`
```java
new LootEntry("MonItem", 1, 1, 1, 2.0) // 2% de drop
```

## Types de StatType disponibles
STRENGTH, VITALITY, INTELLIGENCE, ENDURANCE, AGILITY, LUCK, LEVEL, XP, MONEY, REROLL, DIGNITY, NOBILITY_RANK, CHURCH_RANK

## Architecture
```
Item JSON → Root_ConsumableItemStatsInteraction → ConsumableItemStatsInteraction.java
→ StatsItemRegistry.getEffect(itemId) → StatsItemEffect → applyEntry() → PlayerLevelData
```

## Fichiers a modifier
1. `Server/Item/Items/Food/MonItem.json` (creer)
2. `skills/interaction/StatsItemRegistry.java` (register)
3. `config/configs/LootTableConfig.java` (optionnel, loot)

## Liens
- [[Systems/Consommables]] - Vue d'ensemble
- [[Architecture/Interactions]] - Systeme d'interactions
- [[Items/Consommables]] - Liste complete