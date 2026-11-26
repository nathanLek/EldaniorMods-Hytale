# Suivi : ce qui reste à faire

Mis à jour le 2026-11-26. Détail des travaux : `docs/plan-classes.md`, `docs/plan-clan-luo.md`, `docs/plan-bornes-territoire.md`,
`docs/plan-intendant.md`.
Légende : **[test]** fait, à vérifier en jeu · **[réponse]** j'attends une précision · **[à faire]** pas commencé ·
**[plus tard]** décidé, mis de côté.

## Prochaines étapes (décidées le 2026-11-24)

1. ~~Point sur toutes les compétences passives~~ : fait le 2026-11-24/25 (refonte, voir la liste de contrôle ci-dessous).
2. ~~Butin des coffres au trésor~~ : refait le 2026-11-25 (liste de contrôle ci-dessous).
3. **[test]** Équilibrage des stats et refonte de l'esquive (`docs/plan-equilibrage-stats.md`) : esquive refaite
   (AutoDodge, dash à l'opposé de l'ennemi, 30 % max), vitesse d'attaque retirée, simulateur fait (2026-11-25) ;
   2e version après essai (dash court, attente 5 s, effets lisibles, `/es combat log`), Force et Défense passées en % ;
   **[test]** jar à reconstruire hors partie ; ensuite courbe de vie (armure au simulateur) puis bonus des classes.

4. **[test]** Refonte des classes (2026-11-25/26, `docs/plan-classes.md`) : catalogue `scripts/classes/classes.json`,
   barème (jackpot ×30), choix libre au palier 3, pitié, Éveil au niveau 650, voies Clerc et Barde (Mage) et Dompteur (Marchand),
   155 illustrations de classe ; **[réponse]** Transcendance (une par lignée ?), famille à commencer pour l'identité,
   quête d'Éveil.

## Classes : reste à faire (2026-11-26, détail dans `docs/plan-classes.md`)

- **[à faire]** Classes cachées : obtenues par un exploit comme l'Observateur (ex. Chevalier de la Mort après le Roi de la
  Mort, Champion de l'Arène après N duels gagnés), Légendaire au plus.
- **[à faire]** Quête d'Éveil (niveau 650) : aujourd'hui seul le niveau est demandé.
- **[à faire]** Dompteur ↔ Familiers : bonus du familier, ordres d'attaque.
- **[réponse]** Manuels de runes : les runes de Lumière, Terre… sont réservées aux mages ; les ouvrir au Clerc (Lumière),
  au Dompteur (Terre) ou attendre leurs runes propres ?
- **[réponse]** Transcendance (niveau 900) : une classe par lignée (proposé) ou une par famille ?
- **[réponse]** Chantier identité (armes signature, une rune propre par classe) : commencer par le Guerrier ?
- **[à faire]** Illustrations : fonds « cieux » trop clairs (personnage peu lisible), plusieurs assassins sur le même décor
  de toits ; marge de l'atlas d'interface ~5 Mpx seulement.
- **[test]** Croix rouges corrigées (atlas plein, images en demi-résolution) : jar à reconstruire hors partie, vérifier
  `Texture atlas rejected` = 0 dans le dernier `*_client.log`.
- **[test]** Notification d'apprentissage d'une rune : renvoie vers `/es system`, onglet Compétences.

## Liste de contrôle — accueil des nouveaux joueurs (2026-11-25)

Java : `accueil/Welcome.java` (ouverture, fin, remise à zéro), `accueil/WelcomePage.java` (fenêtre),
`accueil/WelcomeCommand.java` (`/es accueil`) ; fiche : `PlayerLevelData.OnboardingDone` ; fenêtre et illustrations :
`python3 scripts/accueil/gen_accueil_ui.py` (`Common/UI/Custom/Accueil/`). **Pas de race** : tout le monde est humain.
Compilé, jar reconstruit le 2026-11-25 à 19 h 31.

- [ ] Nouveau joueur : la fenêtre s'ouvre ~3 s après l'arrivée, ne se ferme pas avec Échap ; le joueur est invisible pour
      les autres et ne prend aucun dégât.
- [ ] 4 étapes : Bienvenue (Royaume, Empire Luo, Votre destin), Commandes (`/es system`, groupe et duels, commerce),
      Classe (Guerrier, Mage, Archer, Assassin, Marchand ; plus de Novice), Arrivée.
- [ ] « Se connecter » : classe appliquée (bonus, passifs), visible et vulnérable de nouveau, point d'apparition du monde
      par défaut, titre « BIENVENUE À ELDANIOR ».
- [ ] Admin, fiche d'un joueur, carte **Première connexion** (deux clics) : tout effacé (comme une perte totale, poche et
      sac compris), 1 000 or, et la fenêtre s'ouvre chez lui.
- [ ] Déconnexion pendant l'accueil : la fenêtre se rouvre à la connexion suivante.
- [ ] `/es accueil` **réservé aux admins** : aperçu refermable, rien n'est appliqué (le vrai accueil si l'admin ne l'a pas fini).
- [ ] Corrigé le 2026-11-25 : « Première connexion » sur soi-même déconnectait le client (`#IntCat0` introuvable) : l'écran
      admin se ferme d'abord, l'accueil s'ouvre 1 s après la remise à zéro. Jar reconstruit à 19 h 31.
- [ ] Les joueurs existants ne voient pas la fenêtre (fiche sans la clé = accueil passé).
- [ ] À vérifier : l'effet `Elda_InvisTotal` cache-t-il aussi le nom au-dessus de la tête ?

### 2e version (2026-11-25 soir, jar reconstruit à 20 h 17)

Fenêtre à cartes commune : `accueil/CardsPage.java` (+ `ClassCards`, `StarterKits`), même `Accueil.ui` pour l'accueil et la
statue des classes (`classes/gui/ClassChoicePage.java`). Générateur : `python3 scripts/accueil/gen_accueil_ui.py`
(`--fond` : blason et fond ; `--art=Nom` : une illustration).

- [ ] **Fabrication rapide désactivée pour tous** (`config/NoPocketCrafting`) : 1re version cassée (refuser la fenêtre
      bloquait TOUT l'inventaire, le client la demande à chaque ouverture) ; corrigée : l'inventaire et le panneau du
      marteau s'ouvrent, seule la fabrication est refusée (« utilisez un établi »). **Jar à reconstruire hors partie.**
- [ ] Bandeau Eldanior (blason, nom, sous-titre), fond avec la côte du continent à l'encre dorée et rose des vents.
- [ ] Bienvenue en 6 cartes : Royaume, Empire Luo, Runes et magie, Failles et donjons, Guildes et noblesse, Commerce et
      banque (4 nouvelles illustrations : Runes, Faille, Guildes, Banque).
- [ ] Classe : passifs avec leur effet, runes avec icône/qualité/recharge/effet, bonus, **kit avec icônes et infobulles**.
- [ ] **Kit de départ** donné une seule fois à « Se connecter » (jamais à la statue) : Guerrier épée + bouclier + armure de
      cuivre ; Mage bâton + grimoire + tenue de lin ; Archer arc court + 100 flèches + dagues + cuir ; Assassin dagues +
      épée rudimentaire + cuir ; Marchand lance + provisions + cuir ; pour tous : pain, bandages, potions, torches, pioche,
      hachette. **[réponse]** contenu à valider.
- [ ] **Statue des classes** : même fenêtre (niveau 20 : classes de base ; évolution : 3 voies tirées, « Relancer (n) »,
      « Choisir » puis « Confirmer »). Les anciens écrans (ClassIntro, ClassEvolutionIntro, ClassSelection) ne sont plus ouverts.
- [ ] **Contour des cartes de classe selon la rareté** (accueil et évolution à la statue) : Commune blanc, Rare
      bleu, Épique violet, Unique rouge, Légendaire or, Divine cyan (`ClassCards.tint`). **Jar à reconstruire hors partie.**

## Liste de contrôle — comptoir du Maître de guilde (2026-11-25)

Source : `python3 scripts/guards/gen_maitre_guilde.py` (BarterShop `Eldanior_Maitre_Guilde`, prix lus dans `prix.json`) ;
Java : `LuoMerchants` (branche MAITRE_GUILDE, `GUILD_*`), `ShopPage.pricing`. Compilé, jar NON reconstruit.

- [ ] Parler au maître de guilde : deux services, **Guilde** et **Commercer**.
- [ ] Vente (réassort chaque jour à 6 h) : Parchemin de Relance 12 diamants (×1), Failles scellées F 2 / E 6 / D 15 diamants
      (×3/2/1), Clé du donjon d'entraînement 15 or (×3), Tome d'Expérience 3 diamants (×3), pilules +1 à 2 diamants (×3
      chacune), pilules +5 à 1 zénith (×1 chacune). Pilules +10 et élixirs : pas vendus (trouvailles).
- [ ] Rachat : tout ce que les marchands rachètent ; **butins de monstres au double** du barème (comparer avec l'alchimiste).
- [ ] Fermé pendant le couvre-feu (comme les autres boutiques) ; le service Guilde reste ouvert.
- [ ] Prix changés dans `prix.json` (le marchand ambulant rachetait une pilule +1 238 cuivres alors qu'elle en coûtait
      120) : pilules 2 000 / 10 000 / 50 000, Tome d'Expérience 3 000 ; nouveaux : failles F/E/D 2 000 / 6 000 / 15 000,
      clé du donjon d'entraînement 1 500. **[réponse]** prix à valider ; les élixirs (2 500) sont maintenant moins chers
      qu'une pilule +5.

## Liste de contrôle — coffres au trésor (2026-11-25, 2e version)

Source : `scripts/coffres/conception.py` → `python3 scripts/coffres/gen_coffres.py` (objets tirés du catalogue entier :
`eldanior/coffres.json`). Tirage : `TreasureChest/ChestLoot.java`. Compilé, jar NON reconstruit.
À relancer après de nouveaux objets (runes, butins, armes) : `gen_economie.py` puis `gen_coffres.py`.

- [ ] **Tous les coffres** dans l'onglet créatif « Donjons & Keys ».
- [ ] **1 objet** ; un 2e selon le coffre (25 à 50 %) + moitié de la Chance ; un 3e seulement avec beaucoup de Chance
      (Chance 30 % : 3 % des ouvertures ; Chance 100 % : 20 %).
- [ ] **Coffre au trésor** : ressources, minerais, plantes, nourriture (82 % Communs).
- [ ] **Donjon commun** : butins de monstres, minerais, pièces, pages de rang Commun, manuels Communs, métiers.
- [ ] **Donjon** : armes, armures, potions, pilules (consommables de stats), pages et manuels Inhabituel à Épique.
- [ ] **Légendaire** : artefacts, pages et manuels Unique à Divin, potions, pilules, consommables très rares (élixirs, tomes,
      essence) ; en petite part armures/armes Légendaires et Divines, minerai Divin, nourritures et métiers rares. ~3 %
      d'objets Divins.
- [ ] **Boss et failles F/E/D/C** : entre les deux (voir conception.py). La Faille du Terrier a enfin son butin.
- [ ] Jamais dans un coffre : objets des familles et de l'Empire Luo, armes à feu, fourreaux et formes temporaires,
      objets de développeur.

## Liste de contrôle — refonte des passifs et admin Compétences (2026-11-25)

Source : `scripts/passifs/conception.py` → `python3 scripts/passifs/gen_passifs.py` (enum, conversions, pages, butin, prix,
traductions). Index des effets : `python3 scripts/passifs/gen_effets_index.py`. Compilé, jar NON reconstruit.

- [ ] **Jar à reconstruire** : quitter le jeu, `./gradlew jar`, relancer (nouveau `SystemPage.ui` : relancer le monde).
- [ ] **Sauvegarde convertie** : un joueur qui avait Frappe de Fureur a « Frappe Instinctive » au rang Rare ; ses anciens
      passifs supprimés (vitesse d'attaque, Œil de Rapace, Pisteur, Jugement de Koda...) ont disparu ; la maîtrise est gardée.
- [ ] **Onglet Compétences** : « PASSIFS ET MÉTIERS (n) · APPRIS x / 50 », rang affiché (« RARE · RANG 3/6 · Frappe de
      Fureur »), bouton OUBLIER (deuxième clic « SÛR ? ») seulement sur les passifs appris, métiers en bas de liste.
- [ ] **Page de rang supérieur** : fait monter le passif (message « passe au rang ») ; page de rang égal ou inférieur
      refusée ; au-delà de 50 passifs appris, refus avec le message de la limite.
- [ ] **Parchemins de Métier** (12, onglet créatif Parchemin) : apprennent l'établi ; une ancienne page de métier marche
      encore. Le Marchand (et ses évolutions) a Artisanat d'office.
- [ ] **Anges et Péchés refaits** : Orgueil/Humilité comptent les pièces d'armure ; Tempérance = main vide ; Patience et
      Charité = membres du groupe à 15 blocs ; Colère = charges de rage ; Paresse ralentit le joueur qui frappe
      (`Eld_Paresse_Lenteur`) ; Luxure vole 10 % de l'or au kill ; Gourmandise soigne 20 % au kill et copie un passif ;
      Bienveillance : +10 % d'XP et 30 % au groupe.
- [ ] **Instinct de Survie** : signale aussi les joueurs invisibles (ancien Sixième Sens).
- [ ] **Admin > Compétences** : vues Passifs / Métiers / Effets ; donner un passif à un rang, la page, maîtriser, retirer ;
      effets : appliquer (durée), retirer, purger, voir les effets actifs. **Joueurs > Gérer > Passifs** : ses passifs,
      critique/esquive/défense/butin/vitesse/menaces, gestes.
- [ ] Les classes ont perdu leurs passifs « morts » (vitesse d'attaque...) : certaines n'ont plus qu'un ou deux passifs.
- [ ] **Onglet Compétences refait** (`scripts/system/gen_competences_ui.py` génère les 20 cartes de `SystemPage.ui`) :
      filtres Tout / Attaque / Magie / Défense / Vie / Endurance / Agilité / Chance / Divin / Métiers, ligne de résumé
      (actifs, appris / 50, maîtrisés, métiers), 6 pastilles de rang, étiquette CLASSE / APPRIS, rang suivant en gris, barre
      de maîtrise en 10 segments (dorée à 100 %), états ACTIF / DÉSACTIVÉ / RECHARGE. Cartes dans l'ordre 0 à 19 (avant :
      la carte 1 s'affichait après la 19). La fenêtre ne s'ouvre pas ou déconnecte → me le dire (erreur dans `*_client.log`).
- [ ] **Page d'un rang déjà donné par la classe** : refusée (« donné par votre classe »), ne prend plus de place sur les 50.
- [ ] **Admin > Compétences > Runes actives** : une carte par type (runes, possédées / équipées par les connectés, lancers) ;
      fiche : runes les plus lancées, jamais lancées, donner une rune ou son manuel, remettre les compteurs à zéro.
      **Joueurs > Gérer > Runes** : possédées, équipées, lancers, favorite. Lancers comptés à partir du nouveau jar
      (`rune_stats.json` dans les données du mod, sauvegarde toutes les 5 min et à l'arrêt). Nouvelle rune :
      `python3 scripts/runes/gen_runes_index.py`.

## Liste de contrôle — banquier, TP continental, aura, passifs (2026-11-24, suite)

- [ ] **Jar à reconstruire** : quitter le jeu, `./gradlew jar`, relancer. (La partie testée à 15 h 51 tournait encore avec
      l'ancien jar, construit à 16 h 17 : c'est pour ça que la Dignité 5 donnait encore la rune.)
- [ ] **Dignité 5** → « Manuel Spécial : Aura » dans le sac, plus la rune. Un joueur qui avait déjà la rune la garde.
- [ ] **Artistes martiaux** : pas de manuel d'Aura, la rune d'Aura renvoyée au sac s'ils l'avaient ; l'aura d'un autre joueur
      ne les ralentit pas et leur zone draconique ne les gêne pas.
- [ ] **Téléporteur Continental** : fenêtre façon Quêtes, une carte illustrée par destination (distance), fiche à gauche
      (prix, bourse, porte d'arrivée), bouton Voyager.
- [ ] **Banquier (refait, 3e version)** : `Npc/Banque.ui` (`scripts/npcs/gen_banque_ui.py`). À gauche « Vos avoirs » (sac,
      banque, total exact, dette) et le message de la banque. À droite 3 onglets :
      **Déposer / Retirer** : deux colonnes « Sac → Banque » / « Banque → Sac », **somme exacte en cuivres** (sa valeur en
      pièces s'affiche à côté), aperçu en direct, bouton « Déposer 1 234 cuivres », raccourcis Tout / 1 or / 1 diamant / 1 zénith ;
      **Change** : une ligne par paire de pièces voisines (petite à gauche, grosse à droite), boutons « 10 argent → 1 or » et
      « 1 or → 10 argent », texte gris à la place du bouton s'il manque des pièces, Tout regrouper en haut ;
      **Prêt** : somme exacte en cuivres aussi, plafond ou dette en grand, aperçu de ce qui sera dû, Emprunter (second clic),
      Rembourser ; règles à côté. La fenêtre ne s'ouvre pas ou déconnecte → me le dire (erreur dans le `*_client.log`).
      À vérifier : l'aperçu change-t-il pendant la frappe ou seulement en quittant le champ ?
- [ ] **Artistes martiaux** : ils peuvent apprendre et équiper les runes d'**assassin** et de **guerrier** (manuels compris) ;
      un guerrier ou un assassin ne peut toujours pas apprendre les techniques de la voie martiale.
- [ ] **Passifs sans particules** : plus aucun effet visuel au déclenchement d'un passif (peaux, frappes, Égides, Jugement
      de Kodha...) ; le Pisteur garde la teinte orange de la cible, sans papillons.

## Liste de contrôle — Mini-Boss et manuels de magie (2026-11-24)

- [ ] **Jar à reconstruire** : quitter le jeu, `./gradlew jar`, relancer.
- [ ] Bloc de poste › catégorie **Monstres** › **Mini-Boss** : apparaît à l'approche (dans un donjon aussi, sans porte), plus
      petit qu'un boss, tête de mort et barre de boss, niveau 60 par défaut ; abattu → message au tueur, retour après 5 min.
- [ ] **Manuels de Feu** (onglet créatif Runes › Feu) : livre rouge et doré, emblème de la rune sur l'icône ; maintenir le clic
      droit 2,5 s → livre ouvert, pages qui tournent, rune dans le sac, manuel consommé. Rune connue / classe inapte → il reste.
- [ ] **Boule de Feu** (refaite) : sphère lente et grosse, explose au contact (ennemi, mur, sol) ou en fin de course, dégâts
      de zone + projection + brûlure, terrain intact ; son manuel de Feu. La classe Mage la reçoit à la connexion.
- [ ] **Manuels des 12 types** (135 manuels, un livre par type : Feu, Glace, Foudre, Air, Terre, Lumière, Ténèbres, Temps,
      Espace, Spatial, Draconique, Combat) : vérifier quelques livres de chaque type dans l'onglet créatif (couleur, emblème)
      et une lecture par type. La quête de métier du Présage donne maintenant le Manuel du Temps : Présage.
- [ ] **Runes seulement par manuel** : à la connexion / au changement de classe, on reçoit les **manuels** de sa classe (plus
      les runes) ; Dignité 5 → **Manuel Spécial : Aura** ; un ancien parchemin utilisé se change en manuel ; le Labo gobelin ne
      fabrique plus de runes. Message de connexion « Manuels de classe reçus : … ».
- [ ] Manuels des 6 runes du jeu de base (Tir chargé, Frappe de vent, Frappe au sol, Imprégnation de poison, Grappin à chaîne,
      Rage) : la rune apprise doit marcher comme avant (effets du jeu de base).
- [ ] **Boule de Feu** : la rune existait dans le jeu de base (30 mana, 2 s) ; ma version la remplace (120 mana, 60 s, explosion
      de zone). Garder ma version ou revenir à celle du jeu ? (réponse)

## Liste de contrôle — carte du continent (2026-11-22, le soir)

Sauvegarde solo **« Eldanior - carte »** (`Saves/Eldanior_Carte`, Créatif), tirée de l'image `~/Documents/Maps`. Détail :
section 9 de `docs/plan-carte-eldanior.md`. Le mod doit être activé dans ce monde : son générateur fabrique le monde normal
de Hytale au-delà de la carte.

- [ ] Ouvrir le monde : apparition dans les plaines du centre (6, 160, 6).
- [ ] **Arbres sur les pentes** : entiers, pas coupés en escalier ni flottants.
- [ ] **Fleuves et rivières** : continus, eau calme, sans trous ni cascades bizarres ; **plages de sable** au bord de la mer
      et des rivières.
- [ ] **Montagnes** : pics, roche sur les pentes raides, neige au-dessus d'environ Y 212, pas d'arbres sur la neige.
- [ ] Biomes : taïga et prairies au nord, plaines et collines au centre, forêts d'automne au sud, bambouseraies avec glycines
      au sud-est (à garder ou non ?).
- [ ] **Bord de la carte** (au-delà d'environ 6 100 blocs du centre) : la mer puis le monde normal de Hytale, sans mur.
- [ ] Carte du jeu (M) : elle montre les couleurs des zones, pas le relief. Est-ce gênant ?

## Liste de contrôle — session du 2026-11-19 / 20 (territoires, boss, événements)

Cocher en jeu ; ce qui ne va pas → me le dire avec ce qu'on voit. Détail de chaque point juste en dessous (§ 0).

- [ ] **Jar à reconstruire** : quitter le jeu, `./gradlew jar`, relancer (nouveaux fichiers d'interface et de modèles).
- [ ] Borne : poser un **grand territoire**, une **forêt**, une **arène**, une **parcelle** (royaume aussi, crée ses 4 Marquisats).
- [ ] Borne en main : **contour en fil de fer** bloc par bloc des zones proches (couleur par type). Rien ne s'affiche → me le dire.
- [ ] **Logement acheté / loué** → ses **chambres** passent au joueur (affiche des chambres, loyers reçus, taxe à la ville).
- [ ] Registre de ville › **Trésor** : verser, retirer, vendre la ville (prix reçu par le vendeur), quitter la ville.
- [ ] **Boss de donjon** (poste dans une zone Donjon) : rien avant la porte ; clé → boss apparaît **figé 15 s**, alerte au groupe,
      porte refermée seule → combat ; après sa mort la porte s'ouvre sans clé.
- [ ] **Boss de zone** (poste hors donjon) : apparaît à l'approche, comme avant ; nouvelles espèces dans la liste du poste.
- [ ] Panneau admin › **Boss Mondial** : choix du boss (**Roi de la Mort**, **Ursaroth** ou au hasard), niveau au hasard
      500-999, **1 à 3 couronnes** (niveau ×1,2/×1,5/×2) ; annonces, alerte + coordonnées, **marqueur sur la carte ET la
      boussole** ; Roi de la Mort : **flammes** (sous la robe + visage) et **couronne(s) un bloc plus haut** ; Ursaroth : ours
      **noir, flammes rouges**, ×3, ses coups touchent ; annonce du vainqueur.
- [ ] Bloc de poste › catégorie **Monstres** : Monstre étoilé, **Boss de donjon** (refusé hors donjon), **Boss de zone**
      (refusé dans un donjon), **Boss Mondial** (annoncé, apparaît au poste dans 5 min).
- [ ] Bloc de poste › **Statut** : changer le statut d'un PNJ déjà posé (ex. garde Apprenti → Maître) change bien son rang ;
      la fenêtre rouverte affiche le statut choisi.
- [ ] **Carte** : se téléporter depuis la carte → les marqueurs **ne bougent plus**, la carte reste entière (plus de zone vide).
- [ ] **Carte v5 (esquisse)** : panneau admin › Carte du royaume › bouton **« conseillé »** (0,5 px/bloc pour ce royaume de
      100 806 chunks) ; « Royaume dessiné » doit monter vite jusqu'au total ; tout le royaume visible (relief + couleurs des
      biomes là où personne n'est allé) ; en s'approchant, la vraie image remplace l'esquisse. Journal : « royaume esquissé ».
- [ ] **Kokuō-Ryū** (Dragon Céleste Impérial, ×6) : panneau admin › Boss Mondial › l'annoncer ; il **vole** et ondule, s'incline
      dans les virages, se cabre en montée, pique, fait des tonneaux, déploie ses pattes près du sol, cligne des yeux ; morsure
      qui touche ; chute puis impact à la mort ; couronnes au-dessus des cornes.
- [ ] **3 nouveaux Boss Mondiaux** : **Vel'Kazhar** (dragon noir et or, ×1,5), **Gorgoth** (Rex charbon et lave, ×2),
      **Ignarok** (golem de fer ardent, ×3) : apparence, flammes, couronnes sur la tête, coups qui touchent.
- [ ] Panneau admin › **Invasion** (dans un domaine, pas en ville) : **faille** au sol (effet gobelin), marqueur carte, monstres qui
      sortent **un par un**, faille refermée à la fin.
- [ ] Panneau admin › **Carte du royaume** (v4) : activer, attendre « Royaume prêt » complet (fiche de la carte), se
      reconnecter → **carte entière dès l'arrivée, sans zone noire**, ne s'efface plus, dézoom jusqu'au royaume entier,
      **marqueurs sur leurs bornes**.
- [ ] **Boussole** sans les marqueurs des zones (Boss Mondial et failles y restent).
- [ ] Répondre aux questions de `docs/plan-frontieres-dynamiques.md` (règle pour la terre disputée, taille des cases, biens à cheval).

## 0. Territoires, boss et événements (2026-11-19, nuit) — compilé, jar NON reconstruit (partie en cours)

**[test] Borne : zones larges** : royaume, **grand territoire**, territoire, ville, **forêt, arène, parcelle** (`Bornes.TYPES`).
Logements, chambres, fermes, mines et donjons restent à la main. Un royaume posé crée encore ses 4 Marquisats.
**[test] Contour bloc par bloc** (`territory/borne/ZoneOutline`) : en tenant une borne, chaque bloc du bord de TOUTES les zones à
40 blocs est entouré d'un cube en fil de fer (forme de debug du moteur, paquet `DisplayDebug`), couleur du type. Piquets de
particules gardés tous les 8 blocs. À vérifier : les cubes s'affichent-ils (sinon : le client ignore les formes de debug hors
mode développeur → repasser aux particules).
**[test] Chambres d'un logement** (`ParcelManager.syncRooms`) : acheter OU louer un logement donne ses chambres (bailleur) ; les
loyers des chambres vont au bailleur, taxe à la ville. Fin de location → chambres au propriétaire ; logement rendu → sans maître.
Logements déjà pris : rattrapés au démarrage.
**[test] Trésor du registre de ville** (borne de ville / Bailli) : nouvelle catégorie **Trésor** : verser, **retirer**, **vendre la
ville** (prix versé au Comte vendeur, même absent — `LandOffice.ownerSale`/`credit`), **quitter la ville** (2 clics).
**[test] Boss de donjon / de zone** (`boards/DungeonBosses`) : poste DANS une zone Donjon → n'apparaît qu'à l'ouverture de la porte
de boss du même donjon (clé), **figé et invulnérable** (composants `Frozen` + `Invulnerable`) jusqu'à la fermeture automatique
de la porte **après 15 s** ; le groupe de l'ouvreur (ou lui seul) est prévenu (chat + titre), puis « le combat commence ».
Boss abattu → la porte s'ouvre sans clé jusqu'à son retour (pour sortir). Donjon vide 3 min → il se rendort. Poste hors donjon
= **boss de zone** (comme avant). **+31 espèces de boss** (`MonsterContracts.bossSpecies`).
**[test] Boss Mondial** (`boards/WorldBosses`, panneau admin › Tableau › Boss Mondial) : délai, niveau, durée ; annonce + rappels
(10/5/1 min, 30/10 s), lieu au hasard dans le royaume **hors des villes (+48 blocs)**, alerte avec coordonnées, **marqueur sur
la carte** (icône couronne `Eldanior_BossMondial.png`), le monstre se matérialise quand un joueur approche (72 blocs).
Premier boss : **Roi de la Mort** (`scripts/monstres/gen_roi_mort.py`) : Wraith du jeu, robe blanche, sangles noires, lueurs et
flammes violettes/noires, ×5, faux à portée ×3, **couronne d'or** (`Couronne_x5`, gen_emblemes_etoiles.py), 2 500 PV de base.
À vérifier : taille, couronne bien au-dessus de la tête, portée des coups, flammes visibles, icône de carte.
**[test] Invasion = faille** (`territory/laws/Invasions`) : une faille (effet et son de la faille gobeline, bloc
`Eldanior_Faille_Invasion`, `scripts/territoire/gen_evenements.py`) s'ouvre à 24-40 blocs d'un défenseur, jamais en ville,
marquée sur la carte ; les monstres en sortent **un par un** (1,5 s) ; refermée à la fin (ou au redémarrage). Lancement
**depuis le panneau admin** (Tableau › Invasion : vagues, niveau, prime) sans commande.
**[test] Carte du royaume** (`territory/systems/KingdomMap` + `MapCache`, panneau admin › Tableau › Carte du royaume, coupée par
défaut). Historique : v1 « zone fixe » → **carte noire** (chargement ligne par ligne depuis le coin nord-ouest) ; v2 rayon
recalculé toutes les 5 s → **la carte s'effaçait** en s'éloignant / se reconnectant (2026-11-20 : « la map visitée doit rester
chargée », « quand tu refresh tu oublies », « curseur limité »). **v3 (2026-11-20)** : 1) **mémoire sur disque** de chaque
image (`eldanior_data/carte/<monde>/r.X.Z.bin`, régions de 32×32 chunks) resservie sans recharger le chunk ; 2) images refaites
seulement à moins de 32 chunks d'un joueur, chunks du royaume jamais vus générés du plus proche au plus loin, hors royaume
seulement les lieux visités ; 3) rayon de carte par joueur qui couvre le royaume et **ne rétrécit jamais** ; 4) **dézoom** plus
large (minScale 6 au lieu de 32). **v4 (2026-11-20, « à l'arrivée la map chargée entièrement, sans zone noire, marqueurs à
leur position »)** : 5) **préparation du royaume en tâche de fond** (une seule fois, chunks jamais visités générés et
sauvegardés, avancement « Royaume prêt X / Y » dans le panneau) ; 6) **envoi en bloc à l'arrivée** de toutes les images
gardées (paquets ~1,5 Mo, régions les plus proches d'abord), notées dans le suivi du moteur par réflexion (champ privé
`loaded`) ; 7) marqueurs de zone posés sur leur **borne**. **À revérifier en jeu (jar à refaire)** : attendre « Royaume prêt »
complet, se reconnecter → carte entière sans noir ; marqueurs au bon endroit ; tenue du client (sinon 1 px/bloc).
**[test] Boussole** : les marqueurs de zone n'y sont plus (image de boussole transparente `Eldanior_Vide.png`) ; Boss Mondial et
failles y restent (comme la faille gobeline). Marqueurs de forêt sur la carte.
**[test] Série du 2026-11-20 (soir)** — compilé, jar NON reconstruit (partie en cours) :
- **Carte** (`KingdomMap`) : cause trouvée dans le moteur : `WorldMapTracker.setViewRadiusOverride` appelle `clear()`
  (ClearWorldMap au client). Le rayon était recalculé depuis la position du joueur → après une téléportation il grandissait,
  la carte du client était vidée (marqueurs qui « suivent » le joueur), puis seule la spirale du moteur la remplissait autour
  de lui. Désormais rayon **fixe** = diagonale du royaume, posé une fois ; s'il doit changer, tout le royaume est renvoyé en
  bloc. **Trous** : un chunk parti vide (pas encore d'image) est redemandé dès que son image existe (`clearChunks`).
- **Boss Mondial** (`WorldBosses`) : boss au choix ou au hasard, niveau de base au hasard **500-999**, **1 à 3 couronnes** au
  hasard = même bonus que les étoiles (`eliteFor` : ×1,2/×1,5/×2 → le niveau affiché peut dépasser 999, rang SS/EX),
  couronnes côte à côte (`Couronnes_<N>_<Roi|Ours>`, gen_emblemes_etoiles.py), marqueur de carte agrandi + **alerte de
  boussole** du jeu (`MapMarkerAlert`, 30 s dès qu'il entre dans le champ de la boussole). Récompense en or : aucune pour
  l'instant (l'XP suit le niveau) → **[réponse]** faut-il une prime au vainqueur multipliée par les couronnes ?
- **Roi de la Mort** : le moteur multiplie décalages et tailles des particules par l'échelle (×5) : la flamme du haut de la
  capuche flottait 1,5 bloc devant, dans la couronne. Maintenant traîne de flammes sous la robe + feu dans le visage ;
  couronnes **+1 bloc**.
- **Ursaroth, l'Ours des Braises Noires** (`scripts/monstres/gen_ours_braises.py`) : Ours du Vide du jeu (charge, frappe en
  cône, tentacules), fourrure noire, crinière en braise, yeux/gueule rouges, flammes rouges (crinière, dos, pattes), ×3,
  3 000 PV de base, dégâts ×6, coups de base portée ×2,5. À vérifier : la frappe en cône et la charge (non élargies, gabarit du
  jeu) touchent-elles à cette taille ?
- **Catégorie Monstres** du bloc de poste (`NpcPostPage`) : étoilé, boss de donjon, boss de zone (séparés), Boss Mondial ;
  Hors-la-loi = criminels seulement.
- **Statut des PNJ** : 2 défauts corrigés : 1) le Niveau pré-rempli avec celui du PNJ en place écrasait le nouveau statut
  (un garde Apprenti passé en Maître restait Apprenti) ; 2) le statut n'était gardé que pour les PNJ sans rang → la fenêtre
  rouverte affichait « Au hasard ». Gardé pour tous ; « · Expert » dans le nom seulement pour les PNJ sans autre rang.

**[test] Carte v5 — esquisse (2026-11-20, nuit ; « c'est toujours pas ça »)** : le journal montrait un royaume de
**100 806 chunks** dont ~17 000 avec image, et la préparation bloquée (« paquet trop lent » toutes les 2 min) : le moteur
(ImageBuilder) doit charger le chunk ET ses 8 voisins, donc générer le terrain jamais visité. Désormais `MapSketch` dessine
chaque chunk sans image directement depuis le **générateur de monde** (`ChunkGenerator.getHeight`, `Biome.getMapColor` =
champ MapColor des biomes du jeu, hauteur d'eau du biome, ombres du relief), sans créer de chunk : préparation de tout le
royaume en quelques minutes + esquisses à la volée quand le moteur demande un chunk. Gardées à part
(`eldanior_data/carte/<monde>-esquisse-w<largeur>`), un dossier par finesse (changer de finesse ne laisse plus de trous).
La **vraie image** remplace l'esquisse dès qu'un joueur passe à moins de 10 chunks. Finesse **conseillée** selon la taille
(≤ 20 000 chunks : 3 px/bloc ; ≤ 50 000 : 1 ; au-delà : 0,5 — à 3 px/bloc, 100 000 chunks ≈ 1 Go d'images pour le client).
Dézoom poussé (minScale 2). **Non testé en jeu** : couleurs et vitesse à vérifier.
**[test] Boss Mondiaux 3 à 5** (`scripts/monstres/gen_boss_mondiaux.py`) : Vel'Kazhar (Dragon_Frost noir et or, flammes
d'or au front et à la queue, morsure sans animation portée 9,5, 4 000 PV), Gorgoth (Rex_Cave charbon, plumes en lave, feux
sur l'échine, 3 500 PV, morsure élargie ×2), Ignarok (Golem_Firesteel fer sombre/lave vive, 7 coups copiés portée ×2,6,
4 500 PV) ; dégâts ×6. Couronnes à leur hauteur (`CROWN_BOSSES` ; celle d'Ursaroth corrigée : échelle 3, pas 4).

**[test] Boss Mondial 6 : Kokuō-Ryū, le Dragon Céleste Impérial** (2026-11-22, v3, `scripts/monstres/gen_ryu_celeste.py` +
`boards/RyuFlight.java`) : Ryū noir et or construit de zéro, **×6 (~60 blocs de long)**, 9 000 PV, **vole** (gabarit volant du jeu),
morsure en plongée (portée 24, jusqu'à 14 blocs vers le bas, 650 dégâts). **Système d'animation** : « meneur et suiveurs » (la
tête initie, chaque segment suit avec un retard croissant, amplitude croissante vers la queue ; ondulation latérale +
verticale = ellipses, roulis propagé = tire-bouchon) ; 17 animations : Hover, Fly, FlyFast, FlyClimb, FlyDive, FlyTurnLeft/Right,
FlyLand, FlyRoll (tonneau), Alerted/Roar, Hurt, HurtFly, DeathFly (chute) + DeathFlyImpact (impact), Idle (enroulé au sol), Bite,
Blink. Le moteur choisit Hover/Fly/FlyFast/HurtFly/DeathFly/DeathFlyImpact ; `RyuFlight` (4×/s) joue virages, montée, piqué,
approche du sol et tonneau en emplacement **Status**, le clignement en **Face**. À vérifier en jeu :
- les manœuvres remplacent bien le vol du moteur en Status (sinon : passer en Action, mais la morsure les coupera) ;
- sens des virages (s'il s'incline à l'envers : `RyuFlight.TURN_SIGN = -1`), seuils (montée/piqué 2,5 bloc/s, virage 22°/s) ;
- clignement (paupières cachées `*-Blink_Lid` qui apparaissent) ; tonneau toutes les 25-45 s en vol rapide ;
- la morsure touche un joueur au sol ; chute + impact à la mort ; couronnes au-dessus des cornes ; perf. (~730 nœuds, ×6).
- v4 (« pas assez ample, on dirait une ligne ») : deux vagues complètes le long du corps, amplitudes ±48° (côté) / ±42° (hauteur),
  roulis dans chaque courbe, harmoniques, 45-72 images-clés ; traînées dorées (queue, moustaches) et rugissement
  `SFX_Eldanior_Ryu_Roar` (ours du Vide, plus grave) : vérifier taille des traînées à ×6 et volume du rugissement.
- v5 (« transitions qui saccadent ») : le moteur joue la blessure (à chaque coup) et la mort dans **Status** ; plus de Hurt
  dans le modèle (le moteur n'y touche plus), sursaut tête/cou/mâchoire en **Face** quand les PV baissent ; `RyuFlight` joue
  EN PERMANENCE le vol dans Status (Hover/Fly/FlyFast/manœuvres), changements synchronisés en fin de cycle de 2,5 s (même
  phase de vague) ; morsure limitée à l'avant. À vérifier : plus d'à-coups sous les coups, enchaînements fluides, délai des
  manœuvres (jusqu'à 2,5 s) acceptable, reprise du vol après le rugissement.

**[à faire] Boss Mondiaux : compétences et phases** (demande du 2026-11-20) : leur donner des compétences propres (attaques
spéciales, invocations, zones) et des **phases** selon la vie restante (ex. 66 % / 33 % : nouvelles attaques, enrage, aura,
renforts). Roi de la Mort et Ursaroth n'ont aujourd'hui que les attaques des monstres du jeu (Wraith, Ours du Vide).

**[réponse] Frontières dynamiques** : idée développée dans `docs/plan-frontieres-dynamiques.md` (cases de 16 blocs, terre
contestée, guerres de territoire puis de royaumes). Questions à la fin du document.

## 0 ter. Décisions du 2026-11-19

**[test] Tableau de guilde en cartes** (2026-11-19, `quest/chapitres/GuildQuests`, `boards/GuildBoard`) : le bloc
`Eldanior_Tableau_Guilde` ouvre la fenêtre des quêtes (Chapitres) ; contour **rose** (teinte « Rose » de
`scripts/quetes/gen_chapitres_ui.py --tints`). **Quêtes de guilde** = les 3 contrats de la semaine (avancement commun à la
guilde, récompense au trésor ; carte « En cours » d'office pour un membre, « Suivre » l'affiche au HUD ; hors guilde :
refusée). **Missions de guilde** = les 6 missions du jour, devenues de vraies quêtes de chasse (HUD, onglet Quêtes), au plus
2 à la fois **hors de la limite des 2 quêtes** (comme avant), rendues au tableau (pièces + XP avec bonus de la guilde,
contribution), bouton Abandonner. Illustrations existantes selon la famille de monstres. Les missions prises avant la mise à
jour sont perdues. Le **classement des guildes** (tri contribution / monstres / joueurs / trésor, fiche) est passé au
**tableau des classements** : onglets Chasseurs, Combattants, Guildes, Familles. Jar non reconstruit.
**[test] Rang SSS renommé EX** (2026-11-19) : monstres au-delà du niveau 1500 ; même couleur violette, blason `QHudRankEX`.
**[test] Le Comte verse l'or de sa bourse au trésor de sa ville** (2026-11-19, `guards/IntendantPage`, action `cityfund`) :
borne de ville › fiche de la ville › champ « Verser au trésor de la ville » + bouton **Verser au trésor** (compte du joueur →
trésor de la ville, ligne au livre de comptes de la maison s'il y en a une). Jar non reconstruit.
**[test] Onglet Observateur = panneau du menu Système** (2026-11-19, `classes/observateur/ObservateurCourTab`) : le bouton
ouvre la Cour **à droite des onglets** (panneau `#TabObservateur` de `System/SystemPage.ui`, injecté par
`scripts/observateur/gen_ui.py` entre les marqueurs `>>> Observateur` / `<<< Observateur`), bouton allumé comme les autres
onglets. Plus de fenêtre à part : `ObservateurCourPage` et `Classes/ObservateurCour.ui` supprimés ; « Ma charge » (Sans
Visage) et `/es observateur menu` ouvrent le menu Système sur ce panneau. Jar non reconstruit. À vérifier : ouverture du
menu (sinon `*_client.log`), largeur des rangées, champ du nom d'emprunt.

## 0 bis. Décisions du 2026-11-18 et tâches qui en découlent

**[test] Intendant refait le 2026-11-18** (nouvelle fenêtre, rangs Chevalier / Chevalier Royal, coffres, Taxe Impériale,
revente de domaines, quêtes de la maison, décrets, Comptoir…) : détail et points à vérifier dans `docs/plan-intendant.md`
§ 5 quater et § 6. Jar non reconstruit (partie en cours) : quitter le jeu, `./gradlew jar`, relancer.
**[test] Intendant, 4e passe du 2026-11-18** (`docs/plan-intendant.md` § 7.8) : pas de biens dans les Domaines, ordres sans
limite à 1 M sur les coffres (Grand Maître choisi), recrutement sur demande / ordres d'élite, rangs automatiques (Doyen = Duc
et Patriarche d'une maison ducale du Marquisat, Noble = Comte / Baron, Chevalier Royal = Chevalier), titres en plus pour le
Marquisat Luo, terres achetées chez l'intendant seulement. Corrigé ensuite (§ 7.9) : villes de clan hors de leur
territoire rangées sous leur clan ; villes de maison sans maître à vendre d'office (le Patriarche peut les retirer).
**[test] Boutique des marchands refaite façon intendant** (2026-11-18, `scripts/npcs/gen_shop_ui.py` → `Npc/Shop.ui`,
`guards/ShopPage.java`) : habillage Chapitres comme l'intendant ; à droite 11 catégories à icône (premier article de la
catégorie), réplique du marchand, 4 tuiles (articles, à votre portée, réassort, taxe / au-dessus du niveau ; en Rachat :
objets rachetés, dans le sac, valeur totale), grille de 20 cartes (nom à la couleur de qualité, prix en pièces, stock, lot,
ingrédients, niveau) ; à gauche nom du marchand, grande icône, fiche, prix / stock, ingrédients, quantité, total, Acheter /
Vendre ; bourse en pièces en bas (+ compte en banque) et Fermer. Vaut pour tous les commerçants, Sensei, forge légendaire,
réserve de l'intendant. **Jar non reconstruit** (partie en cours). À vérifier : ouverture (sinon `*_client.log`), rangées de
pièces alignées dans les cartes, cadre de la carte choisie à la couleur du marchand.
**[test] Lois des domaines** (2026-11-18, `territory/laws/DomainLaws` + `DomainLawSystem`, chez l'intendant : Domaines ›
fiche › Règles du domaine › Lois du domaine) : **couvre-feu** (heures du jeu, amende — choix de l'utilisateur — versée au
trésor du domaine, 60 s pour rentrer chez soi, puis toutes les 5 min ; impayable = gardes ; boutiques fermées ; Chevaliers et
plus de la maison exemptés ; chez soi = sur un bien où l'on a des droits) ; **armes rengainées** (katana au fourreau permis) ;
**frontières** ouvertes / droit d'entrée (prélevé après 10 s, valable 2 jours) / fermées (les gardes attaquent ; gens de la
maison, maisons vassales et suzeraines, habitants et leur groupe exemptés) ; **bannis** (par nom, joueur connecté ; gardes à
vue) ; héraut dans le chat. À vérifier en jeu : heure du couvre-feu, amende prélevée (pièces puis banque), gardes qui
attaquent un banni, droit d'entrée.
**[test] Portail Continental** (`territory/teleport/`, refait le 2026-11-19 après essai : « pas d'effet ») : le petit bloc
téléporteur est **supprimé** (demande de l'utilisateur). Pierre de fondation `Eldanior_Portail_Continental` (admin, posée en
la regardant de face dans un Marquisat) → plateforme 13 x 13 (bordure ornée, anneau d'aqua, damier de calcite, allée et seuil
d'aqua), 4 tours d'angle à brasero, 2 lanternes, porte **7 × 9** (colonnes à empattement, linteau à clef d'aqua, arc,
couronne au saphir, lanternes sur les colonnes) et le Gardien du portail (devant à droite). Payer le Gardien remplit
l'ouverture d'un **voile translucide** (`Eldanior_Voile_Portail`, traversable) et de **l'effet du téléporteur du jeu agrandi**
(`Eldanior_Portail_Continental.particlesystem`, échelle 2,6, centre à 3,5 blocs : `scripts/portail/gen_portail.py`) pendant
30 s ; traverser → devant le portail de destination. Le Marquis ouvre / ferme et fixe le prix chez son intendant.
`/es portail info|retirer` (admin). À vérifier : taille et orientation de l'effet (régler FX_SCALE / FX_Y dans le script),
blocs qui tournent mal (lanternes, braseros), voile visible.
**[test] Invasions** (`territory/laws/Invasions`, `/es invasion lancer|arreter|liste [vagues] [niveau] [prime]`, **admin
seulement**) : sur le domaine où se tient l'admin ; vagues toutes les 75 s (ou dès la précédente battue) autour des joueurs
présents, **jamais dans une ville** (ni à 4 blocs de ses murs) ; victoire = annonce des défenseurs + prime partagée au prorata
des monstres tués ; défaite (temps écoulé) = 10 % du trésor pillé. À vérifier : apparitions au sol, monstres qui marchent sur
les joueurs, fin de l'invasion.
**[test] Doyens d'office** (2026-11-18, `HouseRoles.vassalDukes`) : Marquis → Doyens de la maison Eldanior, Ducs → Doyens de
la maison du Marquisat, Comtes (maîtres d'une ville d'un Duché) → Doyens de la maison du Duché.
**[test] Registre de ville du Comte** (2026-11-19 ; borne + Bailli + `/es ville`) : la **borne de ville** est un bloc à
part (`Eldanior_Borne_Ville`, **apparence du `Bench_Abilities` du jeu** (modèle, lumière, particules) à la demande de
l'utilisateur, `guards/CityBornes`), posée par le Comte dans sa ville
(une par ville), reçue à l'achat d'une ville ou par « Recevoir la borne de ville » dans le registre ; F = registre pour le
Comte (ou un admin), lois de la ville pour les autres. La borne de territoire redevient un bloc d'admin seulement.
**Corrigé le 2026-11-19** : la borne et `/es ville` plantaient (« this.familyId is null » dans le journal) pour une ville
dont ni elle ni son Duché n'ont de maison ; le registre prend une maison vide et n'écrit plus au livre de comptes dans ce cas.
Registre : Domaines, Biens (trésor, règles, lois, biens, « Percevoir » la moitié du trésor si la ville n'a ni maison ni
guilde, Bailli à 2 000 or par semaine, `guards/CityStewards`).
**[test] Achat d'une ville** (2026-11-19, signalé : « je ne peux pas acheter de ville alors que je suis Comte et 100 M ») :
paiement avec les **pièces puis le compte** (avant : le compte seul), refus affiché en rouge dans la fiche ; un admin n'est
plus soumis à la règle du 13/11 « Comte chef de guilde ou Capitaine d'un Ordre ». **[réponse]** garder cette règle pour les
joueurs ?
**[test] Boutique, colonne de gauche refaite comme l'intendant** (2026-11-19) : emblème compact, prix en pièces pleine
largeur, 6 faits (catégorie, qualité, niveau, lot, stock, dans votre sac ; en Rachat : vous en portez, prix unitaire, tout
vendre, barème), bloc « Achat / Vente » : **total en cases de récompense** (une pièce par case, nombre en bas à droite,
comme les récompenses des quêtes), **barre de quantité en dessous**, puis le grand bouton.
**[test] Fenêtre de l'intendant, colonne de gauche refaite** (2026-11-19) : emblème compact, grille de faits (clé / valeur),
section Actions (principale pleine largeur, gestes destructeurs en rouge) ; **profil d'un membre** : rang, niveau, classe,
titre, ordre, présence (lieu), terres, biens, guilde, combats, exploration, prime, mentions au livre de comptes ; faits d'un
domaine (trésor, population, taxe, présents, couvre-feu, frontières, invasion en cours).
**[test] Borne de ville en bloc personnalisé** (2026-11-19, choix de l'utilisateur : stèle à blason) : `scripts/territoire/gen_borne_ville.py`
→ modèle `Blocks/Eldanior/Territoire/Borne_Ville` (socle de basalte, fût de calcite à pilastres d'or, bannière d'azur, blason de
gueules à la tour d'or, plaque du registre, chapiteau, couronne et cristal), icône, hitbox 1 x 2, indice « registre de la
ville ». Remplace l'apparence du `Bench_Abilities`. Assets validés sur serveur isolé. À vérifier : orientation à la pose, lumière.
**[test] Fenêtre `/es admin` refaite façon intendant** (2026-11-19) : reprend `Intendant/Intendant.ui` ; catégories Tableau,
Joueurs (profil, puis « Gérer » : une carte par geste), Monde (domaines / biens, prix et loyer en champs, gestes avancés),
Économie, Hologrammes, Données (remises à zéro au second clic). L'ancien `Admin/AdminPage.ui` est supprimé.
**[test] Règles du domaine refaites** (2026-11-19, chez l'intendant) : « Règles du domaine » ouvre une grille d'une carte par
règle (combat, blocs, annonce, couvre-feu, armes, amende, frontières, droit d'entrée, bannis, Portail Continental), état en
valeur et liseré coloré ; la fiche de gauche montre la règle choisie, ses champs et ses gestes (choix explicites pour
l'annonce et les frontières au lieu d'un bouton qui tourne en boucle).
**[plus tard] Prospérité, garnison payée, bâtiments du domaine, prison, foire, péages, guerre entre maisons** : proposés le
2026-11-18, pas encore choisis.
**[test] Dieu Martial** : plus d'évolution depuis le Maître Suprême ; `/es setclass <joueur> dieu_martial`.
**[test] Intendant, 3e passe du 2026-11-18** (`docs/plan-intendant.md` § 7) : domaines en descente par niveaux avec population,
catégorie **Titres** chez tous les intendants (quotas personnels et cumulatifs), **Royaume** (Taxe Royale des Marquisats) à la
place de Couronne, hiérarchie à 7 rangs dans les ordres, livre de comptes en cartes, rappels à la connexion. Essai :
`/es intendant fictifs ajouter` (5 joueurs fictifs Luo + un ordre), `retirer` pour les effacer. **Jar non reconstruit**
(partie en cours) : quitter le jeu, `./gradlew jar`, relancer.

À faire (décidé) :
- **[à faire] Famille Luo : 7 Mouvements au lieu de 12**, un par rang martial (Apprenti 1, Disciple 2, Expert 3, Maître 4,
  Grand Maître 5, Saint 6, Maître Suprême 7). Supprimer les manuels `Eldanior_Mouvement_Luo_08` à `_12` (objets, modèles,
  icônes, `FamilyMovements`, filtre du Sensei, catégorie créative).
- **[à faire] Rang martial plafonné à Maître Suprême** pour les joueurs ET les PNJ : plus personne ne devient Dieu Martial
  (promotion, tirage des PNJ, poste, `VoieMartiale`). **Dieu Martial reste une classe** (celle du Patriarche Luo, la classe
  de l'utilisateur, comme Dragon) : **aucun changement de contenu**, seulement retirée des évolutions du niveau 999 et
  **attribuée par commande comme Dragon** ; une quête pour la devenir viendra peut-être plus tard.
- **[à faire] Les 4 clans (Sama, Hanazakura, Kage, Setsuka) sont vassaux du Clan Luo** (proposition A validée).
  **Nighthollow et Swiftquiver ne doivent plus exister** (remplacés par les quatre clans) : familles, duchés, objets.
- **[à faire] Observateur exécuté : les reliques de famille sont exclues de l'effacement** (rendues à la famille).
- **[à faire] PNJ Exclusifs = PNJ non personnalisables** (Dresseur, Commerçant immobilier, Maître de guilde, Sensei,
  Forgeron légendaire…) : refaire la catégorie du poste en conséquence (pas de statut ni de spécialité à régler pour eux).
- **[à faire] Forgeron légendaire : retirer Durgrim et Elarion**, seul Tetsugen Luo reste.
- **[à faire] Tomes Impériaux** : les rendre trouvables par les quêtes, les butins et les PNJ.
- **[à faire] Infobulle d'une rune qui reste à l'écran** : souris au-dessus d'une rune du menu Système + Échap → la fenêtre
  se ferme mais la description de la rune reste affichée.
- **[à faire] Glisser-déposer des runes : intervertir** quand la case visée est occupée (rune posée en case 1 alors qu'il y
  en a déjà une : les deux échangent leur place).
- **[à faire] Plus de Dungeon Boss et de monstres étoilés** (espèces, emplacements).
- **[à faire] Quêtes plus réactives en temps réel** (avancement, HUD, rendu).
- **[à faire] Contrats de prime : vérifier qu'on ne touche pas la prime d'un contrat** qui n'est plus sur le tableau de
  prime et de contrats (soupçon de l'utilisateur).
- **[à faire] Boutiques à retravailler** (économie) ; prix inchangés pour l'instant.

Réponses enregistrées (rien à coder) :
- Ki de l'Épée et Méditation : **aucun changement** (toujours donnés au serment).
- Mousquetaire : classe commune, **tirage aléatoire**, pas de garantie dans les évolutions de l'Archer.
- Dragons étoilés : les étoiles actuelles conviennent.
- Compétences des familiers : conviennent pour l'instant ; évolution du système plus tard.
- Délais des quêtes : **24 h journalières, 48 h secondaires** confirmés ; rester à 5 min tant qu'on teste.
- Prix de l'économie : pas encore modifiés, à revoir plus tard.
- Observateur, propriétés : « ça va / c'est mieux » pour l'instant.

Mis de côté **[plus tard]** : signature et retrait des lames de l'Observateur ; tenues des familles sans tenue ; bugs
d'interaction des gardes ; création des nouvelles quêtes.

## 1. À vérifier en jeu (fait du 2026-11-10 au 15, jamais vu)

### Série du 2026-11-16 (jamais vue en jeu)

- **[test] Classe unique Observateur** (ex-« Arkies Domini », renommée le 2026-11-17 ; `classes/definitions/Observateur`,
  `classes/observateur/*`, `Classes/Observateur.ui`) — « l'Œil et les Oreilles de l'Empereur, l'assassin privé du Roi ».
  Donnée seulement par le **Roi** ou l'**Empereur Luo** (= Patriarche du Clan Luo), **un joueur par trône** (deux au plus) :
  `/es observateur proposer --joueur <nom>` ouvre chez lui une fenêtre (valable 2 min) ; accepter = **simple changement de
  classe** (niveau, XP, caractéristiques, runes, objets gardés). **Toutes les armes** (armes à feu, artefacts de classe ET de
  famille, reliques de Patriarche comprises) et **toutes les runes / parchemins / manuels Luo**. `revoquer` (souverain) et
  `renoncer` (titulaire) rendent la classe d'avant (à la connexion s'il est absent). La place appartient au trône.
  Registre : `<dataDir>/classes/observateur.json`. Qualité **Légendaire**, bonus 500/380/520/420/1000/700 (total 3 520),
  passifs Vue Absolue, Sens du Danger, Précision Absolue, Esquive de l'Ombre, Maîtrise de la Lame d'Ombre ; **voit le marché
  noir** ; **entre sur le territoire Luo sans plaque** ; **nom d'emprunt** `/es observateur nom --nom <nom>` (nameplate seul,
  le chat et la liste des joueurs montrent le vrai nom).
  **Exécution** (2026-11-16) : `/es observateur executer --joueur <nom exact>` par son souverain = personnage effacé
  (`Leveling/utils/CharacterWipe`, sorti de la banque : niveau 1, Novice, tout perdu, inventaire vidé, poche et sac remis à
  zéro, point d'apparition) ; absent : à sa connexion (+3 s). **[à faire]** reliques de famille exclues (réponse du 2026-11-18).
- **[test] Rune Arkies Domini** (`Rune_ArkiesDomini`, Ténèbres, Légendaire, `spells/ArkiesDomini`, HUD `hud/ArkiesDominiHud`
  + `Hud/ArkiesDomini.ui`) — compétence de l'Observateur, à bascule : **1,5 % du mana max de base par seconde** (10 au moins),
  **caractéristiques ×2** (`PlayerLevelData.statMultiplier`, vie et mana courants gardés à l'activation ; filet de sécurité
  dans `PlayerNameplateSystem`), domaine de **100 blocs** : silhouettes des 40 présences les plus proches, doubles rouges des
  12 plus proches cachées par un mur (`WallGhosts`, partagé avec le Sens martial), invisibles révélés ; **écoute** dans le chat
  (arrivées/départs de joueurs, combats, morts, avec distance et côté) ; **un regard** = carte complète dans le HUD.
  **Carte refaite le 2026-11-17** (`scripts/observateur/gen_ui.py`) : liseret de couleur (joueur / PNJ / créature), nom,
  niveau, **modèle 3D dans la carte** (élément `ItemPreviewComponent` des fenêtres d'inventaire du jeu, ItemId = portrait
  caché `Eldanior_Visage_<modèle>`, 398 objets générés par `scripts/observateur/gen_visages.py`), barres de vie et de mana,
  6 caractéristiques, classe (la fausse de Sans Visage + « en vérité : … »), rang/maison, détails, distance, « derrière un
  mur ». La miniature qui flottait dans le monde est supprimée. **Limite** : pas de portrait pour les modèles sous
  `Characters/` (joueurs, gardes du mod, Outlanders, Elfe, Père Noël…) car un objet n'a le droit qu'aux dossiers
  Blocks/Items/Resources/NPC/VFX/Consumable (le mod refusait de charger) : la carte montre « ? ». Les attachements (cheveux,
  armures) ne sont pas dessinés. **À vérifier** : le modèle 3D s'affiche-t-il bien dans un HUD (jamais essayé), sa taille
  (réglage `ItemScale` possible), la rotation, la lisibilité.
  **Refonte du 2026-11-17 (2ᵉ passe)** : la carte prend le style des cartes de quête du HUD (contour coloré selon le genre :
  or joueur, bleu PNJ, rouge créature ; fond `QHudPatch` ; **blason du rang de menace F→EX** calculé sur le niveau comme
  les quêtes ; caractéristiques avec les icônes du menu Système ; lignes vides masquées). **Place** : 320 px de large, à
  droite **sous le panneau de groupe** (Top 334, le groupe s'arrête à ~320 px) : plus de chevauchement. À vérifier : rien
  d'autre du jeu à cet endroit, hauteur totale (≈ 400 px au plus) au-dessus de la barre d'action.
  **Corrigé le 2026-11-17 (3ᵉ passe)** : (1) **stats et dégâts faux** : le ×2 était posé pendant le tick du monde, le recalcul
  (`StatCalculator`) plantait (« Store is currently processing! » à chaque activation) → stats à moitié appliquées, soin à
  l'activation, défense doublée sans vie max doublée ; maintenant différé (`cb.run`). (2) **fond coloré trop grand** : le
  contour s'étirait sur toute la zone réservée ; il suit maintenant le contenu ; créature en rouge orangé (#d9573f) au lieu
  du rose. (3) **gros modèles coupés** : chaque modèle est mesuré (`gen_visages.py`, `scales` dans `visages.json`) et l'aperçu
  est réduit (`ItemScale` = 1,4 bloc / plus grande dimension, entre 0,12 et 1 ; ours 0,52, vache 0,47, dragons 0,13).
  À vérifier : stats correctes avant/pendant/après la rune, dégâts reçus, cadrage des modèles (régler `FIT_BLOCKS`).
- **[test] Rune Sans Visage** (2026-11-17, `Rune_SansVisage`, Ténèbres, Légendaire, `spells/SansVisage`,
  `classes/observateur/SansVisagePage` + `Classes/SansVisage.ui`) — 2ᵉ compétence de l'Observateur : ouvre une fenêtre.
  Onglets **Joueurs** (joueurs du même monde : apparence ET nom copiés), **PNJ** (100), **Animaux** (214), **Monstres** (103),
  recherche, 15 par page, aperçu 3D avant de payer ; **visage au hasard** (`CosmeticsModule.generateRandomSkin`) ; **nom
  d'emprunt** ; **classe apparente** (liste déroulante de toutes les classes, vue dans le groupe et par les autres ; l'Œil
  d'Arkies voit la vraie) ; « Reprendre mon visage ». **300 mana par nouveau visage**, fenêtre gratuite. Sous une forme de
  créature sans nom d'emprunt, aucun nom ne flotte au-dessus de lui. Le masque tombe à la mort, à la déconnexion (apparence
  non sauvegardée ; le nom d'emprunt reste) et à la Sentence Silencieuse. **À vérifier** : animations et caméra dans un corps
  de créature (hitbox d'un gros monstre), retour au vrai skin, fourreaux des Lames de l'Observateur après un changement de
  modèle, liste déroulante de ~400 classes.
- **[test] Rune Sentence Silencieuse** (2026-11-17, `Rune_SentenceSilencieuse`, Ténèbres, Légendaire,
  `spells/SentenceSilencieuse`) — 3ᵉ compétence, **technique ultime d'assassinat** : cible sous le viseur (40 blocs ; avec
  Arkies Domini ouverte, la présence visée par l'Œil, à travers les murs, 100 blocs), figée 1,2 s, l'Observateur devient
  invisible 2,4 s, surgit **dans son dos** (sinon sur ses côtés) et frappe : créature **exécutée sous 40 %** de vie sinon −35 %
  de sa vie max ; joueur exécuté sous 30 % sinon −25 % ; **boss jamais exécutés** (−15 %) ; masqué par Sans Visage : seuil
  +10 points mais le masque tombe. **1 200 mana, 300 s**, pris seulement si une cible est trouvée. **À vérifier** :
  téléportation dans le dos, dégâts réels après armure, exécution des joueurs en zone PvP.
- **[test] Cour de l'Observateur** (2026-11-17 ; **devenue un panneau du menu Système le 2026-11-19**, voir § 0) —
  remplace les commandes : **bouton Observateur dans la barre du menu Système** (Roi, Empereur Luo, admins ; un admin choisit
  le trône), ou `/es observateur menu`. Souverain : son Observateur (en ligne, nom d'emprunt, déguisement, ancienne classe),
  **Révoquer**, **Exécuter** (deux clics) ; sans Observateur : joueurs en ligne, bouton **Proposer**. L'Observateur ouvre sa
  partie depuis Sans Visage (« Ma charge d'Observateur ») : nom d'emprunt, **Rendre mon titre** (deux clics). Pas de bouton
  pour lui dans la barre : avec le Marché noir, 11 onglets dépasseraient les 800 px (un admin qui voit tout peut déborder).
  **Refonte du 2026-11-17** : la Cour, Sans Visage et la proposition reçue (`Classes/Observateur.ui`) reprennent le menu
  Système : cadre doré `@GoldContainer`, titres de section à carré doré, rangées `GoldPanelPatch` à liseret (vert = en ligne),
  cases `CharacterPanelButton`, **vrais boutons du jeu** (Révoquer, Exécuter en rouge, Proposer…), bouton « Menu Système »
  pour revenir au menu. Tout est généré par `scripts/observateur/gen_ui.py`.
- **[test] Sens martial** : les doubles rouges n'ont plus d'étiquette « nom — distance » (« nameplates qui ne servent pas »).
- **[test] Lames de l'Observateur** (2026-11-17, `Artefact_Daggers_Observateur` « Mumei et Kagerō », Légendaire niv. 580,
  `scripts/artefacts/gen_lames_observateur.py`, `classes/observateur/ObservateurLames`) — deux kodachi (lame courbe de
  katana, taille de dague), une dans chaque main : gestes, combo, bond et signature Razorstrike des **dagues vanilla**, dégâts
  ×3,8. **Réservées à l'Observateur** (MasterySystem), **données à la nomination**. Dans le dos : deux fourreaux à
  l'horizontale au creux des reins, croisés en X, poignée de chaque côté = **attachement posé sur le modèle du joueur**
  (racine `Belly`) ; pleins si l'arme est dans l'inventaire, vides si elle est en main. **À vérifier** : le client dessine-t-il
  un attachement ajouté au modèle d'un joueur (jamais essayé : les emblèmes ne sont posés que sur des PNJ) ; skin intact après
  l'échange ; lame et prise en main (gestes de dague) ; hauteur et inclinaison du X dans le dos (réglages `TILT`, `HEIGHT`,
  `DEPTH` du générateur) ; les bras qui traversent les poignées. **[plus tard]** signature propre et retrait à la révocation.

- **[test] Poche dimensionnelle** (`poche/PocheDimensionnelle`, `PocheTab`, `OuvrirPocheInteraction`, `scripts/poche/gen_poche.py`)
  — refaite le 2026-11-16 après retours : **s'achète 10 pièces impériales** chez le **Maroquinier** (nouveau métier de
  commerçant, boutique `Eldanior_Maroquinier` par `scripts/poche/gen_maroquinier.py`), n'est plus offerte. Clic avec l'objet
  en main = elle **s'équipe dans sa case du menu Système** (en bas à droite du personnage 3D, `#PocheSlot`), l'objet est
  consommé (plus d'EquipItem : l'emplacement d'utilitaire du jeu est déjà pris par un sac). Clic sur la case = le panneau
  de l'onglet ouvert est remplacé par la poche **sans quitter le menu** (`#TabPoche` : grille de la poche 4 × 9 + inventaire,
  clic = passe dans l'autre grille, glisser-déposer = case visée). Contenu hors inventaire, jamais perdu à la mort.
  À vérifier : la case et son icône, le panneau, les clics et glisser-déposer (clés du client lues comme pour les runes,
  journal « [Poche] evenement non reconnu » sinon), l'achat à 10 impériales.
  - **Sac à dos du jeu** : 9 cases à l'arrivée ; **`/es setlevel 1` le ramène à 9 cases** (objets des cases retirées rendus
    dans l'inventaire ou lâchés au sol). Améliorations : recettes de `Upgrade_Backpack_1/2/3` retirées (surcharges dans
    `Server/Item/Items/Upgrade/`), **Amélioration I (1 zénith) et II (5 zéniths) seulement chez le Maroquinier** ;
    la dernière, Amélioration III (`_4`, 27→36), reste celle du jeu (marchand de ferraille Klops des donjons, 2 noyaux de
    Gobliterator). À vérifier : les recettes ont bien disparu de l'établi (surcharge d'un objet du jeu).
  - Mort sur ce serveur (`GameplayConfigs/Default`, mode Configured) : 50 % de chaque pile perdue dans l'inventaire (sac à
    dos, bourse comprise), rien dans la poche.
- **[test] Pièce Impériale** `Elda_Imperial_Coins` = 1 000 000 cuivres = 10 astrales (`scripts/coins/gen_imperial_coin.py`,
  qualité Divin) : bourse, `/es retirer`, banque (change 10 astrales ↔ 1 impériale, **7 cartes** dans l'onglet Change :
  `Bank.ui` modifié à la main car `gen_bank_ui.py` ne tourne plus depuis la réécriture de `gen_shop_ui.py`), économie.
- **[test] Plaque Visiteur (laissez-passer Luo)** `Eldanior_Plaque_Luo_Visiteur` (`gen_plaques_luo.py`) : vendue **5 pièces
  d'or** par tout marchand Luo aux étrangers (boutique `Luo_Laissez_Passer`, au lieu du refus) ; la porter = entrer sur le
  territoire Luo sans être attaqué ; durabilité = minutes restantes, **2 jours réels** depuis l'achat (comptés même
  déconnecté), puis elle disparaît avec un message. À vérifier : la barre de durabilité baisse, l'admission par les gardes.
- **[test] Forgeron légendaire unique** (2026-11-16) : un seul PNJ (tenue et forge du forgeron du Dragon) à 3 noms au choix
  (Tetsugen Luo, Durgrim, Elarion) ; forges nain et elfe supprimées.
- **Barres de vie** (2026-11-16, résolu) : elles manquaient sur tous les monstres à cause du **réglage du jeu**
  `DisplayEntityStatBars = False` (Settings.json du client) : Réglages → Général → Interface → afficher les barres de vie des
  entités. Rien dans le mod. Le nom des monstres est de nouveau renvoyé à chaque passage, comme avant.
- **[test] Emblèmes flottants** (2026-11-16, 2e version) : **étoiles ROUGES à 5 branches** (1 à 3) et **petite tête de mort en
  2D** (0,42 bloc), en pixel-art **épaissi** (une boîte par suite de pixels, 2-3 px d'épaisseur, plus d'images croisées), à
  ~2,5 blocs au-dessus du pivot de la tête (`scripts/monstres/gen_emblemes_etoiles.py`). Versions `_x1`/`_x2` selon l'échelle.
- **[test] Bloc de poste cassé** (2026-11-16) : la zone était bien retirée mais **pas le monstre** (le dragon restait) :
  `despawn` cherchait le monde par la zone, déjà supprimée. Corrigé (monde passé à part, référence du monstre utilisée).
  Nouveau : `boards/EmblemOrphanSystem` retire toutes les 5 s un monstre vivant qui porte un emblème sans être un contrat en cours
  ni un boss (le dragon de givre resté dans le monde partira seul). Filet de sécurité si le bloc disparaît, journal.
- **[test] Dungeon Boss** (2026-11-16, `boards/DungeonBosses`) : nouveau hors-la-loi de la fenêtre du poste (Hors-la-loi →
  « Dungeon Boss »). Spécialité = espèce, Niveau = niveau du boss (100 par défaut, jusqu'à 9999), Répliques = nom (« Seigneur
  du donjon » par défaut), le Statut ne change rien. Agrandi ×2 (dragons ×1), tête de mort, barre de boss « difficile » à
  moins de 48 blocs, nom « X · espèce · Boss de donjon · Lv.N ». **Aucun tableau** (ni prime, ni quête). Abattu : annonce,
  revient **5 min** après (essais ; `DungeonBosses.RESPAWN_MS` à passer à 2 h). Garde son point de laisse au poste (reste dans
  sa salle). Fichier `eldanior_data/dungeon_bosses.properties`. À vérifier : apparition, tête de mort, barre, réapparition.
- **[test] Rangs SS et EX** (2026-11-16) : monstre de niveau 1000 à 1500 = **SS**, au-dessus = **EX** (`QuestDifficulty.forMonsterLevel`,
  couleurs orange et violet). Affiché sur la carte du contrat (quêtes Chapitres ; tableau de prime « · Rang SS »), dans le nom et
  la barre de boss ; badges SS/EX ajoutés au HUD de quête (`gen_quest_hud.py`), lettres réduites pour tenir dans les badges.
  Niveau des zones et des boss plafonné à 9999 (au lieu de 1000).
- **[test] Spécialité triée** (fenêtre du poste) : la liste suit le PNJ choisi (classes d'aventurier, sans mage pour chasseurs et
  criminels ; métiers du commerçant ; boutiques du marchand Luo ; noms du forgeron légendaire ; espèces du monstre étoilé et du
  boss ; « Aucune » pour les autres), mise à jour au changement de catégorie ou de PNJ.
- **[test] Case de la poche** (2026-11-16, 3e place) : **colonne du Niveau, à la hauteur de l'Église** (3e rangée de droite) ;
  Famille et Guilde partagent la 2e rangée, côte à côte. **`/es setlevel 1` supprime la poche** (`PocheDimensionnelle.resetPoche`) :
  retirée de sa case, poches en réserve de l'inventaire supprimées, contenu rendu dans l'inventaire (surplus au sol).
- **[test] Dragons étoilés** : les dragons du jeu (`Dragon_Fire`, `Dragon_Frost`) sont des figurants **invulnérables** ; rôles
  combattants créés (`Eldanior_Dragon_Etoile_Feu` / `_Givre`, `scripts/monstres/gen_dragons_etoiles.py`, prédateur au sol,
  morsure de 6 blocs sans animation car le modèle n'en a pas). Validés au chargement par un serveur sans affichage
  (2026-11-16) ; combat jamais vu en jeu. Étoiles actuelles gardées (2026-11-18).
- **[test] Catégorie de PNJ « Exclusifs »** (fenêtre du poste) : Sensei, Maître de guilde, Forgeron légendaire. **Statut
  commun Apprenti / Expert / Maître** (`guards/NpcStatut`) : aventurier ordinaire/exceptionnel/Héros, marchand Luo
  apprenti/négociant/grand marchand, commerçant apprenti/compagnon/maître, rangs martiaux (Apprenti, Expert, Maître
  Martial), Sensei (Grand Maître, Saint, Maître Suprême), chasseur et criminel (niveau d'aventure) ; maître de guilde et
  forgeron légendaire : affiché dans le nom (« · Expert »), sans autre effet. **[à faire]** (2026-11-18) les Exclusifs
  sont les PNJ **non personnalisables** (Dresseur, Immobilier, Maître de guilde…) : catégorie à revoir, voir § 0.
- **[test] Monstres étoilés** (Hors-la-loi → « Monstre étoilé ») : zone de contrat posée au bloc de poste, contrat **chaque
  jour** en plus des 3 tirés au hasard ; Spécialité = espèce, Statut = ★/★★/★★★ (niveau ×1,2/×1,5/×2, récompense ×1/×2/×3),
  Niveau = niveau de zone (50 par défaut), Répliques = nom du lieu ; « Retirer » sur le poste supprime la zone. Noms
  « Grok le Dévoreur (2 étoiles) » (le symbole ★ ne s'affiche pas en jeu : remplacé partout par du texte, « Criminel étoilé » compris).
- **[test] Quête de classe** (`MissionType.CLASSE`, contour cyan, `Chain.forClass` / `forClassGroup`) : invisible pour les
  autres classes, une seule fois par joueur. **Les Épreuves du Dojo du Sensei sont la quête de classe des artistes martiaux**
  (les 8 classes de la voie martiale).
- **[test] Mousquetaire = évolution de l'Archer** (palier 180, tiré parmi ses évolutions ; plus une classe de départ ;
  stats au niveau des évolutions, arc ajouté). Tirage aléatoire, pas de garantie (2026-11-18).
- **Supprimés** : catégorie créative Spells (Boule de Feu, Éclat de Glace, Familier Arcanique : objets, parchemins,
  interactions, projectiles, butins, compétences) ; tenues des forgerons légendaires nain et elfe (objets, modèles,
  `gen_forgerons_legendaires.py`), seule celle de Tetsugen Luo (forgeron du Dragon) reste ; Durgrim et Elarion portent du
  cuir épais : **[à faire]** les retirer aussi (2026-11-18). Boutiques ambulantes régénérées (`gen_marchand_luo.py`).

- **[test] Fenêtre du marchand en deux panneaux, façon établi / enclume** (2026-11-15, `scripts/npcs/gen_shop_ui.py`
  + `ui_kit.py`, `guards/ShopPage.java`) : textures des panneaux du jeu ; **au centre** nom du marchand > rôle et bourse en
  pièces dans l'en-tête, catégories à gauche (comme les catégories de recettes), grille 6 × 4 (prix en pièces, étiquettes
  « + ingrédients » / « lot de N » / « épuisé »), barre de **quantité** (- / + / +10 / Max) et total ; **à droite** fiche de
  l'article, prix et stock, **ingrédients requis** pour la quantité choisie (possédé / requis en vert ou rouge), ligne
  d'aide, bouton « Acheter N » (« Vendre N » dans Rachat). À vérifier : la fenêtre s'ouvre (sinon `*_client.log`), textures
  des panneaux, groupes centrés de la bourse et du total visibles, quantité, panneau d'ingrédients sur un troc (forgeron
  légendaire, Nomade).
- **[test] Éditeur des prix en jeu : `/es prix [recherche]`** (2026-11-15, admin, `economy/PriceBook`,
  `economy/PriceEditorPage`, `Economie/Prix.ui` par `scripts/economie/gen_prix_ui.py`) : mêmes deux panneaux ; familles,
  recherche (nom ou identifiant, sans accents), liste des 5 346 objets avec prix et source ; à droite recette avec le prix de
  chaque ingrédient (cliquer = l'ouvrir), recettes qui l'utilisent, nouveau prix + raccourcis /2 -10 % +10 % x2, retour au
  prix de base. Enregistré dans `run/mods/eldaniorsystem_EldaniorSystem/economie/prix_jeu.json`, tout se recalcule
  aussitôt ; `gen_economie.py` le relit (« (jeu) » dans `docs/economie.md`), `--fusionner` le recopie dans `prix.json`.
  Calcul Java identique au script (vérifié sur les 5 346 objets). À vérifier : ouverture, recherche (champ de saisie),
  champ du nouveau prix au-delà de 2 milliards impossible (entier).
- **[plus tard] Économie centrale** (2026-11-15 ; prix pas encore revus, boutiques à retravailler : 2026-11-18) : `docs/economie.md` liste **les 5 346 objets** (vanilla + Eldanior) avec
  leur prix ; prix des ressources dans `scripts/economie/prix.json` (estimations de départ, à ajuster), prix des objets
  fabriqués calculés par recette × 1,3 ; relancer `python3 scripts/economie/gen_economie.py`. Repères : épée en fer
  17 or, lingot divin 10 astrales. **À faire ensuite** : brancher les générateurs de boutiques (`gen_metiers.py`,
  ambulant, forge légendaire, `ShopPage.buyback`) sur `prix.json` une fois les prix validés.

- **[test] Quêtes remises à zéro** (2026-11-14) : il ne reste que la principale (« Les Secrets du Roi Fou » en carte +
  l'ancienne chaîne Ancien Conseiller → Tavernier → « La Naissance d'un Roi »), les Épreuves du Dojo du Sensei, l'Œuf du
  Dresseur et **5 journalières** (les affiches du tableau du 13/11 : Guerrier Trork, Cendres et Os, Investisseur Avisé,
  Cartographe, Légende de Guerre ; affichées tous les jours, plus aucune pour les PK). Le reste (100 secondaires de PNJ et
  leurs rôles, 111 journalières, quêtes de test) est supprimé. Nouvelles quêtes **ce week-end ensemble**, avec le skill
  `quest-creation` (une illustration par étape). Liste à jour : `.claude/skills/quest-creation/references/catalogue.md`.
  - **Illustrations** à regarder sur les cartes : principale (salle du trône du Roi Fou) et une par étape du Dojo
    (dojo, plaines venteuses, camp de brigands, Trork, Sensei devant la lune).
  - **[test] Quête du Roi, étape 1** (2026-11-14, `QueteRoi.java`) : « Les Rumeurs du Roi Fou » (B, niveau 250) chez un
    civil ; 3 témoins en relais, chacun une exclusive visible seulement si la précédente est commencée : Vieux Garde
    (25 zombies + 3 cerveaux flétris), Servante de la Reine (fruits, sève et bûches d'azur), Ermite des Livres (cobalt,
    thorium) ; chacun donne un indice lisible en main, le dernier renvoie au premier civil. **À poser** : 4 civils (témoins
    à plus de 5 000 blocs les uns des autres), puis ajouter à chacun sa quête avec la carte « + ». Les textes disent près de
    quel lieu vit le suivant. Étapes 2 et suivantes à concevoir (lieux, monstres étoilés…).
- **[test] Livres d'histoire et Tomes Impériaux** (2026-11-14, `scripts/histoires/gen_histoires.py` + `tomes.py`,
  package `histoires`) : 10 Tomes Impériaux du Royaume d'Eldanior (`Eldanior_Tome_01` à `_10`, catégorie Histoires) et la
  Chronique du Roi Fou, **posables et lisibles en main** : clic droit = lire, accroupi + clic droit = poser ; posé, F ou
  clic droit dessus = lire. Nouvelle fenêtre à pages (`Histoires/Livre.ui`, précédente/suivante, texte qui défile).
  Indices de la Quête du Roi réécrits pour coller aux Tomes (Tombeau, Fureur du Dragon, trois reliques, aile de l'Est).
  À vérifier : la fenêtre s'ouvre (sinon `*_client.log`), italique/gras rendus, la pose accroupi, le livre posé s'ouvre,
  le modèle du livre violet. **[à faire]** Tomes à trouver par les quêtes, les butins et les PNJ (2026-11-18).
- **[test] Primes de criminels** (2026-11-14) : un criminel abattu disparaît du tableau de prime (sa prime de base ne tombe
  jamais à zéro, il restait affiché) ; nouvel avis quand il revient.
  - **[test] 5 secondaires + 3 exclusives des PNJ de métier** (2026-11-14, `QuetesMetiers.java`, une image chacune) :
    Éleveur « Les Crocs de la Lisière » (F), Alchimiste « Remèdes du Marais » (E), Forgeron « Braises pour la Forge » (D),
    Marchand Luo « Les Morts du Désert » (C, 15 zombies des sables), Marchand ambulant « Trophées pour un Collectionneur »
    (B) ; exclusives : Maître de guilde « Le Droit d'Entrée » (D, 3 duels gagnés **contre des joueurs** → Épée du
    Gardien), Alchimiste « L'Œil du Vide » (B → Parchemin de Rune : Présage), Forgeron légendaire « L'Acier de l'Ombre »
    (A → Croc du Néant). Secondaires refaisables 24 h (5 min en test, `QuetesMetiers.SECONDAIRES_TEST`). À vérifier :
    « Quêtes » apparaît chez ces PNJ, les butins sont retirés au rendu, le duel entre joueurs fait avancer « Le Droit
    d'Entrée ».
  - Bandes rang ↔ niveau des monstres **validées** (F 0-60, E 60-150, D 150-300, C 300-450, B 450-600, A 600-800, S 800+).
  - **[test] HUD des quêtes** (2026-11-14) : mêmes lignes d'attendus que la fenêtre (3 lignes pour les 3 disciples :
    « Défier <nom> », « Vaincre un disciple », puis « Vaincu : <nom> » en doré) ; rang dans un blason en losange à la
    couleur du rang (halo plus fort pour A et S).
  - **[test] Quêtes d'argent liées à la banque** (2026-11-14) : « Verser X à la banque » avance avec les dépôts de pièces
    chez le banquier et les primes touchées, recule à chaque retrait ; les prêts ne comptent pas.
- **[test] Carte « + » (admin)** (2026-11-14) : propose maintenant **toutes** les quêtes (secondaires et exclusives des
  autres PNJ comprises), sauf celles que ce PNJ a déjà.
- **[test] Histoires et indices** (2026-11-14) : catégorie créative **Eldanior System > Histoires**. **Histoire** = journal
  ouvert posable (comme « Lore_Note_Enkindled_Research_Gateways ») : F dessus ouvre la page de lecture du jeu. **Indice** =
  feuillets (comme « …Journal_Vermin ») **non posables** : clic droit (ou F) en main ouvre la même page. Exemples :
  « Chronique d'Eldanior : le Roi Fou » et « Feuillet déchiré : le sceau brisé ». Ajouter les nôtres :
  `scripts/histoires/gen_histoires.py` (listes HISTOIRES / INDICES). À vérifier : le clic droit ne pose pas l'indice, la
  page s'ouvre en main, la mise en forme (italique, paragraphes) passe.
- **[test] Rappel : parler à un PNJ pour valider une quête ou trouver des indices n'a jamais été vérifié** (ancien
  système `NpcDialogueQuest` / `NpcQuestDetectionSystem` : Ancien Conseiller, Tavernier Alex Silford). À tester avant
  de créer des étapes « va parler à… ».

- **[test] Familiers** (2026-11-13/14, jamais vus en jeu). Code `familiers/`, assets `scripts/familiers/gen_familiers.py`.
  - **18 espèces** : Commun Renard, Chat, Chien, Corbeau ; Inhabituel Loup sombre, Cheval, Faucon ; Rare Ours brun, Ours
    polaire, Léopard des neiges, Loup blanc, Chouette des neiges ; Épique Tigre à dents de sabre, Raptor des cavernes,
    Ptérodactyle ; Légendaire Fenrir, Dragon de givre (Glace), Dragon de feu (Dragonic). Modèles du jeu mis à l'échelle.
    Les oiseaux et dragons **marchent** (pas de vol) ; chat, chien, oiseaux, dragons mordent avec l'attaque du loup (pas
    d'animation de morsure sur leurs modèles). Pas d'âne : aucun modèle dans le jeu.
    Monture : Cheval, Tigre, Raptor, Fenrir et dragons dès l'adolescence ; Loups, Ours, Léopard adultes. **Selles à régler.**
  - **F sur l'animal** (2026-11-14) : choix **Chevaucher** (s'il se monte à son âge) ou **Gestion** (fenêtre). Monte faite par
    le code (comme l'action Mount du jeu) : à vérifier en jeu (descente, déplacements, retour de son rôle).
  - **Assis** : quand il suit et arrive près de vous (≈3 blocs), il s'assoit et ne bouge plus ; au-delà de 5 blocs il vous
    suit. « Rester ici » le garde sur place.
  - **Œuf** : même petit nid de paille pour tous ; seules la coquille (motif de l'espèce) et la rareté changent. Lueur et
    étincelles pour l'épique et le légendaire.
  - **Dresseur de bêtes** : œufs Commun (Renard, 25 or) et Inhabituel (Loup, 10 diamants) (prix ×5 le 2026-11-13) +
    nourriture (tourte, brochette, gibier, bœuf, poisson). Plus de Fenrir en boutique.
  - **Quête « Un Œuf venu d'ailleurs »** (secondaire, chez le Dresseur) : couronne de Trork + 3 sèves spirituelles + 5 cœurs
    d'ours → œuf tiré au hasard : Rare 62 %, Épique 30 %, Légendaire 8 %. Refaisable après 5 min en test, 3 mois en vrai
    (`ChapterQuests.DRESSEUR_TEST`).
  - **Compétences** : aucune avant l'âge adulte. Adulte : bouton **« Faire évoluer »** dans la fenêtre = une compétence tirée
    au hasard parmi celles de l'espèce + celle de son élément (Rare et au-delà). Fenrir : Hurlement glacial ou Croc du
    Ragnarök, + Porte de Niflheim au niveau max. Admin : « Autre compétence » pour retirer au sort.
  - **Fenêtre de gestion** refaite : portrait (œuf de l'espèce), rareté en couleur, élément, barres de vie / XP / croissance,
    âge, puissance, monture, nourriture, compétence et description, prochaine évolution, Nourrir (aliment du sac, 1 fois par
    minute : vie + un peu d'XP), Me suivre / Rester ici, Relâcher.
  - À vérifier aussi (session précédente) : éclosion 5 min, le petit vous suit, croissance (test 5/15 min), Fenrir adulte
    (collier de Gleipnir et pics de glace greffés sur les os, aura de givre), mort = œuf brisé + -20 % XP 7 j (pas pour les admins).
  - Compétences de bête (Morsure sauvage, Bond du chasseur, Rugissement, Coup de patte, Hurlement de meute) : validées pour
    l'instant, évolution du système plus tard (2026-11-18).
  - Autres espèces annoncées : **faites** (faucon, corbeau, cheval, dragons, ours polaire, loup blanc font partie des 18) ;
    seul l'**âne** manque, faute de modèle dans le jeu.

- **[test] Deux quêtes en cours au plus + la principale** (2026-11-13, `QuestManager.MAX_ACTIVE`) : la 3e est refusée avec
  un message ; la quête principale s'ajoute toujours. Les deux quêtes en cours avancent (plus seulement la quête suivie).

- **[test] Tableau des quêtes = fenêtre Chapitres** (2026-11-13) : le bloc `Eldanior_Tableau_Quetes` ouvre la fenêtre en
  cartes. En tête, les **5 affiches du jour** automatiques (quêtes journalières du tirage, celles des PK pour un PK ; changent à
  minuit ; rendu par QuestManager), puis les quêtes ajoutées par un admin (carte « + », propre à chaque tableau). Plus de
  primes ni de contrats ici.
- **[test] Tableau de prime = fenêtre Chapitres** : seulement les catégories **Prime** (rouge) et **Contrat** (orange, les 3
  monstres d'élite du jour). L'ancienne fenêtre n'est plus ouverte : tout se fait sur les cartes (2026-11-14) :
  - carte d'un recherché : bouton **Mettre à prix** (2 clics) et **Montant** (1 000 → 5 000 → 10 000 → 50 000 → 100 000),
    coût = montant + 10 % au trésor de la ville du tableau ;
  - sa propre carte (on a une prime) : **Racheter ma prime** (2 clics, 2× la prime, au trésor) ;
  - prendre un contrat : 5 % de la prime, **jamais rendus** (décision du 2026-11-14) ; prime de la cible +20 % ; un seul
    vainqueur ; sans contrat, pas de prime.
  - Tous les paiements : **pièces portées d'abord, puis le compte en banque** (décision du 2026-11-14).
  - Contrat de monstre : gratuit (suivi à l'écran) ; le premier qui l'abat touche la récompense, contrat ou pas.
- **[test] HUD des quêtes en cours** (`scripts/quetes/gen_quest_hud.py`) : jusqu'à 3 cadres (principale, suivie, l'autre en
  cours), contour à la couleur du type, **rang dans un bandeau vertical à gauche** (fond teinté du type, filet coloré,
  « RANG » + lettre ; 2026-11-14), type, nom, attendus avec cases et avancement ; plus de ligne de récompense. À vérifier : hauteur auto des cadres, place à l'écran (3 cadres).

- **[test] Onglet créatif « Empire Luo »** (2026-11-11) : 5 clans (Luo, Kage, Setsuka, Sama, Hanazakura, retirés de
  « Famille ») + Civils, Comestibles, Arts Martiaux, Runes. À vérifier : l'onglet et ses 9 pages s'affichent avec leurs icônes,
  plus aucune page de ces clans dans « Famille ». Détail : `docs/plan-clan-luo.md`, fin du fichier.

- **[test] Bibliothèque évolutive** (`Eldanior_Bibliotheque`, 2026-11-11) : F ouvre un rangement de 18 cases (fenêtre de coffre) qui
  n'accepte que les ouvrages (parchemins, manuels, livres, rouleaux, grimoires, pages de rune et de compétence, livres de recette) ;
  vide = étagère `Furniture_Village_Shelf_Full`, au moins un ouvrage = `Furniture_Village_Bookcase`. À vérifier : changement de
  modèle en direct, contenu gardé au changement d'état, objets lâchés quand on la casse, refus des autres objets (clic et Maj+clic).
  Recette : 6 bois dur à l'établi de mobilier. Code : `bibliotheque/`, mots acceptés dans `Bibliotheque.OUVRAGES`.

- **[test] Série du 2026-11-13 (après-midi)** :
  - **Duel du disciple** : une victoire n'est comptée que si le combattant meurt vraiment (composant de mort, vie à 0, ou
    disparu alors qu'il était sous 5 % de sa vie). Avant, il disparaissait quand le joueur réapparaissait loin (chunk
    déchargé) et le duel était compté gagné. La mort du joueur arrête le duel (« duel perdu ») avant tout le reste.
  - **Quêtes, admin** : bouton « Retirer cette quête » sur la **première quête d'une série** (second clic pour confirmer) ;
    toute la série part. Quête ajoutée par « + » : rayée ; quête propre au PNJ (Sensei) : notée « -id » dans le poste,
    et remise par la carte « + ».
  - **Disciple désigné** : tiré au hasard, de préférence à plus de 80 blocs du joueur, avec un lieu proche ; redemander
    garde le même.
  - **Répliques du disciple** : génériques (sans le Sensei), selon son rang, son clan ou sa famille, s'il s'entraîne seul
    loin des villes, et s'il attend le joueur pour un duel.
  - **Affiche** : la fenêtre plantait (« UUID.equals … getKey() is null ») pour une zone créée sans propriétaire. Corrigé.
  - **[test] Affiche, retours du 2026-11-17** : (1) après « Enregistrer », l'affiche changeait d'état et **ne répondait plus
    à F** : l'id d'un état commence par « * » (`*Eldanior_Affiche_Propriete_State_Definitions_Vendre`) et n'était plus
    reconnu (F, casse et rafraîchissement ignorés). Corrigé (+ Use déclaré dans chaque état). (2) **Coffres** : comptage
    refait par section de chunk (plus de plafond de volume, cache de 5 s au lieu de 60 s, grand coffre compté une fois,
    « coffer » compté). (3) **Configuration** : une liste « Proposer le bien » (Ne pas le proposer / À vendre / À louer /
    À vendre et à louer) remplace les deux oui/non ; prix et loyer ne s'affichent que s'ils servent. Vente + location
    permises ensemble : affiche **VENDRE / LOUER** (bandeau rouge et bleu) ; tant qu'un locataire occupe le bien, la vente
    attend. À vérifier : F après enregistrement, nombre de coffres posés après la zone, la liste et les lignes qui
    apparaissent / disparaissent.
  - **[test] Gestion des biens, retours du 2026-11-17 (suite)** : la carte de gestion de l'affiche s'ouvre aussi pour le
    **locataire** (« Votre location » : inviter / retirer ses invités ; pas de réglages de vente). Le **propriétaire d'un bien
    loué** n'invite plus personne : il a seulement « Préavis (2 j) » / « Annuler le préavis » ; à l'échéance la location prend
    fin (et le bail ne se prolonge plus pendant le préavis). Admin : « Expulser » immédiat. Les invités du propriétaire sont
    mis de côté pendant la location et reviennent au départ du locataire. **PNJ Immobilier** → Mes biens → « Gérer le bien »
    ouvre la même fenêtre. **Remise en état** (`territory/ZoneSnapshots`) : photo de la zone au début d'une location ou d'un
    achat (parcelles, maisons, chambres ; zones ≤ 160 sections), remise au départ (quitter, préavis, expiration, expulsion,
    abandon) : blocs ajoutés retirés, coffres ajoutés vidés puis retirés, blocs cassés reposés ; les coffres d'origine gardent
    leur contenu (sinon duplication). Biens loués ou achetés avant cette version : pas de photo, rien n'est remis.
    **Corrigé aussi** : à l'expiration d'un bail, le bien perdait son propriétaire (evict au lieu de fin de location).
    À vérifier : remise en état d'une maison louée (blocs, coffre ajouté, porte), préavis, invitation par le locataire.
  - **[test] Portrait du HUD Arkies Domini** : encore trop grand (tête du squelette coupée) → taille cible 1,4 → 0,85 bloc
    (squelette 0,74 → 0,45), `gen_visages.py` relancé.
  - **Noblesse** : le **Roi** devient Patriarche d'Eldanior à son couronnement (commande, menu admin, objet, et à la
    connexion), l'ancien Roi reste Noble de la maison. **Onglet Famille** du menu : seulement pour un **Marquis sans
    maison**, qui y achète un Marquisat (grand territoire). **Ducs et Comtes** : chez un intendant, fenêtre « Terres du
    domaine » (onglet Domaines → « Terres à acquérir » pour les membres ; directement pour un noble étranger) : Duchés
    sans Patriarche et territoires sans maître (Duc), villes sans maître (Comte), choisis dans la liste ; le prix va au
    trésor de la zone qui vendait. Prix d'une maison corrigé (il lisait parfois une ville au lieu du Marquisat → 0).
  - **Retours du même jour** : ville (bureau des terres et affiche) = Comte minimum **et** chef de guilde ou Capitaine d'un
    Ordre de chevalerie ; plus de direction ni de distance pour le disciple désigné, seulement un lieu (« sur le territoire
    X, près de Y », « à Y », « près de Y ») ; attendus sans lieu ni rang (« Défier Kenji Sama », « Vaincre un disciple ») ;
    carte « + » : bouton **Catégorie** (toutes, principale, secondaire, exclusive, journalière), quêtes rangées par catégorie.
    **Contour des cartes** selon le type : principale ou rang S en or, secondaire en bleu, journalière en vert, exclusive
    en violet (verrouillée : cadre sombre, inchangé). Les **éclats** autour de la carte choisie prennent la même couleur.
  - **Quête principale** : une fois par joueur, un seul Roi. Dès qu'un Roi est élu (commande, menu admin, objet, quête) :
    grand titre « UN ROI EST ÉLU » et message à tous les joueurs en ligne ; la quête principale est **fermée pour tout le
    monde** : plus proposée par aucun PNJ ni par la carte « + », retirée des quêtes en cours (en ligne aussitôt, hors ligne
    à la connexion). Elle rouvre si le trône redevient vacant. **Quête exclusive** : une seule fois par joueur.
  - **Commerçant immobilier** (nouveau métier de commerçant) : bureau des terres ouvert à tous : Mes biens (vendre, louer,
    retirer, prolonger, quitter), Marché (acheter, louer), Terres. Ne sort jamais au tirage des commerçants.
- **[test] Série du 2026-11-12 (nuit)** :
  - **Canon de la Percée tout noir** : les faisceaux du moteur sont éclairés par le monde (aucun réglage de lumière, contrairement
    aux traînées) et devenaient noirs dans le tunnel creusé ; le rayon du canon est maintenant dessiné en **particules
    lumineuses** (`Percee_Rayon_T*`, `Percee_Spirale_Point_T*`), et les fumées des impacts et explosions prennent la teinte du
    Ki au lieu du noir. À vérifier : le canon (Maître Suprême et les cinq lances) de jour, de nuit et sous terre.
  - **Bokken et katanas** : la compétence (rafale du kata) demande l'**énergie de signature pleine** et la vide ; le
    fonctionnement du Bokken (maintien = Percée avec posture, compétence = rafale) passe à **tous les katanas sauf le Divin
    (Kinryū-Ō) et le Légendaire** (Tenryūmaru, qui a son propre générateur).
  - **Manuels** : livres de test supprimés ; livre décalé (à gauche, plus haut, plus près du corps : `BOOK_SHIFT` (2,4 ; 6,1 ;
    9,1)). À vérifier : position du livre à la lecture.
  - **Pièces** : plus de « F pour transférer à la banque » ; les pièces ne se déposent et ne se changent que chez le banquier.
  - **Prêts de la banque** (onglet Prêts du banquier, `guards/BankLoans`) : jusqu'à 3 fois ce que l'on possède (compte +
    bourse), un seul prêt, 20 % d'intérêts, 7 jours, remboursement depuis le compte ; emprunt confirmé par un second clic,
    règles et conséquences affichées en rouge ; rappel à la connexion et la veille de l'échéance. Échéance passée : prime de
    la banque = **50 % de la somme empruntée** (chiffre à valider), **Criminel ★** (PK forcé : chasseurs de prime et gardes),
    étoile au tableau des primes et dans le menu ; mort dans cet état = **tout perdu** (niveau 1, Novice, objets, argent,
    famille, guilde, noblesse, titres, quêtes, runes) puis point d'apparition ; solder la dette lève la prime et l'étoile.
    À vérifier : emprunt / remboursement, passage en Criminel ★ (pour tester sans attendre 7 jours : baisser l'échéance dans
    `eldanior_data/bank_loans.properties`, serveur arrêté), mort d'un débiteur.
  - **Affiche de propriété** : la pancarte est **supprimée**, l'affiche reste ; elle change selon le bien : **À VENDRE**
    (rouge), **À LOUER** (bleu), **OCCUPÉ** (brun), neutre sinon (suivi toutes les 15 s et après chaque action). La fiche du
    bien affiche le **nombre de coffres** de la zone (refait le 2026-11-17, voir plus haut).
  - **Intendant** : nouvel onglet **Biens** (tous les biens des domaines de la maison : état, maître, locataire, prix, loyer,
    coffres, affiches ; mettre en vente / en location au montant de la fiche, retirer du marché ; biens d'un joueur en
    lecture), nombre de biens par domaine ; **prendre la tête d'une maison libre** : remplacé le 2026-11-13 (voir la série
    du 2026-11-13 : Roi d'office, Marquis par l'onglet Famille, Duc et Comte par les Terres de l'intendant). À vérifier : 6 onglets lisibles
    dans la largeur, maison libre (il faut un intendant posé sur ses terres).
- **[test] Série du 2026-11-12 (soir)** :
  - **Bokken refait** : touche de compétence = **rafale** du combo (moitié de l'endurance) ; maintenir l'attaque = posture
    d'estoc (`TsukiCharge`) puis **Percée** au relâché après 0,5 s (avant : combo ; au fourreau : posture iai puis dégainé +
    Percée). Garde + clic donne aussi la Percée. **La Fente du Ciel est retirée du Bokken** (décision du 2026-11-12) ; elle
    reste la signature des autres katanas, qui ne changent pas.
  - **Percée refaite (2e et 3e séries)** : animations composées (`percee_anims.py`) — posture du Bokken, vraie fente, double
    estoc (Disciple), ruée rasante (Expert), 7 estocs + éventail final (Grand Maître), garde gatotsu et canon (Maître
    Suprême), cinq estocs dans le vide (Dieu Martial). Tout à la **couleur du Ki du palier** (`gen_percee.py`).
    **3e série (décisions de l'utilisateur)** : Lance de l'Âme **supprimée** ; le **canon de Ki passe au Maître Suprême**
    (240 dégâts, recharge 14 s, nacré) ; le **Dieu Martial** fait cinq estocs éclair, chacun fait naître une **lance de Ki
    derrière lui au-dessus des épaules** (comme Gilgamesh sans portail), puis elles **partent une à une** vers la cible
    (120 chacune, explosion au bout, recharge 20 s). Effets enrichis dès l'Expert (images rémanentes et coupe qui éclate
    après la ruée, aiguille avec fil lumineux et impact au mur, éventail de 6 aiguilles, fleurs qui éclosent sur les
    ennemis, braises le long du canon). **Coût endurance + mana** proportionnel au palier (endurance 8 → 50 %, mana 3 →
    45 % du maximum ; refusée s'il en manque) — chiffres à valider. À vérifier : posture coupée au relâché, faisceaux
    effilés, lances qui suivent bien le joueur et visent la cible, couleurs presque blanches (Apprenti, Maître Suprême).
    **4e série** : chaque lance du Dieu Martial tire le **canon complet du Maître Suprême** (tunnel, débris, spirales,
    paralysie, explosion ; classe `Percee.Shot` commune) ; départs plus lents (1,15 s après le début, puis toutes les
    0,42 s, chaque lance se charge 0,3 s avant ; bras tendu jusqu'à 3,1 s) ; **tous les effets du coup partent de la
    pointe de la lame bras tendu** (positions mesurées sur les animations, `Percee.TIP_*`), plus de la main. À vérifier :
    5 tunnels de canon d'affilée (charge du serveur, blocs remis après 60 s), pointe bien placée en jeu (sinon me dire
    « trop loin / trop près »).
  - **Combo** : le jeu d'animations de la lame au clair avait perdu 4 mouvements du kata (Kaiten, Tourbillon, Salto, Tornade :
    coups 7, 9, 10, 11) à la dernière régénération de Tenryūmaru ; remis, et `tenryumaru_anims.py` les ajoute désormais
    lui-même. À vérifier : combo complet sans coup « vide ». Si d'autres bugs restent, les décrire (quel coup, quel palier).
  - **Barre d'XP** en bas à gauche (dessin de la barre de vie, niveau à la place de l'icône, XP / requis au-dessus) :
    `hud/XpHud.java`, `Hud/XpHud.ui`. À vérifier : position (pas sous le chat), mise à jour en gagnant de l'XP.
  - **Menu système** : onglets Inventaire, Quêtes, Duel, Classements, Territoires, Propriétés retirés (bouton et panneau).
    Les classes `*Tab` restent (logique réutilisée par l'intendant et la pancarte).
  - **Pancarte / affiche de propriété** (`Eldanior_Pancarte_Propriete` = panneau du village, `Eldanior_Affiche_Propriete` =
    parchemin collé au mur) : posée **hors de la zone**, liée à un bien à moins de 48 blocs (liste, le plus proche d'abord ;
    admins : tout bien ; joueurs : leurs biens). F : carte du bien (statut, surface, prix, loyer, propriétaire, bail, accès ;
    Acheter / Louer, ou Prolonger / Quitter pour le locataire) + carte de gestion pour le propriétaire et les admins (prix,
    loyer, vente / location, protection, inviter / retirer, expulser, délier). Casser une pancarte liée : propriétaire ou admin.
    À vérifier : affichage des deux cartes côte à côte (921 px de haut comme le recrutement), orientation de l'affiche sur le
    mur, champs numériques. Code : `territory/pancarte/`, `scripts/territoire/gen_pancarte.py`.
  - **Menu système refait façon inventaire** : profil à gauche dans le cadre vanilla (aperçu 3D du personnage
    `CharacterPreviewComponent`, XP, mana, 6 caractéristiques avec icônes du panneau personnage, boutons « + » et « +5 »
    visibles seulement s'il reste des points), colonne de catégories (boutons carrés à icône d'objet, infobulle, bouton
    allumé = onglet actif ; Famille = bannière de la famille), contenu à droite avec le titre de l'onglet. L'onglet Profil
    garde rang, statut, classe, église, titre, foi, PvP et monstres tués. **Risque** : si le client refuse
    `CharacterPreviewComponent` dans une page de mod, il se déconnecte au chargement (« failed customUI », voir le
    `*_client.log`) : retirer ce seul élément de `System/SystemPage.ui`. S'il s'affiche vide, me le dire.
  - **Menu système, 2e passe** : cadre doré sur les trois fenêtres (cadre du jeu repeint par `scripts/system/gen_cadre_or.py`,
    gabarit `@GoldContainer` dans `SystemPage.ui`), panneaux internes à liseré or ; **Statistiques** au-dessus des
    caractéristiques comme dans Tab : vie, endurance, mana (actuel / max), défense (points retirés par coup), puis dégâts
    +Force, critique %, esquive % ; barre de mana retirée. À vérifier : rendu du cadre (bandeau de titre, coins), lisibilité.
  - **Menu système, 3e passe** : à côté du personnage (sans cadre), l'identité : rang d'aventurier (F à S), classe,
    niveau (+ rang martial), noblesse, famille et rang dans la famille, Église (Laïque, rang religieux, ou Criminel avec la
    prime), guilde et rôle, titre. Foi et Dignité rejoignent les caractéristiques (sans bouton). **Onglet Profil retiré**
    (Compétences s'ouvre par défaut) : les stats PvP et les monstres tués ne sont plus affichés nulle part (à replacer ?).
  - **Menu système, 4e passe** : plus de cadre autour des statistiques et caractéristiques ; identité en lignes à tuile
    d'icône (comme les boutons du sac à dos de Tab) : noblesse (décret du rang), Église (bénédiction / bougie / crâne si
    PK), famille et guilde **seulement si on en a**, plus de titre. **Cadre rouge** des trois fenêtres pour un criminel
    (PK). Colonne de navigation avec son propre bandeau de titre (« Menu ») au lieu du haut tronqué. À vérifier : bandeau
    étroit de la colonne (102 px), icônes des décrets et bénédictions.
  - **Menu système, 5e passe** : navigation = boutons seuls (plus de cadre ni de fond) ; personnage 3D au centre entre deux
    colonnes de cases façon emplacements d'inventaire : à gauche Rang (lettre, « Aventurier » ou rang martial), Noblesse,
    Église / Criminel ; à droite Niveau (nombre, classe dessous), Famille et Guilde (masquées sans). Textes longs rétrécis
    (`ShrinkTextToFit`). À vérifier : lisibilité des valeurs dans 104 px de large.
  - **Menu système, 6e passe** : vie / endurance / mana rafraîchies toutes les 0,5 s tant que le menu est ouvert ;
    Famille seulement pour **choisir** sa famille (Duc, Marquis, Roi sans famille) ; Guilde seulement si membre ;
    Échanges seulement à ≤ 10 blocs d'un autre joueur ; **Compétences** (1er onglet) = Runes actives (3 places des touches
    de compétence (2 lignes : principale + 2 soutiens) et sac de 50 runes défilant, grilles du jeu remplies par le serveur (une grille reliée par `InventorySectionId` reste vide dans une page de mod, `IsAbilitiesSection` refusé) ; clic = équiper / ranger, glisser-déposer entre les grilles ; une rune ne quitte sa case qu'une fois posée ; données brutes des clics notées « [Runes] evenement » dans le journal du serveur pour ajuster les clés) et Passives ; `/es runes` supprimée ;
    nouvel onglet **Quêtes** (liste en lecture seule : suivie, en cours, à rendre) ; infobulle sur le personnage 3D
    (joueurs tués, morts, KDR, séries, monstres tués, duels). Commandes : `docs/commandes.md` (joueurs / admins) ;
    `/es parcel` et `/es retirer` réservées aux admins. À vérifier : infobulle sur le personnage (multi-ligne), équiper
    une rune met bien à jour les touches, pas de déconnexion en changeant de page pendant le rafraîchissement.
  - **Runes, 2e passe** (2026-11-12) : glisser-déposer confirmé en jeu. Nouveau design (panneaux dorés « Runes équipées »
    avec légende Principale / Soutiens et compteur, « Sac de runes » avec compteur et défilement propre, barre d'aide,
    onglets soulignés). **Clic simple = envoi rapide** sac ↔ compétences : le client n'envoie pas l'état de Maj, donc
    Maj+clic et clic font la même chose. Glisser dans le sac = réordonner ; entre deux cases de compétence = déplacer.
    **Wiki** : page 1 = commandes joueur, seule page visible d'un joueur ; les autres pages sont réservées aux admins.
    À vérifier : clic = envoi rapide dans les deux sens, réordonner le sac, rendu du nouvel écran, Wiki en joueur non admin.
  - **Commandes joueur, 3e passe** (2026-11-12) : `/es system` refusée aux non-admins : `canGeneratePermission()` n'existe
    plus dans le serveur (ignorée), toutes les commandes exigeaient une permission. `/es` et les commandes joueur appellent
    maintenant `requireNoPermission()` (system, party, duel, trade, sell, guild, guilddisband, kingdom, /incliner) ; les
    autres restent admin. Supprimées : `/es ancre`, `title`, `titlelist`, `nstatus`, `ordre` (l'intendant gère les ordres),
    `classinfo`. Runes : sac à 5 rangées, description dans un panneau du menu au survol (l'infobulle du jeu, coupée, restait
    affichée après la fermeture) ; icônes des onglets refaites (rune, carte, écu, épée et bouclier, couronne, bourse, dagues,
    coffre, grimoire). À vérifier : `/es system` avec un compte non admin, nouvelles icônes.
    Retour du même jour : panneau de description retiré, l'infobulle du jeu revient au survol (préférée) ; icône Groupe
    dessinée (`scripts/system/gen_icone_groupe.py`, l'objet choisi n'existait pas), Titres = couronne Trork, Échanges = coffre
    du temple de lumière ; puis icônes remises à 46 px, Groupe réduite à 38 px (elle dépassait du bouton), Guilde =
    bannière, Famille = bannière de famille. **[à faire]** infobulle qui reste après fermeture : précisé le 2026-11-18 =
    souris au-dessus d'une rune + Échap (voir § 0).
  - **Touche Tab** : l'inventaire est dessiné par le client, le serveur ne peut pas le modifier. Sonde posée
    (`gui/InventoryOpenProbe`) : ouvrir l'inventaire en jeu puis chercher « [Sonde Tab] » dans le journal du serveur ; si une
    ligne apparaît, on pourra au moins réagir à l'ouverture (ex. ouvrir notre fenêtre à la place). Retirer la sonde ensuite.

- **[test] Retours du 2026-11-12** :
  - **Les 12 Mouvements de la famille Luo** (`titles/nobility/family/FamilyMovements.java`) : manuels
    `Eldanior_Mouvement_Luo_01` à `_12`, nouveau modèle stylisé de la famille (laque noire semée d'or, étiquette cramoisie
    au numéro japonais d'or, grand mon d'or, tranches dorées, dos cramoisi cousu d'or, coins de métal doré), pour toutes
    les compétences de famille Luo. Le 1er (Exécution Céleste) est remis **une fois** en rejoignant la famille (aussi aux
    membres actuels à leur prochaine connexion) ; les autres chez le Sensei. Rangs : Apprenti 1, Disciple 2, Expert 3,
    Maître 4, Grand Maître 5, Saint 6-7, Maître Suprême 8-10, Dieu Martial 11-12 ; hors de la voie = Apprenti.
    Les Mouvements 2 à 12 n'ont pas de technique : lus, ils disent « technique à venir » et restent dans le sac.
    **[à faire] 2026-11-18 : passer à 7 Mouvements** (un par rang, jusqu'au Maître Suprême), voir § 0.
  - **Sensei** : ne montre que les manuels que ton rang martial permet d'apprendre, ni ceux déjà connus ou déjà dans le sac
    (vaut aussi pour un admin, pour vérifier le filtre).
  - ~~Percée refaite (1re série)~~ : remplacée par les séries du soir (voir plus haut).
- **[test] Retours du 2026-11-11 (soir)** :
  - **Manuel** : demi-tour du livre sur lui-même, les pages tournent de gauche à droite ;
  - **Percée** (garde levée + clic gauche) sur tous les katanas sauf Kinryū-Ō : 8 paliers (refaits le 2026-11-12, voir plus haut) ;
  - **système du Bokken sur tous les katanas** : rafale au maintien, Fente du Ciel en signature (kits propres mis de côté) ;
  - ~~Exécution Céleste~~ : pas le geste attendu (2026-11-12), voir § 2 ;
  - **territoires des clans** : ne disparaissent plus au redémarrage ; **recréer les deux zones effacées** (Kage, Hanazakura)
    depuis leurs bornes (F sur la borne) ; l'intendant Luo montre aussi les domaines des clans ;
  - **Sensei en duel** : katana visible, stats des monstres selon son niveau (niv. 750 : +375 dégâts par coup, vie +5 250 :
    à rééquilibrer si c'est trop), plus d'épées de Ki ni de lame qui restent après le duel.
- **[test] Retours du 2026-11-11** :
  - endurance des katanas Luo : **5 %** par coup (au lieu de 2 %), toujours 50 % pour le coup maintenu ;
  - rafale du Bokken : lames de Ki **seulement Ki de l'Épée déployé** (Saint et au-delà) ;
  - manuel reculé encore (Z +8 → +13) ;
  - Kimono du Pèlerin : cape de paille retirée (c'était le « cuir » vu dans le kimono), col blanc ;
  - **taxe du commerce** : 10 % de chaque achat chez un PNJ marchand vont au trésor de la ville / du territoire où l'on achète
    (indiqué dans le message d'achat ; la réserve de l'intendant n'est pas taxée) ;
  - **intendant** : nouvel onglet **Domaines** (trésor, suzerain, zones vassales, lever l'impôt, rapatrier la moitié du trésor aux
    coffres, une fois par semaine) ; chiffres du haut propres à chaque onglet ; membres triés par rang ; réserve triée, articles
    trop chers en gris.

- **[test] Sensei (2e série)** : rang Grand Maître, Saint ou Maître Suprême (niveau 400 à 998, affiché « Sensei, Saint Martial ») ;
  en duel il manie le **Bokken** ; si on le frappe, il **riposte au katana de fer**, pour de vrai (fin si l'agresseur meurt, fuit à
  45 blocs, ou si le Sensei tombe sous 15 %). Son combattant utilise les techniques de son rang : aura de Ki, pas léger, projection
  d'épée, lames de Ki (Saint), rafale du combo (Saint), Incarnation de l'Âme (Maître Suprême), et celles de son clan.
- **[test] Rafale du Bokken** (maintien) : une lame de Ki à **chaque mouvement** à partir du Saint Martial, Ki de l'Épée déployé
  (2026-11-11), la dernière plus grande. Les PNJ martiaux Saints lancent aussi des lames de Ki à distance.
- **[test] Registre de la maison (intendant), refait le 2026-11-11** : la maison affichée est celle du domaine (un admin hors
  famille voyait une fenêtre vide, il agit maintenant au nom du Patriarche) ; l'intendant parle (réplique par onglet, salue par
  le rang) ; icônes d'objets dans les lignes et la fiche ; Listes = tous les inscrits, présents ou absents (registre persistant
  `family_registry.properties`, exclusion d'un absent appliquée à sa connexion) ; Ordres (lever, prêter serment, approuver,
  refuser, dissoudre) ; Gestion (livre de comptes, verser, puiser, accueillir, dissoudre) ; Achats (achat consigné au livre).
- **[test] Endurance des katanas Luo** : 5 % de l'endurance maximale par coup (kata et combo du Bokken), 50 % pour le coup
  maintenu (dégainé foudroyant, rafale du Bokken, Flash Céleste), qui échoue s'il en reste moins. Les PNJ ne paient rien.
- **[test] Fente du Ciel** (8 versions, signature des katanas Luo hors famille ; retirée du Bokken le 2026-11-12) : Apprenti taille verticale chargée ; Disciple onde au sol ; Expert
  ruée ; Maître lame de Ki verticale ; Grand Maître salto plongeant + impact ; Saint trois lames + colonne dorée ; Maître
  Suprême lame géante, sol fendu, l'âme repasse la taille ; Dieu Martial une lame tombe du ciel sur le point visé.
- **[test] Garde Royale refaite** : soie au tatewaku d'or et chrysanthèmes, épaulières et kote lamellaires lacés de cramoisi,
  tassettes (kusazuri) sur les pans, hachigane impérial et voile de nuque, hakama à filets d'or.
- **[test] Tenue pugiliste du clan Sama** (`Armor_Kimono_Sama_*`, nouvelle) : dōgi cramoisi sans manches (bras nus), sarashi,
  soleil du clan au dos, ceinture noire et corde tressée, poings bandés à plaques de fer, pantalon serré, pieds bandés. La Garde
  d'Honneur redevient une tenue du Clan Luo (catégorie et réserve de l'intendant Luo).
- **[test] Armes à feu v2** (2026-11-15, retours de l'utilisateur) : **Mousquet** (Brown Bess détaillée, baïonnette retirée du
  modèle). Force : dégâts 70 → 2 000 à 3 000 points et vitesse de la balle (140 → 600 blocs/s avec l'Endurance) ; Agilité :
  rechargement 50 s → 1 s à 3 000 ; Endurance : portée sûre 40 → 800 blocs, précision au-delà 50 → 95 %, lunette ×2,5 / 3,5 / 5 /
  7 / 9,5 / 12 (paliers 0, 300, 750, 1 350, 2 100, 3 000) et entités envoyées de loin pendant la visée (réglage global du moteur
  `LODCull.ENTITY_LOD_RATIO`, rendu dès que personne ne vise). Nouvelle lunette dessinée (laiton, graduations). Clic droit bref :
  la baïonnette est fixée 0,65 s (objet échangé contre la variante `_Baionnette`) + estoc dédié (animation `Baionnette`, jeu
  `Mousquet`), 30 dégâts × Force. Signature **Concentration** : balle à précision et dégâts ×2 (munitions, pas de rechargement).
  Énergie : 20 max, +7 par balle au but, +3 par estoc. Une balle qui tue : « Tir mortel à N blocs ! ». À vérifier : échange
  d'objet pendant l'estoc (le geste ne doit pas être coupé), **animations de rechargement** (`mousquet_anims.py`, IK : port
  d'armes en diagonale, bourrage le long du canon, amorce du bassinet ; version lente 2,5 s, rapide 1,25 s, éclair 0,7 s choisie
  selon le rechargement ; chargeur pour le SARS et culasse du canon), jouées par Java 0,25 à 0,8 s après le tir, animation d'estoc 3e personne et FPS, lunette et grossissements
  (×12 et ×20 acceptés ?), entités visibles au loin, balles à 800 blocs (chunks chargés), message de distance.
- **[test] Mousquets légendaires** (remplacent Tonnerre, Dragon, Étoiles) : **Black Bess** (noir, runes violettes, lunette ;
  portée/lunette ×1,5, +15 %), **Green Colt** (carabine verte et or ; rechargement ÷2, +10 %), **Blue Tiger** (bleu tigré, frein
  de bouche ; ×1,5, traverse 2 ennemis). Plus de capacité sur l'arme : **Rune de Transformation d'Arme** (Combat, Légendaire,
  200 mana, recharge 600 s, Mousquetaire ; refusée sans mousquet légendaire en main, sans coût) → 5 min sous une autre forme :
  SARS (5 Balles de sniper divines, 2 000 blocs), Colts de cavalerie (Colt SAA, chargeurs 5 + 5 Balles Divines, 40 blocs),
  canon de char (Obus Divin, 5 s, 100 blocs, ×5), puis retour au mousquet (registre serveur + correspondance fixe, contrôle à
  chaque seconde et à chaque tir ; une arme transformée sans fin valide redevient toujours son mousquet). À vérifier : retour
  au mousquet après 5 min et après reconnexion, prise en main du SARS et du canon.
- **[test] Classe Mousquetaire** (2026-11-15) : classe commune de départ, sans évolution, seule à manier les armes à feu
  (mousquets, transformations, fusils et pistolets vanilla : MasterySystem, admins compris) ; 6e carte de la fenêtre de choix
  (cartes de 290 au lieu de 330 : vérifier qu'elles tiennent à l'écran) ; rune de classe : Transformation d'Arme.
- **[test] Armes à feu, retours du 2026-11-15 (soir)** : plus d'effet sur le personnage (aura de signature, éclats) ; Colts de
  nouveau utilisables (ils tombaient sous la maîtrise « pistolet » des classes) ; animations de rechargement et d'estoc limitées
  au haut du corps (la marche continue) et pas pendant la visée ; portée sûre 10 → 500 blocs, jamais plus de 500 (SARS gardé à
  2 000) ; munitions refaites (corne à poudre, bourse de balles, cartouche de sniper, obus) ; messages de munitions restantes
  au rechargement.
- **[test] Chevaliers** : sur un territoire d'un clan de l'Empire (Sama, Hanazakura, Kage, Setsuka), le chevalier doit prendre
  la tenue du clan en gardant le nom du clan (Sama → tenue pugiliste Sama, Hanazakura → Sakura, Kage → Ombre, Setsuka → Setsuka, Luo → Garde).
  Conséquence : les autres PNJ posés sur les terres d'un clan portent eux aussi la tenue de l'Empire.
- **[test] Lames de Ki** : seulement à partir du Saint Martial, une par coup du kata (inclinée comme le coup) et une plus grande
  au coup maintenu (Tenryūmaru, Bokken, dégainé foudroyant à charge pleine des katanas Luo, attaque tenue > 0,8 s ailleurs).
  Plus aucune lame pour le Maître et le Grand Maître.
- **Sens martial** : validé en jeu le 2026-11-11 (« beaucoup mieux, laissons comme ça »).
- **[test] Projection d'épée** : une épée ne disparaît qu'après ses 3 coups ; cible morte → retour en orbite.
- **[test] Sensei** (nouveau PNJ, voir § 3).
- **[test] Fenêtre des quêtes du Sensei, façon Chapitres** (2026-11-12, voir § 3) : à ouvrir avec F > Quêtes. À regarder :
  mise en page (fond, séparateur, 3 cartes, détail à gauche), illustrations provisoires des actes, défilement de l'histoire,
  infobulle des récompenses, et le déroulé complet des 3 actes.
- **[test] Carte** : plus de gel à la création d'une zone ; marqueurs des territoires de l'Empire Luo.
- **[test] Pas léger** : traînées du dash vanilla, ruée guidée (Maître Martial et au-delà).
- **[test] Kata** : 12 temps avec deux tours complets (lance, hache de bataille) : à voir avec un katana en main.
- **[test] Manuel** : animation jouée une fois ; livre décalé (`BOOK_SHIFT`, voir la série du 2026-11-12 nuit) ; les
  manuels de test X, Y, Z sont supprimés.
- **[test] Méditation** du Saint et du Dieu Martial, **Vol sur épée** avancé.
- **[test] Bornes de territoire** : fenêtre, cylindre, marques des bords.
- **[test] Plus anciens** : routes de PNJ, tableaux (quêtes, classements, duels), portes à clé, présentoir, armes divines,
  armures divines de classe, Aigles du Désert, Kimono du Dieu-Dragon d'Or, Tenryūmaru au fourreau, Cercle d'Ancrage.
- **[test] Mannequin d'entraînement du Dojo** (2026-11-14) : objet `Eldanior_Mannequin_Dojo` (onglet Arts Martiaux, établi
  de meubles : 2 troncs, 6 fibres, 4 pierres). À regarder : le mannequin apparaît sur le socle et fait face à celui qui le pose ;
  il plie à chaque coup (animation Hurt) ; il ne meurt jamais, même sous une rune divine ; sa plaque affiche « Coup X · 10 s :
  Y (Z/s) » puis revient au nom ; la jauge de signature se remplit en ~20 coups ; les runes le touchent ; il reste en place
  (sorts qui repoussent) ; casser le socle le fait disparaître ; les aventuriers et familiers ne s'acharnent pas dessus ; un
  disciple posté à moins de 12 blocs vient y faire ses kata puis rentre (un seul disciple par mannequin).

## 2. J'attends une réponse

- **[réponse] Exécution Céleste (1er Mouvement)** : le geste actuel n'est pas le bon (attaque verticale à l'Apprenti). Tu as
  choisi de le décrire toi-même : décris le geste attendu (corps, lame, déplacement, ce qui arrive à la cible) et je refais
  la technique. Rien n'a été changé dans le sort en attendant.

- **[réponse] Incarnation de l'Âme et Domaine Divin du Clan Luo** : proposition à valider (`plan-clan-luo.md`, fin du fichier :
  « Âme du Dragon d'Or », « Éclipse du Soleil Noir »). Les autres clans ensuite. Attention (2026-11-18) : le Domaine Divin
  était prévu au Dieu Martial, rang supprimé ; à replacer (Maître Suprême ?) ou abandonner.
- **[réponse] Proposition B du plan Clan Luo** (9 compétences évolutives de la voie martiale) : pas encore tranchée.
- **[réponse] Contenus du Dieu Martial** maintenant que le rang est plafonné à Maître Suprême (voir § 0).
- **[plus tard] Bugs d'interaction des gardes, chevaliers, patrouilleurs** (signalés à 16h10) : un exemple précis.
- **[plus tard] Tenues des familles sans tenue** (pas la priorité, 2026-11-18) : chevalier et Sensei de Eldanior, Zippel,
  Valmontis, Ironveil, Warbane, Frostguard, Goldcrest, Silkroad, Spellweave (aujourd'hui : armure par défaut, soie pour le
  Sensei).

- **[test] Faille F v4 — Cimetière des Oubliés** (2026-11-23, `docs/plan-failles-donjons.md` § 12, jar déployé ; **relancer le monde**) :
  - [ ] Salle du boss : le Roi Squelette apparaît (générateur de boss), crâne + barre ; victoire seulement à sa mort.
  - [ ] Au-delà du mur : forêt et relief (plus de vide).
  - [ ] **Générateur de monstres** (créatif › Donjons & Keys, § 13) : invisible, marque au sol quand on le tient ; à la pose
        ou F dessus, fenêtre famille / monstre / nombre ; dans la faille, monstres à 24 blocs, retour 1 h après le groupe.
  - [ ] **Boss de faille (corrigé le 2026-11-23, soir, compilé, jar NON déployé : partie en cours)** : le boss n'apparaissait
        plus (ancien bloc de boss vide, et le poste « Boss de donjon » refusé faute de zone Donjon). Maintenant chaque faille
        ouverte fait apparaître SON boss à 24 blocs : celui choisi sur un **poste de PNJ posé dans le modèle en édition**
        (Boss de donjon ou de zone : espèce, nom, niveau, emplacement → `boss.json` du modèle), sinon celui du plan (Roi
        Squelette en 2, -39, -163). À refaire : ton poste du Cimetière (1, 81, -164) est resté vide, rechoisir le boss.
  - [ ] Créatif : plus de « Générateur de faille : … » ni de portails/étincelles ; failles scellées, sortie, coffre de donjon
        dans Donjons & Keys.
  - [ ] Clé ED dans la Nécropole, porte de boss ED, coffres au trésor près du boss.
- **[test] Faille D — La Nécropole Calcinée, v2 « la Caldeira »** (2026-11-24, `docs/plan-failles-donjons.md` § 15 ; compilé,
  modèle construit et vérifié sur serveur isolé, **jar NON reconstruit**) :
  - [ ] Partie fermée, `./gradlew jar`, relancer : le journal doit dire « modele Eldanior_Faille_Necropole remplace par la
        version du mod » (la v1 installée demandait une météo supprimée : on ne pouvait plus entrer dans la faille).
  - [ ] Ciel lie-de-vin, gros soleil rouge, lumière orangée, brume brune : est-ce assez sombre / trop rouge ?
  - [ ] Particules : flocons de cendre qui tombent, braises qui montent ; sons : grondement de lave + vent.
  - [ ] Falaise tout autour, rivière et lac de lave, orgues basaltiques, arches, fumerolles, cristaux rouges, bois en feu.
  - [ ] Esplanade vide au centre (rayon 36) : y construire le donjon (panneau admin › Failles › Éditer), puis déplacer le boss
        avec le poste « Boss de donjon » et poser les coffres au trésor.
- **[réponse] Faille E sans monstres ?** Le modèle du Terrier installé chez toi (`Mods/EldaniorFailles/.../Eldanior_Faille_Terrier`)
  n'a pas de `generateurs.json` (il date d'avant le générateur unique) : ses failles n'ont que le boss. À corriger en l'éditant
  (poser des générateurs) ou en me laissant y écrire ceux du plan.
- **[réponse] Quêtes de failles** (`docs/plan-failles-donjons.md` § 9) : idée à valider.

## 3. Sensei : ce qui est fait, ce qui reste

Fait (`guards/Sensei.java`, `spells/SenseiDuel.java`, `scripts/guards/gen_sensei.py`) :
- type de PNJ **Sensei** (bloc de poste ou `/es garde poser sensei`), un par famille (famille du réglage, sinon du territoire) ;
- tenue noble : kimono de Doyen du clan (Empire Luo), kimono de la Cour (Luo), Capitaine (Runkandel), soie (autres) ;
- **Commercer** : manuels des techniques de la famille, réservés aux membres (Luo : voie martiale ; Sama, Hanazakura, Setsuka :
  techniques du clan) ; les manuels de clan ne sont plus donnés automatiquement (`FamilyKeySystem.AUTO_TECHNIQUES = false`) ;
- **Duel** d'entraînement sans mort (premier sous 15 % de vie, abandon à 30 blocs, 3 min maximum).

Reste :
- **[test] Quêtes du Sensei : les Épreuves du Dojo** (2026-11-13, 3e série) : Q1 vaincre 3 disciples (rang Disciple) ;
  Q2 abattre 8 bêtes rapides (lièvres, cerfs, antilopes, renards) ; Q3 abattre 5 criminels ; Q4 vaincre un disciple Grand
  Maître + rapporter 10 défenses de Trork (monstre niveau 400 = promotion du Maître) ; Q5 vaincre le Sensei en duel (bouton
  « Défier le Sensei »). « Trouver un adversaire » : le Sensei nomme un disciple et un lieu (« à Hanamura », « près de X »),
  plus de coordonnées ; seul ce disciple accepte le duel. Quêtes verrouillées : rouge + cadenas, rien d'affiché (nom « ??? »,
  le Sensei dit seulement d'attendre). Textes dits par le Sensei. Halo doré au survol des cartes.
  À vérifier : que les mises à mort de criminels et d'animaux comptent (DeathXPSystem → QuestManager.onMobKill).
- **[test] Disciples** : on leur parle (F) → Duel **seulement si le Sensei les a désignés** ; duel au **katana de fer, à
  mort** (le disciple tué disparaît, son poste le fait revenir) ; rôle civil (comme le Sensei : coups annulés, riposte au katana de fer) ;
  rang tiré de Apprenti à Grand Maître ; kimono selon le rang (Recrue, Ronin, Garde, Noble, Seigneur), Bokken en main.
  Ils ne se battent plus aux côtés des gardes.
- **[test] Quêtes activables par un admin** sur tout PNJ qui parle (bloc de poste → « Quêtes : Activées ») ; carte « + »
  (admins seulement) : aperçu d'une quête de la bibliothèque, « Ajouter cette quête » / « Autre quête ». La bibliothèque de
  test (Loups de la Lisière, Bois pour l'Hiver, Menace Gobeline) a été supprimée à la remise à zéro du 2026-11-14 ;
  « L'Épopée d'un Roi » est devenue la quête principale. Principale et exclusive : une seule fois ; secondaire et
  journalière : refaisables après 5 min (test). Épreuves du Dojo = quêtes exclusives.
- **[test] Corrigés le 2026-11-13** : disciple invisible (apparition qui plantait sur le fourreau) ; habitants qui fuyaient
  et ne se parlaient plus (rôle régénéré, j'avais remis par erreur une vieille version) ; pas de numéro pour une quête sans suite.
- **Délais définitifs validés** (2026-11-18) : 24 h journalières, 48 h secondaires ; on reste à 5 min
  (`REPEAT_TEST_MINUTES`) tant qu'on teste.
- **[test] Boutique du Sensei** : seulement l'équipement du dojo (kimono de Ronin, Bokken, Parchemin) ; plus aucun manuel.
  Les Mouvements de famille et techniques de clan n'ont donc plus de source tant que leurs quêtes n'existent pas.
- **Ki et Méditation au serment** : toujours donnés à l'entrée dans la voie, **aucun changement** (2026-11-18).
- **[plus tard] Quêtes après le Saint Martial** : « quêtes plus précises », à définir.
- **[à faire] Illustrations des actes** : dessins provisoires générés (aube, nuit, duel) ; vraies images à prévoir.
- **[à faire] Récompense du duel** (XP, titre, accès à une technique ?) et limite (une victoire par jour ?).
- **[à faire] Techniques des clans sans technique** : Kage (assassinat, ombre), Runkandel (Lame d'Aura Céleste), autres familles.
- **[à faire] Prix des manuels** à équilibrer (barème provisoire : 60 × niveau^1,35 cuivres) : désormais dans le barème
  central (`/es prix`, famille Runes & compétences / Livres), à reprendre quand les boutiques y seront branchées.

## 4. Intendant : ce qui reste

**Chantier du 2026-11-18** : tout est repris et détaillé dans `docs/plan-intendant.md` (lots, ordre, questions).

- **[à faire] Domaines** : régler depuis l'intendant ce que l'onglet Territoires permettait (PvP, protection, annonce, mise
  en vente) ; suivi de la taxe du commerce perçue par domaine.
- **[à faire] Changer le rang d'un membre absent** (aujourd'hui : seulement en sa présence ; l'exclusion d'un absent marche).
- **[à faire] Paiement par la trésorerie** de la famille (aujourd'hui : pièces de l'acheteur).
- **[à faire] Contenu de la réserve** à valider (bannière, présentoir, tenues des membres) ; parchemins de rune plus tard.
- **[à faire] Tenues de chef** (Doyen, Patriarche) : remises par le rang, pas vendues ; à confirmer.
- **[à faire] Agréer / révoquer les marchands du Comptoir** (`/es family`) : à déplacer dans l'intendant Luo.
- **[à faire] Décrets royaux** : ne sont plus accessibles depuis la suppression de l'onglet Territoires ; à remettre chez
  l'intendant de la maison royale (Eldanior).

## 5. Voie martiale et Empire Luo

- **[à faire] Spécialités de chaque katana** (plus tard, décision du 2026-11-11) : les anciens kits (Ressac, Éclipse, Neige
  Silencieuse, Vol de l'hirondelle, Iaigiri) sont gardés dans le code mais plus branchés.
- **[à faire] Ce que chaque palier débloque sur les katanas** (mis de côté).
- **[à faire] Les quatre clans** (proposition A du plan, **validée le 2026-11-18** : vassaux du Clan Luo) : rang « Clan »,
  rune de famille de chaque clan.
- **[test] PNJ et rang martial** (proposition C, codé le 2026-11-10, **validée le 2026-11-18**) : rang dans le nom, aura de Ki,
  ruée, lames, épées d'aura. **[à faire]** plafond Maître Suprême (plus de Dieu Martial, joueurs comme PNJ).
- **[à faire] Déplacement propre du PNJ assassin (Ombre).**
- **[à faire] Sens martial en vue à la 3e personne** (l'illusion n'est juste qu'à la 1re personne).
- **Épées de particules `Ki_Epee_Holo_T*`** : réutilisées dans l'Exécution Céleste (2026-11-11).

## 6. Territoires et carte

- **[à faire] Bordures des zones sur la carte** : seuls les marqueurs sont affichés (le dessin sur les images de la carte est
  désactivé : il faisait planter le client).
- Tenues des PNJ par famille hors Empire Luo : voir § 2 (j'attends ta réponse).

## 7. Autres chantiers ouverts

- **[plus tard] Débordement des failles, évolution** (décidé le 2026-11-22 : version simple d'abord = explosion du portail +
  un groupe de monstres du thème) : vagues successives qui grossissent, débordement qui marche vers la ville la plus proche,
  lien avec les invasions des domaines (`Invasions`, amendes, défenseurs), récompense pour qui le repousse, boss de faille
  qui sort pour les rangs A/S. Voir `docs/plan-failles-donjons.md` § 7.4.
- **[à faire] Mousquet de base** : toujours ni recette ni vendeur.
- **[à faire] Rune de famille Runkandel** : Lame d'Aura Céleste (`Rune_AuraBlade` existe avec l'icône vanilla empruntée
  `Rune_WindStrike` : il lui faut son propre emblème ; je n'y ai pas touché).
- **[à faire] Runes des classes mages** : feuille de route du 2026-10-26 (`references/mage-roadmap.md` du skill).
- **[à faire] Commande `/es family`** (agréer / révoquer les marchands du Comptoir) : à déplacer dans l'intendant, voir
  `docs/plan-intendant.md`.
- **[à faire] Forgerons légendaires** : à voir en jeu, contenu à ajuster.
- ~~Quêtes des PNJ : système désactivé~~ : remplacé par les quêtes façon Chapitres (bloc de poste « Quêtes : Activées »,
  carte « + ») ; il reste à vérifier en jeu qu'un PNJ valide une étape « va parler à… » (§ 1).
