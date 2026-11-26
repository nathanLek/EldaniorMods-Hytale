# Refonte des classes (2026-11-25 / 26)

Demande : retravailler les classes (check-up, équilibrage, 1 image par classe dans le choix de classe, nouvelles classes et
évolutions). Décisions de l'utilisateur du 2026-11-25 :

| Point | Décision |
|---|---|
| A. Corrections | oui, **sauf le Mousquetaire : on n'y touche pas pour le moment** |
| B. Moins de hasard | oui : choix libre au palier 3, pitié sur le tirage |
| C. Barème unique | oui, **jackpot gardé : Divine = 30 × Commune** (au lieu de ~40) |
| D. Identité par lignée | oui (gros chantier) : armes signature, une rune propre par classe, passifs variés |
| E. Catalogue | oui |
| Éveil | oui, niveau 650 ; **classe transcendante au niveau 900** |
| Nouvelles classes | Clerc, Dompteur, Barde — **pas des classes de base** (2026-11-26) : Clerc et Barde → lignées du Mage, Dompteur → lignées du Marchand |
| Images | les 117 (bases + palier 2) pour commencer |

## Ce qui est fait (non vu en jeu, jar non reconstruit)

**Catalogue (E).** `scripts/classes/classes.json` (une classe par objet) → `python3 scripts/classes/class_catalog.py`
vérifie (ids et noms uniques, liens, passifs, runes, armes, métiers) et génère `classes/catalog/ClassCatalog.java`.
Les ~400 fichiers `definitions/<famille>/**.java` sont supprimés ; restent en Java : Novice, DragonAncestral, Observateur,
Mousquetaire, `luo/VoieMartiale`. `ClassManager` : catalogue + spéciales, ordre gardé (LinkedHashMap).
`class_runes.py` retiré (il réécrivait les runes de toutes les classes) ; `stats_sim.py`, `build-index.sh`,
`gen_passifs.py` lisent ou ignorent le catalogue.

**Corrections (A).** Lien cassé Élémentaliste → `seigneur_des_elements` retiré (l'Élémentaliste a 2 suites) ; noms en
double renommés (ids inchangés) : `eclaireur_archer` « Éclaireur Sylvestre », `chasseur_de_reliques` « Grand Chasseur de
Reliques », `lame_fantome` (guerrier) « Lame Insaisissable », `chasseur_de_primes` (guerrier) « Chasseur de Têtes » ;
paliers du Marchand ramenés à 400 (le barème fixe les niveaux) ; écrans morts supprimés (`ClassSelectionScreen`,
`ClassIntroScreen`, `ClassEvolutionIntroScreen`, leurs `.ui` et cadres) — leurs utilitaires sont dans `classes/gui/ClassInfo`.
Compétences « actives » d'avant les runes (`INFERNO`, `TEMPETE_ELEMENTAIRE`… sur 6 mages) abandonnées : elles n'existaient nulle part.

**Barème (C).** Total des bonus = BASE[palier] × MULT[rareté], réparti selon le `profil` de la classe :

| Palier (niveau suivant) | Commune | Rare | Épique | Unique | Légendaire | Divine |
|---|---|---|---|---|---|---|
| 1 base (180) | 36 | | | | | |
| 2 (400) | 50 | 125 | 250 | 450 | 800 | 1 500 |
| 3 (650 → Éveil) | | 200 | 400 | 720 | 1 280 | 2 400 |
| 4 Éveil (900) | | 275 | 550 | 990 | 1 760 | 3 300 |
| 5 Transcendance | | 350 | 700 | 1 260 | 2 240 | 4 200 |

Avant : Guerrier ~30 % au-dessus des autres, Marchand Légendaire/Divin jusqu'à 4 845 (au-dessus du Dragon Ancestral) ;
maintenant toutes les familles ont le même budget. Inchangés : Dragon Ancestral 4 500, Observateur 3 520, Mousquetaire 50.

**Choix et pitié (B).** Palier 2 et au-delà : toutes les évolutions sont proposées, choix libre, pas de relance. Classe de
base → palier 2 : 3 voies tirées (2 relances) ; pitié `PlayerLevelData.EvolutionPity` : +1 par tirage sans Unique ou mieux,
remise à 0 dès qu'une voie Unique+ sort ; chaque point donne +50 % de chances aux Unique/Légendaire/Divine (plafond 20 points).
**La pitié survit à la remise à zéro du personnage** (choix fait pour que les rerolls de personnage comptent).

**Éveil (palier 4).** Généré automatiquement pour chacune des classes du palier 3 : id `<id>_eveil`, nom « … Éveillé(e) »,
passifs +1 rang, barème du palier 4. Pour l'instant **seul le niveau 650 est demandé** (la quête de classe reste à faire).

**Nouvelles voies (Clerc, Dompteur, Barde).** Décision du 2026-11-26 : ce ne sont pas des classes de base (on reste à
5). Chaque voie = 12 lignées (3 Communes, 3 Rares, 2 Épiques, 2 Uniques, 1 Légendaire, 1 Divine) et 34 évolutions
(`scripts/classes/new_families_2026_11_26.py`) : **Clerc et Barde rejoignent le Mage** (51 voies tirées à 180 au lieu de 27),
**le Dompteur rejoint le Marchand** (27 voies au lieu de 15). Runes : runes existantes du thème en attendant leurs runes
propres. Le Dompteur ne commande pas encore vraiment de bête (synergie avec les Familiers à faire).

**Images.** `python3 scripts/classes/gen_class_art.py` : une fiche par classe (décor, tenue, arme, effets), rendu vitrail.
152 illustrations (5 bases, qui reprennent celle de leur famille, + 147 lignées du palier 2), dans `Common/UI/Custom/Accueil/Classes/` (`Art_<id>.png` + `.ui`).
**Corrigé le 2026-11-26 (croix rouges)** : l'atlas des textures d'interface du client est limité (~67 Mpx) ; le mod en
utilisait déjà ~54 et les 300 images pleine résolution (C + G, 60 Mpx) l'ont saturé : 606 images refusées
(« Texture atlas rejected image » dans le `*_client.log`), y compris des images vanilla. Désormais **une seule image par
classe en demi-résolution (167×302, ~7,6 Mpx au total)** ; la carte grisée reprend la même texture teintée
(`Color: #8c93a6` dans `Art_<id>G.ui`). Il reste ~5 Mpx de marge : toute nouvelle image d'interface doit en tenir compte
(les classes du palier 3 ne peuvent pas avoir chacune leur image de cette façon).
Les 5 bases d'origine reprennent l'illustration de leur famille. Les classes du palier 3 et au-delà gardent l'illustration de
leur famille (à faire ensuite : fiches dérivées de leur lignée).
Affichage : `CardsPage` vide `#AccArtSlot<i>` puis y ajoute `Accueil/Classes/Art_<id>C.ui` ou `G.ui` (`ui.clear` +
`ui.append(selecteur, document)`) ; `ClassCards.ownArt` ne l'utilise que si les deux `.ui` sont dans le jar.
**À vérifier en premier en jeu** : ce mécanisme n'a jamais servi dans le mod ; s'il déconnecte le client, repasser par
l'illustration de famille (retirer `c.artUi = ownArt(m)` dans `ClassCards`).

**Ajouts du 2026-11-26 (soir).** Les classes des paliers 3 à 5
prennent l'illustration de leur lignée (`ClassManager.parentOf`, `ClassCards.ownArt`), sans image de plus dans l'atlas ;
notification d'apprentissage d'une rune (manuels de magie et du Clan Luo) : « Nouvelle compétence apprise ! Ouvrez
/es system, onglet Compétences, pour l'équiper » (la commande `/es runes` n'existe plus depuis le 2026-11-12, seule la
notification la citait encore). Vérifié : les 122 runes de classe ont toutes un manuel (sinon elles ne sont jamais données).

## Reste à faire (accepté, pas commencé)

- **Classes cachées** (proposition 5, acceptée) : classes obtenues par un exploit, comme l'Observateur (ex. Chevalier de la
  Mort après le Roi de la Mort, Champion de l'Arène après N duels gagnés), Légendaire au plus.
- **Quête d'Éveil** (aujourd'hui seul le niveau 650 est demandé).
- **Dompteur ↔ Familiers** : bonus du familier, ordres d'attaque.
- **Runes des nouvelles familles par manuels** : les runes de Lumière, de Terre… sont réservées aux mages
  (`classes` des fiches de rune) ; un Clerc ne peut lire que les manuels des runes de sa classe. À ouvrir (ex. Lumière au
  Clerc) ou à laisser au chantier « identité ».

## À valider

1. **Transcendance (niveau 900).** Le mécanisme est prêt : une lignée du palier 2 reçoit un champ `transcendance`
   `{id, nom, description, runes?, armes?}` ; ses 3 formes éveillées y mènent ; rareté de la lignée, profil moyen,
   meilleurs passifs +1, barème du palier 5. Proposition : **une classe transcendante par lignée** (147), par exemple
   Berserker → « Avatar de la Rage », Paladin → « Saint Patron », Samouraï → « Kami de la Lame », Nécromancien → « Roi des
   Morts », Tireur → « Flèche Éternelle », Ninja → « Ombre de Minuit », Banquier → « Trésorier du Monde »,
   Acolyte → « Lumière Incarnée ». Autre possibilité : une seule classe transcendante par famille.
2. **Gros chantier D** : par quelle famille commencer ? Proposition : Guerrier (1 rune propre sur 105, même liste d'armes
   pour 101 classes), puis Assassin, Archer, Marchand, puis les 3 nouvelles familles.
3. **Quête d'Éveil** : une quête de classe par famille (Chapitres) ou une épreuve unique ?

## Liste de contrôle en jeu

- [ ] Le serveur démarre : ~930 classes chargées (+ spéciales) et aucune « Classe introuvable » dans le journal.
- [ ] Statue des classes : l'illustration propre de la classe s'affiche (choisie en couleur, les autres grisées) ; aucune
      déconnexion « failed customUI ».
- [ ] Plus aucune croix rouge, ni dans la statue ni ailleurs ; `grep -c "Texture atlas rejected" <dernier>_client.log` = 0.
- [ ] Accueil : toujours 5 classes de base. Mage : 51 voies possibles à 180 (dont Acolyte, Ménestrel…), Marchand : 27 (dont Maître-Chien, Fauconnier…).
- [ ] Évolution base → palier 2 : 3 voies, 2 relances ; palier 2 → 3 : toutes les voies, pas de « Relancer ».
- [ ] Palier 3 au niveau 650 : une carte « … Éveillé », passifs un rang au-dessus.
- [ ] Un ancien joueur Guerrier Divin (Demi-Dieu) voit ses bonus baisser (3 876 → 2 400) : prévenir les joueurs.
