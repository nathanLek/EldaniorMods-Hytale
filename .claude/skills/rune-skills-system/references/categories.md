# Les 12 types de compétences

Chaque type a une **mécanique signature** (ce qui le rend unique), un **style** et une **palette**. Les palettes et mots-clés Hytale
sont dans `scripts/runes/rune_rules.json > types`. Les ids d'assets ci-dessous existent (mod ou vanilla) à la date d'écriture ;
re-vérifier avec `rune_tool.py assets` avant usage.

| Type | Signature mécanique | Style de jeu | Statuts existants (mod) | Visuels utiles |
|---|---|---|---|---|
| **Feu** | brûlure (DoT), propagation, explosion de zone | pression offensive, zones dangereuses | `Elda_Brulure`, `Elda_Embrasement`, `Elda_Ignition`, `Muspelheim_*` | `Muspelheim_Fire_Pillar`, `Fire_Tornado`, `Impact_Fire`, bloc `Rock_Volcanic_Cracked_Lava`, sol `Soil_Dirt_Burnt` |
| **Glace** | ralentir → geler → briser (dégâts bonus sur gelé), murs | contrôle, défense, combos | `Elda_Gel`, `Elda_Givre`, `Elda_Hypothermie`, `Niflheim_Gel` | blocs `Rock_Ice`, `Rubble_Ice`, `Soil_Snow`, particules `Niflheim_*`, `Frost_Trail` |
| **Terre** | murs, pointes, séismes (étourdir), armure de pierre | tank, zone, protection | `Elda_PeauDePierre`, `Elda_Seisme`, `Elda_Enracinement`, `Gaia_Etourdi` | `Gaia_Dust_Burst`, blocs `Rock_*`, `Soil_*`, débris `spawnBlock` |
| **Air** | projection, attraction tournante, mobilité, planer | contrôle de position, déplacement | `Elda_Bourrasque`, `Elda_Legerete`, `Elda_Tourbillon`, `Fujin_Vent_Arriere` | `Fujin_Gust`, `Fujin_Cannon`, `Fujin_Strike`, trails `Fujin_Wind`, `Windstrike_Distortion` |
| **Foudre** | électrocution (étourdissement bref), arcs qui sautent de cible en cible (chaîne), surcharge (dégâts différés), frappes venues du ciel | rafale de dégâts, punition des groupes serrés | `Taranis_Foudroye`, `Taranis_Surcharge`, `Taranis_Benediction` | particules `Taranis_*`, son `SFX_Global_Weather_Thunder` (2D : `sound2dNear`), cause de dégâts `Lightning` |
| **Ténèbres** | malédiction, drain de vie/mana, ombres, peur, invisibilité | attrition, contrôle mental | `Elda_Corruption`, `Elda_DrainDeVie`, `Elda_Terreur`, `Elda_Malediction`, `Nyx_*` | `Nyx_Void`, `Nyx_Mist`, beam `Nyx_Chain`, bloc `Nyx_Goudron` |
| **Lumière** | soin, purification, bouclier, aveuglement, dégâts sacrés aux morts-vivants | soutien, protection d'équipe | `Elda_Benediction`, `Elda_BouclierSacre`, `Elda_Guerison`, `Apollo_*` | `Bifrost_Divine`, `Bifrost_Pillar`, `Apollo_*`, lumière des modèles (`Light`) |
| **Temps** | accélérer (soi/alliés), ralentir/figer (ennemis), rembobiner, échos | tempo, contre-jeu | `Chronos_Hate`, `Chronos_Fige`, `Chronos_Remanence` | particules `Chronos_*` (sable doré, glyphes) |
| **Espace** | **position** : téléportation, portails, échange de places, bannissement dimensionnel, poches | mobilité, repositionnement, évasion | `Chaos_Attraction` (rangé ici historiquement) | `Bifrost_Ribbon`, `Bifrost_Arc_*`, sons `SFX_Portal_*` |
| **Combat** | combos, frappes chargées, parades, ruées, brise-garde (arme en main) | corps à corps technique | effets d'arme vanilla | anims `Sword`, `Battleaxe`, `Daggers` (`assets anims`), trails d'arme |
| **Spatial** | **forces** : gravité, attraction/répulsion, compression, lévitation, télékinésie de blocs | contrôle de foule physique | — (à créer) | `Chaos_*` (vortex, onde), `spawnBlock`, `push` |
| **Dragonic** | souffle (cône), griffes (multi-frappes), ailes (bond/vol plané), rugissement (peur), forme draconique | hybride puissance/mobilité, classe Dragon | `Dragon_Avatar_Forme`, `Bahamut_Terreur` | `Dragon_Breath`, `Muspelheim_*`, `Fire_Tornado` |

## Frontière Espace / Spatial (éviter les doublons)
- **Espace** agit sur **où** sont les choses : la cible **change de lieu** sans parcourir la distance (téléportation, portail,
  échange, bannissement, distance repliée). Aucun mouvement physique visible entre départ et arrivée.
- **Spatial** agit sur les **forces** : la cible **se déplace physiquement** (attirée, repoussée, soulevée, écrasée, mise en orbite),
  avec une trajectoire visible et des collisions.
- Test : « la cible traverse-t-elle l'espace entre A et B ? » Oui → Spatial. Non → Espace.
- Air vs Spatial : Air = **vent** (souffle, rafale, tornade : directionnel, lié au vent, affecte aussi les feuillages) ; Spatial =
  **gravité pure** (radiale, sans vent, peut affecter des blocs).

## Frontière Air / Foudre
- **Air** = mouvement de l'air : pousser, tirer, soulever, se déplacer ; dégâts modestes, fort contrôle de position.
- **Foudre** = énergie : instantanéité, chaînes entre cibles, étourdissements courts, frappes verticales ; peu de déplacement imposé.
- Une tempête qui projette relève de l'Air ; un éclair qui saute de monstre en monstre relève de la Foudre.

## Identité par rareté (exemples de progression d'un même type)
Common : un geste simple (projectile, petit cône). Rare : une mécanique (combo, état). Epic : une zone persistante ou une
invocation. Unique : un changement de règles local (domaine, transformation). Legendaire : un événement de bataille. Divin :
une manifestation divine qui transforme le terrain et dure (voir les sorts divins existants).
