# Clan Luo — plan de travail (validé le 2026-11-06)

Le Clan Luo est **fermé au monde extérieur** : aucun de ses objets (katanas, kimonos, plaques) ne se fabrique ni ne se trouve en
butin. Tout passe par le clan lui-même : rang, trésor familial, marchand Luo.

Ordre de travail : **1** kits des katanas → **2** plaques d'identification → **3** PNJ Luo et inclinaison → **4** marchand Luo →
**5** kimonos → **6** katana divin.

---

## 1. Kits des katanas Luo — FAIT le 2026-11-08 (non vu en jeu)

Écarts : aucun effet du moteur n'accélère les attaques ; Hien a donc un combo 25 % plus vif et l'**Élan de l'hirondelle** donne +5 % de dégâts par coup enchaîné (5 max) et +15 % de vitesse de déplacement dès 3 coups. Le « +50 % de maîtrise » du Bokken porte sur l'**XP de combat** (il n'existe pas d'XP de maîtrise d'arme).

Base commune (déjà en place) : fourreau en main gauche, dégainé au premier coup, rengainage (clic droit bref ou 2,5 s sans
attaquer), garde au clic droit maintenu, posture iai au maintien de l'attaque, signature Iaigiri.

**Combo Luo commun (remplace le combo d'épée vanilla)** — 4 coups :

| Coup | Geste | Note |
|---|---|---|
| 1 | Coupe horizontale (dégainé si la lame est au fourreau) | |
| 2 | Kesa-giri : diagonale descendante | |
| 3 | Kiri-age : diagonale remontante | |
| 4 | Karatake-wari : coupe verticale avec un pas en avant | coup lourd |

**Kit propre à chaque katana :**

| Katana | Rareté | Combo | Maintien | Mouvement / passif |
|---|---|---|---|---|
| Bokken du Dojo Luo | Commun | plus lent | **Contre parfait** : garde levée dans les 0,3 s avant un coup → dégainé de riposte | maîtrise d'épée +50 % |
| Katana de fer du Clan Luo | Commun | pas en avant au 4e coup | dégainé large | garde −30 % d'endurance |
| Aonami, la Vague Bleue | Rare | chaque coup repousse | **Ressac** : la lame d'air revient, deux touches | pas chassé latéral en fin de combo |
| Tsukikage, Ombre de Lune | Épique | ×1,5 dans le dos, givre | **Éclipse** : invisible 0,8 s, réapparaît derrière la cible et coupe | fondu d'ombre au dégainé |
| **Shirasagi, l'Aigrette Blanche** (nouveau) | **Unique** | le 4e coup éblouit | **Neige Silencieuse** : éventail de trois lames d'air blanches | bond en arrière en fin de combo |
| Hien, l'Hirondelle Écarlate | Unique | +5 % vitesse d'attaque par coup (×5), embrasement | **Vol de l'hirondelle** : ruée traversante, traînée de feu | ruée courte au dégainé |
| Tenryūmaru (relique) | Légendaire | lame d'or à chaque coup | Flash Céleste | signature : les Dix Serments |

Shirasagi : katana blanc — lame d'argent pâle à la trempe blanche, garde d'argent, soie blanche, fourreau de laque blanche
au mon d'argent.

## 2. Plaques d'identification — FAIT le 2026-11-08 (compilé, non vu en jeu)

Fait : `scripts/familles/gen_plaques_luo.py` (6 objets `Eldanior_Plaque_Luo_<Rang>`, un modèle, une texture par rang : Recrue
bois Commun, Noble laque noire et argent Rare, Seigneur laque cramoisie et or Épique, Doyen laque violette et or Unique,
Patriarche or et laque noire Légendaire, Marchand bronze Rare ; marques de rang 1 à 5 sous le mon, pièces trouées pour le
marchand) ; `com.eldanior.system.luo.LuoPlaques` (rang → plaque, `held`, `carries`, `admitted` pour l'étape 3) et
`LuoPlaqueSystem` (toutes les 5 s, comme la clé du trésor ; les admins gardent les plaques qu'ils portent).
Agrément : `/es family agreer <joueur>` et `/es family revoquer <joueur>` (Patriarche, Doyens Luo, admins) ; registre
`FamilyManager.merchants`, sauvegardé dans families.properties (`luo.merchants`) ; liste dans `/es family info luo`.


Une plaque par rang : **Patriarche**, **Doyen**, **Seigneur**, **Noble**, **Recrue** (membre sans titre), et **Marchand agréé
du Comptoir** pour les étrangers.

- **Remise automatique** à chaque changement de rang, l'ancienne est reprise (comme la clé du trésor). Une plaque est
  personnelle : volée ou ramassée par un autre, elle disparaît.
- **Territoire** : porter sa plaque permet d'entrer sur le territoire Luo **sans être attaqué**.
- **Inclinaison** : tenir en main la plaque de Patriarche, de Doyen ou de Seigneur fait **s'incliner les PNJ Luo** sur son
  passage.
- **Marchand agréé** : remise à un étranger par le Patriarche ou un Doyen ; elle l'autorise à entrer et à acheter au Comptoir.

## 3. PNJ du Clan Luo — FAIT le 2026-11-08, revu le même jour (validé au chargement, non vu en jeu)

Révision de l'utilisateur : **pas de types de PNJ propres au clan**. Tout PNJ posé par `/es garde poser <type>` sur une
parcelle de la famille luo s'adapte (`Post.luo`, relevé à chaque apparition ; skin de joueur + kimono) :
garde de ville, garde royal, patrouilleur → Recrue ; chevalier → Sakura ; **assassin** (nouveau type, dagues, cuir de corbeau
hors Luo) → Ombre, déplacement propre à faire plus tard ; **pèlerin** (nouveau type, promeneur qui prie) → Pèlerin ;
villageois, aventurier, chasseur de prime, criminel → kimonos civils (Rapiécé, Comptoir, Pèlerin ; Rōnin et Rizières à
ajouter après l'étape 5). Mage royal, chevalier errant, maître de guilde : inchangés. Noms : « Garde Luo », « Chevalier Luo »,
« Ombre Luo »…, prénoms du clan pour les villageois. Les gardes du territoire attaquent les étrangers sans plaque ; tous
s'inclinent devant les plaques. `guards/LuoNpcs`, `luo/LuoClanSystem`, `scripts/guards/gen_assassin_pelerin.py`,
`scripts/npcs/gen_luo_anims.py` (Inclinaison, InclinaisonProfonde, Priere, Salut, Appel), `/incliner [--profond true]`.
Armes sur le territoire (2026-11-08) : variantes de rôle `<rôle>_Luo` (`scripts/guards/gen_postes_luo.py`, choisies par
`GuardManager.spawnAs` via `LuoNpcs.role`), katana au clair, sans bouclier, CAE du rôle sans parade au bouclier :
katana de fer pour garde de ville, garde royal, patrouilleur, et les guerriers aventuriers / chasseurs / criminels ;
Aonami (rare) pour le chevalier ; Tsukikage pour l'assassin ; bokken pour le **disciple** (nouveau type `disciple` :
épée de bois hors territoire, kimono de recrue chez les Luo, séries de kata à son poste). Archers, assassins et mages
aventuriers gardent leurs armes ; le mage royal aussi. Fourreau comme le joueur : les variantes `_Luo` portent la lame au clair
(barre 0), au fourreau (barre 1), fourreau en main gauche ; `LuoNpcs.sheathTick` rengaine au repos (animation Rengainer,
barre 1, fourreau plein mis dans l'emplacement 0 de la main gauche : changer d'emplacement actif ne tient pas sur un PNJ,
la poignée manquait) et remet la lame au clair et le fourreau vide en combat ; l'aventurier libre est toujours rengainé ;
le disciple garde son bokken au clair pour ses kata. Abandonnés : nobles (éventail).

Recette de PNJ combattant validée (Template_Intelligent + armure en attachements), en kimonos et katanas Luo :
gardes de la Porte (Recrue, Sakura), disciples du dojo (bokken, kata sur mannequins), Ombres (patrouilles de nuit), nobles
(promenade, éventail), pèlerins et moines, marchands du Comptoir.

- Hostiles aux étrangers **sans plaque** ; avertissement à la frontière puis délai de grâce ; admins exemptés ; le Comptoir
  est neutre pour les marchands agréés.
- **Inclinaison** : à moins de 6 blocs d'un porteur de plaque (Patriarche, Doyen, Seigneur) tenue en main, le PNJ s'arrête,
  se tourne et s'incline (profondément devant le Patriarche) ; 60 s avant de recommencer pour la même personne.
- Animations d'ambiance : kata des disciples, salut des gardes, éventail des nobles, seiza à la maison de thé, appel des
  marchands. Commande `/incliner` pour les joueurs.

## 4. Marchand Luo (sur le modèle des aventuriers) — FAIT le 2026-11-08 (compilé, vérifié hors ligne, non vu en jeu)

Fait : type de poste `marchand` (`/es garde poser marchand --type armurerie|tailleur|herboriste|maison_the --statut
apprenti|negociant|maitre`, sinon au hasard ; `Post.shop` sauvegardé), kimono du Comptoir, nom « Négociant Haruki ·
Tailleur », geste d'appel. Rôle interactif `Eldanior_Npc_Marchand_Luo` (copie du maître de guilde) ; `guards/LuoMerchants`
+ `LuoMerchantSystem` ouvrent la boutique (`BarterPage`) selon le client : membre du clan si son rang le permet
(Apprenti : tous ; Négociant : Nobles et plus ; Maître de Guilde : Seigneur, Doyens, Patriarche), marchand agréé →
`Luo_Comptoir` (soie, thé vert, riz), admin → la boutique, étranger → refus. 13 boutiques (`scripts/guards/gen_marchand_luo.py`),
réassort 1 / 2 / 3 jours, prix 40 × niveau^1,3 cuivres (consommables et marchandises : 8 %). Nouveaux consommables
(`scripts/consommables/gen_luo_consommables.py`) : thés vert, au jasmin, de jade, noir de l'Éclipse ; onguents de fleur de
cerisier, du Dragon d'Or. Écarts : pas de plats Luo (la maison de thé vend thés et plats vanilla) ; aucun katana légendaire à
vendre, le Maître armurier vend les katanas Unique à une pièce.

Retours du 2026-11-08 : boutique impossible à ouvrir et nom bugué → `GuardType.fromRole` retirait le suffixe `_Luo` du rôle
`Eldanior_Npc_Marchand_Luo` (corrigé : seulement pour les vraies variantes). Les boutiques Luo vendent aussi des produits du
monde (armes, tissus, potions, pilules de stats, nourriture). Marchand en **Grand Négociant** (`Armor_Kimono_Negociant_*`,
Rare 160 : daikoku-zukin et lunettes d'or, haori aux pièces, boulier, bourse). Nouveau **marchand ambulant** (`ambulant`) :
ouvert à tous, 6 assortiments `Eldanior_Ambulant_1..6` de 9 objets tirés de 836 objets du jeu et du mod. **Bloc de poste de PNJ**
`Eldanior_Poste_PNJ` (invisible, traversable ; admins : poser ou F → fenêtre `Npc/NpcPost.ui` : type, spécialité, statut/rang,
niveau, regard ; casser = retirer ; marque de particules pour les seuls admins ; `guards/NpcPostBlocks`, `NpcPostPage`,
`NpcPostEvents`). Tous les PNJ à poste fixe se remettent face à leur direction au repos.


- **Rangs** (tirés de son poste) : Apprenti (Commun, Rare), Négociant (Épique, Unique), Maître de Guilde (Légendaire,
  une seule pièce en stock).
- **Boutiques** : Armurerie (katanas), Tailleur (kimonos), Herboriste (thés, onguents), Maison de thé (nourriture).
- **Stocks** : emplacements fixes + tirages pondérés (BarterShops `Fixed` et `Pool`), réassort quotidien (Apprenti) à tous
  les 3 jours (Maître).
- **Accès** : les objets du clan aux seuls membres, selon leur rang ; les marchandises neutres (soie, thé, riz) aux seuls
  **marchands agréés**.

## 5. Kimonos

| Tenue | Pour | Qualité |
|---|---|---|
| Robe de Cour (robes superposées, manches traînantes, coiffe haute) | nobles de salon | Épique |
| Haori de la Garde d'Honneur (veste courte au mon, hakama serré) | nobles d'épée | Épique |
| Kariginu du Lettré (col rond, haut chapeau noir, rouleau) | nobles érudits | Épique |
| Grand Négociant du Comptoir (haori aux pièces, bourse, boulier, lunettes) | marchands | Rare |
| Rōnin Vagabond (haori usé, hakama, bandeau, gourde) — **nouveau** | aventuriers | Rare |
| Kimono des Rizières (manches retroussées, tablier, chapeau de riziculteur) — **nouveau** | villageois | Commun |

## 6. Katana divin — Kinryū-Ō, Lame du Dieu-Dragon d'Or

- Divin, niveau 800 ; même mécanique de fourreau ; lame d'or blanc, fourreau d'écailles d'or, garde en dragon enroulé.
- **Trésor familial** avec l'armure divine (Tenue Ancestrale du Dieu-Dragon d'Or) : au **Patriarche seul**, remis
  **quand on devient Patriarche**, repris à la succession (comme Tenryūmaru).
- Maintien : **Souffle du Dragon d'Or** (un dragon de lumière serpente devant le porteur et transperce).
- Signature : **Éclipse du Dieu-Dragon** (le ciel s'assombrit, un dragon d'or descend et frappe toute la zone).

---

## Voie martiale du Clan Luo — validée et codée le 2026-11-09 (compilée, assets chargés sur serveur isolé, non vue en jeu)

`classes/definitions/luo/VoieMartiale.java`. Réservée au clan (`filter` / `ensure`). Un Guerrier du clan peut y entrer **dès le
niveau 40** (`promotionLevel`) ; s'il refuse, il garde les évolutions normales du Guerrier au niveau 180 (les choix
sauvegardés de l'entrée anticipée sont alors retirés, `staleEarlyChoices`). Les aventuriers PNJ ne tirent jamais la voie.
Plus aucun passif emprunté : chaque classe donne **Ki de l'Épée**, **Méditation martiale** et **une rune nouvelle par
palier** ; les runes des paliers précédents restent.

| # | Classe | Rang | Niveau | Qualité | Rune du palier |
|---|---|---|---|---|---|
| 1 | Guerrier | Débutant | 20 | Commun | — |
| 2 | Apprenti en Arts Martiaux | Troisième Ordre | 40 | Rare | Paume de Ki (Commun) |
| 3 | Disciple Martial | Deuxième Ordre | 100 | Rare | Frappe des Méridiens (Peu commun) |
| 4 | Expert Martial | Premier Ordre | 180 | Épique | Garde du Fleuve (Rare) |
| 5 | Maître Martial | Zénith | 280 | Épique | Croissant de Ki (Rare) |
| 6 | Grand Maître Martial | Sommet Suprême | 400 | Unique | Domaine de l'Épée (Épique) |
| 7 | Saint Martial | Transcendance | 550 | Unique | Fleur de Prunier (Unique) |
| 8 | Maître Suprême | Hwakyung | 750 | Légendaire | Transformation Hwakyung (Légendaire) |
| 9 | Dieu Martial | Unité Naturelle | 999 | Légendaire | Unité du Ciel et de la Terre (Légendaire) |

**Ki de l'Épée** (bascule comme l'Aura : clic = active / coupe ; coupé à court de mana ; mana/s 3, 4, 5, 6, 7, 8, puis 5 et 4) :
armes à lame (épée, katana, dague ; toute arme et mains nues au palier 8). Dégâts +8 / 12 / 18 / 22 / 26 / 32 / 40 / 50 % ;
Disciple : −10 % de dégâts subis, endurance +3 %/s, éclat de Ki au 4e coup ; Expert : lame imprégnée (Ki +10 % qui traverse
l'armure) ; Maître : lame de Ki au 4e coup ; Grand Maître : Ki solidifié (Ki +15 %, −15 %) ; Saint : lame à chaque coup, 20 %
d'écho, en garde la lame tranche les projectiles ; Maître Suprême : lames géantes blanc nacré, fleur de Ki au 4e coup, −20 % ;
Dieu Martial : aura presque invisible, coupe de zone (6 blocs) au 4e coup. Couleurs : blanc pâle, bleu pâle, bleu vif, cyan,
violet, **or** (Saint), blanc nacré, invisible.

**Méditation martiale** (bascule, sans mana) : assis en tailleur, immobile et vulnérable, coupée par un coup reçu ou un
second clic ; vie, mana et endurance +2 / 3 / 4 / 5 / 6 / 8 / 10 / 12 toutes les 10 s, XP +1 / 1 / 2 / 2 / 3 / 3 / 4 / 5
toutes les 15 s. Méditer coupe le Ki.

Fichiers : `scripts/runes/specs/Rune_{KiEpee,MeditationMartiale,PaumeKi,FrappeMeridiens,GardeFleuve,CroissantKi,DomaineEpee,
FleurPrunier,TransformationHwakyung,UniteCielTerre}.json` (aucun butin : `"loot": {}`), sorts Java du même nom + `MartialKi`,
`scripts/classes/gen_voie_martiale.py` (auras Ki_T1..8, effets), posture `Meditation` dans `scripts/npcs/gen_luo_anims.py`.
Reste possible plus tard : ce que chaque palier débloque sur les katanas Luo eux-mêmes (non demandé pour l'instant).

### Retours de test du 2026-11-09 (traités, compilés, assets chargés sur serveur isolé, non revus en jeu)

- **Entrée dans la voie : le Rouleau de la Voie Martiale** (`Eldanior_Parchemin_Voie_Martiale`, makimono). Clic droit :
  fenêtre de serment de style japonais (`Voie/VoieMartiale.ui`, `luo/VoieMartialePage`) qui prévient que le personnage
  recommence à zéro. « Prêter serment » : niveau 1, XP 0, caractéristiques à 1, points 0, compétences et runes oubliées
  (l'Aura revient avec la dignité), classe Apprenti ; on garde famille, rang, guilde, argent, objets, titres, quêtes. Réservé
  au clan (admins exemptés). Le Guerrier ne mène plus à la voie ; la voie n'est jamais classe de départ.
- **Manuels secrets** (`Eldanior_Manuel_Luo_<Technique>`, livre cousu à la japonaise, géométrie du livre de sorts vanilla) :
  ils **remplacent les parchemins** de ces techniques. Maintenir le clic droit = le livre s'ouvre et ses pages tournent
  (animation `Lecture` du jeu `Luo_Manuel`), 2,5 s = la rune associée arrive dans le sac de runes et le manuel se consume
  (`spells/LuoManuels`). Il faut être du clan et avoir le palier. Remise : au serment (Ki, Méditation, Paume de Ki) puis à
  chaque promotion (`RuneManager.grantClassRunes` donne le manuel au lieu de la rune).
- **Type de technique « Luo »** (noir et or) au lieu de Combat, rangées dans `Eldanior_Familles.Luo`, puis (2026-11-11) dans `Eldanior_Empire_Luo.Runes`.

### Retours de test du 2026-11-10 (traités, compilés, assets chargés sur serveur isolé, non revus en jeu)

- **La voie martiale est ouverte à tous** : pas besoin d'être du Clan Luo (rouleau, manuels, évolutions).
- **Le serment fait tout perdre sauf 5 % des caractéristiques** (choix de l'utilisateur : en plus de la progression, famille
  et rang noble, guilde, argent remis à 1000, barre d'objets, titres, quêtes, statistiques PvP, église). Famille quittée comme
  « Quitter » de l'onglet Famille (succession du Patriarche) ; un chef de guilde qui a encore des membres doit transmettre son
  rôle avant. Le sac (hors barre d'objets) est gardé.
- **Méditation** : l'emplacement Action n'animait que la tête ; la posture est maintenant une **emote** du mod
  (`Server/Emote/Luo_Meditation.json`, corps entier, objet en main caché) ; attaque et garde annulées à chaque tick
  (`SpellFx.cancelChains`) et coups annulés (`MeditationMartiale.isMeditating` dans DivineDamageSystem).
- **Livre retourné** : les poses vanilla présentent le livre ouvert vers l'extérieur ; demi-tour de `R-Attachment` dans
  `Manuel_Lecture` et `Manuel_Lecture_FPS` (vérifié par calcul : le lecteur voit les faces intérieures) ; l'intérieur des
  couvertures est maintenant peint en page imprimée.
- **Méditation** : la posture est rejouée 0,45 s puis 1,95 s après le clic (l'animation de la rune l'effaçait) ; attaque et
  garde bloquées pendant la méditation.
- **Bokken du Dojo** (base à compléter avec l'utilisateur) : ne tue ni joueur ni PNJ (laisse 1 PV, `LuoPassives.trainingCap`) ;
  combo selon la classe : 2 coups hors de la voie, puis 3 (Apprenti) à 8 (Saint et plus), choisi par l'effet invisible
  `Voie_Combo_<N>` (`LuoPassives.trackTier` + `EffectCondition` dans `Luo_Combo_Bokken`) ; maintien = posture chargée puis
  rafale du combo entier (`LuoBokkenRafale`), aussi depuis le fourreau. Garde et rengainage inchangés ; signature : à venir.

## Retours de test du 2026-11-08 (traités, non revus en jeu)

Marque du poste ×4 ; boutique refaite (`scripts/npcs/gen_shop_ui.py`, `guards/ShopPage`) : onglets Lames, Distance,
Boucliers, Tenues, Remèdes, Mets et thés, Outils, Matériaux, Divers, **Rachat** (le marchand rachète au quart de la
valeur), grille de 4 par ligne, cadre or / or vif (Grand Marchand) / jade (Comptoir) / bronze (ambulant) ; paiement corrigé
(`shop/CoinWallet.pay` : plus petites pièces d'abord, seule la différence est rendue ; avant, toute la bourse était
reconvertie et rangée dans le sac) ; rang « Maître de Guilde » des marchands renommé **Grand Marchand** (`--statut grand`) ;
assortiments élargis ; salut des PNJ dès la plaque de Noble (Noble : salut ; Seigneur, Doyen : inclinaison ; Patriarche :
profonde), marchands et civils compris ; prénoms pour tous les PNJ + nom de la famille du territoire (« Haruki Luo ») ;
variantes `_Luo` sur le modèle Player (l'armure du modèle de garde se superposait au kimono) ; rengainage animé seulement
après un combat ; replacement sur le poste une fois par retour. Système central des PNJ (`NpcServices`, `NpcInteractSystem`,
`NpcTalkPage`, rôles civils interactifs `scripts/npcs/gen_roles_civils.py`) : commerce, quêtes, répliques réglables dans la
fenêtre du poste.

---

## Retours du 2026-11-10 (soir) — fait, compilé, non vu en jeu

- **Étape 5 (kimonos)** : Robe de Cour, Kariginu du Lettré, Haori de la Garde d'Honneur (Épique 320/330/340), Rōnin Vagabond
  (Rare 100), Kimono des Rizières (Commun 10) dans `scripts/armures/gen_kimono_luo.py` (`Armor_Kimono_<Cour|Lettre|Garde|Ronin|Rizieres>_*`).
  Habitants Luo surtout en Rizières, aventuriers en Rōnin (`LuoNpcs`) ; tailleurs : Rizières et Rōnin chez l'Apprenti, les trois
  tenues nobles dans le tirage du Négociant (`scripts/guards/gen_marchand_luo.py`).
- **Étape 6 (Kinryū-Ō)** : `Artefact_Sword_Kinryu` (Divin 800, ×5) généré avec les katanas Luo (`gen_luo_katanas.py` : gravure
  du dragon, fourreau d'écailles d'or `SAYA_SCALES`). Sorts `skills/runes/spells/Kinryu.java` appelés par `LuoIai` / `LuoIaigiri` :
  Souffle du Dragon d'Or (maintien, recharge 4 s : dragon de faisceaux d'or qui serpente sur 26 blocs, 170 par ennemi traversé),
  Éclipse du Dieu-Dragon (signature : écran assombri pour les joueurs à 48 blocs, ennemis ralentis, dragon en spirale depuis
  22 blocs, 320 dans 11 blocs puis deux ondes de 60, porteur intouchable). Trésor du Patriarche (`FamilyKeySystem.RELICS` :
  Tenryūmaru, Kinryū-Ō et les 4 pièces de la Tenue Ancestrale, pièces portées comprises ; repris à la succession).
- **Tableaux** : quêtes journalières (`boards/QuestBoardScreen`), classements (`RankingBoardScreen`), duels (`DuelBoardScreen`),
  blocs `Eldanior_Tableau_<Quetes|Classement|Duel>` (`scripts/boards/gen_boards.py`).
- **Balises de route des PNJ** (`Eldanior_Route_PNJ`, `guards/NpcRouteBlocks`) : réseau = balises du rayon d'action + toutes celles
  reliées de proche en proche (16 blocs) jusqu'à 600 blocs du poste ; carrefours, demi-tour au bout et après 20 min ; marques
  visibles en tenant une balise ou un poste : colonne bleue, bout de route orange, pointillés entre balises reliées.
- **Empire Luo** : duchés du marquisat Luo muets (pas de titre ni de marque sur la carte), titre « Empire indépendant du Clan Luo »
  à l'entrée (`LuoClanSystem.luoRealm`).
- **Réglage du PNJ** : nouveau champ Famille (celle du territoire, neutre, ou une famille ; `Post.allegiance`, le PNJ réapparaît).
- **Vêtements sous le kimono** (habitant en Pèlerin) : le skin est allégé vêtement par vêtement (`GuardManager.strip`), au lieu
  de revenir au skin complet dès qu'un retrait était refusé.
- **Manuel** : axes X et Y essayés en jeu, toujours faux. Défaut = axe Z, et 4 manuels de test `Eldanior_Manuel_Test_<0|X|Y|Z>`
  (menu créatif, Autres) pour choisir le bon sens en jeu.

## Propositions à valider (ne pas coder avant)

### A. Les quatre grands clans de l'Empire Luo
- Les familles ducales **Nighthollow et Swiftquiver sont retirées** ; leurs deux duchés du marquisat Luo disparaissent : l'empire
  n'a plus qu'un seul territoire. Les autres marquisats et duchés ne changent pas.
- Quatre **clans** = familles d'un nouveau rang « Clan » (sans territoire, comme des comtes : ils peuvent posséder des **villes**
  de l'empire), vassaux du Clan Luo :
  | Clan | Voie | Tenue | Katana / arme |
  |---|---|---|---|
  | Sama | pugilistes, mains nues, corps et Ki | nouveau keikogi de pugiliste | poings (gantelets) |
  | Hanazakura | épéistes floraux, vitesse | Sakura (kimono des chevaliers) | Shirasagi |
  | Kage | assassins, infiltration | Ombre | Tsukikage |
  | Setsuka | maîtres de l'épée, défense, contres, Ki | Garde d'Honneur | Aonami |
- **4 Doyens = les 4 chefs de clan** : le Patriarche d'un clan siège d'office parmi les Doyens du Clan Luo (les 4 sièges).
  Hiérarchie : Empereur (Patriarche Luo) > Doyens (chefs de clan) > Seigneur > Nobles Luo / membres des clans.
- Les membres d'un clan sont **admis sur les terres de l'empire** (plaque de leur clan) et saluent l'Empereur et les Doyens.
- Chaque clan a **sa rune de famille** (une active, règle du 2026-11-05) et ses techniques : les 8 runes de palier actuelles de la
  voie martiale pourraient devenir les techniques des clans (Sama : Paume de Ki, Frappe des Méridiens ; Hanazakura : Fleur de
  Prunier, Domaine de l'Épée ; Setsuka : Garde du Fleuve, Croissant de Ki ; Kage : nouvelles, assassinat et ombre).
- À trancher : sort des joueurs et des parcelles de Nighthollow / Swiftquiver ; un clan = famille à part (proposé) ou simple
  groupe dans la famille Luo.

### B. Compétences de la voie martiale (liste de l'utilisateur, 9 compétences évolutives)
Méditation (à améliorer), Ki de l'Épée (existe, à aligner sur les 8 noms de palier), Renforcement corporel (Disciple), Pas léger
(Expert), Sens martial (Maître), Projection d'épée d'Aura (Grand Maître), Vol sur épée de Ki (Saint), Incarnation de l'Âme
(Maître Suprême), Domaine Divin (Dieu Martial). Proposition de forme :
- Renforcement corporel, Sens martial : bascules comme le Ki (mana par seconde) ; réduction de dégâts / bouclier de Ki ;
  repérage des ennemis (marque visible du seul lanceur, puissance au Grand Maître, présences cachées au Saint).
- Pas léger : passif de vitesse + rune de ruée (distance et forme selon le palier : pas du vent, pas fantôme, bond aérien,
  téléportation courte, longue).
- Projection d'épée d'Aura : 1, 3, 6 puis 12 épées d'aura qui tournent autour du lanceur et frappent seules.
- Vol sur épée de Ki : vol en bascule (faisabilité moteur à vérifier en premier).
- Incarnation de l'Âme : double spirituel (comme les clones des Dix Serments) qui combat à côté.
- Domaine Divin : zone de 15 blocs (lanceur renforcé, ennemis ralentis et écrasés).
- À trancher : les 8 runes de palier actuelles sont-elles remplacées (→ techniques des clans, A) ; ce que « améliorer la
  Méditation » doit changer.

### C. Rang martial des PNJ
- Gardes, patrouilleurs, chevaliers, assassins, disciples affichent leur **rang martial** au lieu du type
  (« Haruki Luo · Expert Martial »), tiré de leur niveau (paliers 40/100/180/280/400/550/750/999) ; niveau réglable au poste,
  sinon tiré dans une fourchette par type (disciple 20-60, garde 60-180, garde royal 180-300, chevalier 280-450, assassin 300-500).
- Ils utilisent les compétences de leur rang (B) : aura de Ki dès Apprenti, Renforcement, ruée vers la cible, lames de Ki,
  épées d'aura, Domaine pour les plus hauts.
- À trancher : PNJ du Clan Luo seulement, ou tous les gardes du royaume.

### D. Borne de territoire (docs/plan-bornes-territoire.md)

## Décisions et travaux du 2026-11-10 (nuit) — compilé, non vu en jeu

**Clans (A, validé)** : clan = famille à part, de rang **Comte** (`definitions/clan/` : Sama, Hanazakura, Kage, Setsuka),
promue par décret comme les autres ; elle achète ses villes. **Nighthollow → Kage, Swiftquiver → Hanazakura**
(`FamilyManager.RENAMED` : joueurs, parcelles, trésoreries suivent) ; leurs duchés du marquisat Luo sont supprimés au
chargement (`ParcelManager.dropClanDuchies`, villes et parcelles remontées au marquisat) et ne sont plus créés
(`ParcelCreateCommand`). `luo/LuoClans` : le Patriarche d'un clan = **Doyen de l'Empire** (plaque de Doyen, kimono de Doyen de
sa couleur remis comme un trésor par `FamilyKeySystem`) ; membres = plaque `Eldanior_Plaque_Luo_Clan_<Clan>` (admis sur les
terres de l'empire) ; escorte et boutiques Luo ouvertes aux clans.
| Clan | Membres | Doyen (ex-« Noble ») |
|---|---|---|
| Sama | Haori de la Garde d'Honneur | cramoisi (`Armor_Kimono_Noble_Cramoisi_*`) |
| Hanazakura | Sakura | violet (`Noble_Violet`) |
| Kage | Ombre | noir (`Noble`) |
| Setsuka | **nouveau kimono Setsuka** (`Armor_Kimono_Setsuka_*`, blanc nacré, lunes et étoiles) | blanc et jade (`Noble_Jade`) |
Intendant = Kariginu du Lettré ; Robe de Cour = nobles de l'empire (comtes, barons). Kimonos de Doyen retirés du tailleur.
PNJ : champ Famille du poste accepte les clans, sauf gardes, patrouilleurs et chevaliers.

**Méditation (B, en partie)** : bonus en % des maximums (2 → 12 % / 10 s) et de l'XP du niveau (0,3 → 1,2 % / 15 s) ; aura
`Meditation_Martiale_T<palier>` : aucune pour l'Apprenti, de plus en plus forte, couleur du Ki du palier ; Saint et au-delà
lévitent à 3 blocs (Dieu Martial : or blanc).

**Reste de B** : remplacer les 8 runes de palier par Renforcement corporel, Pas léger, Sens martial, Projection d'épée d'Aura,
Vol sur épée de Ki, Incarnation de l'Âme, Domaine Divin ; les 8 anciennes deviennent les techniques des clans (Sama : Paume de
Ki, Frappe des Méridiens ; Hanazakura : Fleur de Prunier, Domaine de l'Épée ; Setsuka : Garde du Fleuve, Croissant de Ki ;
Kage : à créer ; Transformation Hwakyung et Unité du Ciel et de la Terre : à répartir).
**C (validé)** : rang martial pour tous les PNJ (affiché sauf pour les civils), techniques de leur classe, de leur famille et de
leur clan. **D (validé)** : borne de territoire avec type, nom, famille, dimensions, PvP, régénération, zone rectangulaire ou
**cylindrique** ; la borne doit rester posée pour que le territoire existe.
**Manuel** : 8 manuels de test (`Eldanior_Manuel_Test_<0|X|Y|Z>` = animation, `_M<0|X|Y|Z>` = modèle tourné).

## 2026-11-10 (fin de nuit) — compilé, non vu en jeu
- **Livre** : le bon sens (manuel de test « modèle Y ») est cuit dans `Manuel_Luo.blockymodel` (demi-tour Y de
  `Origin_Projectile`), reculé de 6 unités pour sortir de la main (`BOOK_SHIFT`) ; plus de demi-tour dans l'animation ; objets
  et animations de test supprimés.
- **PNJ** : sur les terres de l'empire, nom de clan **tiré au hasard** (Sama, Hanazakura, Kage, Setsuka ou Luo, graine du poste ;
  `GuardManager.randomClan`), sauf gardes, patrouilleurs, chevaliers ; les clans ne sont plus proposés dans le réglage.
- **Catégories créatives** : 4 sous-catégories Sama, Hanazakura, Kage, Setsuka (`familles.FAMILIES` : soleil sur cramoisi,
  fleur de cerisier sur violet, shuriken sur noir, croissant sur bleu nuit) ; clés, portes, présentoirs, bannières générés ;
  anciens fichiers Nighthollow/Swiftquiver supprimés (portes et présentoirs déjà posés de ces familles = blocs inconnus).
  Tenues rangées par clan (`ARMOR_PREFIXES`, `ARMOR_EXACT` pour Sakura), plaques de clan dans leur clan, katanas aussi.
- **Katanas uniques des clans** : Hanazakura = Hien, Setsuka = Shirasagi, Sama = **Gōtetsu, l'Acier Cramoisi** (×3,0, niv. 370),
  Kage = **Yamikiri, Tranche-Ténèbres** (×2,8, niv. 380, venin) ; Tsukikage rangé chez Kage. Vendus à l'Armurerie (Grand
  Marchand). Pas de kit propre (LuoPassives) pour les deux nouveaux : à décider.

## 2026-11-10 (journée) — B, C, D codés : compilé, assets chargés sur serveur isolé, non vu en jeu

- **Méditation (retour de test)** : la lévitation tremblait (téléportation toutes les 0,4 s) et le joueur n'était pas assis en
  l'air. Désormais le Saint et au-delà montent 1,5 s puis s'**assoient sur un siège invisible** (`Eldanior_Siege_Meditation_<0|1|2>`,
  bloc vide avec point d'assise à 3 blocs au-dessus des pieds, `gen_voie_martiale.py`) via `BlockMountAPI.mountOnBlock` : le moteur
  le tient immobile ; l'emote en tailleur est rejouée une fois assis. Descendre du siège = se relever ; le siège est retiré à la fin.
- **Manuel** : livre avancé et relevé (`BOOK_SHIFT` = (0, 5, −3) ; calcul pose_kit : +Z local = le long de l'avant-bras).
- **B. 7 compétences de palier** (remplacent les 8 anciennes ; `VoieMartiale.STEPS`) : Disciple Renforcement corporel (bascule,
  −10 à −30 %, bouclier de Ki dès le Grand Maître) ; Expert Pas léger (ruée 8 m → pas fantôme → bond aérien → téléportation 14/20/26 m) ;
  Maître Sens martial (bascule, marques vues du seul lanceur, puissance, révèle les invisibles au Saint, coups dans le dos ÷2) ;
  Grand Maître Projection d'épée d'Aura (1/3/6/12 épées `Ki_Epee` en orbite 20 s) ; Saint Vol sur épée de Ki (bascule, vol forcé,
  épée géante `Ki_Epee_Vol` sous les pieds) ; Maître Suprême Incarnation de l'Âme (1 double, 2 au Dieu Martial, PNJ copie autonome) ;
  Dieu Martial Domaine Divin (15 m, 12 s). Apprenti : Ki et Méditation seulement. Manuels `Eldanior_Manuel_Luo_<Id>`.
  Écarts à la proposition : Pas léger sans passif permanent (vitesse ×1,15–1,35 pendant 4 s après chaque pas) ; Sens martial
  indique la puissance par la forme de la marque (flèche / triple chevron), pas par la couleur.
- **Techniques des clans** (`luo.LuoClans.TECHNIQUES`, niveau du personnage requis) : Sama = Paume de Ki (20), Frappe des Méridiens
  (100), Transformation Hwakyung (500) ; Hanazakura = Domaine de l'Épée (300), Fleur de Prunier (400), Unité du Ciel et de la Terre
  (600) ; Setsuka = Garde du Fleuve (150), Croissant de Ki (220) ; **Kage : à créer**. Réservées aux membres du clan, toutes classes
  (`RuneManager.canUseRune`), manuels remis automatiquement aux membres qui ont le niveau (`FamilyKeySystem`, toutes les 5 s),
  rangés dans la catégorie créative du clan. Répartition de Hwakyung et d'Unité choisie par moi : à confirmer.
- **C. Rang martial des PNJ** (`guards/MartialRanks`, `spells/MartialNpcs`) : gardes, gardes royaux, patrouilleurs, chevaliers,
  chevaliers errants, assassins, disciples. Niveau réglable au poste (champ Niveau), sinon tiré par type ; nom « Prénom Famille -
  Expert Martial » (rôle gardé sous 40). Combat : aura de Ki, Renforcement (réduction des dégâts), Pas léger vers la cible (Expert+),
  Projection d'épée (Grand Maître+), Domaine Divin (Dieu Martial), techniques de leur clan (sauf Garde du Fleuve et Hwakyung, liées au
  joueur). Leurs sorts ne visent jamais les joueurs, sauf leur cible verrouillée. Mages royaux, aventuriers, chasseurs, criminels et
  civils : pas de rang. Familles : pas encore de rune, donc rien à lancer.
- **D. Borne de territoire** : voir docs/plan-bornes-territoire.md (fait).

## 2026-11-10 (soir, retours de test) — fait : compilé, assets chargés sur serveur isolé, non vu en jeu

- **Vol sur épée** : l'origine du modèle est la pointe (comme une flèche) : pointe devant, milieu de la lame sous les pieds.
- **Ki de l'Épée** : lueur seulement sur la lame (particules attachées à l'arme comme la signature chargée, couleur du palier) :
  rien (Apprenti, Disciple), atténuée (Expert), normale (Maître), + aura du Renforcement sur la lame (Grand Maître et +). Saint et
  + : une lame de Ki à chaque coup du kata et au coup chargé (sort `KiEpeeCoup` inséré dans les interactions) ; lames ×1,8 (Maître
  Suprême) et ×2,6 (Dieu Martial), qui fend le sol devant lui (`MartialTerrain.fissure`, restauré après 25 s).
- **Kata du Dragon d'Or** (`gen_luo_katanas.py`, `KATA`) : 12 mouvements enchaînés (yoko-giri, kesa-giri, kiri-age, karatake-wari,
  tsuki, coupe tournoyante, coupe sautée en vrille, frappe lourde, estoc en ruée, salto avant tranchant, coup de pied tornade, estoc
  tournoyant en ruée) ; Apprenti 3, Disciple 4, Expert 5, Maître 6, Grand Maître 8, Saint 9, Maître Suprême 10, Dieu Martial 12 ; 4 hors
  de la voie ; tous les katanas Luo (Hien en version rapide, Bokken 2 hors voie).
- **Projection d'épée d'Aura** : plus de portails ni de modèle ; épées de lumière et de fumée (`Ki_Epee_Holo_T*`), orbites aléatoires
  ondulantes, lancées chacune à leur tour, acharnées sur leur cible, dissipées après 3 attaques.
- **Méditation** : suspendu par un vol forcé à vitesse nulle (plus de siège), posture en tailleur en l'air, yeux fermés ; Maître et + :
  méditation éveillée (mains jointes), orbes, souffle au sol ; Saint et + : colonne ; Maître Suprême et + : blocs du sol en lévitation.
- **Pas léger** : ruées seulement (animation de dash de l'arc), 8 → 24 blocs, recharge 3 s → aucune au Maître Suprême / Dieu Martial.
- **Sens martial** : silhouette rouge collée à l'ennemi (le moteur ne dessine pas à travers les murs), portée 30 → 150 blocs, durée
  12 → 30 s, 1 / 2 cibles traquées (Maître, Grand Maître) puis toutes.
- **Renforcement corporel** : posture de montée en puissance, onde, colonne, aura par palier, pulsations, sol fendu en étoile (Maître Suprême et +).
- **PNJ** : niveaux par défaut (patrouilleur Apprenti/Disciple, garde Disciple/Expert, chevalier Expert/Maître, garde royal Grand
  Maître/Saint), plafond Saint ; gardes royaux = Luo, nouvelle tenue `Armor_Kimono_Garde_Royale_*` ; mage royal = **alchimiste royal**
  dans l'empire (fioles de poison, `Armor_Kimono_Alchimiste_*`) ; certains habitants sans nom de clan.
- **Bornes** : royaume, territoire, ville ; un royaume crée ses 4 marquisats ; carte sans hachures (bordures seules, cercle pour un
  cylindre) et redessinée après chaque changement ; option « Annonce » à l'entrée ; colonne et piquets colorés par type de zone.

### Propositions à valider : Incarnation de l'Âme et Domaine Divin par clan
Incarnation (âme translucide propre au clan du lanceur ; sans clan : le double actuel) :
- **Luo** : dragon d'or translucide qui serpente autour du lanceur, mord et souffle de l'or (base : le dragon de Kinryū-Ō).
- **Hanazakura** : cerisier géant qui pousse derrière le lanceur ; pluie de pétales tranchants en cône, racines qui entravent.
- **Sama** : colosse de Ki (double géant ×2,5) qui frappe à mains nues, ondes de choc au sol.
- **Kage** : ombres jumelles qui se téléportent dans le dos des ennemis, coups empoisonnés.
- **Setsuka** : grue de lune qui plonge, coupes en croissant qui gèlent, halo protecteur.
Domaine Divin (taille = zone d'influence du joueur, environnement transformé, une « loi » par clan, recliquer = déchaîner le domaine) :
- **Luo, Éclipse du Soleil Noir** : ciel assombri, ennemis aveuglés et affaiblis, frappes du dragon d'or.
- **Hanazakura, Jardin Éternel** : cerisiers qui jaillissent, pétales qui coupent, alliés soignés.
- **Sama, Arène du Titan** : gravité ×3 pour les ennemis (ni saut ni fuite), chaque coup du lanceur crée une onde.
- **Kage, Nuit sans Lune** : ténèbres, lanceur invisible qui frappe d'ennemi en ennemi.
- **Setsuka, Lac de Lune** : sol de glace, ennemis gelés peu à peu, croissants de lune.

## 2026-11-10 (nuit, retours de test) — fait : compilé, assets chargés sur serveur isolé, non vu en jeu

- **Vol sur épée** : sens validé en jeu ; épée encore avancée de 0,7 bloc (`VolEpeeKi.CENTER`).
- **Projection d'épée d'Aura** : épées **3D** de Ki (`Ki_Epee_Proj_T<n>` : katana vanilla, texture de lumière recolorée à la
  couleur du Ki du palier, rendue translucide par le ModelVFX `Ki_Epee_Spectrale_T<n>` via l'effet `Ki_Epee_Corps_T<n>`, traînée
  `Ki_Epee_Trail_T<n>` et fumée continue `Ki_Epee_Fumee_T<n>` attachées à la lame), en orbite dans le ciel (2,6 à 4,2 blocs de haut).
  Les épées de particules `Ki_Epee_Holo_T*` sont gardées pour d'autres compétences (aimées de l'utilisateur).
- **Lames de Ki inclinées comme le coup** : sorts `KiEpeeCoup_H|G|D|V` (`gen_luo_katanas.KATA_ORIENT`), modèles
  `Tenryumaru_Lame_<couleur>_<G|D|V>` ; bug corrigé : la détection générique des coups lançait une lame horizontale avant celle du
  kata (écart de 250 ms), elle ignore maintenant les katanas Luo. **Coup chargé** (dégainé foudroyant) : sort `KiEpeeCharge`, lame
  ×1,4, dès le Maître Martial.
- **Pas léger** : sillage attaché au corps (`Pas_Leger_Sillage_<n>` : traits de lumière, voile, fumée et éclats laissés dans l'air,
  corps lumineux) ; **ruée guidée** dès le Maître Martial : ennemi dans un cône de 28° devant le regard → la ruée le suit et
  s'arrête devant lui, tournée vers lui.
- **Sens martial à travers les murs** : pour un ennemi caché, double rouge de son modèle (`Projectile_Config_Sens_Fantome` + effet
  `Sens_Fantome`) placé devant le mur sur la ligne des yeux, réduit dans le rapport des distances (il couvre à l'écran la place
  de l'ennemi) ; vu du seul lanceur grâce au système `PrivateVisibility` (même mécanique que le masquage des joueurs du moteur).
  Limite : juste en vue à la première personne. En vue directe, la silhouette de particules reste.
- En attente d'une description : problème de carte et de pose de territoire, torse du Pèlerin, « Domaine divin pas clair ».

## 2026-11-10 (nuit, 2e série de retours) — fait : compilé, assets chargés sur serveur isolé, non vu en jeu

- **Carte** : le gel à la création d'un territoire venait de `ParcelMapSystem.refreshMap` (images vidées + `ClearWorldMap` au
  client, jamais renvoyées) ; le dessin des zones sur les images est désactivé, `refreshMap` ne vide plus rien. Marqueurs des
  territoires de l'Empire Luo de nouveau affichés.
- **Chevaliers** : tenue selon la famille du poste (réglage, sinon celle du territoire) : Empire Luo → tenue du clan (Sama
  Garde, Hanazakura Sakura, Kage Ombre, Setsuka Setsuka ; Luo → Garde) ; Runkandel → armure de Capitaine ; autres familles :
  armure du type (pas encore de tenue). `LuoNpcs.knightOutfit`, `GuardManager.KNIGHT_ARMOR`, `familyIdOf`.
- **Pas léger** : traînées du dash vanilla (`Daggers_Dash_Effect` : `Daggers_Dash_Straight`, `Daggers_Dash_Distortion`, caméra
  `Daggers_Dash`, particules à la 1re personne) dans l'interaction ; plus d'éclat ni d'anneaux ; corps lumineux gardé.
- **Coup maintenu** : `KiEpeeCharge` ajouté au Flash Céleste de Tenryūmaru et à la rafale du Bokken ; autres armes à lame :
  attaque tenue plus de 0,8 s = coup chargé (lame ×1,4, dès le Maître Martial).
- **Kata** : 12 temps avec deux tours complets : `LuoKaiten` (lance, `SpinSwingLeft` ×1,4) et `LuoTourbillon` (hache de bataille,
  `Whirlwind` ×1,3) à la place de la frappe lourde et de l'estoc en ruée ; rafale du Bokken alignée.
- **Manuel** : animation jouée une seule fois sur les 2,5 s de lecture (avant : boucle à 0,7 = deux passages coupés) ; livre
  reculé (Z +8) ; demi-tour à choisir avec `Eldanior_Manuel_Test_X|Y|Z` (puis `EXTRA_TURN` et retrait des tests).
- **Garde Royale** : refaite sur la forme du Sakura (jingasa conique, longues manches, obi et nœud) en version impériale :
  laque noire, or, cramoisi, chrysanthèmes, épaulières laquées au mon, obi de brocart (`build_royal_*`).
- **Méditation** (Saint, Dieu Martial) : couronne d'épées 3D de Ki en rotation (6 ; Dieu Martial 12 sur deux couronnes
  contraires), mandala d'ondes au sol ; Dieu Martial : voile d'or et secousse toutes les 5 s.
- **Projection** : épées qui jaillissent du lanceur, prise d'élan, frappe qui accélère et traverse la cible, orientation lissée.
- **Sens martial** : doubles animés (repos, marche, course), étiquette « nom — distance », rouge vif à moins de 15 blocs et
  sombre au-delà, onde du sixième sens toutes les 3 s (vue du seul lanceur).

## 2026-11-10 (nuit, 3e série de retours) — fait : compilé, assets chargés sur serveur isolé, non vu en jeu

- **Chevaliers** : `p.luo` vrai aussi sur les terres d'un clan de l'Empire ou dans l'Empire (`GuardManager.allegiance`) ; avant,
  seul un territoire de la famille Luo habillait en kimono, un chevalier posé chez les Sama gardait l'armure par défaut.
- **Lames de Ki** : seulement au Saint Martial et au-delà (coup chargé, attaque tenue, kata) ; lame du 4e coup du Maître et du
  Grand Maître retirée.
- **Sens martial** : porteur des doubles = entité modèle simple (`SpellFx.spawnStatic`, plus de projectile que le moteur pouvait
  supprimer) ; un mur = bloc plein ET opaque (l'herbe, les feuilles et le verre faisaient apparaître des doubles partout).
- **Projection** : épée gardée tant qu'il lui reste des coups (cible morte → orbite) ; plafond 90 s.
- **Sensei** : type `SENSEI`, rôle `Eldanior_Npc_Sensei`, `guards/Sensei.java` (tenue noble, boutique des manuels réservée à la
  famille, `scripts/guards/gen_sensei.py`), duel sans mort `spells/SenseiDuel.java` (combattant au rôle `Eldanior_Clone_Lumiere`
  à l'image du Sensei, Sensei civil caché par `PrivateVisibility`). Don automatique des manuels de clan coupé.
- **Intendant** : barre de catégories Listes / Ordres / Gestions / Achats dans `SystemPage.ui` (blocs `#FamSec*`, `#FamCatBar`),
  `FamilleTab.STEWARD_CAT` ; réserve `Eldanior_Intendant_<Famille>` (`scripts/guards/gen_intendant.py`, `ShopPage.reserve`).
- Suivi de tout ce qui reste : `docs/a-faire.md`.

## 2026-11-11 — retours traités (compilé, non vu en jeu) et propositions à valider

Fait :
- **Endurance des katanas** : 5 % de l'endurance maximale par coup (au lieu de 2 %), coup maintenu toujours 50 % (`LuoPassives.COUP_COST`).
- **Rafale du Bokken** : une lame de Ki par mouvement seulement si le **Ki de l'Épée est déployé** (Saint et au-delà) ; les PNJ
  martiaux gardent leurs lames.
- **Manuel** : aucun test dans le bon sens → livre encore reculé (`BOOK_SHIFT` Z +8 → +13) ; manuels de test gardés pour le sens.
- **Pèlerin** : la cape de paille (mino, au dos et aux épaules) se lisait comme une armure de cuir sanglée sur le kimono :
  retirée, col de coton blanc ; la pièce s'appelle « Kimono du Pèlerin ».
- **Taxe du commerce** : 10 % du prix de chaque achat chez un PNJ marchand vont au trésor de la zone où se tient l'acheteur
  (ville, sinon territoire, grand territoire ou royaume ; `ParcelEconomyManager.payTradeTax`). Le prix ne change pas ; la
  réserve de l'intendant n'est pas taxée. Le message d'achat indique la taxe et la zone.
- **Intendant** : 5e onglet **Domaines** (villes, territoires, royaumes de la maison : trésor, suzerain, zones vassales, impôt,
  rapatriement ; « Lever l'impôt » et « Rapatrier la moitié du trésor aux coffres », une fois par semaine chacun, consignés au
  livre de comptes ; Patriarche, Seigneur ou maître du domaine). Tuiles de chiffres propres à chaque onglet ; registre trié par
  rang puis présence ; réserve triée par catégorie, niveau affiché, articles hors de portée de la bourse en gris.

### Proposition 1 — Estoc du Bokken : « Percée » (compétence évolutive, 8 paliers comme la Fente du Ciel)

**Geste** (aucune touche libre sur le Bokken : attaque, attaque tenue, garde, rengainage, signature sont pris) — je propose
**la garde puis l'attaque** : clic droit maintenu (garde) + clic gauche = estoc qui part de la garde. Autre possibilité : attaque
en sprintant. Coût : le coup maintenu (50 % d'endurance) pour le Dieu Martial, 15 % aux autres paliers.

| Palier | Nom | Effet |
|---|---|---|
| Apprenti | Tsuki | estoc droit avec un pas, 3 blocs, perce la garde (ignore le blocage) |
| Disciple | Tsuki du Ruisseau | perce 2 ennemis alignés, repousse le dernier |
| Expert | Estoc Fendant | ruée de 5 blocs qui traverse tous les ennemis de la ligne, saignement |
| Maître | Aiguille de Ki | l'estoc projette un trait de Ki fin sur 12 blocs, traverse, perce 30 % d'armure |
| Grand Maître | Mille Aiguilles | 7 estocs en 0,6 s dans un cône étroit, puis le trait ; le dernier fait éclater les marques |
| Saint | Percée du Prunier | trait de Ki d'or sur 20 blocs, pétales ; traverse aussi feuilles, herbe et verre |
| Maître Suprême | Lance de l'Âme | l'âme répète l'estoc avec une lance géante : 30 blocs, perce un trou d'un bloc sur 8 blocs de profondeur |
| **Dieu Martial** | **Percée Céleste** (coup ultime) | 1 s de posture (anneaux de Ki qui se resserrent sur la pointe, le sol tremble), puis l'estoc libère un **cylindre de 2,5 blocs de rayon sur 48 blocs** : tous les ennemis sont **transpercés** (gros dégâts qui ignorent l'armure, immobilisés 1 s, saignement), **tous les blocs du cylindre sont détruits** ; onde de choc au départ, tunnel de lumière, débris, tonnerre |

À trancher : le geste ; les blocs détruits par la Percée Céleste reviennent-ils (comme la fissure du Ki, 25 s) ou restent
détruits ? Les zones protégées (territoires, villes) sont-elles épargnées (proposé : oui) ?

### Proposition 2 — Rune de famille du Clan Luo : « Exécution Céleste »

Ton idée développée (une seule rune active pour la famille, règle du 2026-11-05) :
1. **Viser** : regarder un ennemi à 20 blocs au plus, en ligne droite sans mur ; une marque d'or apparaît sur lui.
2. **Pas léger** : ruée en ligne droite qui **traverse la cible** (le lanceur ne peut pas être touché pendant 0,3 s) et
   s'arrête 3 blocs derrière elle, tourné vers son dos. Un mur sur la ligne arrête la ruée devant lui.
3. **Images rémanentes** : le long du trajet, 5 doubles dorés du lanceur restent figés 0,7 s dans une posture de coupe ;
   chacun **tranche la cible** quand le lanceur la dépasse (traits de lame en ligne droite) ; les ennemis sur la ligne prennent
   une coupe plus faible.
4. **Les épées de Ki** (`Ki_Epee_Holo`, celles qui te plaisaient) : une fois derrière, 6 épées d'or apparaissent en cercle
   autour de la cible, immobiles 0,4 s (« le temps s'arrête »), puis convergent ensemble au moment où le lanceur **rengaine**
   (son du sabre dans le fourreau) : coupe en croix, flash blanc.
5. **Exécution** : si la cible tombe sous 20 % de vie après le coup, elle est **décapitée** (tuée). Boss : dégâts ×2 à la place.
   Joueurs : dégâts seulement (proposé).
6. Recharge 20 s, mana ; bonus si le Ki de l'Épée est déployé (épées plus nombreuses, couleur du Ki).

À trancher : le nom (Exécution Céleste ou Décapitation Céleste), la portée, le seuil d'exécution, le comportement en PvP.

### Proposition 3 — Incarnation de l'Âme et Domaine Divin par clan (développées)

Base commune : Incarnation 25 s (30 au Dieu Martial), Domaine 12 s sur 15 blocs. Chaque clan remplace l'apparence et l'arme de
l'âme, et la « loi » du domaine ; recliquer pendant le domaine le **déchaîne** (fin anticipée en un coup final). Sans clan : le
double actuel et le domaine actuel.

**Luo (Empire)**
- *Incarnation, Dragon d'Or* : dragon translucide (celui de Kinryū-Ō, plus petit) qui s'enroule autour du lanceur ; toutes les
  3 s il mord l'ennemi le plus proche (et le soulève), toutes les 8 s il souffle un trait d'or en ligne. Au Dieu Martial :
  deux dragons, or et blanc, qui alternent.
- *Domaine, Éclipse du Soleil Noir* : le ciel s'assombrit, un soleil noir cerclé d'or au zénith ; ennemis aveuglés (vision
  réduite) et −30 % de dégâts ; toutes les 2 s un éclair d'or tombe sur l'un d'eux. Déchaîner : le soleil noir s'effondre sur
  le centre (gros dégâts, aspiration).

**Hanazakura (épéistes floraux, vitesse)**
- *Incarnation, Esprit du Cerisier* : arbre-esprit qui pousse derrière le lanceur ; pluie de pétales tranchants en cône vers sa
  cible, racines qui jaillissent sous les ennemis proches (immobilisés 1,5 s). Le lanceur gagne +20 % de vitesse d'attaque.
- *Domaine, Jardin Éternel* : cerisiers qui jaillissent du sol tout autour, tempête de pétales ; les ennemis saignent
  (pétales), les alliés regagnent 2 % de vie par seconde. Déchaîner : tous les pétales convergent en une spirale qui tranche.

**Sama (pugilistes, mains nues)**
- *Incarnation, Colosse de Ki* : double géant (×2,5) aux poings de Ki rouge ; ses coups font des ondes de choc au sol,
  projette les ennemis en l'air ; il imite les coups du lanceur avec un temps de retard.
- *Domaine, Arène du Titan* : anneau de pierre qui sort du sol au bord du domaine (personne n'en sort) ; gravité ×3 pour les
  ennemis (pas de saut, ralentis) ; chaque coup du lanceur crée une onde. Déchaîner : le colosse frappe le sol, tout le
  domaine se fend.

**Kage (assassins, ombre)**
- *Incarnation, Ombres Jumelles* : deux silhouettes noires qui se téléportent dans le dos des ennemis, coups empoisonnés,
  invisibles entre deux attaques.
- *Domaine, Nuit sans Lune* : nuit noire dans le domaine, ennemis aveuglés ; le lanceur est invisible et chaque attaque le
  téléporte derrière l'ennemi suivant (×1,5 dans le dos). Déchaîner : toutes les ombres frappent en même temps.

**Setsuka (maîtres de l'épée, défense, Ki)**
- *Incarnation, Grue de Lune* : grue de lumière pâle qui plane au-dessus du lanceur, plonge et coupe en croissant (gel),
  et le couvre d'un halo qui renvoie les projectiles.
- *Domaine, Lac de Lune* : le sol devient un lac gelé qui reflète une pleine lune ; ennemis ralentis puis gelés peu à peu
  (3 s immobiles au bout de 4 s), le lanceur glisse (+30 % de vitesse) ; croissants de lune qui tombent. Déchaîner : la lune
  se brise, tous les ennemis gelés éclatent.

À valider : ces dix effets, et s'il faut un « déchaîner » pour l'Incarnation aussi.

### Fente du Ciel sur les autres katanas
Décision du 2026-11-11 : une fois le Bokken fini (Percée comprise), copier son fonctionnement (Fente du Ciel, rafale, Percée)
sur **tous les autres katanas** ; seuls les katanas de famille gardent leur kit propre. À préciser : katanas de famille =
Tenryūmaru et Kinryū-Ō seulement, ou aussi ceux rangés dans les clans (Hien, Shirasagi, Gōtetsu, Yamikiri, Tsukikage) ?

## 2026-11-11 (soir) — Percée, système du Bokken sur tous les katanas, Exécution Céleste (compilé, non vu en jeu)

- **Manuel** : demi-tour du livre sur lui-même autour de l'axe normal aux pages (`PAGE_TURN`, nœud Handle) : les pages tournent
  maintenant de gauche à droite, le livre regarde toujours le lecteur.
- **Percée** (validée) : garde levée + clic gauche (variable `Guard_Bash` de la garde vanilla → `Luo_Percee`), `spells/Percee.java`,
  8 paliers ; **Percée Céleste** = canon de Ki : 1,2 s de charge (orbes qui convergent sur la pointe, anneaux, sol qui tremble,
  éclairs), faisceau de 48 blocs (cœur blanc, aura d'or, deux spirales, faisceaux `Ki_Canon_*` de `scripts/classes/gen_percee.py`),
  tunnel de 2,5 blocs de rayon creusé au fil du front (blocs remis après 60 s, zones protégées épargnées), débris projetés,
  ennemis transpercés et paralysés 1 s, explosion au bout, recul, tremblement de caméra. Recharge 20 s, 50 % d'endurance.
- **Système du Bokken sur tous les katanas** (sauf Kinryū-Ō, `family_kit` ; Tenryūmaru a son propre générateur) : rafale au
  maintien, Percée, Fente du Ciel en signature ; dégâts × puissance du katana (dégâts d'iai / 26, au moins 1). Les kits propres
  (Ressac, Éclipse, Neige Silencieuse, Vol de l'hirondelle, Iaigiri) sont mis de côté pour les spécialités à venir.
- **Exécution Céleste** (rune de famille du Clan Luo, `Rune_ExecutionCeleste`, technique de famille `LuoClans.TECHNIQUES` avec
  `familyId` luo, niveau 20, manuel vendu par le Sensei Luo, 60 mana, recharge 20 s) : Apprenti/Disciple posture iai accroupie
  puis dégainé court arrêté devant soi (nouvelle animation `IaiCourt`) ; Expert à Grand Maître ruée de 8/10/12 blocs qui traverse
  la cible, images rémanentes dorées (`Execution_Remanence`), épées `Ki_Epee_Holo` figées puis convergentes au rengainage ;
  Saint et au-delà katana présenté au fourreau, lame tirée de quelques centimètres (`Koiguchi`), cibles de la zone (8/11/15
  blocs) tranchées une à une par des images reliées par des éclairs, claquement final (`KoiguchiFin`), foudre sur chaque cible ;
  âme de Ki derrière le Dieu Martial. Exécution sous 10 → 25 % de vie (pas les joueurs ; monstres de 2 000 PV et plus : dégâts × 2).
- **Territoires des clans** : `ParcelManager.dropClanDuchies` supprimait à chaque chargement tous les territoires Kage et
  Hanazakura (même posés avec une borne) ; migration unique désormais (`_clanDuchiesDropped`), jamais sur une zone à borne.
  L'intendant Luo montre aussi les domaines des quatre clans (lecture seule).
- **Sensei** : katana dégainé en duel (la forme au fourreau n'a rien en main) avec fourreau vide en main gauche ; niveau des
  monstres (`MobLevelData` : +0,5 dégât par niveau, vie de base + 7 par niveau) ; les épées de Ki, lames et doubles d'un PNJ
  martial sont nettoyés à la fin du duel et quand le PNJ disparaît (`MartialNpcs.release`).

## 2026-11-12 — Les 12 Mouvements de famille, Percée refaite (compilé, non vu en jeu)

- **Compétences de famille = les Mouvements** (décision de l'utilisateur) : chaque famille a environ 12 compétences, ses
  « Mouvements » (« 1er Mouvement : Exécution Céleste »…), chacune évolutive selon le rang martial. Manuel du 1er remis en
  rejoignant la famille (une fois, `FamilyRegistry` clé `w.`), les autres chez le Sensei, qui ne montre que ceux du rang du
  joueur. Le nom de la technique s'ajoute sur le manuel quand elle est prête. Code : `FamilyMovements.java`,
  `LuoManuels` (lecture, `learnable`, `welcome`), `ShopPage.only`, `Sensei.openShop`, `FamilyKeySystem`.
  Générateurs : `gen_voie_martiale_objets.py` (`MOVEMENTS`, `build_family_book`), `gen_sensei.py`.
- Rangs du Clan Luo (choix « gros mouvements en fin de voie ») : Apprenti 1, Disciple 2, Expert 3, Maître 4, Grand Maître 5,
  Saint 6-7, Maître Suprême 8-10, Dieu Martial 11-12.
- **Exécution Céleste** : n'est plus une technique à part (ancien manuel `Eldanior_Manuel_Luo_ExecutionCeleste` supprimé),
  c'est le 1er Mouvement. Son geste n'était pas le bon (attaque verticale) : **en attente de la description de l'utilisateur**.
- **Percée** : nouvelles animations `Tsuki`, `TsukiRafale`, `TsukiCharge`, `TsukiCeleste` (`tenryumaru_anims.py`), coup
  synchronisé sur l'extension du bras (`STRIKE_MS`), faisceau `Estoc_Air` (`gen_percee.py`), impacts `Impact_Dagger_Stab*`.

### Proposition — Incarnation de l'Âme et Domaine Divin du Clan Luo (à valider avant de coder)

**Incarnation de l'Âme — « Âme du Dragon d'Or »** (Maître Suprême ; Dieu Martial entre parenthèses)
- Au lancement : le lanceur s'élève de 0,5 bloc, une colonne d'or le traverse, un rugissement ; son double spirituel (le double
  actuel, mais **doré**, tenue et katana compris) se détache de lui **et un dragon d'or translucide** (chaîne de faisceaux, comme
  Kinryū-Ō, plus fin) s'enroule autour des deux, 25 s (30 s).
- Le **double** combat comme aujourd'hui, en reprenant les techniques du lanceur : il lance la Percée et la Fente du Ciel du
  palier du lanceur toutes les 8 s.
- Le **dragon** tourne autour du lanceur ; toutes les 3 s il fond sur l'ennemi le plus proche (morsure : soulève et lâche),
  toutes les 8 s il souffle un trait d'or en ligne (8 blocs). (Dieu Martial : deux dragons, or et blanc, qui alternent.)
- Le lanceur porte un **halo d'écailles d'or** : −15 % de dégâts subis (−25 %).
- **Fin** : le double et le dragon remontent dans le lanceur (onde d'or, soin de 10 % de la vie).

**Domaine Divin — « Éclipse du Soleil Noir »** (Dieu Martial, 15 blocs autour du lanceur, 12 s)
- **Ouverture** : le ciel s'assombrit pour les joueurs du domaine (comme l'Éclipse du Dieu-Dragon), un **soleil noir cerclé
  d'or** apparaît au zénith au-dessus du lanceur, le sol se couvre d'un mandala d'or ; un cercle de lumière marque le bord.
- **Loi du domaine** : les ennemis sont **aveuglés** (vue réduite), **affaiblis** (−30 % de dégâts) et ralentis ; ceux qui
  tentent de sortir sont repoussés par le cercle. Le lanceur et ses alliés : +20 % de dégâts, −30 % de dégâts subis, 3 % de vie
  par seconde (comme aujourd'hui).
- **Foudre d'or** : toutes les 2 s, un éclair d'or tombe sur un ennemi au hasard (80 dégâts, paralysé 0,5 s).
- **Dragon du soleil** : toutes les 4 s, un dragon d'or traverse le domaine en ligne droite d'un bord à l'autre (120 dégâts).
- **Déchaîner** (relancer la compétence pendant le domaine) : le soleil noir **s'effondre** sur le centre : aspiration des
  ennemis vers le lanceur pendant 1 s, puis explosion d'or (400 dégâts, projection), le ciel redevient clair. Le domaine prend
  fin plus tôt.

À trancher : les chiffres ; le double de l'Incarnation lance-t-il vraiment la Percée et la Fente du Ciel ; le domaine
empêche-t-il vraiment de sortir (cercle infranchissable) ou ralentit-il seulement au bord.

### Catégorie créative « Empire Luo » (2026-11-11)

Onglet `Eldanior_Empire_Luo` (Order 7, `scripts/familles/gen_categorie.py`) : les cinq clans **Luo, Kage, Setsuka, Sama,
Hanazakura** (sortis de l'onglet « Famille ») puis quatre pages communes :
- **Civils** : kimonos Vagabond (Pauvre), Rizières, Ronin, Marchand, Négociant, Pèlerin, Cour, Lettré ; katanas de fer et
  Aonami ; plaque du Marchand.
- **Comestibles** : thés et onguents.
- **Arts Martiaux** : Rouleau de la Voie Martiale, manuels des techniques de classe, Bokken, manuels de test.
- **Runes** : toutes les runes de type Luo (y compris les techniques de clan).

Restent dans leur clan : clés, portes, présentoirs, bannières, plaques de rang et de clan, tenues de membres et de chefs,
katanas de clan et reliques, manuels de techniques de clan, Mouvements de la famille Luo. `familles.category(<Id>)` choisit
la bonne catégorie (clan ou page commune) ; la réserve de l'intendant Luo garde les Civils et Comestibles.

