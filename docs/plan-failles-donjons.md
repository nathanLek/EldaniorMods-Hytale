# Failles-donjons : analyse de la faille gobeline et plan

Rédigé le 2026-11-22. Analyse seulement : rien n'est codé. Les fichiers vanilla ont été lus dans
`~/Desktop/Assets Hytale/Server/`, l'API du moteur vérifiée avec `javap` sur `HytaleServer.jar`. Rien n'a été testé en jeu.

---

## 1. Fonctionnement de la faille gobeline

La faille réunit quatre mécanismes du moteur, tous décrits en JSON. Aucun n'est propre aux gobelins.

### 1.1 L'événement du monde fait apparaître la faille dehors (`WorldEvent/`)

Un événement est une suite d'**étapes**. Chaque étape a des **embranchements** (`Forks`), et chaque embranchement a des
conditions, des actions et une étape suivante. Le premier embranchement dont toutes les conditions sont remplies s'exécute.

`Goblin_Breach_Perpetual_Event` (la faille qui revient sans fin) :

| Étape | Attente | Ce qui se passe |
|---|---|---|
| Stage1 | `IntervalCondition` de 1 800 s (minuteur « prochaine dans » sur la carte) | rien |
| Stage1_Place | `LocationCondition` `WildernessLocation`, entre 64 et 200 blocs d'un joueur, 8 blocs dégagés, sol de terre, roche ou plantes | colle le préfab **étincelle** (`BreachSpark`) |
| Stage2 | `ProximityCondition` : un joueur à moins de 20 blocs (abandon au bout de 300 s) | retire l'étincelle, titre « Faille gobeline », particules `GoblinBreach_Portal`, son d'ouverture |
| Stage3 | 5,5 s | explosion en anneau, **volume de déclenchement** qui projette les joueurs, colle le préfab **portail**, volume `NoDestroy` + `NoBuild` autour |
| Stage4 | `EnterInstanceTeleporterCondition` (quelqu'un est entré), puis `WorldClosedCondition` ; abandon au bout de 600 s sans entrée | fin. Les préfabs collés avec `Permanent: false` sont **retirés tout seuls** quand l'événement se termine |

Autres pièces utiles :
- `Goblin_Breach_Prelude_*` : la première faille du joueur (rayon lumineux, `EncounterManager` qui fait apparaître un groupe,
  puis `SignalCondition goblin_initial_breach_cleared` quand le groupe est mort). Les drapeaux de partie (`GameFlagLevelCondition`)
  arrêtent l'événement.
- Le gameplay `Default` lance `Goblin_Breach_Prelude_Bootstrap` dans tout monde qui l'utilise. **À vérifier :** est-ce que des
  failles gobelines s'ouvrent déjà sur la carte d'Eldanior ?

### 1.2 Le bloc portail fait entrer dans une instance

`Goblin_Breach_Portal` (dans le préfab `Goblin_World_Event_Portal`) est un bloc dont l'interaction `CollisionEnter` est :

```json
{ "Type": "TeleportInstance", "InstanceName": "Portals/Portals_Goblins",
  "PersonalReturnPoint": true, "CloseOnBlockRemove": false, "PositionOffset": { "Z": 6 }, "OriginSource": "Block" }
```

- `TeleportInstanceInteraction` crée l'instance si besoin, puis **enregistre l'UUID du monde dans le composant `InstanceBlock`
  du bloc**. D'après le bytecode, le joueur suivant qui touche le même bloc entre dans **la même instance**. C'est ainsi que
  le groupe se retrouve ensemble.
- `PersonalReturnPoint` : chaque joueur ressort là où il est entré, décalé de 6 blocs pour ne pas retoucher le portail.
- `CloseOnBlockRemove` : avec `true`, casser le bloc ferme l'instance. `RemoveBlockAfter` retire le bloc après N secondes.
- Le moteur émet `EnterInstanceTeleporterEvent` (événement ECS qu'on peut écouter en Java).

### 1.3 L'instance est un monde éphémère (`Instances/` + `PortalTypes/`)

- `Instances/Portals/Portals_Goblins/instance.bson` est en fait du JSON. Il donne le monde modèle : `GameMode Adventure`,
  `WorldGen HytaleGenerator / Portals_Goblins`, point d'apparition, `GameplayConfig PortalsGoblins`, titre affiché à la
  fermeture, et `PortalWorldConfig { PortalType: Goblin_Breach, TimeLimitSeconds: 600 }`.
- `PortalTypes/Goblin_Breach.json` : nom, couleur, `TimeLimitSeconds 600`, `VoidInvasionEnabled`, `AllowedEntryCount`,
  `CloseWhenEmpty`, et `EphemeralItems: ["Coffer_Key"]` : ces objets sont **retirés de l'inventaire à la sortie**.
- La ressource `PortalWorld` (par monde) tient le minuteur, le nombre d'entrées par joueur et le HUD du temps. La condition de
  suppression `PortalRemovalCondition` vaut « monde vide » ou « temps écoulé ». À la fin, titre `VoidEviction`, puis
  tout le monde est renvoyé.

**Le terrain** : `WorldStructures/Portals_Goblins.json` produit une **île procédurale** de 320 blocs de rayon (biome
`Portal_Goblin_Biome`, puis un bord, puis du Vide au-delà de 430 blocs). Les `Assignments/Portal_Goblin/*` y placent les
rencontres : campements, coffres, cryptes, araignées, ressources du Vide. Chaque faille est donc une **carte différente**.

### 1.4 Le gameplay de l'instance (`GameplayConfigs/PortalsGoblins.json`, parent `Portal`)

- Mort : la moitié des objets perdus, puis réapparition dans l'instance. Le parent `Portal` utilise plutôt `RespawnController: ExitInstance`.
- `PortalUpgrades` : +120, +180 ou +240 s de temps selon un drapeau de progression.
- `VoidEvent` : de 120 à 780 s, la météo se gâte par paliers et des titres s'affichent. Dans `Portal`, des **portails
  d'invasion du Vide** (`InvasionPortals` + balises d'apparition) s'ouvrent à 420 s : la pression monte avec le temps.
- `WorldEvents` interne : `Hunt_Breach` place une cible de chasse à 160-256 blocs de l'arrivée. Sa mort envoie le signal
  `hunt_complete`, qui sert d'objectif.
- Butin : `Coffer_Goblin` (préfab + `Drops/Prefabs/Coffer_Goblin_Loot_Wave5.json`). Il s'ouvre avec `Coffer_Key`, qui est éphémère.

### 1.5 Ce que le mod a déjà

| Existant | Où | Intérêt |
|---|---|---|
| Instance faite main `Dungeon_V1` (monde `Void` + chunks construits, `DeleteOnRemove`, suppression quand vide) | `resources/Server/Instances/Dungeon_V1`, `PortalTypes/Dungeon_V1.json`, `PortalKey_Dungeon_V1` | modèle direct d'un donjon fixe |
| Faille d'invasion (bloc `Eldanior_Faille_Invasion`, effet de la faille gobeline, vagues de monstres) | `territory/laws/Invasions.java`, `scripts/territoire/gen_evenements.py` | effet visuel et logique de vagues réutilisables (« débordement ») |
| Boss de donjon, porte à clé et gel de 15 s | `boards/DungeonBosses.java`, `locks/` | **lié à une zone `DUNGEON` du `ParcelManager`**, qui n'existera pas dans un monde d'instance (voir § 4) |
| Montée en difficulté selon la taille du groupe | `dungeon/DungeonScaling.java`, `party/` | pour les donjons de groupe |
| Coffres de donjon par palier | `LootTableConfig` `DUNGEON_TIER1-4_CHEST`, `DUNGEON_BOSS_CHEST` | récompenses |
| Boss Mondiaux, marqueurs carte et boussole, panneau admin | `boards/WorldBosses`, `KingdomMap` | apparition dehors, annonces |

---

## 2. L'idée : les Failles d'Eldanior

> Une déchirure s'ouvre dans le monde. Derrière : un donjon d'un autre plan, limité dans le temps, qui se referme une fois
> son maître abattu. Si personne ne la ferme, elle déborde.

### 2.1 Le cycle de vie d'une faille

```
Apparition (dehors) → Ouverte (marquée, visible) → Donjon en cours (instance) → Fermée (boss vaincu)
                                 │                           │
                                 └── jamais nettoyée ────────┴── temps écoulé → Débordement (invasion dehors)
```

1. **Apparition** : hors des villes, à 64-200 blocs d'un joueur, comme la faille gobeline. Au début depuis le panneau
   admin, ensuite automatique (une toutes les X minutes, plafond par région). Étincelle puis ouverture à l'approche.
2. **Ouverte** : bloc portail animé (effet `GoblinBreach_Portal_Active` recoloré selon le rang), marqueur sur la carte et
   la boussole avec le rang, durée de vie (ex. 30 min).
3. **Donjon** : le premier qui entre crée l'instance, son groupe le rejoint par le même bloc. Minuteur du moteur (HUD
   natif), salles, monstres au niveau du rang, mini-boss, puis salle du boss.
4. **Fermée** : boss vaincu, alors coffre de fin, portail de sortie, titre « Faille refermée », annonce. Le bloc dehors
   disparaît et l'instance est supprimée.
5. **Débordement** (optionnel) : faille ouverte trop longtemps ou groupe mort, alors la faille dehors crache des vagues
   de monstres du même thème. On réutilise `Invasions`.

### 2.2 Rangs de faille

Rangs E → S validés le 2026-11-22. Les bandes de niveau et les couleurs sont celles **déjà validées** pour les quêtes
(`quest/QuestDifficulty`, skill quest-creation § 3), à modifier ensemble :

| Rang | Couleur (`QuestDifficulty`) | Niveau des monstres | Temps | Boss | Coffre |
|---|---|---|---|---|---|
| E | `#4CAF50` vert | 60-150 | 15 min | boss à phases du jeu (Duc gobelin) | `donjon_tier1` |
| D | `#2196F3` bleu | 150-300 | 15 min | boss de donjon | `donjon_tier1` |
| C | `#9C27B0` violet | 300-450 | 18 min | boss de donjon + phase | `donjon_tier2` |
| B | `#ff9800` orange | 450-600 | 18 min | boss de donjon + phase | `donjon_tier2` |
| A | `#cc4444` rouge | 600-800 | 20 min | boss couronné | `donjon_tier3` |
| S | `#FFD700` or | 800+ | 25 min | boss couronné, 2 phases | `donjon_tier4` |
| SS/EX | `#ff7a3d` / `#c46bff` | événement | — | Boss Mondial dans sa faille | `donjon_boss` |

**Apparence selon le rang** (demande du 2026-11-22) : la faille change de **couleur** et de **taille** selon son rang.

| Rang | E | D | C | B | A | S | SS/EX |
|---|---|---|---|---|---|---|---|
| Couleur | vert | bleu | violet | orange | rouge | or | orange vif / violet |
| Échelle de la faille | ×1,0 | ×1,2 | ×1,4 | ×1,6 | ×1,9 | ×2,2 | ×2,6 |

Comment le faire :
- **Couleur** : une copie des particules `GoblinBreach_Portal` / `_Active` / `_Passive` par rang, avec les couleurs des
  `.particlespawner` remplacées. On ajoute la lumière du bloc (`Light.Color`), la couleur du marqueur de carte et celle du
  titre « Faille de rang X ». Un script génère les 7 variantes.
- **Taille** : un bloc portail par rang (`Eldanior_Faille_<Rang>`), avec `Particles[].Scale` et le décalage multipliés par
  l'échelle. Le rayon des volumes (projection à l'ouverture, zone sans construction) et l'explosion du débordement suivent
  aussi l'échelle. Une faille S se voit de loin, une faille E reste discrète.
- À vérifier en jeu : la taille réelle des particules à ×2,2. Le moteur multiplie le décalage et la taille par l'échelle
  (déjà observé sur les Boss Mondiaux).

**Taille du groupe** : réglée pour chaque faille, de 1 à 10 joueurs (décision du 2026-11-22). Les PV des monstres suivent
le nombre de joueurs **réellement entrés** (`DungeonScaling`, +35 % par joueur en plus : ×1 en solo, ×4,15 à 10).

Le **rang** règle trois choses : le niveau et les étoiles des monstres, la table de butin (tier1-4, puis boss) et la clé
ou le droit d'entrée. Un même modèle de donjon peut servir à plusieurs rangs.

### 2.3 Thèmes (un modèle d'instance chacun)

Commencer avec 2 ou 3 thèmes et réutiliser les monstres et préfabs vanilla :
- **Terrier gobelin** : préfabs `Orbis/Goblins`, environnement `Dungeon_Goblinlair_Duke` (déjà utilisé par Dungeon_V1).
- **Crypte** : squelettes, `Encounters_Crypt`, Roi de la Mort en version S.
- **Antre de givre**, **Forge ardente** (Golem Firesteel, Ignarok) : plus tard.

### 2.4 Liens avec les systèmes existants

- **Guilde et aventuriers** : missions de guilde « Fermer une faille de rang C » sur le tableau. Les PNJ aventuriers
  (commit `feat(aventuriers)`) pourraient « nettoyer » les failles qu'aucun joueur ne prend.
- **Quêtes** : une étape de chapitre « Fermer une faille ».
- **Économie** : objets propres à la faille (éclat de faille = monnaie d'échange, ingrédient des clés de faille).
- **Clé de faille (porte d'entrée personnelle)** : un objet `PortalKey` à poser dans le `Portal_Device` vanilla ouvre une
  faille privée d'un rang donné. C'est déjà ce que fait `PortalKey_Dungeon_V1`.
- **Domaines et intendant** : un débordement dans un domaine compte comme une invasion (amendes, défenseurs).

---

## 3. Deux façons de construire le donjon

| | A. Instance faite main (comme `Dungeon_V1`) | B. Instance procédurale (comme `Portals_Goblins`) |
|---|---|---|
| Principe | carte construite en créatif (`/instances edit new …`), chunks copiés à chaque ouverture | `HytaleGenerator` : biomes et `Assignments` qui placent des préfabs au hasard |
| Variété | même carte à chaque fois (corrigée par les rangs, le placement des monstres et plusieurs variantes) | carte différente à chaque fois |
| Coût | construire les salles (de 1 à 3 jours par thème) | graphe de 380 Ko, format peu documenté, lourd à régler |
| Contrôle (couloirs, porte de boss, salle finale) | total | faible (un monde ouvert, pas un donjon linéaire) |

**Recommandation : A d'abord.** On ajoute ensuite de la variété par des **balises de salle** (blocs invisibles posés dans la
carte, que le Java lit à l'ouverture) qui tirent au hasard monstres, pièges et coffres. Un **assemblage de salles
préfabriquées** (variante C, un donjon « roguelike ») viendra plus tard, si le besoin se confirme.

**Répartition JSON et Java** : les mécanismes vanilla (portail, instance, minuteur, objets éphémères, expulsion) restent en
JSON. Un `RiftManager` Java gère ce que le JSON ne sait pas faire : le rang, le niveau des monstres du mod, l'entrée de groupe,
le boss, les récompenses et le débordement.

---

## 4. Pièges connus et à vérifier

1. **Zones `DUNGEON` absentes dans l'instance** : `DungeonBosses` et la porte de boss cherchent une parcelle du
   `ParcelManager`, qui n'existe que dans le monde principal. Dans une faille, il faut une logique de boss propre, reconnue
   au nom du monde (préfixe d'instance) ou à une balise « porte du boss » dans la carte.
2. **Apparition hors chunk** ([[spawn-unloaded-chunk-and-stale-writes]]) : faire apparaître les monstres d'une salle quand un
   joueur approche, pas à la création de l'instance.
3. **Écritures pendant un tick** ([[store-processing-deferred-writes]]) : téléportations et composants via `cb.run`.
4. **Partage de l'instance** : d'après le bytecode, il passe par le `InstanceBlock` du bloc portail. Si le bloc est posé
   par Java (et non par l'interaction), il faut le vérifier. Autre solution : `InstancesPlugin.spawnInstance(nom, clé, …)`
   + `teleportPlayerToLoadingInstance` en Java, avec notre propre table faille vers monde.
5. **Mort dans la faille** : `ExitInstance` (on ressort) ou réapparition dedans ? Faut-il appliquer la pénalité d'XP de
   `DeathXPSystem` ?
6. **Arrêt du serveur avec des joueurs dedans** : point de retour personnel du moteur, failles dehors à refermer au
   redémarrage (comme `Invasions`, fichier des failles ouvertes).
7. **Failles gobelines vanilla** : vérifier si elles tournent déjà sur Eldanior (gameplay `Default`).
8. **Pas de jar pendant une partie** ([[no-jar-while-playing]]) : tests sur serveur isolé.

---

## 5. Plan

**Étape 0 : sonde (JSON seul, ½ jour).** Bloc `Eldanior_Faille_Donjon` copié de `Goblin_Breach_Portal`, avec
`TeleportInstance` vers `Dungeon_V1`. À vérifier : deux joueurs arrivent dans la même instance, le retour se fait au point
d'entrée, et l'instance est supprimée une fois vide. Ajouter `PortalWorldConfig` à l'instance pour voir le minuteur natif.

**Étape 1 : modèle d'instance et règles.** `PortalTypes/Eldanior_Faille_<Theme>.json` et
`GameplayConfigs/Eldanior_Faille.json` (parent `Portal`, `ExitInstance`, perte d'objets nulle ou faible, pas de
VoidEvent au début). Construire la carte du **Terrier gobelin** : entrée, 3 ou 4 salles, porte du boss, salle du boss,
sortie. Poser les balises de salle et de boss.

**Étape 2 : `RiftManager` (Java).** Une faille ouverte = monde, position, rang, thème, heure de fin, UUID d'instance.
Ouverture depuis le **panneau admin** (règle : pas de commande), effet et son vanilla recolorés selon le rang, marqueur
carte et boussole (réutiliser les marqueurs des Boss Mondiaux et d'`Invasions`), fermeture et sauvegarde sur disque.

**Étape 3 : peuplement et boss.** À l'entrée dans une salle, apparition des monstres de la balise au niveau du rang
(rôles et étoiles existants, `DungeonScaling` selon le groupe). Boss de faille (gel et invulnérabilité à l'ouverture,
comme `DungeonBosses`), clé de boss éphémère (`EphemeralItems`). Mort du boss : coffre de fin, portail de sortie,
annonce, fermeture de la faille dehors, puis `safeRemoveInstance` après 60 s.

**Étape 4 : règles d'entrée.** Groupe seulement (le chef entre, les membres du groupe le suivent), niveau minimal selon le
rang, nombre maximal de joueurs, entrée refusée une fois le boss engagé (à décider).

**Étape 5 : apparition automatique et débordement.** Minuteur global (comme `Perpetual_Stage1`), lieu sauvage hors des
villes et des domaines protégés, plafond de failles ouvertes. Faille non fermée à temps : débordement par `Invasions`.

**Étape 6 : intégrations.** Missions de guilde, étape de quête, clés de faille au `Portal_Device`, éclats de faille dans
l'économie (`docs/economie.md`), classement « failles fermées ».

**Étape 7 : variété.** Deuxième et troisième thèmes, variantes de cartes, puis assemblage de salles si besoin.

---

## 6. Décisions de l'utilisateur (2026-11-22)

1. **Rangs E → S** : oui.
2. **Débordement** : oui, mais simple pour l'instant. Le portail explose et un groupe de monstres du thème de la faille
   apparaît. C'est tout. L'évolution est notée dans `docs/a-faire.md`.
3. **Taille du groupe** : réglable de 1 à 10 pour chaque faille.
4. **Mort** : on est éjecté de la faille.
5. **Ouverture** : seule dans le monde, par l'admin, et par **« Faille scellée » à poser** (option B du § 7.6, choisie le
   2026-11-22).
7. **Plus de monstres** dans le Terrier du Duc, et **pas de coffre propre aux failles** : les récompenses sont **nos coffres
   au trésor** (`TreasureChest`) posés dans la salle du boss (2026-11-22).
6. **Premier donjon** : un donjon de **rang E** pour tester. Proposition au § 7.

---

## 7. Proposition : « Le Terrier du Duc », faille de rang E

### 7.1 L'idée

Une faille verte s'ouvre près d'une forêt. Derrière, un terrier gobelin creusé dans la roche, avec une mine, un campement,
la fosse de l'ogre, puis la salle du trône du **Duc gobelin**. C'est un donjon **linéaire et court** (10 à 15 min), fait pour
tester tout le cycle : entrée en groupe, salles, mini-boss, porte du boss, boss, coffre, sortie et fermeture.

Pourquoi ce thème :
- les gobelins du mod sont de **niveau 40 à 110** (`GoblinData`), ce qui tombe dans la bande E (60-150) ;
- le jeu fournit **97 préfabs `Prefabs/Dungeon/Goblin_Lair`**, avec une **salle de boss** et une **antichambre de boss**.
  La salle du boss contient déjà le Duc et son escorte, et la salle Large1 a des chemins de patrouille ;
- le Duc a un **combat à 3 phases** du jeu (`CAE_Goblin_Duke_Phase_1/2/3_Fast/Slow`) : un vrai boss sans rien créer ;
- `Dungeon_V1` utilise déjà l'ambiance `Dungeon_GoblinLair_Duke`.

### 7.2 Plan des salles (préfabs vanilla mis bout à bout)

```
[1 Arrivée] → [2 Galerie de mine] → [3 Le Campement] ─┬→ [4 Repaire caché] (bonus)
                                                      └→ [5 La Fosse de l'Ogre] → [6 Descente] → [7 Antichambre] ═porte═ [8 Trône du Duc]
```

| # | Salle | Préfab | Taille (blocs) | Contenu |
|---|---|---|---|---|
| 1 | Arrivée | `Prefabs_Mine/Transition_Goblin/Lair_Entrance_001` | 28×37×35 | zone sûre (pas de monstre), portail de retour, rappel des règles en titre |
| 2 | Galerie de mine | `Prefabs_Mine/Corridor/Medium_001` | — | 2 patrouilles : 5 gobelins (Mineur, Lanceur, Voleur ; Lv 60-70) |
| 3 | Le Campement | `Prefabs_Goblin/Room/Large1_001` | 54×33×55 | 11 gobelins (Bagarreur, Voleur, Lanceur, Récupérateur à l'épée et à la hache), patrouilles du préfab, 1 monstre ★ au hasard |
| 4 | Repaire caché | `Prefabs_Goblin/Cap/Hidden_Treasure_001` (derrière un `T-Junction`) | 19×15×13 | facultatif, **sans coffre** : 4 gobelins + 1 Ermite ★ (son butin de monstre est le bonus) |
| 5 | La Fosse de l'Ogre | `Prefabs_Goblin/Room/Beast_001` | 27×19×19 | **mini-boss : Ogre gobelin** (élite ★★, Lv 100) + 3 gobelins qui l'excitent ; il lâche la **Clé du Trône** |
| 6 | Descente | `Prefabs_Goblin/Straight/Length_24_Stairs_001` | — | 4 Lanceurs en hauteur |
| 7 | Antichambre | `Prefabs_Goblin/Room/Boss_Entrance_001` | 45×32×33 | garde de 7 gobelins armés (Lv 75-85), dont 1 ★ ; **porte du boss** (clé de l'Ogre) |
| 8 | Trône du Duc | `Prefabs_Goblin/Room/Boss_001` | 55×35×57 | **Duc gobelin** Lv 120 (3 phases du jeu), 3 gardes au début, **4 renforts à la phase 2** ; **coffres au trésor** scellés jusqu'à sa mort (§ 7.7), puis portail de sortie |

Emprise totale : environ 200 × 60 × 200 blocs, soit quelques régions comme `Dungeon_V1`.
La salle `Large2_Goblin_City` (59×80×66, ville gobeline) est trop grande pour un rang E : elle est gardée pour le rang D.

### 7.3 Chiffres du rang E

| Réglage | Valeur proposée |
|---|---|
| Niveau des monstres | 60-90 (sbires), Ogre 100, Duc 120 |
| Nombre de monstres | **~41** (37 sbires dont 3 ★ + 3 gardes du Duc + Ogre + Duc). Leurs PV suivent `DungeonScaling` selon les joueurs entrés |
| Taille du groupe | réglée à l'ouverture (1-10), **3 par défaut** pour un rang E |
| Temps dans la faille | 15 min (HUD natif du moteur), titre d'alerte à 2 min de la fin |
| Durée de vie de la faille dehors | 30 min ; non fermée à temps → **débordement** |
| Mort | **éjecté**, sans retour possible (une entrée par joueur) ; la faille reste ouverte pour les autres |
| Groupe entier éjecté ou temps écoulé | faille ratée → **débordement** immédiat |
| Récompense | les **coffres au trésor** de la salle du boss (§ 7.7), le butin des monstres, et 800 XP + 300 cuivres par joueur à la fermeture (repères de quête E) |
| Faille scellée (E) | rare dans `donjon_tier1` (remplace le 2 % de `PortalKey_Dungeon_V1`) |

### 7.4 Débordement (version simple)

Quand la faille n'est pas fermée à temps ou que tout le groupe est éjecté :
1. explosion au portail (`Explosion_Big` en anneau, comme l'étape 3 de la faille gobeline) et le bloc disparaît ;
2. un groupe de monstres du thème apparaît sur place. Rang E : 4 Bagarreurs + 2 Lanceurs + 1 Ogre chef, au niveau du rang ;
3. annonce dans la région et marqueur sur la carte jusqu'à la mort du groupe.

Pas de vagues ni d'effet sur les domaines pour l'instant (évolution dans `docs/a-faire.md`).

### 7.5 Construction

- Faire l'instance `Eldanior_Faille_Terrier_E` comme `Dungeon_V1` (monde `Void`, `DeleteOnRemove`) et y coller les préfabs
  bout à bout. Proposition : un **plan de collage** (préfab, position, rotation) dans un fichier, et une action admin qui le
  colle dans l'instance ouverte en édition. Le montage peut ainsi être refait et corrigé sans tout reconstruire à la main.
- **Retirer les PNJ des préfabs** au collage (le Duc et les gobelins enregistrés dedans) et les remplacer par des
  **balises** : le `RiftManager` fait apparaître chaque salle à l'approche, au bon niveau, avec la barre de boss et le gel.
  Les chemins de patrouille (`PatrolPathMarker`) sont gardés.
- Coffres au trésor posés à la main dans la salle du trône pendant l'édition du modèle (§ 7.7).
- Porte du boss : grande porte double du mod (`locks/`) avec une clé **éphémère** (`EphemeralItems` du PortalType),
  retirée à la sortie. La logique ne passe pas par les zones `DUNGEON` (voir § 4.1).

### 7.6 Ouvrir une faille « par clé »

La **clé** est un **objet** (`PortalKey`), comme `PortalKey_Dungeon_V1` qui existe déjà. On l'utilise sur un **bloc**,
le `Portal_Device` du jeu (le socle à portail), qui ouvre la faille à cet endroit. Deux options :
- **A (proche du jeu)** : clé « Éclat de faille (E) » à poser dans le `Portal_Device`. Fenêtre et effets du jeu.
- **B (plus simple pour le joueur)** : un objet « Faille scellée (E) » qu'on **pose comme un bloc**. La faille s'ouvre là où il
  est posé (hors des villes), sans socle.

Recommandation : **B**. Une seule action, et le même bloc que les failles naturelles. **Choisie le 2026-11-22.**
Règles proposées : impossible en ville et dans un domaine protégé ; on choisit la taille du groupe (1-10) en la posant ;
l'objet est consommé.

### 7.7 Récompenses : nos coffres au trésor dans la salle du boss

Pas de coffre propre aux failles : on pose **nos coffres au trésor** (`TreasureChest`) dans la salle du trône, dans
l'instance modèle en édition. Ils sont enregistrés tout seuls à la pose (`TreasureChestPlaceBlockEvent`).

- Proposition pour le rang E : **2 × `ChestTreasureDungeonTier1`** (table `donjon_tier1`). Ce sont des pages de rune pour
  `ChestTreasureDungeonBoss` (`donjon_boss`), trop fort pour un E : à garder pour les rangs C et plus.
- **Chaque joueur a son propre butin** : le système tire le contenu par joueur (`PlayerChestData`), selon sa chance.
  À 10 joueurs, chacun ouvre les deux coffres pour lui.
- **Scellés jusqu'à la mort du Duc** : le `RiftManager` refuse l'ouverture tant que le boss vit (message « Le trésor est
  scellé tant que le Duc respire »), puis les particules du coffre s'allument.

À vérifier avant de coder :
1. Les coffres enregistrés dans le modèle (ressource `TreasureChestTemplate` du monde) sont-ils **copiés dans chaque
   instance** ? On s'attend à oui, puisque le moteur copie `resources/` avec les chunks. Sinon, le `RiftManager` les
   enregistre à l'ouverture.
2. **Données qui gonflent** : `PlayerChestData` garde une entrée par coffre et par **nom de monde**. Chaque instance a un
   nom unique, donc les entrées s'accumulent : les purger quand l'instance est supprimée. Comme le nom change, le délai de
   recharge du coffre ne gêne pas d'une faille à l'autre.

---

## 8. Réalisé le 2026-11-23 (compilé, validé sur serveur isolé, non vu en jeu)

Demandes du jour : reprendre le plan, ajouter un **donjon de rang F** (cimetière, crypte, boss, zone morte, végétation,
brouillard constant, peu de visibilité, squelettes ; **F en blanc**), un **compteur d'entrées au-dessus du portail**,
mini-boss bien distinct du boss de donjon et du boss de zone, **étoiles au hasard** dans le donjon, apparitions
**proportionnelles aux joueurs** (pas de rang au-dessus des joueurs, pas 200 failles pour 1 joueur, même faille pas avant 1 h
sauf admin).

| Élément | Fichiers |
|---|---|
| Éléments du jeu : failles F → S (couleur + taille), particules recolorées, failles scellées, Sceau, Clé du Sceau, sortie, marqueurs, gameplay, instances, brouillard | `scripts/failles/gen_failles.py` |
| Donjons : chaîne de pièces vanilla, couche « sceau », repères, aperçus, contrôle « boss atteignable » | `scripts/failles/gen_donjons.py` → `resources/eldanior/failles/<id>.json`, `Server/Prefabs/Eldanior/Failles/<Id>_Sceau.prefab.json` |
| Code | `failles/` : `FailleManager`, `Faille`, `FailleDungeon`, `FailleRank`, `EntrerFailleInteraction`, `FailleBlockEvents`, `FailleNpcSystem` ; carte `ParcelMapSystem`, mort `DeathXPSystem`, panneau admin (carte « Failles-donjons ») |

**Le Cimetière des Oubliés (F)** : île de 70 blocs entourée d'une falaise (sol mort, ~60 tombes, arbres morts, clôture noire,
lanternes, ossements), mausolée vanilla au centre, escalier vers la crypte maudite (Couloir des Ossuaires, Nécropole,
Passage, Entrée du Tombeau, Galerie des Gisants, Antichambre), Tombeau du Roi Squelette. Nuit figée, météo
`Eldanior_Faille_Brume` (brouillard du cimetière vanilla rapproché à 46 blocs). Squelettes niv. 3 à 50, mini-boss
**Gardien des Ossements** (Chevalier squelette ×1,4), boss **Roi Squelette** (`Skeleton_Elite`, phase 2 du jeu), 12 min.

**Distinction des champions** : mini-boss = « Mini-boss · nom · Lv » + barre normale ; boss = « Boss de donjon · nom · Lv »,
crâne au-dessus de la tête, barre « difficile », gelé 5 s à l'apparition ; monstres étoilés = « … étoilé, N étoiles » +
étoiles. Le boss de zone reste celui du bloc de poste (`DungeonBosses`), hors des failles.

**Étoiles** : N monstres tirés au hasard parmi ceux de la salle (1 étoile 70 %, 2 : 25 %, 3 : 5 %), niveau × 1,2 / 1,5 / 2.
Ils ne créent PAS de contrat au tableau (un contrat est lié à un bloc de poste) : leur **prime est versée au tueur** comme
celle d'un contrat.

**Apparitions naturelles** (toutes les 10 min, 60 % de chance) : au plus ⌈joueurs ÷ 3⌉ failles naturelles (8 max) ; un
joueur au hasard, faille à 64-200 blocs de lui, hors des villes ; rang = celui de son donjon le plus haut **sans dépasser son
rang** ; un même donjon ne revient pas avant **1 h** après sa fin (sauf bouton admin). Groupe des failles naturelles : 3.

Constats techniques (vérifiés sur serveur isolé) :
- les pièces vanilla doivent être collées **sans leurs entités** (`PrefabUtil.paste(..., loadEntities=false)`) : sinon les
  balises de patrouille plantent l'outil de chemins du jeu (« Key is EMPTY ») ;
- collage exact (45 blocs sondés sur 45), arrivée, sorties, Sceau et coffres à leur place dans les deux donjons ;
- `teleportPlayerToLoadingInstance(ref, store, monde, RETOUR, ARRIVÉE)` (ordre lu par javap) ;
- un monde d'instance **sans joueur** refuse les PNJ dans ses chunks chargés (même l'instance vanilla `ShortLived`) :
  les salles ne s'éveillent qu'avec un joueur dedans, à **vérifier en jeu**.

### Réponses du 2026-11-23

- **Modifier une instance à la main depuis le jeu** : le jeu a ses outils (`/instances edit load <nom>`, édition en créatif,
  sauvegarde). Mais nos failles sont **collées à l'ouverture** dans un monde vide : retoucher le modèle d'instance ne
  changerait rien. Il faudrait un mode « prébâti » (comme `Dungeon_V1`) : coller le donjon une fois, le retoucher à la main,
  sauvegarder les chunks dans `Server/Instances/<nom>/chunks` et ne plus coller (une option par donjon dans le plan). À faire
  si tu veux retoucher un donjon à la main.
- **Quête « Failles » à l'apparition** : possible, voir § 9.

## 9. Idée : quêtes de failles (à valider)

À l'apparition d'une faille, une carte **« Faille de rang X »** s'ajoute au tableau des quêtes (onglet Failles, comme les
missions de guilde : hors de la limite de quêtes). Un joueur la prend, entre dans la faille : la quête est remportée quand la
faille est **refermée** (boss abattu) par un groupe dont il fait partie. Récompense en plus de celle de la faille (XP du rang,
réputation de guilde). Si la faille déborde, la quête échoue et devient « Repousser le débordement ».

---

## 10. Version 2 (2026-11-23, après le premier essai en jeu)

Retours de l'utilisateur : pouvoir **supprimer** les failles ; boutons admin peu clairs ; pas de mini-boss vu ; il veut des
**instances fixes qu'il modifie lui-même**, les monstres venant de **balises d'apparition** (`NPC_Spawn_Marker`) ; le bouton
admin ouvre une faille **autour de lui 15 s après le clic** ; **deux failles seulement pour l'instant (F et E)** ; reproduire
**l'apparition de la faille gobeline du jeu avec ses blocs** à l'endroit où elle s'ouvre.

**Apparition (comme la faille gobeline vanilla)** : le lieu est choisi, l'état des blocs est mémorisé, des **étincelles** du
rang apparaissent avec le compteur « s'ouvre dans N s » (15 s pour l'admin et les failles naturelles, 5 s pour une faille
scellée) ; puis explosion en anneau, éclat d'ouverture et **arche de roche du jeu** (`Goblin_World_Event_Portal`, 529 blocs)
collée avec notre portail (copie du portail gobelin : même modèle, même boîte). Rang F : ossements au lieu des déchets, herbe
sèche. À la fermeture (victoire, débordement, suppression), **le terrain d'origine revient** (vérifié sur serveur isolé).

**Instances fixes** : les modèles vivent dans un dossier modifiable, `Mods/EldaniorFailles` (le jeu refuse d'éditer une instance
rangée dans un jar). Le mod l'installe au premier lancement et l'enregistre comme pack (avertissement « Skipping pack… missing
manifest » au démarrage : sans conséquence). Panneau admin › **Failles** › un donjon › **Éditer l'instance** : on entre dans le
modèle en créatif, sans monstre (le donjon d'origine y est collé la première fois). On y pose des balises `NPC_Spawn_Marker`,
des coffres au trésor, des sorties (`Eldanior_Faille_Sortie`) ; **Terminer l'édition** sauvegarde. Chaque faille ouverte ensuite
est une copie du modèle, en aventure, balises actives. Dès qu'un donjon a été édité, **ses monstres ne viennent que des
balises** ; le boss est reconnu à son rôle (`Skeleton_Elite`, `Goblin_Duke`, et leurs phases) : crâne, barre « difficile »,
victoire à sa mort, sortie posée où il tombe. « Remettre le donjon d'origine » efface les retouches. Pour livrer les retouches
avec le mod : `scripts/failles/sync_pack.sh` (partie fermée).

**Panneau admin** : nouvel onglet **Failles** (l'ancienne carte du Tableau dépassait la limite de 6 boutons). Une carte par
donjon (ouvrir autour de moi dans 15 s, éditer, terminer, placer l'arrivée, remettre l'origine), une carte par faille ouverte
(supprimer), « Supprimer la faille la plus proche », « Apparitions naturelles ».

**Le Sceau et la Clé** ne sont plus posés (les monstres viennent des balises) : les blocs restent disponibles en créatif.

---

## 11. Version 3 : la faille de rang F (2026-11-23)

Retours : la liste `/instances` montrait deux dossiers « EldaniorMods » et des instances vides (impossible de les modifier) ;
utiliser **nos clés et portes de boss existantes** selon le niveau du donjon au lieu d'une clé de faille ; s'occuper d'abord du
**rang F** : zone naturelle **fermée par une barrière comme le royaume**, **forêt-cimetière de 200 × 200**, **brouillard
partout**, **crypte au milieu** qui mène au boss.

- **Pack d'instances** : enregistré sous son propre nom (`Eldanior:EldaniorFailles`) ; les modèles sont **livrés construits**
  (chunks dans `resources/failles-pack`), plus d'instance vide.
- **Clés et portes** : la salle du boss se ferme par la **porte de boss du mod** (`Eldanior_Porte_Boss_ED` pour F et E), posée
  par le code (cases de remplissage par le moteur ; rotation 0 = le long de X, 1 = le long de Z, vu au serveur isolé). La clé
  `Eldanior_Cle_Boss_ED` sort d'un coffre **`ChestTreasureCleBossED`** (une clé par joueur, système des coffres au trésor) posé
  dans la Nécropole (F) / la Fosse (E). Le Sceau et la Clé de faille sont supprimés.
- **Monstres** : **balises d'apparition** (`NPC_Spawn_Marker`, `SpawnMarkerComponent`) posées par le plan dans chaque salle
  (53 pour F, 40 pour E), visibles et déplaçables en édition. Boss : balise `Eldanior_Faille_Boss_Roi_Squelette` (le jeu n'en a
  pas pour le Squelette d'élite) / `Goblin_Duke`. Plus aucun monstre posé par le code.
- **Le Cimetière des Oubliés (F)** : forêt-cimetière de 200 × 200 au relief doux, ~415 arbres morts du jeu (frênes morts, sapins
  morts, arbres brûlés, secs, tordus, chênes moussus), 9 parcelles de tombes clôturées, ~136 tombes, 10 petits tombeaux, ruines,
  lanternes le long de l'allée, végétation morte et ossements ; **mur `Barrier` invisible + panneaux d'énergie violets** tout
  autour (comme `KingdomBarrierSystem`) ; brouillard épais et nuit (météo `Eldanior_Faille_Brume`) ; arrivée au nord, au bout de
  l'allée ; **mausolée au centre** → escalier → crypte → porte de boss → Tombeau du Roi Squelette.

Constats techniques (serveur isolé, 2026-11-23) :
- un monde d'instance où **aucun joueur n'est encore entré refuse les entités** (balises comme PNJ), même dans un chunk non
  chargé (le monde principal, lui, les accepte) : les balises du plan sont posées dans le modèle **à la première entrée de l'admin
  en édition** (témoin `.balises`), sinon dans la faille à l'arrivée du premier joueur ;
- modèles livrés **construits** (`failles-pack/.../chunks`, version `.version` = 3) ; un modèle plus ancien et jamais retouché
  (pas de `.balises`) est remplacé au démarrage, un modèle retouché ne l'est jamais ;
- porte de boss posée par `setBlock(…, rotation)` : le moteur pose les cases de remplissage (rotation 0 : x-1..x+2, 1 : z-2..z+1).

## 12. Version 4 (2026-11-23)

- **Boss absent, sortie immédiate** : le boss apparaissait, mais `EmblemOrphanSystem` (monstres à emblème orphelins) le
  retirait 3 s plus tard, et la disparition comptait comme une mort. Corrigé : ce système ignore les mondes de faille, et la
  victoire n'est donnée que sur une **vraie mort** (composant de mort, ou tué par un joueur) après 3 s sans nouvelle phase.
- **Terrain au-delà du mur** : 60 blocs de forêt dense et de relief tout autour (décor, derrière la barrière), pour que la
  carte ne soit pas coupée.
- **Générateurs de monstres (blocs)** : `Eldanior_Spawner_*` (Squelettes, Archers, Mages, Chevalier, BOSS Roi Squelette ;
  Gobelins, Lanceurs, Gardien, Glouton, BOSS Duc). À poser en édition ; le mod suit leur position (`spawners.json` du modèle).
  Dans la faille, chacun fait apparaître ses monstres quand un joueur approche (rayon 16 à 22 blocs) puis s'efface ; le
  générateur de boss fait apparaître le boss (crâne, barre « difficile », gelé 5 s). Le plan en pose 28 (F) et 25 (E).
- Un modèle **retouché** (« Terminer l'édition ») est marqué `.retouche` et n'est jamais remplacé par une mise à jour ; les
  modèles v3 non retouchés sont remplacés par la v4 au démarrage.

## 13. Générateur de monstres unique (2026-11-23, soir)

Demande : « un bloc invisible comme pour les PNJ, avec le design de Spawner_Maker, qui permet de sélectionner le monstre et le
nombre ; respawn 1 h ; supprimer les blocs qui ne servent à rien ; ne pas toucher à l'instance ; blocs du donjon dans la
rubrique Donjons ».

- **Un seul bloc** `Eldanior_Generateur_Monstres` (créatif › Donjons & Keys) : invisible et traversable comme le poste de PNJ,
  icône au design du marqueur d'apparition du jeu (cadre cyan, flèche, tête de monstre ; `scripts/failles/gen_generateur.py`).
  Le poser (admin) ou **F** dessus ouvre sa fenêtre : famille (filtre), monstre (tous les rôles du jeu et du mod), nombre
  (1 à 20), Enregistrer / Retirer. Tenir le bloc en main montre la marque au sol des générateurs posés (32 blocs).
- Fonctionnement (`failles/Generateurs`) : les monstres apparaissent quand un joueur passe à **24 blocs** ; quand tout le
  groupe est mort, il revient **1 h** plus tard. Marche partout : dans le monde normal (réglages dans
  `eldanior_data/generateurs.json`) et dans les failles (réglages du modèle : `generateurs.json` dans le dossier de
  l'instance, repris par chaque faille ouverte). Rien n'apparaît pendant l'édition d'un modèle.
- **Anciens blocs** `Eldanior_Spawner_*` : retirés du créatif et devenus vides (invisibles, sans effet). Leurs ids restent
  définis pour que les modèles déjà retouchés qui en contiennent se chargent **sans y toucher**.
- Hors du créatif (posés par le mod) : `Eldanior_Faille_F/E` (portail) et leurs étincelles.
  Dans **Donjons & Keys** : failles scellées F/E, sortie de faille, coffre au trésor de donjon, générateur (les portes de boss,
  clés et coffres à clé y étaient déjà).
- Le plan (`gen_donjons.py`) pose 24 générateurs (F) avec leurs réglages ; ils ne servent qu'à un modèle **neuf ou remis à
  zéro** (« Réinitialiser » efface aussi `generateurs.json`). Un modèle retouché garde les siens.

## 14. Propositions : un donjon par rang (2026-11-23, v2 après la remarque sur les Scarak)

Règle (remarque de l'utilisateur) : **chaque donjon prend la famille dont les niveaux du mod tombent dans son rang**. Les
monstres des générateurs gardent les niveaux de leur famille (`config/configs/Mobs/Mobs/*Data`) : une ruche Scarak
(800-880, palier 14) au rang D aurait fait des monstres 800+. Niveaux des familles : squelettes 1-60, gobelins 40-110,
squelettes de givre 150-230, calcinés 210-290, incandescents 215-280, des sables 270-360, zombies 330-420, Trorks 400-490,
Outlanders 460-560, Vide 530-630, esprits 600-700, golems 670-770, dinosaures 740-830, Scarak 800-880, dragons 860-999.

| Rang | Donjon | Famille (niveaux du mod) | Mini-boss | Boss | Porte | Temps |
|---|---|---|---|---|---|---|
| F | Cimetière des Oubliés (fait) | squelettes 1-60 | Gardien des Ossements | Roi Squelette (`Skeleton_Elite`) | ED | 12 min |
| E | Terrier du Duc (fait) | gobelins 40-110 | Ogre gobelin | Duc gobelin | ED | 15 min |
| D | La Nécropole Calcinée | squelettes calcinés et incandescents 210-290 | Emberwulf ★★ (250-330) | Prétorien calciné ×1,8 (300) | ED | 15 min |
| C | Les Égouts de la Peste | zombies, aberrations, goules, chiens blanchis 330-420 | Goule ★★ | L'Aberration (`Zombie_Aberrant_Big` ×2, 450) | CB | 18 min |
| B | Le Temple du Loup Noir | Outlanders 460-560 | Grand Prêtre ★★ | Fenrak, Loup-garou Alpha (`Werewolf` ×1,8, 600) | CB | 18 min |
| A | La Forge des Cendres | esprits 600-700, golems 670-770 | Golem de cristal de flamme ★★ | Le Forgemaître (`Golem_Firesteel` ×2, 800) | A | 20 min |
| S | La Ruche Écarlate | Scarak 800-880 | Garde Royale ★★ | Reine-Mère (`Dungeon_Scarak_Broodmother`, 950) | S | 25 min |
| SS/EX | Le Palais Céleste (événement) | chevaliers des ombres 750-850, golem gardien du Vide 850-999 | Gardien du Torii | Kokuō-Ryū hybride (999) | S | 25 min |

Variante gardée : Citadelle de Givre (esprits de givre 615-650, golems de givre 710-740, yétis 550-700) = autre donjon A.
À prévoir : `Eldanior_Ryu_Hybride` n'a pas de niveau de famille (tombe dans `default` 1-50) : lui en donner un (999) avant
de le poser en boss.

## 15. Faille de rang D : La Nécropole Calcinée (2026-11-24)

**v1 abandonnée** : le Cimetière du F « calciné » (même forêt, même crypte, blocs remplacés). Retour de l'utilisateur : « il
ressemble trop au donjon de rang F ; je veux un biome spécifique ; je construirai le donjon, crée juste l'environnement
extérieur, les teintes et les particules d'environnement ».

**v2 : la Caldeira** (`scripts/failles/biome_caldeira.py`, entrée `necropole_d` de `gen_donjons.py` avec `"exterior":
"caldeira"` : pas de pièces, de porte ni de coffres).

| Élément | Détail |
|---|---|
| Relief | cuvette de cendres fermée par une **falaise volcanique** (22 à 40 blocs au bord, 60-70 aux coins), sommets déchiquetés au-delà (décor) ; mur `Barrier` invisible sans panneaux d'énergie |
| Esplanade | **plate et vide**, rayon 36 autour de (0, 0, 8) : le chantier du donjon que l'utilisateur construit en édition. Boss **provisoire** (Prétorien Calciné) au centre, à déplacer avec le poste « Boss de donjon » |
| Lave | rivière de lave dans une gorge (ouest → sud) jusqu'à un lac au sud-est, source en mare ; un seul niveau (pas d'écoulement) ; berges de magma refroidi et de roche incandescente, roseaux de lave |
| Décor du jeu | 2 orgues basaltiques (colonnes de hauteurs variées), 2 grandes arches de lave (est, nord-ouest), 2 arches de cendre, 26 pics, 18 aiguilles, rochers géants, 7 ruines calcinées, 12 amas de cristaux rouges, 14 bois tordus en feu, troncs brûlés, ronces de lave, 18 **fumerolles** (fumée du jeu sur fissure incandescente), gravats et ossements |
| Sols | cendre, sable cendré sur les crêtes, gravier volcanique dans les creux, affleurements de roche volcanique et de basalte |
| Teinte | `WorldGen.Tint` de l'instance = `#6b4b33` (vérifié : `ff6b4b33` dans les chunks) |
| Météo | `Eldanior_Faille_Caldeira` : ciel lie-de-vin, horizon de braise, gros soleil rouge bas (17 h figée), lumière orangée, brume brun-rouge à 96 blocs, nuages d'orage rougis |
| Particules | `Eldanior_Faille_Caldeira` : flocons de cendre lents (`Eldanior_Caldeira_Cendres`), braises qui montent (`Eldanior_Caldeira_Braises`), fumée basse du jeu |
| Environnement | `Env_Eldanior_Faille_Caldeira` : **aucune apparition naturelle**, météo forcée à toute heure, tag `Eldanior_Caldeira` → ambiance sonore grondement de lave + vent (`AmbFX_Eldanior_Caldeira_*`) |
| Monstres | 15 générateurs (33 monstres + boss) hors de l'esplanade : squelettes calcinés et incandescents, 2 Emberwulf |

Serveur isolé : modèle construit et copié, lave, fumerolles, cristaux, feu, mur, teinte et météo vérifiés ; aucune erreur
d'asset. Les sorties F et E sont **identiques à l'octet**. Le code « calciné » de la v1 a été retiré de `gen_donjons.py`.
**Non vu en jeu** (couleurs du ciel, particules et sons ne se vérifient que dans le client).

Constats :
- la v1 était installée chez l'utilisateur et demandait la météo `Eldanior_Faille_Cendres`, supprimée : **impossible d'entrer**
  (« Failed to decode 'ForcedWeather' »). Corrigé dans `FaillePack` : un modèle **jamais retouché** (pas de `.retouche`) est
  remplacé quand le mod en livre un `.version` plus récent (Nécropole : 5). Les modèles retouchés ne sont jamais touchés ;
- le modèle du Terrier (E) installé chez l'utilisateur n'a pas de `generateurs.json` : ses failles n'ont que le boss.
