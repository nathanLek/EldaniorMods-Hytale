# Configuration des Mobs

#mobs #combat #xp #scaling

## Scaling des mobs
| Parametre | Valeur |
|-----------|--------|
| HP par niveau | +10 HP |
| Degats par niveau | +0.5 DMG |
| HP de base | Variable par mob |

## Familles de mobs (22 types)

### Animaux (passifs)
| Mob | Keyword | XP | Niveau | Invincible |
|-----|---------|-----|--------|------------|
| Animaux generaux | animal | 5 | 1-20 | Non |
| Aquatiques | aquatic | 3 | 1-15 | Non |
| Dinosaures | dinosaure | 100 | 50-200 | Non |
| Dragons | dragon | 500 | 100-500 | Non |

### Hostiles
| Mob | Keyword | XP | Niveau | Invincible |
|-----|---------|-----|--------|------------|
| Goblins | goblin | 15 | 5-50 | Non |
| Trorks | trork | 25 | 10-80 | Non |
| Zombies | zombie | 20 | 10-60 | Non |
| Squelettes | skeleton | 20 | 10-60 | Non |
| Void | void | 80 | 30-150 | Non |
| Elementals | elemental | 60 | 20-120 | Non |
| Fens | fen | 30 | 15-80 | Non |
| Ferans | feran | 35 | 15-80 | Non |
| Scaraks | scaraks | 40 | 20-100 | Non |
| Saurians | saurian | 45 | 25-100 | Non |
| Slothians | slothian | 50 | 30-120 | Non |
| Outlanders | outlanders | 70 | 40-150 | Non |
| Golems | golem | 100 | 50-200 | Non |

### NPC (neutres)
| Mob | Keyword | XP | Invincible |
|-----|---------|-----|------------|
| Kweebecs | kweebec | 10 | Oui |
| NPCs | npc | 0 | Oui |

### Boss (4 tiers)
| Tier | Niveau | XP |
|------|--------|-----|
| Tier 1 | 300 | 5000 |
| Tier 2 | 500 | 10000 |
| Tier 3 | 750 | 25000 |
| Tier 4 | 999 | 50000 |

## Systemes ECS associes
- `MobVirtualHPSystem` - HP virtuels (au-dela du max natif Hytale)
- `MobDamageReductionSystem` - Reduction de degats basee sur le niveau
- `MobDeathCheckSystem` - Detection mort + distribution XP
- `MobNameplateColorSystem` - Couleur du nom selon le niveau relatif
- `MobNameplateUpdateOnDamageSystem` - Update HP dans le nameplate

## Fichiers cles
- `config/configs/MobsWorldConfig.java` - Formules globales
- `config/configs/Mobs/*.java` - 22 fichiers de data par famille
- `config/configs/Mobs/MobLevelData.java` - Composant ECS niveau mob

## Liens
- [[Config/LootTables]] - XP et loot des mobs
- [[Systems/Quetes]] - Quetes de chasse
