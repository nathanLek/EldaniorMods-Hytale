# Architecture des runes (vérifiée dans le code, octobre 2026)

## Projet
- Plugin serveur Hytale : `com.eldanior.system.EldaniorSystem`, Java 25, Gradle. `server_version=~0.7.2` (gradle.properties).
- `./gradlew compileJava` (compilation), `./gradlew jar` (écrit le mod dans `~/Library/Application Support/Hytale/UserData/Mods/`, c'est ce que le jeu charge), `./gradlew runServer` (serveur local avec `Assets.zip` vanilla).
- Assets vanilla : `~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip` (lecture seule, utilisé par `rune_tool.py`).

## Fichiers d'une rune (tous générés par `rune_tool.py new`)
| Rôle | Chemin (`src/main/resources/…`) |
|---|---|
| Item rune (bloc `Ability` : `Cost`, `Cooldown`, `CostType: Mana`, `Cast`, `AbilityKeywords`, `Quality`) | `Server/Item/Items/Rune/Ability/<Id>.json` |
| Parchemin (consommable, `Root_ConsumableItemSkillInteraction`) | `Server/Item/Items/Rune/Parchemin/<page>.json` |

Catégorie créative (`Server/Item/Category/CreativeLibrary/Eldanior_System.json`) : runes **et** leurs parchemins dans `Eldanior_System.Competences_Actives` ; compétences passives dans `Eldanior_System.Parchemin` (affiché « Compétences passives ») ; spellbooks et leurs parchemins dans `Eldanior_System.Spells`.
| Racine d'interaction (`RequireNewClick`) | `Server/Item/RootInteractions/Abilities/Root_Ability_<Court>.json` |
| Incantation (StatsCondition → anim → `RuneSpell` → anim ; TriggerCooldown ; ChangeStat) | `Server/Item/Interactions/Abilities/<Court>/Ability_<Court>_Cast.json` |
| Icône compétence 128 px (HUD, établi) | `Common/Icons/Abilities/<Id>.png` |
| Icône inventaire 64 px | `Common/Icons/ItemsGenerated/<Id>.png` |
| Texture du modèle 3D 64 px (modèle `Items/Runes/Rune_Ability.blockymodel`) | `Common/Items/Runes/Rune_Textures/Rune_Ability_<Court>.png` |
| Code du sort | `src/main/java/com/eldanior/system/skills/runes/spells/<Classe>.java` |

Branchements Java (insérés par l'outil) :
- `RuneSpells.registerAll()` : `RuneSpellRuntime.register(new <Classe>());`
- `RuneManager.RUNE_CLASSES` : `"<Id>" -> "mage, dragon"` (classes autorisées ; absente = toutes). Format de `SkillModel.requiredClass`.
- `RuneManager.PAGE_TO_RUNE` : `"<page>" -> "<Id>"` (le parchemin donne la rune dans le sac de runes via `ConsumableItemSkillInteraction`).
- `LootTableConfig` : `new LootEntry("<page>", 1, 1, 1, <chance %>)` dans les tables de la rareté (`rune_rules.json > loot`).
  Tables : `DUNGEON_CHEST`, `DUNGEON_COMMON_CHEST`, `LEGENDARY_CHEST`, `GOLD_CHEST`, `DEFAULT`, `DUNGEON_BOSS_CHEST`.

Le moteur Hytale lit `Cost`/`Cooldown` dans l'item. La source de vérité est `scripts/runes/rune_rules.json` : l'outil y lit
les valeurs et `check` signale tout écart non documenté.

## Moteur commun `RuneSpell` (nouvelles runes)
- `RuneSpell` : `id()` (= champ `"Spell"` du JSON) et `start(caster, store, cb)` → `null` (instantané) ou une `Instance`.
- `RuneSpell.Instance` : `tick(caster, now, store, cb)` (renvoie `true` quand c'est fini), `cleanup(cb)`, `abort()` (déconnexion).
- **Coups du lanceur** : `onOwnerHit(target, montant brut)` est appelé pour chaque coup qu'il inflige (`DivineDamageSystem` → `RuneSpellRuntime.onDamageDealt`) ; mémoriser et rejouer dans `tick`. Avant d'infliger un coup copié, appeler `RuneSpellRuntime.markEcho(cible, montant)` pour qu'il ne soit pas recopié (boucle infinie). Exemple : `Doppelganger.java`.
- **Copies du lanceur** : `CloneFx.spawn` crée un vrai PNJ (rôles `Server/NPC/Roles/Eldanior/Summons/Eldanior_Clone_Lumiere` et `Eldanior_Doppel`, apparence `Player`) via `NPCPlugin.spawnNPC` dans `world.execute` (la ref n'existe qu'au tick suivant), puis l'habille : modèle + skin du lanceur, arme en main (`setWeapon`), vie max (`StaticModifier`), effet de teinte. Il est inscrit comme invocation (`RuneSpellRuntime.registerSummon`) : pas de tir ami avec le lanceur, coup fatal = dissipation (`isDying`), option intouchable. Pilotage : `mark(npc, "LockedTarget"|"Owner", cible)`, `hostile(npc, autre, s)` (attitude forcée), `pose` (position/rotation/états de mouvement imposés). Rejeu d'attaques : `InteractionContext.forInteraction` + `InteractionManager.tryStartChain` sur le PNJ (`Doppelganger.java`). Les minuteurs d'un rôle PNJ doivent être > 0 (sinon le rôle est rejeté au chargement). **Déplacer une copie sans glissade** : ne pas la téléporter (le moteur recalcule ses animations depuis sa propre vitesse) ; déplacer son point de laisse (`CloneFx.leash`) et laisser son rôle y aller (`Sensor Leash` + `Seek`, `UsePathfinder` pour contourner) : vraies animations de marche/course. Pour imposer seulement son regard, l'appliquer dans un système `Order.AFTER` `MovementStatesSystem` et `RoleSystems$BehaviourTickSystem` (`DoppelPoseSystem` → `CloneFx.face`). **Attitudes forcées** : toujours `util/NpcAttitudes.hostile` (le moteur ne crée la mémoire d'override que si le rôle utilise `OverrideAttitude`, sinon `overrideAttitude` plante). **Combat d'un clone** : rôles générés par `scripts/runes/gen_clone_roles.py` (épée → CAE du chevalier vanilla, sinon `Root_NPC_Attack_Melee`), qui n'attaquent que l'hostile : le sort force l'attitude (`hostile`, 3 s rafraîchies) et l'état (`CloneFx.setState`). Les rôles `Eldanior_Clone_*` / `Eldanior_Doppel` sont ignorés par les systèmes de monstres (`RuneSpellRuntime.isSummonRole`) : pas de niveau, PV ni plaque de monstre ; la plaque du lanceur est recopiée.
- **Clics pendant un sort** : `listensClicks()` → `true`, puis `onClick(left, targetEntity, caster, store, cb)` reçoit chaque clic gauche/droit du lanceur (livré sur le thread du monde, avec l'entité sous le curseur). Deux voies, avec anti-doublon (250 ms) : les nouvelles chaînes d'interaction `Primary`/`Secondary` du joueur (méthode du Bifrost, fiable même rune en main, cible = `SpellFx.targetLiving`) et l'événement `PlayerMouseButtonEvent` (`RuneSpellRuntime.onMouseButton`, qui ne s'est pas déclenché pour la Télékinésie en jeu). Exemple : `Telekinesie.java`.
- **Invocations de créatures** : `SummonPack` (spawn via `CloneFx.spawn` d'un rôle `Eldanior_Summon_*` = variante de `Template_Summoned_Ally` vanilla, cible répartie, laisse vers le lanceur, dissipation/cleanup/abort). Exemples : `GolemDePierre` (provocation : `hostile` + `LockedTarget` des monstres vers le golem), `MeuteSpectrale`.
- `RuneSpellInteraction` : type JSON `"RuneSpell"` enregistré dans `InteractionManager` ; il met le lancement en file.
- `RuneSpellRuntime` : exécute la file au tick suivant du lanceur (dans `DivineSpellsSystem`, qui fournit un `CommandBuffer`), fait avancer les instances, **attrape les exceptions** (le sort s'arrête et se nettoie, le monde ne plante pas), vide tout à la déconnexion (`EldaniorSystem`).
- Exemples : `Bourrasque.java` (instantané, Common) ; `Telekinesie.java` (clics, saisie d'entités et de blocs, lancer avec physique simple, Epic) ; `FujinWrath.java` (persistant, Divin : instance qui suit le lanceur, budget fixe d'entités, débris, lames téléguidées, terrain restauré, `abort()` avec `RuneSpellRuntime.removeLater`). Gabarit à état : `rune_tool.py new` avec `"persistent": true`.

État d'un sort persistant : garder les champs dans l'`Instance` (positions, refs d'entités, `TerrainRestore.Restore`, horodatages `lastX`).
Pas de `static Map` par joueur : le moteur gère déjà joueur → instances.

## Sorts divins historiques (avant le moteur commun)
Chaque sort divin (sauf Fujin, migré sur `RuneSpell`) a sa classe (`NyxVeil`, `BifrostBridge`…) + une `XxxInteraction` (type JSON dédié), un `tick` appelé
explicitement dans `DivineSpellsSystem`, et `handleDisconnect` dans `EldaniorSystem`. Ne pas les migrer sans demande ; s'en inspirer :
- zone persistante + contrôle souris : `NyxVeil` (`PlayerMouseButtonEvent` enregistré dans `EldaniorSystem`, file de clics par joueur) ;
- terrain massif restauré : `GaiaAwakening`, `ChaosAbyss` (aspiration de blocs) ;
- invocation de modèles géants : `ChaosAbyss` (`setModelScale`), `GilgameshBabylon`, `ApolloSanctuary` ;
- téléportation longue distance et zones non chargées : `BifrostBridge` (`surface()`, attente de chargement, descente) ;
- statuts et gel des monstres : `NiflheimGate` (`lockInPlace`), `ChronosHourglass`.

## API `SpellFx` (package `spells`, méthodes statiques vérifiées)
| Méthode | Usage |
|---|---|
| `fx(id, pos, scale, store, cb)` / `fx(id, pos, yawDeg, pitchDeg, scale, store, cb)` | particules envoyées aux joueurs à moins de 200 blocs. yaw = `deg(atan2(dx, dz))`, pitch négatif = vers le bas |
| `sound(id, pos, cb)`, `sound2dNear(id, pos, radius, store)` | son 3D ; son 2D (stéréo, ex. tonnerre) |
| `arc(fxId, a, b, scale, store, cb)`, `stun(target, effectId, ms, store, cb)` | arc électrique en particules entre deux points ; étourdissement (effet + verrou des monstres) |
| `shake(pos, radius, cameraEffectId, store)` | secousse caméra (`Impact_Light`, `Impact_Strong`, `Ability_Cast_Generic`) |
| `enemiesAround(caster, center, r, store)`, `alliesAround(...)`, `isEnemy`, `isAlly` | groupe, guilde et famille épargnés |
| `damage(caster, target, causeId, amount, knockback, cameraEffect, cb)` | causes : `Wind`, `Fire`, `Ice`, `Earth`, `Lightning`, `Physical`, `Poison`, `Water`, `Elemental`, `Slashing`, `Bludgeoning`… (`assets damage .`) |
| `push(target, velocity, add, cb)` | projection sans dégâts (KnockbackComponent) |
| `effect(target, effectId, cb)` | effet de statut (`Server/Entity/Effects/...`) |
| `position`, `lookDirection`, `forward`, `targetPoint(caster, store, range, fallback)`, `ground(world, p)`, `horizontalDistance` | géométrie (refs invalides → `null` géré) |
| `targetEntity(caster, store, range)` / `targetLiving(...)` | ennemi / créature ou joueur quelconque (jamais le lanceur) sous le curseur, sans traverser un bloc, ou `null` |
| `spawnModel(caster, projectileConfigId, pos, dir, cb)`, `hold(ref, pos, rot, store)`, `remove(ref, cb)`, `setModelScale(ref, modelId, scale, hitboxHalf, cb)`, `facing(dir, withPitch)` | entités modèles (porteurs de visuels) |
| `spawnBlock(blockId, pos, store, cb)` | vrai bloc rendu en entité (débris, blocs volants) |
| `teleport(ref, pos[, facing], store, cb)`, `protectFromFall(ref, ms, store)` | téléportation, immunité de chute |
| `lockInPlace(target, ms, store)`, `unlock(target)` | figer un monstre (les effets `MovementEffects` ne figent que les joueurs) |
| `restore(ref, statIndex, percentOfMax, store)` | soin / mana en % |
| `chains(ref, store, InteractionType)` | détecter un nouveau clic sans événement souris |
| `world(store)`, `uuid(ref, store)`, `DIVINE_ZONE_RADIUS` (35) | divers |

## API `TerrainRestore` (voir `world-edit.md`)
`open(world, restoreAt)`, `place(world, batch, x, y, z, original, placed, rotation)`, `placeFluid(...)`, `restoreNow(world, batch)`,
`isEmpty`, `isKept(world, id, x, y, z)` (blocs à ne jamais toucher), `isProtectedArea(worldName, x, y, z)` (villes, parcelles, arènes),
`restoreArea(...)`, `EMPTY`. Les écritures de blocs se font dans `world.execute(() -> ...)`.

## Vérifier une API moteur inconnue
```bash
J="$HOME/Library/Application Support/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"
unzip -l "$J" | grep -i <Nom>            # trouver la classe
unzip -o -q "$J" '<chemin>.class' -d /tmp/hyt && javap -p -c /tmp/hyt/<chemin>.class | less
```
Le MCP `hytale-docs` (search_docs) et les agents `sabrina` (doc/assets), `manuel` (particules) et `nina` (effets) peuvent aider.
