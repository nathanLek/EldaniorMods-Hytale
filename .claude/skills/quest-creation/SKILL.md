---
name: quest-creation
description: Création de quêtes du mod Eldanior (fenêtre façon Chapitres) — principale, secondaire, exclusive, de classe, journalière — avec une illustration de carte générée pour CHAQUE étape. Garantit la cohérence rang / niveau des monstres / demandes / récompenses, privilégie les butins de monstres pour la chasse, sépare Duel joueur et Duel PNJ. À utiliser dès que l'utilisateur veut créer, modifier, équilibrer ou illustrer une quête ou une étape de quête.
---

# Création de quêtes

Quêtes en cartes (fenêtre « Chapitres ») : `src/main/java/com/eldanior/system/quest/chapitres/ChapterQuests.java`
(+ `QuestChaptersPage.java`), images et `.ui` générés par `scripts/quetes/gen_chapitres_ui.py`.
Remise à zéro le 2026-11-14 : il ne reste que les quêtes listées dans `references/catalogue.md`. **Le mettre à jour à chaque quête.**

## 1. Méthode (une quête à la fois, avec l'utilisateur)

1. **Concevoir** avec l'utilisateur, sans coder : type (§ 2), donneur, rang, nombre d'étapes, et pour chaque étape :
   histoire, 1-2 notes, attendus, récompenses, et idée d'illustration. Présenter la fiche ; attendre son accord.
2. **Vérifier la cohérence** (§ 3 et § 4) : `python3 scripts/quetes/butins_quete.py <RANG|nom>` donne les butins du rang.
   N'utiliser que des objets qui existent (le script marque « ABSENT » ceux qui ne sont pas générés).
3. **Coder** la `Chain` dans `ChapterQuests` (méthode privée `xxx()` + `register(xxx())` dans le bloc `static`).
4. **Illustrer chaque étape** (§ 5) : une valeur `Art` + une scène par étape, rendu, planche montrée à l'utilisateur.
5. **Compiler** : `./gradlew compileJava -q` (jamais `jar`/`runServer` pendant une partie : voir la mémoire du projet).
6. **Tenir à jour** : `references/catalogue.md`, `docs/a-faire.md` (entrée **[test]**, « non vu en jeu »), mémoire `chapter-quests`.

## 2. Types de quête (`MissionType`)

| Type | Règle | Contour |
|---|---|---|
| PRINCIPALE | Une fois par joueur, mais **un seul Roi** au bout ; fermée pour tous dès qu'un Roi est élu (`mainClosed`) | or |
| EXCLUSIVE | **Une seule fois par joueur** (pas une fois par serveur) | violet |
| CLASSE | Quête de classe (2026-11-16) : `Chain.forClass("archer", ...)` (identifiants `ClassModel.getId`), invisible pour les autres classes ; une seule fois par joueur, comme l'exclusive | cyan |
| SECONDAIRE | Refaisable après `Chain.repeat(minutes)` (24 h/48 h en vrai ; test `REPEAT_TEST_MINUTES` = 5) | bleu |
| JOURNALIÈRE | Affiches du tableau, renouvelées à minuit. Le tirage du jour (`QuestManager.refreshDailySelection`, 5 quêtes) se fait parmi les `QuestModel` journaliers de `quest/definitions/journaliere/` | vert |

Un rang S donne aussi le contour or. Pas plus de 2 quêtes en cours + la principale (`QuestManager.MAX_ACTIVE`).

## 3. Rang ↔ niveau ↔ récompenses (cohérence obligatoire)

| Rang | Niveau des monstres visés | XP (repère) | Argent en cuivres (repère) |
|---|---|---|---|
| F | 0-60 | 300 | 100 |
| E | 60-150 | 800 | 300 |
| D | 150-300 | 1 500 | 800 |
| C | 300-450 | 3 000 | 2 000 |
| B | 450-600 | 6 000 | 5 000 |
| A | 600-800 | 12 000 | 12 000 |
| S | 800+ | 25 000 | 30 000 |

- Repères tirés des Épreuves du Dojo (F→B). Les bandes de niveau sont aussi dans `butins_quete.py` (`BANDS`) : modifier les deux ensemble.
  Bandes **validées par l'utilisateur le 2026-11-14**.
- Un attendu ne vise **jamais** un monstre d'une bande au-dessus du rang (une étape peut viser plus bas).
- **Le rang suit aussi le niveau du joueur visé** (rappel de l'utilisateur, 2026-11-14) : un joueur niveau 250 est déjà
  rang B ; une quête pour lui est C ou B, jamais D. Viser des monstres de son niveau ou un peu au-dessus.
- Une étape avec un butin de boss ou un mélange de 3 familles monte d'un rang (ex. : œuf du Dresseur = B).
- Récompense « argent » = cuivres (`Reward.money`), banque 1:1 ; objets via `Reward.item(id, qté, libellé)`, manuels via `manual()`.
- Une récompense rare (œuf, manuel, arme) **remplace** une partie de l'argent, elle ne s'y ajoute pas.

## 4. Attendus (`Kind`) : quoi demander

| Demande | Kind | Règles |
|---|---|---|
| **Chasse** | `ITEM` (butin) **par défaut** ; `KILL` parfois | La plupart des quêtes de chasse demandent **des butins de monstres** (`Butin_*`, retirés du sac au rendu). Seules quelques-unes demandent un **nombre de monstres** (`KILL`, cible = morceaux de l'identifiant de rôle, séparés par `|`, ex. `"rabbit\|antelope\|deer\|fox"`). Quantités selon la rareté : commun 5-15, rare 3-8, précieux 1-3, trophée de boss 1. |
| Récolte, artisanat | `ITEM` | Identifiant exact ou morceau (`"_Trunk"` = toutes les bûches). Les `Butin_*` sont comparés **exactement** (`QuestManager.matchesItemId`, sinon « Butin_Croc_Loup » compterait « Butin_Croc_Loup_Trork »). |
| Niveau | `LEVEL` | Seuil cohérent avec le rang (F ≤ 60, etc.). |
| **Duel PNJ** | `DUEL_RANK` / `DUEL_SENSEI` | Contre des **PNJ postés** (Disciple, Sensei) : `DUEL_RANK` (cible = `GuardType`, `rank` = palier martial 2-6), `DUEL_SENSEI` (le Sensei du joueur). Désignation sans distance ni direction (lieu-dit seulement). |
| **Duel joueur** | `DUEL_PLAYER` | **Différent du Duel PNJ, ne jamais mélanger.** Duels gagnés contre des joueurs : `duel/DuelManager` → `QuestManager.onDuelWin` → `ChapterQuests.onPlayerDuelWon` (2026-11-14). |
| Prime | `PRIME` | Réservé aux contrats créés à la volée (`Primes`). |
| **Argent** | *(QuestModel `COLLECTION`)* | Liée à la **banque** (2026-11-14) : avance avec l'argent versé au compte (dépôt de pièces chez le banquier `BankPage.deposit`, primes touchées), recule à chaque retrait, les prêts ne comptent pas (`QuestManager.onBankDeposit/onBankWithdraw`). Ramasser des pièces ne compte pas. Pas encore de `Kind` en carte : l'ajouter sur le même modèle si une carte en a besoin. |

Conditions d'accès d'une chaîne : `Chain.minLevel(n)` (niveau minimum pour la voir), `Chain.requires(idChaîne)` (visible
seulement si cette autre quête est commencée ou rendue : quêtes en relais, comme les témoins du Roi). Dans les textes,
`{lieu:<idChaîne>}` devient le lieu-dit du PNJ à qui un admin a ajouté cette chaîne (pas de distance ni de direction).

Une étape a **un seul compteur** (`KILL` / `DUEL_*`) ; les `ITEM` et `LEVEL` peuvent s'y ajouter.
Le HUD des quêtes affiche **les mêmes lignes que la fenêtre** (`QuestChaptersPage.rows`, partagé avec `QuestHud`, 4 lignes
au plus : `QuestHud.OBJECTIVES`) : un `DUEL_RANK` à 3 adversaires = 3 lignes (« Défier <nom> », puis « Vaincu : <nom> »).
Ne pas dépasser 4 lignes par étape.

## 5. Illustrations : une par étape

Recette complète, gabarit et pièges : `references/illustrations.md`. En bref :
1. `ChapterQuests.Art` : ajouter une valeur par étape (nom **sans tiret bas**, ex. `BRIGANDS` → `ChapArtBrigands`), l'affecter à l'`.act(...)`.
2. `gen_chapitres_ui.py` : écrire `scene_<nom>(u, v, rnd)`, l'ajouter à `ARTS` et `SCENES`.
3. Rendu + planche : `python3 scripts/quetes/gen_chapitres_ui.py --art=Nom1 --art=Nom2 --planche=<scratchpad>/planche.png`,
   **regarder la planche** (Read), corriger, puis la montrer à l'utilisateur.
4. L'image raconte l'étape (lieu, adversaire, objet demandé) ; jamais deux étapes avec la même image.

## 6. Histoires et indices (objets de lecture)

Pour raconter le monde ou semer un indice de quête sur la carte : `scripts/histoires/gen_histoires.py` (catégorie créative
Eldanior System > Histoires). **Histoire** = bloc posable (journal ouvert, F = page de lecture du jeu) ; **Indice** =
feuillets non posables, lus en main (clic droit / F, `histoires/LireIndiceInteraction`). Texte dans `server.lang`
(`<i>`, `<b>`, `\n`). Une étape « trouve l'indice » peut demander l'objet indice en `ITEM` (il est alors retiré au rendu).

## 7. Textes

- Histoire et notes **à la voix du PNJ**, entre « », phrases courtes, tutoiement du joueur ; pas de « [Admin] ».
- Attendus : verbe + quantité + objet (« Rapporter 10 défenses de Trork »), sans lieu ni rang.
- Pas de distance ni de direction pour trouver une cible (refusé par l'utilisateur) : lieu-dit seulement.
- La carte « + » d'un admin propose toutes les quêtes (`ChapterQuests.addable`) : une quête peut être donnée par plusieurs PNJ.

## 8. Non vérifié : ne pas s'appuyer dessus sans test en jeu

- **Parler à un PNJ pour valider une quête ou obtenir des indices** (ancien système : `NpcDialogueQuest`,
  `quest/interaction/NpcQuestDetectionSystem`, `NpcData` ; chaîne Ancien Conseiller → Tavernier « Alex Silford » →
  « La Naissance d'un Roi »). **Jamais vérifié en jeu.** Avant de créer une étape « va parler à X » ou « trouve un indice
  auprès de Y », le rappeler à l'utilisateur et proposer de le tester d'abord. Il n'existe pas encore de `Kind` « parler à
  un PNJ » dans les cartes.
