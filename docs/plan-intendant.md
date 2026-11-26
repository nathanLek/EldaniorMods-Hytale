# Plan : l'intendant (chantier du 2026-11-18)

Code : `guards/StewardScreen.java` (registre, 1 120 lignes), ouvert par `guards/LuoMerchants.java` (droit d'accès :
`stewardAllowed`, maison du domaine : `stewardFamily`). Fenêtre partagée avec les tableaux : `boards/BoardScreen.java` +
`Common/UI/Custom/Board/Board.ui` (**6 onglets au plus, 4 tuiles, 7 lignes par page**, fiche à droite avec un champ de
montant, un bouton principal, un bouton secondaire et Fermer). Logique métier réutilisée : `gui/tabs/FamilleTab.java`
(rangs, trésorerie, invitations, dissolution), `gui/tabs/TerritoiresTab.java` (impôt, rapatriement ; onglet retiré du menu
Système le 2026-11-12), `titles/nobility/family/FamilyRegistry.java` (registre persistant, livre de comptes de 40 lignes,
exclusion différée), `territory/ParcelEconomyManager.java` (taxe du commerce), `guards/LandOfficeScreen.java` (Terres).

## 1. Ce qui existe

| Onglet | Contenu | Gestes |
|---|---|---|
| **Listes** | tous les inscrits (présents / absents), rang, rang martial, niveau | monter / descendre d'un rang (**présent seulement**), rayer (Patriarche, absent compris), prendre la tête d'une maison libre |
| **Ordres** | ordres de chevalerie et requêtes | lever un ordre, prêter serment, approuver / refuser, dissoudre |
| **Gestion** | livre de comptes (11 dernières lignes affichées), coffres | verser, puiser (Patriarche), accueillir un noble sans maison, dissoudre la maison |
| **Domaines** | villes, territoires, royaumes de la maison (+ clans vassaux en lecture pour Luo), trésors | lever l'impôt, rapatrier la moitié du trésor (1 fois / semaine), Terres à acquérir (Comte+) |
| **Biens** | parcelles, maisons, chambres, fermes, forêts, mines des domaines | mettre en vente, en location (7 j), retirer du marché |
| **Achats** | réserve de la maison (`Eldanior_Intendant_<Famille>`) | acheter avec **la bourse** de l'acheteur |

## 2. Ce qui manque (constaté dans le code)

1. **Rang d'un absent** : `FamilleTab.changeRoleOf` refuse si le joueur n'est pas en ligne (l'exclusion, elle, est différée
   par `FamilyRegistry.kickLater`).
2. **Réserve payée par les coffres** : `ShopPage.purchase` ne sait payer qu'avec la bourse du joueur.
3. **Réglages d'un domaine** perdus avec l'onglet Territoires : combat entre joueurs, protection des blocs, annonce à
   l'entrée (`ParcelData.announce`), revente d'une ville. Seule la borne en garde une partie.
4. **Taxe du commerce** : `ParcelEconomyManager.payTradeTax` crédite le trésor sans rien noter : impossible de savoir ce
   qu'un domaine a perçu.
5. **Décrets royaux** (`TerritoiresTab.handleGiveDecret`, quotas `NobilityManager.canKingPromote`) : plus aucun bouton ne
   les délivre depuis le retrait de l'onglet.
6. **Comptoir de la Porte du Dragon** : agréer / révoquer un marchand étranger passe encore par la commande `/es family`.
7. **Ordres de chevalerie** : « Territoire : non assigné » partout, aucun moyen d'en assigner un.
8. **Tenues de chef** (Doyen, Patriarche) et contenu de la réserve : jamais validés.
9. **Livre de comptes** : 40 lignes gardées, 11 visibles, aucun tri.
10. **Clans vassaux** (proposition A validée le 2026-11-18) : l'intendant Luo les voit mais l'Empire n'a aucun lien
    financier ni de gestion avec eux.

## 3. Lots de travail (ordre proposé)

### Lot 1 — Membres : rang d'un absent *(simple, sans question)*
- `FamilyRegistry.roleLater(familyId, uuid, role)` + `consumeRole` (persistés dans le registre, comme `kickLater`) ;
  appliqué à la connexion dans `PlayerLoginSystem` (même endroit que l'exclusion différée), avec un message (« Pendant votre
  absence, le Patriarche vous a élevé au rang de Noble »).
- Le registre affiche aussitôt le nouveau rang, suivi de « (à sa prochaine visite) » ; la limite de 4 Doyens compte les
  promotions en attente ; un second changement remplace le premier.
- Fiche du membre : supprimer « Son rang ne peut changer qu'en sa présence » ; boutons ↑ / ↓ actifs pour les absents.
- Écriture différée : passer par `cb.run` / hors tick si on touche `PlayerLevelData` (piège « Store is currently
  processing! »).

### Lot 2 — Domaines : gouverner depuis l'intendant
- **2.1 Réglages du domaine** : trois boutons de réglage dans la fiche, à bascule : **Combat entre joueurs** (permis /
  interdit), **Blocs** (protégés / ouverts), **Annonce à l'entrée** (habituelle / toujours / jamais). Il faut ajouter à
  `Board.ui` une rangée de 3 petits boutons (`#BoardSideOpt0` à `2`, invisibles dans les autres fenêtres) et leur prise en
  charge dans `BoardScreen` (`sideOptions()` / `onOption(i)`). IDs sans tiret bas après `#`. Droit : Patriarche, Seigneur,
  maître du domaine (`canRule`). Chaque changement va au livre de comptes.
- **2.2 Taxe du commerce suivie** : compteurs par zone dans `ParcelData` (`tradeTaxWeek`, `tradeTaxTotal`, début de
  semaine), alimentés par `payTradeTax`. Affichés dans la fiche (« Taxe du commerce : X or cette semaine, Y depuis
  toujours »), la ligne du domaine et une tuile « Taxe (7 j) ». Une ligne de bilan au livre de comptes à chaque rapatriement.
- **2.3 Revente d'un domaine** (voir question Q2) : bouton « Mettre en vente » sur une ville ou un territoire de la maison →
  il apparaît dans les Terres du bureau des terres (`LandOfficeScreen`). Remplace `handleSellCity` (remboursement fixe de 50 %).

### Lot 3 — Trésorerie
- **3.1 Achats payés par les coffres** : dans la fiche d'un article de la réserve, bouton secondaire « Payer avec les
  coffres ». Nouvelle variante `ShopPage.purchaseFromTreasury(shop, index, qty, familyId, …)` : même stock et même échange
  (`executeTrade`) mais prix retiré de `FamilyRuntimeData.treasury` ; objets donnés à l'acheteur ; ligne au livre. Qui y a
  droit : voir Q1.
- **3.2 Livre de comptes lisible** : la fiche montre les 40 lignes avec un défilement, et chaque ligne a un genre (dépôt,
  retrait, impôt, taxe, achat, rang, ordre) ; une tuile par genre dans l'onglet Gestion (entrées / sorties de la semaine).

### Lot 4 — Pouvoirs de la couronne et de l'Empire
- **4.1 Décrets royaux** chez l'intendant de la maison royale (Eldanior), pour le **Roi** seulement : lignes dans Gestion
  « Délivrer un décret de Marquis / Duc / Comte / Baron / Chevalier », quota restant affiché (`canKingPromote`). La logique de
  `handleGiveDecret` passe dans une méthode commune.
- **4.2 Comptoir de la Porte du Dragon** chez l'intendant Luo (Patriarche et Doyens) : liste des marchands agréés
  (« Révoquer », deux clics) et des étrangers présents (« Agréer ») ; la plaque est remise ou retirée par `LuoPlaqueSystem`
  comme aujourd'hui. Ensuite, suppression de `/es family` (ou passage en commande d'admin).
- **4.3 Clans vassaux** (après Q5) : l'Empereur lève un **tribut** hebdomadaire sur les trésors des quatre clans, depuis
  l'onglet Domaines (même geste que l'impôt).

### Lot 5 — Ordres de chevalerie
- **Assigner un domaine à un ordre** (Patriarche) : choix parmi les domaines de la maison depuis la fiche de l'ordre ; le
  nom du domaine remplace « non assigné ». Effet en jeu : voir Q3.
- Nommer ou retirer le **Lieutenant** depuis la fiche (Capitaine ou Patriarche), s'il n'existe pas déjà ailleurs.

### Lot 6 — Réserve et tenues de rang
- **Contenu de la réserve à valider** (Luo aujourd'hui) : bannière (10 or), présentoir (5 diamants), kimonos Rizières,
  Pauvre, Recrue, Ronin, Marchand, Négociant, Pèlerin, Cour, Lettré…, thés, onguent. Les autres maisons : bannière,
  présentoir, porte, tenues. Générateur : `scripts/guards/gen_intendant.py`.
- **Tenues de chef** (Doyen, Patriarche) : retirées de la vente et remises par le rang (voir Q4), avec une ligne « Recevoir
  ma tenue de rang » dans Gestion si on l'a perdue.

### Lot 7 — Finitions
- Rappels au Patriarche (et au Seigneur) à la connexion : impôts à lever, rapatriement possible, requêtes d'ordre en attente,
  baux qui arrivent à échéance dans les domaines.
- Répliques de l'intendant pour les nouveaux gestes ; icônes des nouvelles lignes (décret, plaque de marchand).
- Mise à jour de `docs/commandes.md` (suppression de `/es family`), `docs/a-faire.md` § 4, mémoire du projet.

## 4. Décisions de l'utilisateur (2026-11-18)

- **Q1** : réserve payable par les coffres pour le **Patriarche, les Doyens et le Seigneur**, avec une **limite par semaine**
  (choix : le Patriarche n'a pas de limite ; il fixe l'allocation hebdomadaire des Doyens et du Seigneur, 5 000 or par défaut).
- **Q2** : oui, la maison peut **revendre une ville ou un territoire** ; l'argent va **aux coffres de la famille**.
- **Q3** : un ordre est **une petite guilde dans la famille**. Hiérarchie de la maison :
  **Patriarche > Doyens > Seigneur > Nobles (Comte, Baron) > Chevalier Royal (nouveau rang) > Chevalier > Recrue**.
  Seuls les **Chevaliers ou plus** demandent la création d'un ordre au Patriarche ; le **Capitaine** est le créateur ou le
  chef de l'ordre. (Plus d'assignation de domaine à un ordre.)
- **Q4** : **seul le Patriarche reçoit un équipement** à la promotion (ex. un Doyen / Duc qui achète un territoire et devient
  Patriarche de sa maison reçoit sa tenue). Aucun autre rang ne reçoit de butin.
- **Q5** : **Taxe Impériale** des quatre clans vassaux vers le Clan Luo, taux à mon choix (**10 % par semaine** du trésor des
  domaines des clans).
- **Q6** : accord donné ; devenu sans objet : la **fenêtre de l'intendant est refaite entièrement** (voir § 5 bis).
- **Nouveau** : le **Patriarche crée des quêtes** pour les joueurs depuis l'intendant ; il **paie la récompense** (retenue à
  la création, rendue si la quête est retirée).
- **Nouveau** : **nouvelle fenêtre** de l'intendant « comme la fenêtre des quêtes, stylisée et adaptée », avec des
  catégories et des actions cliquables.

## 5 bis. Nouvelle fenêtre « Registre de la maison »

Même habillage que la fenêtre Chapitres (fond, bande d'encre, cadre d'emblème, séparateurs, cadres teintés ; images
reprises de `Custom/Quest/` par `../Quest/…`). Fichier `Intendant/Intendant.ui` généré par
`scripts/intendant/gen_intendant_ui.py`, page `guards/intendant/IntendantPage.java` (remplace `StewardScreen`).
- **Gauche (fiche)** : emblème (bannière de la maison), nom de la maison, réplique de l'intendant ; puis la fiche de
  l'élément choisi : titre, sous-titre, texte, lignes d'information à puces, **jusqu'à 4 boutons d'action**, champ de montant.
- **Droite** : **barre de catégories** (Membres, Ordres, Trésor, Domaines, Biens, Réserve, Quêtes, + Couronne pour le Roi /
  Empire pour Luo), **4 tuiles de chiffres**, puis une **grille de cartes cliquables** (icône, nom, ligne d'état, valeur,
  liseret coloré selon l'état) avec pages.

## 5 ter. Ordre de travail révisé

1. Moteur : rangs (Chevalier Royal, Chevalier, Recrue), rang d'un absent, suivi de la taxe du commerce, coffres
   (allocation, achat), tenue du Patriarche, Taxe Impériale, revente de domaine, règles des ordres, décrets, Comptoir.
2. Nouvelle fenêtre et branchement de tout le moteur.
3. Quêtes de la maison (création par le Patriarche, récompense retenue).
4. Hors intendant, décidé le même jour : Dieu Martial hors des évolutions (commande comme Dragon) ; suppression de
   Nighthollow et Swiftquiver.

## 5 quater. Fait le 2026-11-18 (compilé, jamais vu en jeu, jar non reconstruit : partie en cours)

- **Nouvelle fenêtre** `guards/IntendantPage.java` + `Intendant/Intendant.ui` (`scripts/intendant/gen_intendant_ui.py`) ;
  l'ancienne `StewardScreen` est retirée (copie de secours hors du projet). Ouverte par tout membre de la maison ; un étranger
  n'y voit que **Quêtes** (+ **Domaines** réduit aux « Terres à acquérir » s'il est Comte ; + **Membres** s'il peut prendre la
  tête d'une maison libre).
- **Rangs** `FamilyRole` : Recrue (ex-« Membre », même code `MEMBER`), **Chevalier**, **Chevalier Royal**, Noble, Seigneur,
  Doyen, Patriarche. Plaques Luo : Chevalier et Chevalier Royal portent la plaque de Recrue (à dessiner si besoin).
- **Rang d'un absent** : `FamilyRegistry.roleLater` / `consumeRole`, appliqué dans `PlayerLoginSystem`.
- **Tenue du Patriarche** : `PatriarchOutfit` (Luo → `Armor_Kimono_Empereur`, Runkandel → `Armor_Runkandel_Patriarche`,
  Eldanior → `Armor_Eldanior_Roi`), remise une fois par maison, vérifiée toutes les 5 s par `LuoPlaqueSystem` (les
  Patriarches actuels la reçoivent aussi). Autres maisons : aucune tenue définie.
- **Coffres** : allocation hebdomadaire (`FamilyRegistry.allowance`, 5 000 or par défaut, fixée par le Patriarche) ; achat de
  la réserve payé par les coffres (`ShopPage.purchaseWith`).
- **Taxe du commerce suivie** (`ParcelData.addTradeTax`, semaine et total) ; **Taxe Impériale** 10 % par semaine et par
  domaine de clan (catégorie Empire) ; **revente d'une ville ou d'un territoire** par la maison (`LandOffice.houseSale`,
  pas la terre principale, prix aux coffres, l'acheteur en devient le maître).
- **Domaines** : impôt, rapatriement, combat, blocs, annonce, mise en vente. **Biens** : vendre, louer, retirer.
- **Ordres** : demande ou fondation réservée aux Chevaliers ou plus ; tout membre peut prêter serment ; quitter ; dissoudre
  (Patriarche ou Capitaine).
- **Couronne** (maison Eldanior, Roi ou admin) : décrets royaux. **Empire** (Luo, Seigneur ou plus) : Taxe Impériale,
  agréer / révoquer les marchands du Comptoir (`/es family` existe encore, à retirer après essai en jeu).
- **Quêtes de la maison** (`quest/chapitres/HouseQuests`, type `MissionType.MAISON`, contour orange) : chasse (12 cibles)
  ou collecte (objet tenu en main), quantité, récompense par joueur, places (1/3/5/10/25), maison seule ou tous ; coût
  retiré du compte du Patriarche ; places non prises rendues au retrait ; objets livrés récupérés chez l'intendant.
- **Dieu Martial** : le Maître Suprême n'y évolue plus (`VoieMartiale.GOD`) ; attribué par `/es setclass … dieu_martial`.

## 5 quinquies. 2e passe du 2026-11-18 (retours de l'utilisateur après essai)

- **Déconnexion corrigée** (« Selected element … #IntCat0 not found ») : une action qui ouvre une autre fenêtre (quêtes,
  terres, réserve) ne renvoie plus de mise à jour à l'intendant (`replaced`).
- **Quêtes de la maison au tableau des quêtes**, plus chez l'intendant (`HouseQuests.boardActs`, ajoutées dans
  `QuestChaptersPage.acts` pour le donneur `tableau`) : ouvertes à tous, ou seulement aux membres ; sans place libre, plus
  montrées (sauf à qui l'a prise). Annonce dans le chat à la publication. L'intendant garde la gestion (suivre, récupérer
  les objets, retirer) ; un étranger sans rien à y faire reçoit une réplique qui l'envoie au tableau.
- **Création refaite** : « Publier une quête » passe la grille en mode création (cibles de chasse avec icônes, ou objets du
  sac pour une collecte ; Annuler ; changer de type) ; la fiche est un formulaire (nombre, récompense, **places** = nouveau
  champ, titre, **message du Patriarche**), coût total, compte et rang mis à jour en direct ; contrôles à la publication.
- **Nom des objets** traduit dans la langue du joueur (repli anglais), comme `/es prix` (avant : « DragonHeart »).
- **Habillage** : cartes et tuiles sur le panneau du HUD des quêtes (`QHudPatch`), survol en cadre `ChapSlot`, case d'icône,
  valeur à la couleur de l'état, bandeau et séparateurs dorés des catégories et des sections.

## 6. À vérifier en jeu à la fin

Rang changé hors ligne puis connexion du membre ; limite des Doyens avec une promotion en attente ; taxe perçue après un
achat chez un marchand du domaine ; trois réglages du domaine (PvP réel, blocs, annonce à l'entrée) ; décret délivré et
quota ; agrément puis révocation d'un marchand (plaque) ; achat sur les coffres (stock, livre de comptes).

## 7. Brainstorming du 2026-11-18 (3e passe) : domaines, titres, Royaume, ordres

Principe rappelé par l'utilisateur : **un seul intendant par maison, le même pour tout le monde** ; seule la vue change
selon le rôle dans la maison, le rang de noblesse et la famille du joueur.

### 7.1 Gestion des domaines : vue d'ensemble et détail

Constat : l'onglet Domaines est une liste plate (royaumes, grands territoires, territoires, villes mêlés), sans population
et sans lien parent / enfant ; les biens sont dans un autre onglet.

Pistes étudiées :
- **A. Arbre déplié** (toutes les zones, indentées) : tout d'un coup d'œil, mais illisible dès 30 zones avec 12 cartes par page.
- **B. Descente par niveaux (fil d'Ariane)** : on part du sommet (Royaume Eldanior, Grand Territoire Luo…), chaque carte est
  une terre sous tutelle ; « Entrer » montre ses terres vassales, une carte « ↑ Remonter » revient au niveau du dessus ;
  le titre de la grille donne le chemin (« Royaume Eldanior › Marquisat Luo › Duché X »). Au dernier niveau (ville) : les biens.
- **C. Filtres par type** (Grands territoires / Territoires / Villes / Biens) : utile pour retrouver une ville, mais perd la hiérarchie.

**Retenu : B**, avec les tuiles du haut qui résument **tout le sous-arbre** du niveau affiché (population, trésors, taxe du
commerce 7 j, impôts à lever). C reste possible plus tard (une carte « Toutes les villes »).

Population d'une terre (sous-arbre compris) :
- **Habitants** : joueurs distincts propriétaires ou locataires d'un bien, maîtres de la terre et membres inscrits sur la zone ;
- **PNJ** : postes de PNJ placés dans les limites (gardes, marchands, artisans, aventuriers…), comptés par genre ;
- **Présents** : joueurs connectés qui s'y trouvent en ce moment.
La carte montre « 12 habitants · 30 PNJ » ; la fiche détaille (propriétaires, locataires, PNJ par genre, présents, biens
libres / à vendre / à louer / occupés).

Terres sous tutelle d'une maison : ses propres domaines **et tout ce qui est dessous** (une ville d'une autre maison dans
notre duché apparaît, en lecture, avec le nom de sa maison). Eldanior part du Royaume (les 4 Marquisats sont dessous) ;
Luo part de son Grand Territoire (les domaines des 4 clans sont dessous).

### 7.2 Titres de noblesse chez chaque intendant

Constat : les quotas de décrets étaient **par rang et partagés** (« ROI:MARQUIS ») : tous les Comtes du serveur se
partageaient 2 Chevaliers, et seul le Roi avait un bouton.

**Retenu** : catégorie **Titres** chez *tous* les intendants, pour tout noble qui peut en conférer (Comte, Duc, Marquis, Roi).
- Quotas du rang actuel (inchangés) : Roi 1 Marquis, 2 Ducs, 3 Comtes, 4 Barons, 10 Chevaliers ; Marquis 2 Comtes, 1 Baron,
  4 Chevaliers ; Duc 1 Baron, 3 Chevaliers ; Comte 2 Chevaliers.
- **Registre personnel et cumulatif** de ce que chaque noble a déjà conféré : restant = quota du rang actuel − titres déjà
  donnés **depuis toujours**. Un Comte qui a donné ses 2 Chevaliers et devient Duc n'en a plus qu'1 (3 − 2).
- Le décret va dans le sac ; le joueur à nommer l'utilise (comme aujourd'hui). La fiche liste les décrets déjà délivrés.
- Les anciens compteurs du Roi passent au Roi actuel au premier chargement.

### 7.3 Royaume = Empire pour Eldanior

La catégorie **Couronne** devient **Royaume** (maison Eldanior), symétrique d'**Empire** (Clan Luo) :
- les **4 Marquisats vassaux** (Luo, Runkandel, Valmontis, Zippel) avec trésor, population, et la **Taxe Royale**
  (10 % du trésor par semaine, comme la Taxe Impériale), levée par le Roi ou le Seigneur ;
- les décrets du Roi passent dans **Titres** (même registre que les autres nobles).

### 7.4 Hiérarchie dans un ordre

L'idée fournie (7 rangs) est adaptée ainsi :

| Rang | Titre (Luo : « Impérial », Eldanior : « Royal ») | Places | Qui le confère |
|---|---|---|---|
| I | Grand Maître | 1 (le fondateur, ex-Capitaine) | — |
| II | Vice-Maître | 1 (ex-Lieutenant) | Grand Maître |
| III | Haut Commandant | 2 | Grand Maître, Vice-Maître |
| IV | Commandant | 4 | jusqu'à un rang sous le sien |
| V | Maître | — | idem |
| VI | Disciple | — | idem |
| VII | Aspirant | — | entrée par serment |

- Un officier ne change que le rang de ceux **sous lui**, et jamais au-delà du rang **juste sous le sien** ; il peut renvoyer
  ceux sous lui (Commandant ou plus).
- **Lien avec la maison** : les rangs d'officier (Commandant et plus) sont réservés aux **Chevaliers de la maison ou plus** ;
  une Recrue plafonne à Maître.
- Le Patriarche agit comme Grand Maître dans tous les ordres de sa maison.
- **20 membres** par ordre (au lieu de 10) pour que la hiérarchie ait du sens.
- Dans l'intendant, choisir un ordre puis « Entrer » montre ses membres en cartes, triés par rang.

### 7.5 Fin de la session précédente

- **Livre de comptes lisible** : « Ouvrir le livre » montre les 40 lignes en cartes, colorées par genre (entrée, sortie, rang,
  ordre, domaine).
- **Rappels à la connexion** (Patriarche et Seigneur) : impôts à lever, rapatriements possibles, requêtes d'ordre, taxes de
  vassaux dues.

### 7.6 Joueurs fictifs (essai)

`/es intendant fictifs` (admin) inscrit 5 joueurs fictifs dans le Clan Luo (rangs, niveaux, classes et dernière visite
variés) et les place dans un ordre de démonstration ; `/es intendant fictifs retirer` les efface. Ils ne se connectent
jamais : leurs changements de rang restent « à sa prochaine visite ».

### 7.7 Fait le 2026-11-18, 3e passe (compilé, jamais vu en jeu, jar NON reconstruit : partie en cours)

- **Domaines** en descente par niveaux (`guards/IntendantLands` : racines, enfants, chemin, population, vassaux) : cartes
  « habitants · PNJ · terres », « Remonter », titre = chemin, tuiles sur tout le niveau ; fiche avec population détaillée (PNJ
  par genre), « Entrer », impôt, rapatriement, **Règles du domaine** (sous-fiche combat / blocs / annonce, pour rester à 6
  boutons), vente. Terres d'une autre maison en violet, en lecture. Au dernier niveau : les biens (même fiche que Biens).
- **Titres** (`NobilityManager.given / remaining / recordGrant / grantHistory`, persistés dans `hierarchies.properties`) :
  catégorie chez tous les intendants pour Comte, Duc, Marquis et Roi ; anciens compteurs « ROI:… » passés au Roi actuel.
- **Royaume** (maison Eldanior, Seigneur ou plus, Roi, admin) remplace Couronne : Marquisats vassaux, population,
  **Taxe Royale** 10 %/semaine (mêmes champs que la Taxe Impériale, `IntendantLands.vassals`).
- **Ordres** : `KnightOrderRole` = 7 rangs (titres « Impérial » chez Luo, « Royal » chez Eldanior), `KnightOrder` garde rang
  et nom de chaque membre (persistés : `.ranks`, `.names` ; ancien Lieutenant = Vice-Maître), 20 membres ; « Voir les
  membres » → cartes triées par rang, Élever / Abaisser / Renvoyer selon les règles du § 7.4.
- **Livre de comptes** en cartes colorées par genre ; **rappels à la connexion** (`IntendantPage.reminder`, `PlayerLoginSystem`).
- **Sauvegarde immédiate** des ordres et des titres (`PersistenceManager.saveOrdersAndTitles`) : avant, seulement à l'arrêt.
- **Joueurs fictifs** : `/es intendant fictifs ajouter|retirer` (`guards/IntendantCommand`) : Luo Mingyu (Doyen, 640),
  Luo Shuang (Noble, 430), Tian Wei (Chevalier Royal, 355), Jian Hao (Chevalier, 220), Yun Xiao (Recrue, 48) ; Ordre des
  Lames du Dragon d'Or (Tian Wei Grand Maître, Luo Shuang Vice-Maître, Jian Hao Commandant, Yun Xiao Disciple).

À vérifier en jeu : descente Royaume › Marquisat › Duché › Ville › biens et retour ; chiffres de population (PNJ de poste,
présents) ; un décret délivré puis le compteur après un changement de rang de noblesse ; Taxe Royale sur un Marquisat ;
élever / abaisser un fictif dans l'ordre (limite des officiers pour une Recrue) ; livre de comptes ; rappel à la connexion ;
les symboles « et » des cartes de retour s'affichent bien.

### 7.8 Retours du 2026-11-18 (4e passe, compilé, jamais vu en jeu, jar NON reconstruit : partie en cours)

Décisions de l'utilisateur :
- **Domaines** : plus de biens dans la descente (Marquisat › Duché › Ville s'arrête aux domaines ; les biens restent dans Biens).
- **Ordres** : plus de limite ; chaque ordre coûte **1 000 000 or aux coffres** (fondation par le Patriarche, ou approbation
  d'une requête). Le Patriarche choisit le **Grand Maître** (un Chevalier libre de la maison, ou lui-même) : il peut donc en
  lever autant qu'il veut. **Recrutement sur demande** activable par le Grand Maître / Vice-Maître (ou le Patriarche) :
  ouvert = un membre demande, un officier (Commandant ou plus) accepte ou refuse ; fermé = **ordre d'élite**, les officiers
  enrôlent depuis la fiche d'un membre (« Enrôler »).
- **Rangs de maison automatiques** (`FamilyRole.effective`, `HouseRoles.sync` toutes les 5 s dans `LuoPlaqueSystem`) :
  **Doyen = Duc**, et le **Patriarche d'une maison ducale dont le Duché est dans le Marquisat est Doyen d'office** de la maison
  du Marquisat (`HouseRoles.vassalDukes` : carte « Doyen d'office » dans Membres, droits de Doyen chez cet intendant) ;
  **Noble = Comte ou Baron** ; **Chevalier Royal = titre de Chevalier**. Le Patriarche ne choisit que **Recrue, Chevalier,
  Seigneur** (Doyens et Seigneur : Recrue ↔ Chevalier). Un rang automatique sans son titre redevient Chevalier.
- **Marquisat Luo** : son Patriarche confère en plus **4 Ducs, 3 Comtes, 8 Barons, 20 Chevaliers** (`NobilityManager.LUO_BONUS`,
  ajoutés aux quotas de son rang : avec Marquis, 4 Ducs, 5 Comtes, 9 Barons, 24 Chevaliers).
- **Terres** : achat **dans l'intendant** (Domaines › Terres à acquérir : cartes vertes si le titre et le compte suffisent,
  achat en deux clics, `LandOffice.buy`) ; le **Commerçant immobilier** n'a plus l'onglet Terres (Mes biens, Marché).

À vérifier en jeu : un Comte / Baron / Chevalier qui entre dans une maison prend Noble / Noble / Chevalier Royal en 5 s ;
le Patriarche d'un clan apparaît Doyen d'office chez l'intendant Luo ; fondation à 1 M (coffres) avec un fictif comme
Grand Maître ; demande d'entrée puis acceptation ; recrutement fermé puis « Enrôler » ; achat d'une ville chez l'intendant ;
le Commerçant immobilier ne montre plus que deux onglets.

### 7.9 Corrections du 2026-11-18 (retours sur les Domaines)

- **Lien par maison** (`IntendantLands.parentOf`) : une terre d'une maison bâtie hors des terres de sa maison (Sakuragami de
  Hanazakura, Dojo Yoru no Kiba de Kage : géographiquement dans le Marquisat Luo) se range sous la terre principale de sa
  maison (Clan Hanazakura, Clan Kage) dans l'arbre, la population et les Terres à acquérir. Une ville sans territoire de sa
  maison reste une ville du Marquisat.
- **Villes d'une maison sans maître** (Mont Tenryūzan, Palais Shinkōkyū, Ryūtekō…) : désormais **dans les Terres à acquérir
  d'office** (`LandOffice.houseCity`), au prix fixé ou 5 000 000 or, payé aux coffres de la maison ; un acheteur de la maison
  la garde dans la maison. Le Patriarche peut la **retirer du marché** (`ParcelData.reserved`, sauvegardé) ou la remettre en
  vente à son prix. Avant, seule une ville *sans maison* ou mise en vente à la main était proposée.
