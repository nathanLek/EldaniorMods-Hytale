# Systeme d'Interactions

#interactions #items #bench #npc

## Types d'interactions enregistrees

| Interaction | Declencheur | Effet |
|-------------|-------------|-------|
| ConsumableItemSkillInteraction | Item parchemin (Use) | Apprend un skill passif |
| ConsumableItemMoneyInteraction | Item piece (Use) | Ajoute de l'argent |
| ConsumableItemStatsInteraction | Item consommable (Use) | Modifie stats/rang/reroll |
| OpenClassSelectionInteraction | Bench de classe (Use) | Ouvre selection/evolution de classe |
| QuestNpcInteraction | NPC de quete (Use) | Ouvre dialogue de quete |

## Architecture
```
Item JSON → RootInteraction JSON → Interaction Java → Effet
```

### Exemple : Pillule de Force
1. `Consomable_Stat_Force_One.json` → `"Use": "Root_ConsumableItemStatsInteraction"`
2. `Root_ConsumableItemStatsInteraction.json` → `["ConsumableItemStatsInteraction"]`
3. `ConsumableItemStatsInteraction.java` → lit itemId → cherche dans `StatsItemRegistry`
4. `StatsItemRegistry` → `"Consomable_Stat_Force_One" → STR +1`

## Enregistrement
Dans `InteractionManager.registerInteractions()` :
```java
plugin.getCodecRegistry(Interaction.CODEC)
    .register("ConsumableItemStatsInteraction",
        ConsumableItemStatsInteraction.class,
        ConsumableItemStatsInteraction.CODEC);
```

## Fichiers JSON necessaires
Pour chaque interaction :
1. `Server/Item/Interactions/<NomInteraction>.json` → `{"Type": "<NomInteraction>"}`
2. `Server/Item/RootInteractions/Root_<NomInteraction>.json` → `{"Interactions": ["<NomInteraction>"], "RequireNewClick": true}`

## Fichiers cles
- `skills/InteractionManager.java` - Enregistrement de toutes les interactions
- `skills/interaction/` - Implementations
- `Server/Item/Interactions/` - JSON des interactions
- `Server/Item/RootInteractions/` - JSON des root interactions

## Liens
- [[Systems/Consommables]] - Items qui utilisent les interactions
- [[Systems/Classes]] - Bench de selection de classe
- [[Systems/Quetes]] - NPC de quete
