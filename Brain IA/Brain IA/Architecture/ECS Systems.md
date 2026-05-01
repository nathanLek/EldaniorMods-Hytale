# Architecture ECS

#architecture #ecs #systemes #technique

## Deux types de systemes

### EntityTickingSystem (par frame)
Execute a chaque tick du serveur pour les entites filtrees.

| Systeme | Role | Frequence |
|---------|------|-----------|
| PlayerLoginSystem | Init joueur a la connexion | 1er tick |
| CombatTrackerSystem | Suivi des attaques | Chaque tick |
| CombatStatsSystem | Calcul degats + passifs + effets | Sur dommage |
| GlobalRegenSystem | Regeneration HP/Mana | Chaque tick |
| FallDamageSystem | Degats de chute | Chaque tick |
| DeathXPSystem | XP a la mort | Sur mort |
| PlayerPositionTracker | Suivi positions joueurs | Chaque tick |
| MobVirtualHPSystem | HP virtuels des mobs | Chaque tick |
| MobNameplateUpdateOnDamageSystem | Update nameplate mob | Sur dommage |
| MobDamageReductionSystem | Reduction degats mob | Sur dommage |
| MobDeathCheckSystem | Mort mob + XP | Chaque tick |
| MobNameplateColorSystem | Couleur nameplate mob | Chaque tick |
| DetectionSystem | Radar menaces | Chaque tick |
| FlySystem | Vol (skill VOL) | Chaque tick |
| MorphFlightSystem | Vol morphe | Chaque tick |
| PlayerNameplateSystem | Nameplate joueur (rang, titre) | ~20 ticks |
| PartyHudUpdateSystem | HUD groupe | ~20 ticks |
| DuelProtectionSystem | Anti-mort en duel | ~5 ticks |
| QuestHudUpdateSystem | HUD quete active | ~20 ticks |
| DignityAuraSystem | Aura de dignite (ralentit mobs) | ~10 ticks |
| MasterySystem | Bonus maitrise d'armes | Chaque tick |
| CraftingRestrictionSystem | Restrictions craft par classe | Sur craft |
| ParcelRangeSystem | Detection entree/sortie zone | ~20 ticks |

### EntityEventSystem (evenementiel)
Repond a un evenement specifique.

| Systeme | Evenement | Role |
|---------|-----------|------|
| TreasureChestInteractEvent | UseBlockEvent.Pre | Ouverture coffre |
| TreasureChestBreakBlockEvent | BreakBlockEvent | Protection coffre |
| TreasureChestPlaceBlockEvent | PlaceBlockEvent | Protection coffre |
| TreasureChestDamageBlockEvent | DamageBlockEvent | Protection coffre |
| NpcQuestDetectionSystem | - | Detection NPC quete |
| ParcelBreakBlockEvent | BreakBlockEvent | Protection parcelle |
| ParcelPlaceBlockEvent | PlaceBlockEvent | Protection parcelle |
| ParcelInteractEvent | UseBlockEvent.Pre | Protection parcelle |

## Enregistrement
Tous les systemes sont enregistres dans `EldaniorSystem.setup()` via :
```java
this.getEntityStoreRegistry().registerSystem(new MonSysteme());
```

## Composants (EntityStore)
| Composant | Donnees |
|-----------|---------|
| PlayerLevelData | Toutes les stats joueur (niveau, classe, skills, argent, titres...) |
| PlayerPersonalChestData | Coffre personnel |
| PlayerChestData | Etat des coffres au tresor visites |
| MobLevelData | Niveau et stats des mobs |

## Ressources (ChunkStore)
| Ressource | Donnees |
|-----------|---------|
| TreasureChestTemplate | Templates des coffres au tresor |
| TreasureChestConfig | Config des coffres (cooldown, particules) |

## Fichiers cles
- `EldaniorSystem.java` - Point d'entree, enregistrement de tout
- `ESCommand.java` - Commande racine /es avec sous-commandes

## Liens
- [[Architecture/GUI SystemScreen]] - Interface graphique
- [[Architecture/Persistence]] - Sauvegarde des donnees
