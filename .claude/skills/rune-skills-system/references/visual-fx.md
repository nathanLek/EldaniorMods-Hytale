# Visuels : ce qui marche, et les pièges vérifiés

## Animations du personnage (incantation)
- Déclarées dans le JSON d'incantation : `Effects.ItemPlayerAnimationsId` (jeu) + `ItemAnimationId` (clip).
- Lister les clips réels : `python3 scripts/runes/rune_tool.py assets anims Staff` (aussi `Spellbook`, `Sword`, `Battleaxe`, `Daggers`, `Item`).
- Clips éprouvés dans le mod : `Staff/CastSummonCharging` → `Staff/CastSummon` (invocation), `Spellbook/CastPushCharging` →
  `CastPushCharged` (poussée), `Spellbook/CastHurlCharging` → `CastHurlCharged` (lancer), `Sword/CastEnrage`, `Sword/Guard`.
  Corps à corps : `Battleaxe/DownstrikeCharging`, `DownstrikeLeap`, `Whirlwind`, `Sword/StabDashCharged`, `Daggers/DashForward`, `Daggers/KickTornadoRight`.
- Pas d'animation personnalisée sans fichier `.blockyanim` ; ne pas en promettre.
- Particules dans les mains pendant l'incantation : `Effects.Particles` avec `TargetEntityPart: "Entity"` et `TargetNodeName: "R-Hand"` / `"L-Hand"`.
- Caméra : `Effects.CameraEffect` (`Ability_Cast_Generic`) et `SpellFx.shake(pos, r, "Impact_Strong" | "Impact_Light")`.

## Particules émises par le code (`SpellFx.fx`)
- **Un système sans `LifeSpan` dont un spawner émet en continu boucle à l'infini** quand on l'émet seul (ex. `Ability_ChainHook_Hit` :
  anneaux restés après la mort). Faire une copie du mod avec `LifeSpan`. `rune_tool.py check` et `assets particles` le signalent.
- Le rendu `BlendAdd` blanc est **invisible sur un ciel clair** : utiliser `BlendLinear` (fumée, poussière) ou des couleurs saturées.
- Un quad à plat (`Rotation X 90`) est invisible de profil.
- `ScaleRatioConstraint: OneToOne` force Y = X (impossible d'aplatir).
- `ParticleRotationInfluence` : `Billboard` (face caméra), `Velocity` (étiré dans le mouvement, idéal pour traits et pluie), `None` + `ParticleRotateWithSpawner` (orienté par yaw/pitch du `fx`).
- Rayon d'envoi : joueurs à moins de 200 blocs du point d'émission.
- **Trop fin ou trop clairsemé = invisible** : un trait de 0,2 bloc de large n'est qu'un pixel à 15 blocs. Viser ≥ 0,4 de large,
  plusieurs blocs de long, et une densité suffisante (taux × durée de vie) sur toute la hauteur de l'effet.
- Direction garantie : plutôt qu'un `Pitch` (signe à vérifier), donner une vitesse initiale faible et un attracteur
  `LinearAcceleration` (ex. `Y: -75` pour une pluie qui tombe). Exemple : `gen_gravite.py`.
- Doubler un effet important : modèle porteur de trails animés **et** particules émises par le code (colonne de gravité).
- Colonne qui monte : `InitialVelocity Pitch 90` (vers le haut), vitesse × durée de vie = hauteur (pilier du Bifrost : 80 b/s × 2,5 s = 200 blocs).
- Tourbillon : attracteur `RadialAxis Y`, `RadialAcceleration` négative (vers l'axe), `RadialTangentAcceleration` (rotation) (`Example_Tornado`, `Fire_Tornado_Spin`).

## Visuels attachés à un modèle (fiables, gérés par le client)
- Un `ModelAsset` (`Server/Models/...json`) peut porter `Particles` (`SystemId`, `TargetNodeName`, `Scale`), `Trails`
  (`TrailId`, `TargetNodeName`, offsets) et `Light`. Ces systèmes bouclent tant que l'entité existe.
- La tornade vanilla **n'a aucune géométrie** : son rendu vient de **trails `Wind` sur des nœuds animés en rotation** (`Tornado_Spin`) et de
  particules sur ces nœuds. Le cyclone de Fujin applique le principe à grande échelle (26 anneaux sur 100 blocs, 192 trails) :
  `scripts/rune_aura_icons/gen_fujin_cyclone.py` génère le `.blockymodel`, le `.blockyanim` et le `ModelAsset`.
  Format d'animation : `nodeAnimations` indexé par **nom** de nœud, `orientation` = quaternions par pas de 120° (interpolation au plus court).
- Plutôt qu'émettre des centaines de particules par le code (coûteux, causes de freezes), préférer un modèle porteur de trails/particules.
- **Confirmé en jeu (Fujin)** : le mur de traînées sur anneaux tournants est visible. Un anneau tous les 4 blocs donne une tornade
  « coupée » : mettre **un anneau par bloc** (une traînée de mur par nœud suffit, filets et poussière seulement sur certains anneaux).
- Les grosses particules de nuage émises par le code ne sont pas apparues dans le ciel (l'utilisateur a seulement vu une fumée noire
  au sol) : les nuages se font en **larges traînées sur des anneaux lents** du modèle (plafond de `Fujin_Cyclone`).
- **Règle de chargement** : le chemin `Model` doit être sous `Characters/`, `NPC/`, `Items/` ou `VFX/`, sinon **tout le mod échoue à
  charger** (« échec des assets »). Copier un modèle de bloc sous `Common/Items/...` avant de l'utiliser.
- Échelle : unités des modèles d'entité = 64 / bloc (modèles de blocs 32 / bloc). `setModelScale` (modèle recréé à l'échelle) marche là où
  `EntityScaleComponent` est ignoré (projectiles). Garder une hitbox large en X : le serveur masque les entités à hitbox minuscule.
- Un porteur sans géométrie peut marcher (la tornade vanilla en est un) ; l'essai `Fujin_Vortex` (particules seules, sans trails ni animation) n'a rien affiché : privilégier trails + nœuds animés.

## Effets de statut (`Server/Entity/Effects/Eldanior/<Type>/`)
- `ApplicationEffects` : teintes (`EntityTopTint`/`EntityBottomTint`), `MovementEffects`, `HorizontalSpeedMultiplier`, `Particles`, `ScreenEffect`, `ModelVFXId`.
- Réappliquer un effet en `Overwrite` à chaque tick fait clignoter la teinte : l'appliquer une fois, avec une longue durée.
- Une teinte `#000000` est ignorée : utiliser `#0d0614` / `#030106` pour du noir.
- Lister : `rune_tool.py assets effects <regex>`.

## Beams (chaînes, rayons)
`Server/Entity/Beams/<Id>.json` + texture `Common/Trails/...`. Les beams attachés à une entité (`toEntity`) disparaissent : utiliser des
beams de position rafraîchis toutes les 100 ms (`NyxVeil`).

## Choix artistiques de l'utilisateur
- **La gravité est invisible** : pas d'explosion, d'onde de choc ni de poussière d'impact ; seuls le terrain sous les cibles (cratère, fissures) et la colonne de traits noir-violet la montrent (Puits / Autorité Gravitationnelle).

## Mise en scène par rareté
Common : 1 effet d'incantation + 1 d'impact. Rare : + traînée / particules de trajet, son dédié. Epic : zone visible persistante,
secousse caméra. Unique / Legendaire : modèle invoqué, lumière, terrain transformé. Divin : changement d'ambiance (brume, sol,
dôme, ciel via modèles géants), effets à 100–200 blocs, multiples phases (apparition, règne, final).
