# Banque d'idées de compétences

Idées validées par l'utilisateur comme **pistes** (pas des commandes) : s'en servir pour proposer ou construire des sorts, en
les adaptant à sa demande. Les chiffres sont des points de départ (budget par rareté : `rarity-balance.md`, coûts : `rune_rules.json`).
Les identifiants d'assets cités sont des pistes à vérifier avec `rune_tool.py assets` avant implémentation.
Une idée réalisée est marquée ✔ (avec son id de rune) et ajoutée à `library.md`. **Aucune nouvelle compétence Divine.**

Format : **Mécanique** (ce que fait le sort) · **Chiffres** (dégâts, portée, durée) · **Visuel / son** · **Terrain** (toujours restauré) · **Jeu** (rôle, contre-jeu, synergies).

## Sommaire

_En italique : famille des clones en miroir (détaillée dans sa propre section, juste après le sommaire)._

| Type | Common | Uncommon | Rare | Epic | Unique | Legendaire |
|---|---|---|---|---|---|---|
| **Feu** | Flammèche · Souffle ardent · Paume brûlante · Braise rebondissante · Sillage ardent | Mine de magma · Torche vivante | Mur de flammes | Pluie de comètes | Phénix renaissant | Cœur de volcan |
| **Glace** | Éclat de givre · Flaque gelée · Armure de givre · Pique de givre · Glissade | Prison de glace · Souffle hivernal | Lance de cristal | Blizzard | Sculpteur de glace | Ère glaciaire |
| **Terre** | Jet de pierre · Piétinement · Peau de pierre · Mur de terre · Fracture | Armure de roche · Sables mouvants | Lancer de rocher | Pilier ascendant | Tremblement tectonique | Colosse de pierre |
| **Air** | Bourrasque ✔ · Pas du vent · Lame d'air · Saut porté · Bulle d'air | Vrille · Courant ascendant | Lames de rafale | Jet-stream | Danse des vents | Ouragan dirigé |
| **Foudre** | Décharge · Arc chaîné · Charge statique · Étincelle · Pas éclair | Orbe statique · Paratonnerre | Chaîne d'éclairs | Surcharge | Avatar de l'orage | Supercellule |
| **Ténèbres** | Trait d'ombre · Voile de fumée · Marque funeste · Griffe d'ombre · Pas dans l'ombre · *Ombre jumelle* | Chaînes noires · Malédiction de faiblesse · *Pantins d'ombre* | Puits d'âmes · *Fondu dans l'ombre* | *Légion d'ombres* | Éclipse · *Doppelgänger* | Faucheur · *Nuit des doubles* |
| **Lumière** | Rayon sacré · Soin mineur · Éclat aveuglant · Halo protecteur · Flèche sacrée · *Reflet jumeau* | Purification · Phare · *Miroir trompeur* | Lance de lumière · *Prisme* | Sanctuaire mineur · *Galerie des glaces* | Résurrection · *Reflet parfait* | Jugement céleste · *Palais des miroirs* |
| **Temps** | Pas pressé · Retard · Écho · Accélération locale · Dégâts différés | Boucle · Hâte de groupe | Stase | Distorsion | Retour en arrière | Prophétie |
| **Espace** | Clignement · Échange · Ancre · Portail de poche · Pas de côté | Paire de portails · Rappel | Bannissement mineur | Portail d'artillerie | Labyrinthe | Porte des mondes |
| **Combat** | Brise-garde · Ruée · Riposte · Coup de bouclier · Taillade tournoyante | Exécution · Cri de défi | Bond fracassant | Danse des lames | Maître d'armes | Mille coupes |
| **Spatial** | Onde de répulsion · Attraction · Lévitation · Alourdir · Allègement | Orbite · Inversion | Aimant | Télékinésie ✔ · Ceinture d'astéroïdes | Puits Gravitationnel ✔ · Lentille gravitationnelle | Autorité Gravitationnelle ✔ · Marée gravitationnelle |
| **Dragonic** | Griffes · Crachat de braises · Rugissement · Écailles · Battement d'ailes | Morsure · Œil du dragon | Souffle de feu | Ailes de dragon | Rugissement ancestral | Pluie d'écailles |

## Famille : clones en miroir (Lumière / Ténèbres)

> **Réalisé (2026-10-21), selon la demande révisée de l'utilisateur :**
> - ✔ **Clonage** (`Rune_Clonage`, Lumière, Epic) : 5 clones **PNJ** du lanceur (son modèle, son skin, son arme de corps à corps ou
>   mains nues, 50 % de sa vie) le suivent 20 s ; **clic droit sur un ennemi = un clone libre part le combattre en autonomie** (vraies
>   attaques de l'arme, compétence de signature, sauts), puis enchaîne sur le monstre le plus proche ; re-clic = un autre clone. Les
>   monstres voient et attaquent les clones ; un clone vaincu se dissipe. Classes : dragon, guerrier, assassin, archer.
> - ✔ **Doppelgänger** (`Rune_Doppelganger`, Ténèbres, Epic) : **une seule technique, sans variantes** — un double d'ombre PNJ qui
>   apparaît à droite du lanceur, reflet exact par rapport à un miroir parallèle à son regard (gauche ↔ droite), 30 s. Il tient le même
>   objet en main et **rejoue chaque chaîne d'interaction du lanceur** (clic gauche, clic droit, capacités d'arme ; pas les runes),
>   intouchable. Repli si le rejeu échoue : coups recopiés à 60 %. Classes : dragon, guerrier, assassin, archer.
> Les autres fiches de cette section restent des pistes ; « Doppelgänger » (Unique) ci-dessous est remplacé par la version Epic réalisée.

Demande de l'utilisateur (2026-10-21) : des **doppelgängers / clones qui fonctionnent comme un miroir**, pour le combat, en **Lumière** ou en
**Ténèbres**, avec plus de clones aux raretés hautes (Common = 2, Epic = 10).

**Principe du miroir** : chaque clone est placé en symétrie du lanceur (par rapport à lui, ou à sa cible pour les plus hautes raretés),
bouge en miroir de ses déplacements et reproduit ses coups vers l'ennemi en face de lui, à une fraction des dégâts.
- **Lumière — Reflets** : illusions dorées translucides ; leurres, aveuglement, renvoi des attaques, échange de place.
- **Ténèbres — Doppelgängers** : ombres fumantes ; elles combattent, volent la vie, explosent, possèdent.

| Rareté | Clones | Lumière | Ténèbres |
|---|---|---|---|
| Common | 2 | Reflet jumeau | Ombre jumelle |
| Uncommon | 3–4 | Miroir trompeur | Pantins d'ombre |
| Rare | 4–5 | Prisme | Fondu dans l'ombre |
| Epic | 10 | Galerie des glaces | Légion d'ombres |
| Unique | 1 parfait | Reflet parfait | Doppelgänger |
| Legendaire | variable | Palais des miroirs | Nuit des doubles |

**Pistes techniques (vérifiées dans le projet)** :
- Clone = entité modèle portant le **modèle et le skin du lanceur** (`ModelComponent` + `PlayerSkinComponent`), avec ses états de mouvement
  (`MovementStatesComponent`) et l'orientation de la tête (`HeadRotation`) : c'est ce que font les rémanences de `ChronosHourglass.dress()`,
  et les clones marchent et courent donc animés. Teintes : `Chronos_Remanence` (lumière dorée à créer), `Nyx_Clone` (ombre).
- Position en miroir : chaque tick, `centre + réflexion(position du lanceur − centre)` ; `SpellFx.hold(clone, pos, rot)`.
- Copie des coups : intercepter les dégâts infligés par le lanceur (`DivineDamageSystem`), puis chaque clone applique `SpellFx.damage` à la
  fraction prévue sur l'ennemi en face de lui. **À vérifier** : rejouer l'animation d'attaque sur le clone (les états de mouvement couvrent
  la marche, pas les coups) ; à défaut, traînée d'arme ou éclat au moment du coup.
- Clones « autonomes » (Pantins, Doppelgänger) : déplacés par le code vers leur cible, comme les ombres de `NyxVeil`.

### Lumière — Reflets

#### Reflet jumeau — Common · 25 mana · 15 s
- **Mécanique** : deux reflets de lumière apparaissent à ta gauche et à ta droite et se comportent comme ton image dans un miroir : ils se déplacent en symétrie de tes mouvements et reproduisent chacun de tes coups vers l'ennemi en face d'eux.
- **Chiffres** : 2 reflets · 6 s · chaque coup copié inflige 20 % de tes dégâts · un reflet se brise au premier coup reçu et aveugle 0,5 s l'ennemi qui l'a frappé.
- **Visuel / son** : copies de ton personnage (skin et mouvements) translucides et dorées, contour lumineux, éclat de verre qui tinte à la rupture.
- **Terrain** : aucun.
- **Jeu** : initiation à la famille : petits dégâts en plus, deux leurres fragiles.

#### Miroir trompeur — Uncommon · 60 mana · 30 s
- **Mécanique** : quatre reflets t'entourent en losange et imitent tes gestes ; les monstres choisissent leur cible au hasard entre toi et tes reflets. Clic droit pendant le sort : tu échanges ta place avec le reflet visé.
- **Chiffres** : 4 reflets · 8 s · coups copiés 20 % · chaque reflet encaisse 1 coup · échanges illimités pendant la durée.
- **Visuel / son** : reflets dorés miroitants, flash et tintement de cristal à chaque échange de place.
- **Terrain** : aucun.
- **Jeu** : survie et repositionnement : on ne sait jamais lequel est le vrai.

#### Prisme — Rare · 120 mana · 60 s
- **Mécanique** : cinq reflets se placent en étoile autour de toi ; chacune de tes attaques est réfractée : un rayon de lumière part de chaque reflet vers ta cible.
- **Chiffres** : 5 reflets · 10 s · 5 rayons de 30 dégâts par attaque (150 si tout touche) · les reflets ne se déplacent pas, ils pivotent pour viser.
- **Visuel / son** : rayons dorés qui convergent sur la cible, prismes qui décomposent la lumière en arc-en-ciel au départ de chaque reflet.
- **Terrain** : aucun.
- **Jeu** : multiplicateur de dégâts monocible, à placer avant un combat.

#### Galerie des glaces — Epic · 300 mana · 120 s
- **Mécanique** : dix reflets apparaissent en cercle autour de l'ennemi visé, comme une salle de miroirs : chacun reproduit tes attaques en miroir depuis son côté, l'ennemi est frappé de partout. À la fin, les reflets convergent vers lui et éclatent.
- **Chiffres** : 10 reflets · 12 s · coups copiés 15 % chacun (jusqu'à 150 % au total) · éclatement final : 60 dégâts par reflet en zone de 3 blocs + aveuglement 1,5 s.
- **Visuel / son** : cercle de silhouettes dorées, miroitement entre elles, explosion de lumière blanche et pluie d'éclats de verre au final.
- **Terrain** : aucun.
- **Jeu** : écrase un boss ou un duelliste ; l'ennemi peut casser les reflets un par un (1 coup chacun).

#### Reflet parfait — Unique · 600 mana · 180 s
- **Mécanique** : un double de lumière indiscernable de toi (même apparence, même barre de vie affichée) : il se tient exactement en miroir par rapport à ta cible et reproduit tout ce que tu fais. Clic droit : échange instantané avec lui.
- **Chiffres** : 1 reflet · 20 s · coups copiés 50 % · renvoie 30 % des dégâts qu'il subit · 40 % de tes PV · échanges illimités.
- **Visuel / son** : copie parfaite avec un très léger halo doré, distorsion à chaque échange.
- **Terrain** : aucun.
- **Jeu** : duel psychologique : l'adversaire ne sait pas qui frapper ; contré par les sorts de zone.

#### Palais des miroirs — Legendaire · 1 200 mana · 300 s
- **Mécanique** : une zone de 15 blocs se couvre de miroirs de lumière : chaque allié qui y entre reçoit 2 reflets, et les projectiles ennemis qui touchent un miroir sont renvoyés vers leur tireur.
- **Chiffres** : zone 15 blocs · 20 s · 2 reflets par allié (coups copiés 20 %) · renvoi des projectiles ennemis à 100 % · ennemis désorientés (leur cible change toutes les 3 s).
- **Visuel / son** : panneaux de miroirs lumineux qui flottent et tournent lentement, reflets dorés de toute l'équipe, éclats lumineux à chaque renvoi.
- **Terrain** : aucun.
- **Jeu** : version d'équipe : toute l'escouade se démultiplie ; contrée en sortant de la zone.

### Ténèbres — Doppelgängers

#### Ombre jumelle — Common · 25 mana · 15 s
- **Mécanique** : deux ombres se détachent de toi, à gauche et à droite, et bougent en miroir de tes déplacements ; elles reproduisent tes coups vers l'ennemi en face d'elles et volent un peu de vie à chaque coup.
- **Chiffres** : 2 ombres · 6 s · coups copiés 20 % · chaque coup d'ombre te rend 2 PV · une ombre se dissipe au premier coup reçu.
- **Visuel / son** : silhouettes noires de ton personnage (teinte `Nyx_Clone`), fumée sombre aux contours, murmure à chaque coup.
- **Terrain** : aucun.
- **Jeu** : pendant de Reflet jumeau : moins de contrôle, un peu de survie.

#### Pantins d'ombre — Uncommon · 60 mana · 30 s
- **Mécanique** : trois ombres de toi qui ne copient plus tes gestes : elles se jettent seules sur l'ennemi le plus proche et le harcèlent.
- **Chiffres** : 3 ombres · 8 s · 40 dégâts/s chacune · 2 coups suffisent à en détruire une · elles attirent l'attention des monstres.
- **Visuel / son** : ombres qui courent en fumant, griffures noires à chaque coup, elles s'effondrent en flaque à leur mort.
- **Terrain** : aucun.
- **Jeu** : harcèlement autonome pendant que tu te concentres ailleurs.

#### Fondu dans l'ombre — Rare · 120 mana · 60 s
- **Mécanique** : tu te fonds dans une de tes 4 ombres placées en miroir autour de toi : tu deviens invisible, et chaque clic droit te fait ressurgir à la place d'une ombre en frappant.
- **Chiffres** : 4 ombres · 10 s · invisible tant que tu ne frappes pas · ressurgir = 120 dégâts dans le dos de l'ennemi le plus proche de l'ombre choisie · chaque ombre ne sert qu'une fois.
- **Visuel / son** : ombres immobiles qui ondulent, tu jaillis d'une flaque noire avec une gerbe de fumée.
- **Terrain** : aucun.
- **Jeu** : assassinat : quatre angles d'attaque imprévisibles.

#### Légion d'ombres — Epic · 300 mana · 120 s
- **Mécanique** : dix doppelgängers d'ombre t'encerclent et reproduisent tes attaques en miroir, chacun vers l'ennemi en face de lui ; quand l'un est détruit, il explose en ténèbres.
- **Chiffres** : 10 ombres · 12 s · coups copiés 15 % chacun · 1 PV chacune · explosion à la mort : 30 dégâts + Corruption 4 s en zone de 2 blocs.
- **Visuel / son** : cercle d'ombres fumantes de ton personnage, explosion de fumée noire et de violet à chaque destruction. (Remplace l'idée « Double maléfique ».)
- **Terrain** : aucun.
- **Jeu** : punit l'ennemi qui frappe les ombres ; énorme dégâts si on les ignore.

#### Doppelgänger (ancienne piste, remplacée par `Rune_Doppelganger` Epic) — Unique · 600 mana · 180 s
- **Mécanique** : ton double d'ombre parfait : il combat seul, utilise tes runes équipées à demi-puissance, et sur clic droit tu échanges de corps avec lui (tu prends sa place, il prend la tienne avec tes PV actuels).
- **Chiffres** : 1 ombre · 20 s · 50 % de tes dégâts et de l'effet de tes runes · 50 % de tes PV · à sa mort, tu récupères 20 % de tes PV.
- **Visuel / son** : copie noire de ton personnage aux yeux violets, traînée de fumée, inversion de couleur à chaque échange de corps.
- **Terrain** : aucun.
- **Jeu** : un deuxième toi : double la pression ; à vérifier : faire lancer une rune par une entité non joueur.

#### Nuit des doubles — Legendaire · 1 200 mana · 300 s
- **Mécanique** : pendant 20 s, chaque ennemi tué par toi ou par une de tes ombres se relève en ombre alliée qui reproduit tes coups en miroir.
- **Chiffres** : 20 s · jusqu'à 8 ombres · chaque ombre relevée : 25 % de tes dégâts par coup, dure jusqu'à la fin du sort · à la fin, toutes explosent (50 dégâts chacune en zone de 2).
- **Visuel / son** : les ennemis tombés se dissolvent en fumée puis se redressent sous ta silhouette, l'armée d'ombres grossit à vue d'œil.
- **Terrain** : aucun.
- **Jeu** : boule de neige en combat de groupe ; inefficace contre un boss seul.

## Feu
_Signature du type : brûlure, propagation, explosions, lave (voir `categories.md`)._

#### Flammèche — Common · 25 mana · 15 s
- **Mécanique** : projectile de feu rapide qui enflamme la cible touchée.
- **Chiffres** : 60 dégâts + Brûlure 3 s (10/s), portée 25.
- **Visuel / son** : boule de flammes avec traînée de fumée, gerbe d'étincelles à l'impact (`Impact_Fire`), crépitement.
- **Terrain** : aucun.
- **Jeu** : dégâts sur la durée ; contré par l'esquive latérale.

#### Souffle ardent — Common · 25 mana · 15 s
- **Mécanique** : souffle de flammes en cône court devant le lanceur.
- **Chiffres** : cône de 5 blocs, 35 dégâts + Brûlure 2 s à chaque cible.
- **Visuel / son** : jet de flammes (`Dragon_Breath` réduit), chaleur ondulante, rugissement de brasier.
- **Terrain** : herbe brûlée dans le cône (restaurée).
- **Jeu** : contre l'encerclement ; inutile à distance.

#### Paume brûlante — Common · 25 mana · 15 s
- **Mécanique** : frappe de mêlée qui embrase et repousse.
- **Chiffres** : 40 dégâts + Embrasement 2 s, recul de 4 blocs.
- **Visuel / son** : main enflammée, explosion de braises au contact.
- **Terrain** : aucun.
- **Jeu** : dégagement en mêlée.

#### Braise rebondissante — Common · 25 mana · 15 s
- **Mécanique** : projectile qui rebondit 3 fois au sol en rebonds décroissants avant d'exploser.
- **Chiffres** : 35 dégâts par rebond touché + Brûlure 2 s (`Elda_Brulure`), portée 20.
- **Visuel / son** : boule de braise avec traînée de fumée, gerbe d'étincelles à chaque rebond (`Impact_Fire`), son de crépitement.
- **Terrain** : herbe brûlée aux points de rebond (`Soil_Dirt_Burnt`, restauré).
- **Jeu** : se joue dans les couloirs et contre les ennemis à couvert.

#### Sillage ardent — Common · 25 mana · 15 s
- **Mécanique** : ruée de 5 blocs qui laisse une traînée de flammes au sol.
- **Chiffres** : traînée 3 s, 15 dégâts/s aux ennemis qui la traversent.
- **Visuel / son** : flammes basses le long du sol, souffle de chaleur à l'arrivée.
- **Terrain** : aucun (flammes = particules).
- **Jeu** : fuite ou poursuite ; inutile à l'arrêt.

#### Mine de magma — Uncommon · 60 mana · 30 s
- **Mécanique** : pose au sol une mine de lave camouflée (2 max), qui explose au contact d'un ennemi.
- **Chiffres** : 90 dégâts rayon 3 + Embrasement 3 s ; s'arme en 1 s ; dure 30 s.
- **Visuel / son** : bloc de roche fissurée qui rougeoie faiblement ; explosion avec projection de braises.
- **Terrain** : petite cuvette de roche volcanique à l'explosion (restaurée).
- **Jeu** : contrôle de zone ; contrée par l'observation.

#### Torche vivante — Uncommon · 60 mana · 30 s
- **Mécanique** : buff 8 s : les attaques de mêlée enflamment, aura de chaleur.
- **Chiffres** : +25 dégâts de feu par coup, 5 dégâts/s aux ennemis adjacents.
- **Visuel / son** : flammes sur les mains et le dos, lumière orangée portée par le joueur.
- **Terrain** : aucun.
- **Jeu** : combo avec les classes au corps à corps.

#### Mur de flammes — Rare · 120 mana · 60 s
- **Mécanique** : dresse un mur de feu de 9×3 blocs perpendiculaire au regard.
- **Chiffres** : 6 s, 40 dégâts/s à la traversée, applique Brûlure ; bloque la vision.
- **Visuel / son** : rideau de flammes (`Fire_Tornado_Flames`), fumée noire au sommet, grondement continu.
- **Terrain** : ligne de sol brûlé (restaurée).
- **Jeu** : couper une poursuite, protéger un allié.

#### Pluie de comètes — Epic · 300 mana · 120 s
- **Mécanique** : 6 météores tombent au curseur sur 3 s, à des points aléatoires d'un rayon de 8.
- **Chiffres** : 150 dégâts par impact (rayon 3) + feu au sol 4 s.
- **Visuel / son** : météores avec traînée de feu et de fumée, ombre au sol avant impact, explosion et onde de chaleur.
- **Terrain** : 6 petits cratères de 2 blocs en basalte fumant (restaurés).
- **Jeu** : annoncée par les ombres : on peut esquiver.

#### Phénix renaissant — Unique · 600 mana · 180 s
- **Mécanique** : buff de 60 s : si le lanceur meurt pendant ce temps, il renaît dans une explosion de feu.
- **Chiffres** : renaissance à 40 % PV, explosion 300 dégâts rayon 6 + projection ; consommé à l'usage.
- **Visuel / son** : ailes de feu spectrales dans le dos pendant le buff ; renaissance en colonne de flammes avec cri de phénix.
- **Terrain** : cercle de sol brûlé à la renaissance.
- **Jeu** : contré par le burst ou le contrôle avant la mort.

#### Cœur de volcan — Legendaire · 1 200 mana · 300 s
- **Mécanique** : fait surgir un volcan de 8 blocs de haut au curseur, en éruption pendant 12 s.
- **Chiffres** : bombes de lave aléatoires rayon 15 (120 chacune), coulée de lave descendante, fumée qui aveugle.
- **Visuel / son** : cône de basalte qui s'élève en tremblant, cratère rougeoyant, panache de fumée, bombes avec traînées.
- **Terrain** : volcan + coulée (blocs `Rock_Volcanic_Cracked_Lava`), restaurés après 3 min.
- **Jeu** : zone interdite pendant 12 s ; le lanceur doit se placer.

## Glace
_Signature du type : ralentir → geler → briser, murs de glace (voir `categories.md`)._

#### Éclat de givre — Common · 25 mana · 15 s
- **Mécanique** : projectile de glace qui ralentit.
- **Chiffres** : 55 dégâts, Givre 2 s (−30 % de vitesse), portée 25.
- **Visuel / son** : éclat de cristal bleu, traînée de buée, bris de glace à l'impact.
- **Terrain** : aucun.
- **Jeu** : prépare les combos de gel.

#### Flaque gelée — Common · 25 mana · 15 s
- **Mécanique** : zone de glace au sol qui ralentit.
- **Chiffres** : zone de 3 blocs, 4 s, −35 % de vitesse aux ennemis qui la traversent.
- **Visuel / son** : plaque de givre brillante, vapeur froide.
- **Terrain** : sol givré (restauré).
- **Jeu** : contrôle de passage.

#### Armure de givre — Common · 25 mana · 15 s
- **Mécanique** : bouclier de glace qui punit les attaquants au corps à corps.
- **Chiffres** : 3 s, absorbe 50 dégâts ; ralentit de 40 % pendant 2 s ceux qui frappent le lanceur.
- **Visuel / son** : plaques de glace translucides sur le corps, craquements.
- **Terrain** : aucun.
- **Jeu** : contre les assaillants de mêlée.

#### Pique de givre — Common · 25 mana · 15 s
- **Mécanique** : une pique de glace jaillit du sol sous l'ennemi visé.
- **Chiffres** : 50 dégâts, soulève de 2 blocs, portée 15.
- **Visuel / son** : pique de glace qui perce le sol puis se brise en éclats.
- **Terrain** : aucun (la pique se brise).
- **Jeu** : interrompt une charge.

#### Glissade — Common · 25 mana · 15 s
- **Mécanique** : trace un couloir de glace de 8 blocs devant soi pendant 5 s.
- **Chiffres** : +40 % de vitesse pour soi sur la glace ; les ennemis y glissent (−30 % de contrôle, inertie).
- **Visuel / son** : sol gelé brillant, givre qui s'en évapore.
- **Terrain** : couche de glace sur 8×2 blocs (restaurée).
- **Jeu** : mobilité ou piège.

#### Prison de glace — Uncommon · 60 mana · 30 s
- **Mécanique** : enferme une cible dans un bloc de glace.
- **Chiffres** : 3 s d'immobilisation, −50 % de dégâts subis pendant ce temps, éclate à la fin (60 dégâts).
- **Visuel / son** : cube de glace translucide qui craque, éclats à la fin.
- **Terrain** : bloc de glace temporaire autour de la cible (retiré).
- **Jeu** : sauver un allié ou neutraliser un ennemi.

#### Souffle hivernal — Uncommon · 60 mana · 30 s
- **Mécanique** : cône de 7 blocs de froid mordant.
- **Chiffres** : 30 dégâts, Givre 4 s (−30 % vitesse), gèle les surfaces d'eau touchées.
- **Visuel / son** : rafale bleutée, flocons et buée.
- **Terrain** : eau → glace dans le cône (restaurée).
- **Jeu** : prépare les combos « gel ».

#### Lance de cristal — Rare · 120 mana · 60 s
- **Mécanique** : lance de glace perçante qui traverse jusqu'à 3 ennemis.
- **Chiffres** : 120 dégâts, ×1,5 sur les cibles gelées ou givrées (180).
- **Visuel / son** : lance de cristal bleu avec traînée de givre, éclats au contact.
- **Terrain** : aucun.
- **Jeu** : récompense les combos de gel.

#### Blizzard — Epic · 300 mana · 120 s
- **Mécanique** : tempête de neige de 10 blocs au curseur pendant 8 s.
- **Chiffres** : 20 dégâts/s, ralentissement croissant ; 6 s cumulées dedans = gelé 2 s.
- **Visuel / son** : neige à l'horizontale, brouillard blanc, vent qui siffle.
- **Terrain** : couche de neige au sol (restaurée).
- **Jeu** : fuir tôt ou être gelé.

#### Sculpteur de glace — Unique · 600 mana · 180 s
- **Mécanique** : pendant 15 s : clic gauche dresse un mur ou une rampe de glace là où l'on vise (6 max), clic droit fait exploser toutes les structures.
- **Chiffres** : structures de 4×3 blocs ; explosion 80 dégâts rayon 3 par structure.
- **Visuel / son** : glace qui pousse en cristaux, explosion en pluie d'éclats.
- **Terrain** : blocs de glace temporaires (retirés à l'explosion ou à la fin).
- **Jeu** : créativité tactique : murs, ponts, pièges.

#### Ère glaciaire — Legendaire · 1 200 mana · 300 s
- **Mécanique** : dans un rayon de 18 autour du lanceur, tout gèle pendant 10 s.
- **Chiffres** : eau → glace, sol → neige ; ennemis gelés 4 s puis Hypothermie (−40 % vitesse 6 s) ; stalactites qui tombent du ciel (90 chacune).
- **Visuel / son** : ciel blanc, givre qui monte le long des blocs, craquements.
- **Terrain** : zone gelée et enneigée (restaurée).
- **Jeu** : ouverture de combat ; distinct de Niflheim (porte et zone d'hiver) : ici, gel instantané autour de soi.

## Terre
_Signature du type : murs, pointes, séismes, armure de pierre (voir `categories.md`)._

#### Jet de pierre — Common · 25 mana · 15 s
- **Mécanique** : pierre lancée en cloche.
- **Chiffres** : 70 dégâts, étourdissement 0,3 s, portée 20.
- **Visuel / son** : pierre arrachée au sol qui part en arc, gerbe de terre à l'impact.
- **Terrain** : petit trou d'arrachement (restauré).
- **Jeu** : dégâts lourds lents.

#### Piétinement — Common · 25 mana · 15 s
- **Mécanique** : frappe au sol autour du lanceur.
- **Chiffres** : rayon 4, 35 dégâts, léger recul.
- **Visuel / son** : onde de poussière circulaire, grondement.
- **Terrain** : aucun.
- **Jeu** : dégagement de zone.

#### Peau de pierre — Common · 25 mana · 15 s
- **Mécanique** : durcit la peau du lanceur.
- **Chiffres** : −20 % de dégâts subis 4 s, −10 % de vitesse.
- **Visuel / son** : teinte de pierre sur la peau, poussière qui s'en détache.
- **Terrain** : aucun.
- **Jeu** : défense de base.

#### Mur de terre — Common · 25 mana · 15 s
- **Mécanique** : un mur de 3×2 blocs surgit du sol devant soi.
- **Chiffres** : dure 6 s, arrête les projectiles.
- **Visuel / son** : le mur sort du sol en tremblant, poussière.
- **Terrain** : blocs de terre temporaires (retirés).
- **Jeu** : défense de base.

#### Fracture — Common · 25 mana · 15 s
- **Mécanique** : une ligne de 6 pointes de pierre jaillit séquentiellement vers l'avant.
- **Chiffres** : 40 dégâts, soulève de 1 bloc chaque cible touchée.
- **Visuel / son** : pointes qui sortent l'une après l'autre, gravats.
- **Terrain** : pointes temporaires (retirées).
- **Jeu** : portée en ligne, prévisible.

#### Armure de roche — Uncommon · 60 mana · 30 s
- **Mécanique** : des plaques de pierre recouvrent le lanceur.
- **Chiffres** : absorbe 150 dégâts pendant 8 s, −15 % de vitesse ; à la rupture, éclats 30 dégâts rayon 3.
- **Visuel / son** : plaques de pierre sur le corps (modèle attaché), éclats qui volent.
- **Terrain** : aucun.
- **Jeu** : tank court.

#### Sables mouvants — Uncommon · 60 mana · 30 s
- **Mécanique** : transforme une zone de 4 blocs en sables mouvants pendant 6 s.
- **Chiffres** : −60 % de vitesse, les cibles s'enfoncent visuellement d'un demi-bloc.
- **Visuel / son** : sable qui ondule, bulles de poussière.
- **Terrain** : sol → sable (restauré).
- **Jeu** : contrôle de zone.

#### Lancer de rocher — Rare · 120 mana · 60 s
- **Mécanique** : arrache un rocher du sol et le projette en cloche.
- **Chiffres** : 150 dégâts + étourdissement 1 s rayon 2 ; le rocher roule 3 blocs après l'impact.
- **Visuel / son** : le sol se fend, le rocher s'élève puis part ; impact avec gerbe de terre.
- **Terrain** : trou d'arrachement + rocher qui se pose (restaurés).
- **Jeu** : gros dégâts lents, esquivable.

#### Pilier ascendant — Epic · 300 mana · 120 s
- **Mécanique** : un pilier de 10 blocs soulève le lanceur et ses alliés proches, puis s'effondre sur la zone.
- **Chiffres** : position surélevée 5 s ; effondrement 200 dégâts rayon 5.
- **Visuel / son** : pilier qui monte en tremblant, gravats, chute en morceaux.
- **Terrain** : pilier temporaire puis gravats (restaurés).
- **Jeu** : prise de hauteur + finisher.

#### Tremblement tectonique — Unique · 600 mana · 180 s
- **Mécanique** : 10 s de secousses dans un rayon de 14.
- **Chiffres** : une vague de sol ondulante toutes les 2 s : 60 dégâts et étourdissement 1 s aux ennemis au sol.
- **Visuel / son** : sol qui se soulève en vagues (blocs déplacés), poussière, grondement, caméra qui tremble.
- **Terrain** : ondulations (restaurées).
- **Jeu** : sauter au bon moment esquive la vague.

#### Colosse de pierre — Legendaire · 1 200 mana · 300 s
- **Mécanique** : le lanceur se revêt d'une armure-colosse de blocs pendant 20 s.
- **Chiffres** : taille ×2, PV +50 %, coups de poing qui créent de petits cratères (80 rayon 2), vitesse −20 %.
- **Visuel / son** : armure de blocs qui se forme autour du joueur, pas lourds qui font trembler.
- **Terrain** : cratères de coups (restaurés).
- **Jeu** : forme de siège ; vulnérable au kite.

## Air
_Signature du type : projection, mobilité, tornades, vent (voir `categories.md`)._

#### Bourrasque — Common · 25 mana · 15 s · ✔ `Rune_Bourrasque`
- **Mécanique** : souffle de vent en cône qui repousse.
- **Chiffres** : cône de 8 blocs (70°), 40 dégâts, projection.
- **Visuel / son** : rafales `Fujin_Cannon` / `Fujin_Gust`, son de Wind Strike.
- **Terrain** : aucun.
- **Jeu** : se dégager d'un groupe.

#### Pas du vent — Common · 25 mana · 15 s
- **Mécanique** : ruée portée par le vent.
- **Chiffres** : 6 blocs, sans dégâts de chute pendant 3 s.
- **Visuel / son** : spirale d'air sous les pieds, feuilles soulevées.
- **Terrain** : aucun.
- **Jeu** : mobilité pure.

#### Lame d'air — Common · 25 mana · 15 s
- **Mécanique** : lame de vent horizontale qui traverse.
- **Chiffres** : 45 dégâts, traverse 2 cibles, portée 20.
- **Visuel / son** : lame Wind Strike horizontale, sifflement.
- **Terrain** : coupe les herbes hautes (restaurées).
- **Jeu** : dégâts en ligne.

#### Saut porté — Common · 25 mana · 15 s
- **Mécanique** : bond de 8 blocs vers le regard, atterrissage plané.
- **Chiffres** : sans dégâts de chute.
- **Visuel / son** : spirale d'air sous les pieds au départ, plumes de vent.
- **Terrain** : aucun.
- **Jeu** : mobilité verticale.

#### Bulle d'air — Common · 25 mana · 15 s
- **Mécanique** : bulle de vent autour du lanceur.
- **Chiffres** : 3 s, dévie les projectiles.
- **Visuel / son** : sphère d'air translucide qui tourbillonne.
- **Terrain** : aucun.
- **Jeu** : contre les archers.

#### Vrille — Uncommon · 60 mana · 30 s
- **Mécanique** : une petite tornade file en ligne droite et emporte la première cible.
- **Chiffres** : 20 m de portée ; cible soulevée et tournoyante 2 s, puis relâchée (40 dégâts).
- **Visuel / son** : mini-tornade de poussière, sifflement.
- **Terrain** : arrache l'herbe sur son passage (restaurée).
- **Jeu** : isoler une cible.

#### Courant ascendant — Uncommon · 60 mana · 30 s
- **Mécanique** : colonne de vent vertical pendant 4 s.
- **Chiffres** : alliés propulsés et planent ; ennemis projetés à 6 blocs puis chutent (dégâts de chute).
- **Visuel / son** : colonne de feuilles et de poussière qui monte.
- **Terrain** : aucun.
- **Jeu** : combo avec les sorts de chute.

#### Lames de rafale — Rare · 120 mana · 60 s
- **Mécanique** : 5 lames de vent en éventail.
- **Chiffres** : 60 dégâts chacune, coupent herbes, feuillages et plantes.
- **Visuel / son** : lames Wind Strike en éventail, feuilles hachées.
- **Terrain** : feuillages coupés (restaurés).
- **Jeu** : nettoyage de zone végétale.

#### Jet-stream — Epic · 300 mana · 120 s
- **Mécanique** : ouvre un couloir de vent de 30 blocs pendant 8 s dans la direction visée.
- **Chiffres** : alliés dans le couloir ×2 en vitesse, leurs projectiles +50 % de portée ; ennemis repoussés vers la sortie.
- **Visuel / son** : couloir de filets de vent visible (traînées), feuilles emportées.
- **Terrain** : aucun.
- **Jeu** : ouverture de passage, repli.

#### Danse des vents — Unique · 600 mana · 180 s
- **Mécanique** : pendant 10 s, chaque clic droit est une ruée qui laisse une tornade chasseuse de 3 s.
- **Chiffres** : ruée 6 blocs ; tornade 40 dégâts/s qui poursuit l'ennemi le plus proche.
- **Visuel / son** : traînée d'air à chaque ruée, tornades miniatures.
- **Terrain** : aucun.
- **Jeu** : style hit-and-run.

#### Ouragan dirigé — Legendaire · 1 200 mana · 300 s
- **Mécanique** : une tornade géante suit le curseur pendant 15 s.
- **Chiffres** : aspire les ennemis rayon 6 (30 dégâts/s), les éjecte à la fin (200) ; arrache le sol sur son passage.
- **Visuel / son** : tornade de 40 blocs (modèle à traînées comme Fujin, plus petit), débris en orbite.
- **Terrain** : sillon de terre arrachée (restauré).
- **Jeu** : distinct de Fujin : une seule tornade pilotée.

## Foudre
_Signature du type : électrocution, chaînes entre cibles, surcharge, frappes du ciel (voir `categories.md`)._

#### Décharge — Common · 25 mana · 15 s
- **Mécanique** : éclair instantané sur la cible visée.
- **Chiffres** : 65 dégâts, étourdissement 0,3 s, portée 10.
- **Visuel / son** : éclair bleu-blanc, flash, crépitement.
- **Terrain** : aucun.
- **Jeu** : interruption instantanée, courte portée.

#### Arc chaîné — Common · 25 mana · 15 s
- **Mécanique** : éclair qui saute d'ennemi en ennemi.
- **Chiffres** : 3 cibles : 50 → 35 → 25 dégâts, saut de 6 blocs.
- **Visuel / son** : arcs électriques entre cibles.
- **Terrain** : aucun.
- **Jeu** : récompense les groupes serrés.

#### Charge statique — Common · 25 mana · 15 s
- **Mécanique** : charge la prochaine frappe.
- **Chiffres** : dans les 5 s, la prochaine attaque ajoute 60 dégâts de foudre.
- **Visuel / son** : arcs sur l'arme, grésillement continu.
- **Terrain** : aucun.
- **Jeu** : burst de mêlée.

#### Étincelle — Common · 25 mana · 15 s
- **Mécanique** : trait électrique rapide.
- **Chiffres** : 45 dégâts, 20 % de chance d'étourdir 0,5 s, portée 25.
- **Visuel / son** : éclair bleu-blanc, crépitement.
- **Terrain** : aucun.
- **Jeu** : harcèlement rapide.

#### Pas éclair — Common · 25 mana · 15 s
- **Mécanique** : téléportation de 6 blocs dans une traînée électrique.
- **Chiffres** : 25 dégâts aux ennemis traversés.
- **Visuel / son** : ligne d'électricité entre départ et arrivée, flash.
- **Terrain** : aucun.
- **Jeu** : mobilité offensive.

#### Orbe statique — Uncommon · 60 mana · 30 s
- **Mécanique** : orbe électrique qui flotte au curseur pendant 6 s.
- **Chiffres** : électrocute les ennemis à 4 blocs : 15 dégâts/s.
- **Visuel / son** : sphère crépitante, arcs vers les cibles.
- **Terrain** : aucun.
- **Jeu** : zone de déni.

#### Paratonnerre — Uncommon · 60 mana · 30 s
- **Mécanique** : plante une tige métallique ; 5 s plus tard, la foudre la frappe.
- **Chiffres** : 150 dégâts rayon 4, étourdissement 1 s.
- **Visuel / son** : tige qui grésille de plus en plus, éclair du ciel.
- **Terrain** : cratère calciné de 1 bloc (restauré).
- **Jeu** : piège à retardement.

#### Chaîne d'éclairs — Rare · 120 mana · 60 s
- **Mécanique** : éclair qui rebondit sur 6 cibles.
- **Chiffres** : 100 dégâts, −15 % par rebond, rayon de saut 8.
- **Visuel / son** : arcs électriques entre cibles, flash à chacune.
- **Terrain** : aucun.
- **Jeu** : efficace contre les groupes serrés.

#### Surcharge — Epic · 300 mana · 120 s
- **Mécanique** : marque 5 ennemis ; après 4 s, ils explosent ensemble en arc électrique.
- **Chiffres** : 250 dégâts chacun + arcs entre marqués (50 par lien).
- **Visuel / son** : marque électrique au-dessus des têtes, compte à rebours, explosion reliée.
- **Terrain** : aucun.
- **Jeu** : se disperser contre la marque.

#### Avatar de l'orage — Unique · 600 mana · 180 s
- **Mécanique** : pendant 15 s, le lanceur devient électricité : chaque clic est un éclair-dash de 10 blocs.
- **Chiffres** : 120 dégâts aux ennemis traversés ; immunité aux dégâts pendant le dash.
- **Visuel / son** : corps lumineux, arcs autour, traînée d'éclair.
- **Terrain** : aucun.
- **Jeu** : ultime mobile.

#### Supercellule — Legendaire · 1 200 mana · 300 s
- **Mécanique** : un orage suit le curseur pendant 20 s.
- **Chiffres** : 1 éclair par seconde sur l'ennemi le plus proche du centre (180 dégâts), pluie et vent.
- **Visuel / son** : nuage d'orage (traînées comme le plafond de Fujin), éclairs `Taranis_Bolt`, tonnerre.
- **Terrain** : points d'impact calcinés (restaurés).
- **Jeu** : distinct de Taranis : orage mobile ciblé.

## Ténèbres
_Signature du type : malédictions, drain, ombres, peur, invisibilité (voir `categories.md`)._

#### Trait d'ombre — Common · 25 mana · 15 s
- **Mécanique** : projectile d'ombre qui vole la vie.
- **Chiffres** : 50 dégâts, soigne le lanceur de 25 % des dégâts.
- **Visuel / son** : trait noir à volutes violettes, âme aspirée vers le lanceur.
- **Terrain** : aucun.
- **Jeu** : soutien personnel.

#### Voile de fumée — Common · 25 mana · 15 s
- **Mécanique** : nuage noir qui désoriente.
- **Chiffres** : rayon 3, les ennemis dedans perdent leur cible pendant 2 s.
- **Visuel / son** : nuage de fumée noire épaisse.
- **Terrain** : aucun.
- **Jeu** : fuite ou repositionnement.

#### Marque funeste — Common · 25 mana · 15 s
- **Mécanique** : marque une cible.
- **Chiffres** : +15 % de dégâts subis pendant 6 s.
- **Visuel / son** : sceau violet au-dessus de la tête de la cible.
- **Terrain** : aucun.
- **Jeu** : focus d'équipe.

#### Griffe d'ombre — Common · 25 mana · 15 s
- **Mécanique** : une griffe d'ombre frappe à 5 blocs.
- **Chiffres** : 40 dégâts + vole 10 mana.
- **Visuel / son** : griffes noires qui surgissent du sol.
- **Terrain** : aucun.
- **Jeu** : gestion de mana.

#### Pas dans l'ombre — Common · 25 mana · 15 s
- **Mécanique** : invisibilité courte.
- **Chiffres** : 3 s, brisée à l'attaque ; première attaque +30 % de dégâts.
- **Visuel / son** : le corps se dissout en fumée noire.
- **Terrain** : aucun.
- **Jeu** : embuscade.

#### Chaînes noires — Uncommon · 60 mana · 30 s
- **Mécanique** : lie 2 ennemis proches l'un de l'autre.
- **Chiffres** : 6 s : les dégâts subis par l'un sont partagés à 50 % avec l'autre ; s'éloigner de plus de 8 blocs brise le lien (30 dégâts chacun).
- **Visuel / son** : chaîne d'ombre entre les deux (beam).
- **Terrain** : aucun.
- **Jeu** : combo avec les sorts de zone.

#### Malédiction de faiblesse — Uncommon · 60 mana · 30 s
- **Mécanique** : maudit une cible.
- **Chiffres** : −25 % de dégâts infligés pendant 8 s.
- **Visuel / son** : fumée violette qui suinte de la cible (`Elda_Faiblesse`).
- **Terrain** : aucun.
- **Jeu** : contre les gros frappeurs.

#### Puits d'âmes — Rare · 120 mana · 60 s
- **Mécanique** : zone de 5 blocs qui draine la vie pendant 6 s.
- **Chiffres** : 20 dégâts/s aux ennemis ; 50 % soignent le lanceur.
- **Visuel / son** : âmes spectrales aspirées vers le centre.
- **Terrain** : sol noirci (restauré).
- **Jeu** : soutien offensif.

#### Éclipse — Unique · 600 mana · 180 s
- **Mécanique** : plonge une zone de 15 blocs dans la nuit pendant 12 s.
- **Chiffres** : ennemis : vision réduite (écran assombri), terreur 1 s toutes les 4 s ; alliés : invisibles hors combat.
- **Visuel / son** : obscurité et brume au sol (pas de dôme, différent de Nyx), étoiles qui disparaissent.
- **Terrain** : aucun.
- **Jeu** : domination de zone sans dégâts directs.

#### Faucheur — Legendaire · 1 200 mana · 300 s
- **Mécanique** : une faux spectrale géante tournoie autour du lanceur pendant 10 s.
- **Chiffres** : 90 dégâts par passage (2 passages/s) rayon 5 ; exécute les ennemis sous 15 % PV.
- **Visuel / son** : faux fantomatique (modèle) avec traînée violette, gémissements.
- **Terrain** : herbe fauchée en cercle (restaurée).
- **Jeu** : ultime de mêlée de zone.

## Lumière
_Signature du type : soin, purification, boucliers, dégâts sacrés (voir `categories.md`)._

#### Rayon sacré — Common · 25 mana · 15 s
- **Mécanique** : rayon de lumière court.
- **Chiffres** : 8 blocs, 55 dégâts, ×2 contre les morts-vivants.
- **Visuel / son** : rayon doré, éclat au contact.
- **Terrain** : aucun.
- **Jeu** : anti-undead de base.

#### Soin mineur — Common · 25 mana · 15 s
- **Mécanique** : soin sur soi ou un allié visé.
- **Chiffres** : 15 % des PV max.
- **Visuel / son** : pluie de pétales lumineux, carillon.
- **Terrain** : aucun.
- **Jeu** : soutien de base.

#### Éclat aveuglant — Common · 25 mana · 15 s
- **Mécanique** : flash autour du lanceur.
- **Chiffres** : rayon 4, aveugle 1 s (`Apollo_Aveugle`).
- **Visuel / son** : flash blanc intense.
- **Terrain** : aucun.
- **Jeu** : interruption de groupe.

#### Halo protecteur — Common · 25 mana · 15 s
- **Mécanique** : bouclier sur un allié visé ou soi.
- **Chiffres** : absorbe 60 dégâts pendant 5 s.
- **Visuel / son** : anneau doré au-dessus de la tête, bulle lumineuse.
- **Terrain** : aucun.
- **Jeu** : soutien de base.

#### Flèche sacrée — Common · 25 mana · 15 s
- **Mécanique** : projectile de lumière.
- **Chiffres** : soigne l'allié touché (60) ou blesse l'ennemi (50).
- **Visuel / son** : flèche dorée, éclat au contact.
- **Terrain** : aucun.
- **Jeu** : soin à distance.

#### Purification — Uncommon · 60 mana · 30 s
- **Mécanique** : retire les malus du lanceur et des alliés à 6 blocs.
- **Chiffres** : retire tous les débuffs ; immunité aux nouveaux débuffs 2 s.
- **Visuel / son** : onde de lumière blanche, cloche.
- **Terrain** : aucun.
- **Jeu** : contre les malédictions.

#### Phare — Uncommon · 60 mana · 30 s
- **Mécanique** : pose une lanterne de lumière pendant 15 s.
- **Chiffres** : révèle les invisibles rayon 10, régénère 10 PV/s aux alliés rayon 6.
- **Visuel / son** : lanterne flottante (modèle + lumière), faisceau rotatif.
- **Terrain** : aucun.
- **Jeu** : point de ralliement.

#### Lance de lumière — Rare · 120 mana · 60 s
- **Mécanique** : rayon perçant de 20 blocs.
- **Chiffres** : 150 dégâts, ×2 contre les morts-vivants.
- **Visuel / son** : rayon doré éblouissant, flash.
- **Terrain** : aucun.
- **Jeu** : anti-undead.

#### Sanctuaire mineur — Epic · 300 mana · 120 s
- **Mécanique** : dôme de lumière de 6 blocs pendant 8 s.
- **Chiffres** : alliés dedans : immunité aux contrôles, −40 % de dégâts subis.
- **Visuel / son** : dôme translucide doré, pétales lumineux.
- **Terrain** : aucun.
- **Jeu** : défense d'équipe (version réduite d'Apollon).

#### Résurrection — Unique · 600 mana · 180 s
- **Mécanique** : ranime un allié mort depuis moins de 30 s.
- **Chiffres** : revient avec 50 % PV, 2 s d'invulnérabilité.
- **Visuel / son** : colonne de lumière sur le corps, ailes dorées.
- **Terrain** : aucun.
- **Jeu** : **à vérifier** : API de réanimation d'un joueur mort.

#### Jugement céleste — Legendaire · 1 200 mana · 300 s
- **Mécanique** : une colonne de lumière suit l'ennemi visé pendant 8 s.
- **Chiffres** : 80 dégâts/s à la cible ; soigne de 20 PV/s les alliés qui la traversent.
- **Visuel / son** : colonne dorée du ciel au sol, runes sacrées au sol.
- **Terrain** : sol doré (restauré).
- **Jeu** : focus d'un boss.

## Temps
_Signature du type : accélérer, ralentir, figer, rembobiner (voir `categories.md`)._

#### Pas pressé — Common · 25 mana · 15 s
- **Mécanique** : accélère le lanceur.
- **Chiffres** : +30 % de vitesse 4 s.
- **Visuel / son** : sable doré qui tourbillonne aux pieds.
- **Terrain** : aucun.
- **Jeu** : mobilité.

#### Retard — Common · 25 mana · 15 s
- **Mécanique** : ralentit le temps d'une cible.
- **Chiffres** : −40 % de vitesse pendant 2,5 s.
- **Visuel / son** : cadran translucide sur la cible, tic-tac lent.
- **Terrain** : aucun.
- **Jeu** : contrôle simple.

#### Écho — Common · 25 mana · 15 s
- **Mécanique** : retour à la position d'il y a 3 s.
- **Chiffres** : téléportation à la position mémorisée 3 s plus tôt.
- **Visuel / son** : rémanence (`Chronos_Ghost`) qui attend au point d'écho, son de rembobinage.
- **Terrain** : aucun.
- **Jeu** : esquive d'un engagement.

#### Accélération locale — Common · 25 mana · 15 s
- **Mécanique** : accélère les gestes du lanceur.
- **Chiffres** : +50 % de vitesse d'attaque pendant 4 s.
- **Visuel / son** : aiguilles d'horloge dorées autour des mains.
- **Terrain** : aucun.
- **Jeu** : burst de mêlée.

#### Dégâts différés — Common · 25 mana · 15 s
- **Mécanique** : le lanceur repousse dans le futur les coups qu'il reçoit.
- **Chiffres** : pendant 3 s, les dégâts subis ne sont pas appliqués ; ils tombent d'un coup 3 s plus tard, réduits de 25 %.
- **Visuel / son** : aiguilles d'horloge figées autour du joueur, compteur doré qui s'emplit, coup de gong à l'échéance.
- **Terrain** : aucun.
- **Jeu** : survivre à un burst en se soignant avant l'échéance ; à vérifier : interception des dégâts (`DivineDamageSystem`).

#### Boucle — Uncommon · 60 mana · 30 s
- **Mécanique** : marque l'instant présent ; 5 s plus tard, le lanceur y revient.
- **Chiffres** : retour à la position et aux PV de la marque (si plus élevés).
- **Visuel / son** : silhouette fantôme à la marque (rémanence `Chronos_Ghost`), son de rembobinage.
- **Terrain** : aucun.
- **Jeu** : prise de risque calculée.

#### Hâte de groupe — Uncommon · 60 mana · 30 s
- **Mécanique** : accélère les alliés proches.
- **Chiffres** : +25 % de vitesse 6 s, rayon 8.
- **Visuel / son** : sable doré qui tourbillonne aux pieds.
- **Terrain** : aucun.
- **Jeu** : engagement ou fuite d'équipe.

#### Stase — Rare · 120 mana · 60 s
- **Mécanique** : fige une cible dans le temps.
- **Chiffres** : 3 s : immobile et invulnérable (ennemi ou allié).
- **Visuel / son** : bulle verte (`Chronos_Fige`), tic-tac.
- **Terrain** : aucun.
- **Jeu** : sauver un allié ou retirer un ennemi du combat.

#### Distorsion — Epic · 300 mana · 120 s
- **Mécanique** : zone de 8 blocs où le temps ralentit pendant 6 s.
- **Chiffres** : projectiles ×0,2, ennemis ×0,5 ; alliés normaux.
- **Visuel / son** : ondulation de l'air, particules figées en suspension.
- **Terrain** : aucun.
- **Jeu** : contre les tirs et les groupes.

#### Retour en arrière — Unique · 600 mana · 180 s
- **Mécanique** : annule les 4 dernières secondes de dégâts subis par le lanceur.
- **Chiffres** : rend les PV perdus, retire les débuffs reçus pendant ces 4 s.
- **Visuel / son** : rembobinage visuel (rémanences qui reviennent vers le joueur).
- **Terrain** : aucun.
- **Jeu** : contre le burst.

#### Prophétie — Legendaire · 1 200 mana · 300 s
- **Mécanique** : pendant 10 s, le lanceur voit l'avenir.
- **Chiffres** : les attaques ennemies sont annoncées par des marqueurs ; les 3 premières sont esquivées automatiquement ; +30 % de critique.
- **Visuel / son** : yeux dorés, futur montré par des silhouettes translucides.
- **Terrain** : aucun.
- **Jeu** : défensif et offensif ; distinct du Sablier de Chronos.

## Espace
_Signature du type : téléportation, portails, échanges, bannissement (voir `categories.md`)._

#### Clignement — Common · 25 mana · 15 s
- **Mécanique** : téléportation vers le regard.
- **Chiffres** : 8 blocs, jamais dans un bloc.
- **Visuel / son** : flash et distorsion aux deux points.
- **Terrain** : aucun.
- **Jeu** : mobilité instantanée.

#### Échange — Common · 25 mana · 15 s
- **Mécanique** : échange de place avec un ennemi.
- **Chiffres** : portée 10 blocs.
- **Visuel / son** : deux déchirures reliées.
- **Terrain** : aucun.
- **Jeu** : sauvetage ou piège.

#### Ancre — Common · 25 mana · 15 s
- **Mécanique** : pose une ancre ; relancer ramène à l'ancre.
- **Chiffres** : fenêtre de 6 s pour revenir.
- **Visuel / son** : ancre runique au sol, fil lumineux.
- **Terrain** : aucun.
- **Jeu** : engager puis se replier.

#### Portail de poche — Common · 25 mana · 15 s
- **Mécanique** : ouvre une poche qui avale le prochain projectile reçu et le renvoie.
- **Chiffres** : fenêtre 3 s.
- **Visuel / son** : petit portail violet devant soi.
- **Terrain** : aucun.
- **Jeu** : contre les archers.

#### Pas de côté — Common · 25 mana · 15 s
- **Mécanique** : téléportation latérale de 3 blocs.
- **Chiffres** : esquive instantanée.
- **Visuel / son** : flash et distorsion.
- **Terrain** : aucun.
- **Jeu** : esquive pure.

#### Paire de portails — Uncommon · 60 mana · 30 s
- **Mécanique** : pose un portail A puis un portail B (30 blocs max).
- **Chiffres** : 10 s, tout le monde peut les traverser.
- **Visuel / son** : deux anneaux bleu et orange (traînées).
- **Terrain** : aucun.
- **Jeu** : mobilité d'équipe, pièges.

#### Rappel — Uncommon · 60 mana · 30 s
- **Mécanique** : téléporte l'allié visé à côté de soi.
- **Chiffres** : portée 25.
- **Visuel / son** : rayon de lumière entre les deux.
- **Terrain** : aucun.
- **Jeu** : sauvetage.

#### Bannissement mineur — Rare · 120 mana · 60 s
- **Mécanique** : envoie une cible dans une poche dimensionnelle.
- **Chiffres** : 3 s : la cible disparaît et ne peut rien faire, puis revient au même endroit.
- **Visuel / son** : déchirure qui l'avale puis la recrache.
- **Terrain** : aucun.
- **Jeu** : retirer un ennemi d'un combat.

#### Portail d'artillerie — Epic · 300 mana · 120 s
- **Mécanique** : pendant 10 s, les projectiles du lanceur sortent d'un portail au-dessus de la cible visée.
- **Chiffres** : tirs à la verticale, ignorent les couverts ; +20 % de dégâts.
- **Visuel / son** : portail sous le ciel au-dessus de la cible.
- **Terrain** : aucun.
- **Jeu** : contre les ennemis à couvert.

#### Labyrinthe — Unique · 600 mana · 180 s
- **Mécanique** : piège les ennemis d'une zone de 10 blocs dans un espace replié.
- **Chiffres** : pendant 10 s, téléportation aléatoire dans la zone toutes les 2 s, désorientation.
- **Visuel / son** : zone aux contours ondulants, éclats bleus à chaque saut.
- **Terrain** : aucun.
- **Jeu** : contrôle de groupe sans dégâts.

#### Porte des mondes — Legendaire · 1 200 mana · 300 s
- **Mécanique** : ouvre un portail durable vers un point mémorisé.
- **Chiffres** : 60 s, utilisable par tout le groupe ; un seul point mémorisé (commande ou rune).
- **Visuel / son** : grand portail (modèle), son d'ouverture.
- **Terrain** : aucun.
- **Jeu** : logistique ; distinct du Bifrost (pas de téléportation visée).

## Combat
_Signature du type : combos, frappes chargées, parades, ruées (voir `categories.md`)._

#### Brise-garde — Common · 25 mana · 15 s
- **Mécanique** : frappe qui brise le blocage.
- **Chiffres** : 50 dégâts, désactive le blocage de la cible 2 s.
- **Visuel / son** : impact métallique, étincelles.
- **Terrain** : aucun.
- **Jeu** : contre les défenseurs.

#### Ruée — Common · 25 mana · 15 s
- **Mécanique** : charge en avant.
- **Chiffres** : 6 blocs, 45 dégâts + recul.
- **Visuel / son** : traînée de poussière, impact.
- **Terrain** : aucun.
- **Jeu** : engagement.

#### Riposte — Common · 25 mana · 15 s
- **Mécanique** : posture de contre.
- **Chiffres** : 1 s : le prochain coup subi est réduit de 50 % et renvoie 40 dégâts.
- **Visuel / son** : posture de garde, flash au contre.
- **Terrain** : aucun.
- **Jeu** : duel, lecture de l'adversaire.

#### Coup de bouclier — Common · 25 mana · 15 s
- **Mécanique** : coup qui étourdit.
- **Chiffres** : 0,8 s d'étourdissement, 30 dégâts.
- **Visuel / son** : impact sourd, étoiles.
- **Terrain** : aucun.
- **Jeu** : interruption.

#### Taillade tournoyante — Common · 25 mana · 15 s
- **Mécanique** : attaque circulaire.
- **Chiffres** : 40 dégâts à 360°, rayon 3.
- **Visuel / son** : animation `Battleaxe/Whirlwind`, traînée d'arme.
- **Terrain** : herbe coupée (restaurée).
- **Jeu** : contre l'encerclement.

#### Exécution — Uncommon · 60 mana · 30 s
- **Mécanique** : coup puissant contre une cible affaiblie.
- **Chiffres** : +100 % de dégâts sur une cible sous 30 % PV.
- **Visuel / son** : ralenti visuel, impact lourd.
- **Terrain** : aucun.
- **Jeu** : finisher.

#### Cri de défi — Uncommon · 60 mana · 30 s
- **Mécanique** : provoque les ennemis proches.
- **Chiffres** : aggro des monstres rayon 8, −15 % de dégâts subis 5 s.
- **Visuel / son** : onde sonore, cri.
- **Terrain** : aucun.
- **Jeu** : rôle de tank.

#### Bond fracassant — Rare · 120 mana · 60 s
- **Mécanique** : saut sur la cible visée.
- **Chiffres** : 150 dégâts rayon 3, étourdissement 0,5 s.
- **Visuel / son** : animation `Battleaxe/DownstrikeLeap`, cratère de poussière.
- **Terrain** : petit cratère (restauré).
- **Jeu** : engagement.

#### Danse des lames — Epic · 300 mana · 120 s
- **Mécanique** : 6 coups rapides automatiques sur les ennemis proches.
- **Chiffres** : 6 × 60 dégâts, le lanceur se déplace de cible en cible.
- **Visuel / son** : images rémanentes du lanceur, traînées d'arme.
- **Terrain** : aucun.
- **Jeu** : nettoyage de groupe.

#### Maître d'armes — Unique · 600 mana · 180 s
- **Mécanique** : pendant 15 s, chaque coup ajoute une charge.
- **Chiffres** : à 10 charges, coup final de 600 dégâts en zone de 4.
- **Visuel / son** : aura d'armes spectrales qui s'accumulent autour.
- **Terrain** : cratère au coup final (restauré).
- **Jeu** : récompense la régularité.

#### Mille coupes — Legendaire · 1 200 mana · 300 s
- **Mécanique** : le lanceur devient flou et frappe toutes les cibles à 10 blocs pendant 4 s.
- **Chiffres** : 40 coups de 30 dégâts répartis, invulnérable pendant l'action.
- **Visuel / son** : silhouette floue, traînées croisées, éclats.
- **Terrain** : herbe et feuillages hachés (restaurés).
- **Jeu** : ultime de duel de groupe.

## Spatial
_Signature du type : gravité, attraction, répulsion, lévitation, télékinésie (voir `categories.md`)._

#### Onde de répulsion — Common · 25 mana · 15 s
- **Mécanique** : onde de gravité qui repousse, sans vent.
- **Chiffres** : rayon 4, 30 dégâts, recul de 5 blocs.
- **Visuel / son** : anneau violet qui s'étend, distorsion.
- **Terrain** : aucun.
- **Jeu** : dégagement (différent de Bourrasque : pas de vent).

#### Attraction — Common · 25 mana · 15 s
- **Mécanique** : attire une cible vers le lanceur.
- **Chiffres** : portée 12, la cible est tirée jusqu'à 2 blocs.
- **Visuel / son** : fil de gravité violet, traînée.
- **Terrain** : aucun.
- **Jeu** : isoler une cible.

#### Lévitation — Common · 25 mana · 15 s
- **Mécanique** : soulève un ennemi.
- **Chiffres** : 3 blocs pendant 1 s, puis chute.
- **Visuel / son** : halo violet sous la cible, débris qui montent.
- **Terrain** : aucun.
- **Jeu** : combo de chute.

#### Alourdir — Common · 25 mana · 15 s
- **Mécanique** : alourdit une cible.
- **Chiffres** : −40 % de saut et de vitesse 4 s.
- **Visuel / son** : marque violette sous les pieds, pas lourds.
- **Terrain** : aucun.
- **Jeu** : contre la mobilité.

#### Allègement — Common · 25 mana · 15 s
- **Mécanique** : allège le lanceur.
- **Chiffres** : saut ×2 et chute lente 6 s.
- **Visuel / son** : particules violettes qui flottent vers le haut.
- **Terrain** : aucun.
- **Jeu** : exploration et mobilité.

#### Orbite — Uncommon · 60 mana · 30 s
- **Mécanique** : 3 pierres tournent autour du lanceur.
- **Chiffres** : 10 s, 25 dégâts au contact chacune, absorbent 1 projectile chacune.
- **Visuel / son** : pierres arrachées au sol qui gravitent.
- **Terrain** : 3 trous d'arrachement (restaurés).
- **Jeu** : défense active.

#### Inversion — Uncommon · 60 mana · 30 s
- **Mécanique** : inverse la gravité d'une cible.
- **Chiffres** : elle tombe vers le haut sur 6 blocs pendant 2 s, puis retombe.
- **Visuel / son** : halo inversé, débris qui montent avec elle.
- **Terrain** : aucun.
- **Jeu** : combo de chute.

#### Aimant — Rare · 120 mana · 60 s
- **Mécanique** : un point au curseur attire ennemis et blocs légers pendant 3 s, puis relâche.
- **Chiffres** : rayon 8, 30 dégâts au regroupement.
- **Visuel / son** : spirale de particules vers le point, débris qui convergent.
- **Terrain** : plantes et feuillages arrachés (restaurés).
- **Jeu** : regroupe pour un sort de zone.

#### Télékinésie — Epic · 300 mana · 120 s · ✔ `Rune_Telekinesie`
- **Mécanique** : main spectrale : un clic saisit tout le contenu d'une sphère de 5 blocs au curseur, qui suit le curseur ; clic gauche = écraser, clic droit = projeter.
- **Chiffres** : 20 s ; écraser 120 + souffle 40 ; projeter 80, vitesse 55 ; blocs lancés 50.
- **Visuel / son** : main spectrale violette (modèle), fil d'énergie, blocs qui tournoient, impact avec cuvette et éclats.
- **Terrain** : blocs saisis reposés à l'atterrissage, cuvette d'impact (restaurés).
- **Jeu** : manipulation de champ de bataille.

#### Ceinture d'astéroïdes — Epic · 300 mana · 120 s
- **Mécanique** : arrache 12 blocs du sol qui orbitent autour du lanceur ; chaque clic en tire un vers le curseur.
- **Chiffres** : 80 dégâts par bloc tiré, 15 s.
- **Visuel / son** : blocs en orbite (modèle des débris de Fujin), traînée violette au tir.
- **Terrain** : blocs arrachés puis reposés à l'impact (restaurés).
- **Jeu** : artillerie personnelle.

#### Puits Gravitationnel — Unique · 600 mana · 180 s · ✔ `Rune_PuitsGravitationnel`
- **Mécanique** : zone de gravité extrême sous l'ennemi visé : il est cloué au sol.
- **Chiffres** : cratère 7×2, colonne de 50 blocs, immobilisation 10 s, 2 dégâts/s.
- **Visuel / son** : pluie de traits noir-violet (modèle + particules), fissures qui suintent ; pas d'explosion (gravité invisible).
- **Terrain** : cratère en demi-dalles et fissures (restaurés).
- **Jeu** : contrôle dur d'une cible.

#### Lentille gravitationnelle — Unique · 600 mana · 180 s
- **Mécanique** : zone de 8 blocs pendant 8 s qui courbe les trajectoires.
- **Chiffres** : projectiles ennemis renvoyés vers leur tireur ; projectiles alliés accélérés.
- **Visuel / son** : distorsion en anneau (lentille), étoiles déformées.
- **Terrain** : aucun.
- **Jeu** : contre les archers et mages.

#### Autorité Gravitationnelle — Legendaire · 1 200 mana · 300 s · ✔ `Rune_AutoriteGravitationnelle`
- **Mécanique** : zone de 20 blocs autour du lanceur pendant 30 s : chaque ennemi subit un Puits Gravitationnel.
- **Chiffres** : 10 ennemis cloués à la fois, 2 dégâts/s.
- **Visuel / son** : une colonne de gravité par ennemi, fissures ; gravité invisible.
- **Terrain** : un cratère par ennemi (restauré).
- **Jeu** : verrouillage de zone.

#### Marée gravitationnelle — Legendaire · 1 200 mana · 300 s
- **Mécanique** : pendant 15 s, dans un rayon de 25, la gravité alterne toutes les 2 s.
- **Chiffres** : faible : tout flotte (ennemis soulevés de 4 blocs) ; forte : tout est plaqué au sol (60 dégâts).
- **Visuel / son** : débris qui montent et retombent en rythme, colonne de traits violets à chaque phase forte.
- **Terrain** : cuvette qui se creuse un peu plus à chaque phase forte (restaurée).
- **Jeu** : distinct de l'Autorité (pas de cratère par ennemi, rythme de zone).

## Dragonic
_Signature du type : souffle, griffes, ailes, rugissement (voir `categories.md`)._

#### Griffes — Common · 25 mana · 15 s
- **Mécanique** : trois coups de griffe en cône.
- **Chiffres** : cône de 3 blocs, 3 × 20 dégâts.
- **Visuel / son** : griffures spectrales dorées (traînées).
- **Terrain** : aucun.
- **Jeu** : dégâts de mêlée rapides.

#### Crachat de braises — Common · 25 mana · 15 s
- **Mécanique** : petit souffle de braises.
- **Chiffres** : cône de 5 blocs, 30 dégâts + Brûlure légère 2 s.
- **Visuel / son** : braises incandescentes, fumée.
- **Terrain** : herbe roussie (restaurée).
- **Jeu** : harcèlement.

#### Rugissement — Common · 25 mana · 15 s
- **Mécanique** : rugissement qui effraie.
- **Chiffres** : rayon 6, les ennemis fuient 1 s.
- **Visuel / son** : onde sonore, tête de dragon spectrale.
- **Terrain** : aucun.
- **Jeu** : désengagement.

#### Écailles — Common · 25 mana · 15 s
- **Mécanique** : durcit la peau.
- **Chiffres** : −20 % de dégâts subis 5 s.
- **Visuel / son** : écailles dorées qui apparaissent sur la peau (teinte).
- **Terrain** : aucun.
- **Jeu** : défense de base.

#### Battement d'ailes — Common · 25 mana · 15 s
- **Mécanique** : recul en arrière avec un battement d'ailes.
- **Chiffres** : repousse les ennemis proches (rayon 3), saut arrière de 4 blocs.
- **Visuel / son** : ailes spectrales, souffle de poussière.
- **Terrain** : aucun.
- **Jeu** : désengagement.

#### Morsure — Uncommon · 60 mana · 30 s
- **Mécanique** : morsure draconique au corps à corps.
- **Chiffres** : 50 dégâts + saignement 6 s (15/s).
- **Visuel / son** : mâchoire spectrale qui se referme (modèle).
- **Terrain** : aucun.
- **Jeu** : dégâts sur la durée.

#### Œil du dragon — Uncommon · 60 mana · 30 s
- **Mécanique** : vision draconique.
- **Chiffres** : révèle les ennemis rayon 30 pendant 8 s, +15 % de critique.
- **Visuel / son** : yeux reptiliens dorés, contours des ennemis lumineux.
- **Terrain** : aucun.
- **Jeu** : traque.

#### Souffle de feu — Rare · 120 mana · 60 s
- **Mécanique** : souffle canalisé en cône.
- **Chiffres** : cône de 10 blocs, 3 s, 60 dégâts/s ; enflamme l'herbe.
- **Visuel / son** : flammes de dragon (`Dragon_Breath`), rugissement.
- **Terrain** : herbe brûlée (restaurée).
- **Jeu** : contrôle frontal.

#### Ailes de dragon — Epic · 300 mana · 120 s
- **Mécanique** : ailes qui permettent de planer 10 s ; clic : piqué destructeur.
- **Chiffres** : piqué 200 dégâts rayon 4.
- **Visuel / son** : ailes (modèle attaché), traînée au piqué, impact de feu.
- **Terrain** : cratère au piqué (restauré).
- **Jeu** : mobilité aérienne.

#### Rugissement ancestral — Unique · 600 mana · 180 s
- **Mécanique** : rugissement d'un ancien dragon.
- **Chiffres** : terreur 3 s rayon 15 aux ennemis, +20 % de dégâts aux alliés 10 s.
- **Visuel / son** : onde de choc sonore, tête de dragon spectrale.
- **Terrain** : feuillages arrachés par l'onde (restaurés).
- **Jeu** : ouverture d'équipe.

#### Pluie d'écailles — Legendaire · 1 200 mana · 300 s
- **Mécanique** : une nuée d'écailles tranchantes tombe sur une zone de 15 blocs pendant 8 s.
- **Chiffres** : 50 dégâts/s, saignement cumulé, réduit l'armure de 20 %.
- **Visuel / son** : écailles dorées qui tournoient et tombent (traînées), cri de dragon.
- **Terrain** : sol lacéré (restauré).
- **Jeu** : zone d'usure ; distinct de l'Avatar et de Bahamut.
