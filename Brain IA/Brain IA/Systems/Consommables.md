# Systeme de Consommables

#items #consommables #stats #interaction

## Architecture
Interaction `ConsumableItemStatsInteraction` → Registre `StatsItemRegistry` → Effet sur `PlayerLevelData`

## Types de stats
| StatType | Description |
|----------|-------------|
| STRENGTH | Force |
| VITALITY | Vitalite |
| INTELLIGENCE | Intelligence |
| ENDURANCE | Endurance |
| AGILITY | Agilite |
| LUCK | Chance |
| LEVEL | Niveau (+points d'attributs) |
| XP | Experience |
| MONEY | Or |
| REROLL | Relances d'evolution |
| DIGNITY | Dignite |
| NOBILITY_RANK | Rang de noblesse (string) |
| CHURCH_RANK | Rang d'eglise (string) |

## Liste des items

### Pillules de stats (+1 / +5 / +10)
| Item | Effet | Rarete |
|------|-------|--------|
| Consomable_Stat_Force_One/Five/Ten | STR +1/+5/+10 | Epic/Legendary |
| Consomable_Stat_Vitalite_One/Five/Ten | VIT +1/+5/+10 | Epic/Legendary |
| Consomable_Stat_Intelligence_One/Five/Ten | INT +1/+5/+10 | Epic/Legendary |
| Consomable_Stat_Endurance_One/Five/Ten | END +1/+5/+10 | Epic/Legendary |
| Consomable_Stat_Agilite_One/Five/Ten | AGL +1/+5/+10 | Epic/Legendary |
| Consomable_Stat_Chance_One/Five/Ten | LCK +1/+5/+10 | Epic/Legendary |

### Elixirs combines
| Item | Effet |
|------|-------|
| Elixir_Guerrier | STR +3, END +3 |
| Elixir_Assassin | AGL +3, LCK +3 |
| Elixir_Mage | INT +3, VIT +3 |
| Elixir_Archer | AGL +3, LCK +3 |
| Elixir_Complet | Toutes stats +1 |

### Speciaux
| Item | Effet | Rarete |
|------|-------|--------|
| Parchemin_Relance | +1 relance evolution | Legendary |
| Tome_Experience | +5000 XP | Rare |
| Tome_Niveau | +1 niveau | Legendary |
| Essence_Dignite | +1 dignite | Divine (0.02% coffre leg.) |

### Decrets de noblesse
| Item | Effet |
|------|-------|
| Decret_Chevalier | → CHEVALIER |
| Decret_Baron | → BARON |
| Decret_Comte | → COMTE |
| Decret_Duc | → DUC |
| Decret_Marquis | → MARQUIS |

### Benedictions d'eglise
| Item | Effet |
|------|-------|
| Benediction_Pretre | → PRETRE |
| Benediction_Archeveque | → ARCHEVEQUE |
| Benediction_Cardinal | → CARDINAL |

## Loot dans les coffres
| Coffre | +1 | +5 | +10 | Elixirs | XP | Niveau | Dignite |
|--------|----|----|-----|---------|----|----|---------|
| Default | 1% | - | - | - | 0.5% | - | - |
| Donjon Common | 3% | 0.5% | - | - | 2% | - | - |
| Donjon | 2% | 0.3% | 0.05% | 0.2% | 1% | - | - |
| Legendaire | 5% | 1% | 0.2% | 0.5% | 3% | 0.1% | 0.02% |

## Fichiers cles
- `skills/interaction/ConsumableItemStatsInteraction.java` - Interaction principale
- `skills/interaction/StatsItemRegistry.java` - Registre item → effet
- `skills/interaction/StatsItemEffect.java` - Modele d'effet (multi-stats + ranks)
- `skills/interaction/StatType.java` - Enum des types de stats
- `Server/Item/Items/Food/` - JSON des items

## Liens
- [[Systems/Skills]] - Parchemins de competences
- [[Systems/Noblesse]] - Decrets royaux
- [[Systems/Economie]] - Pieces de monnaie
