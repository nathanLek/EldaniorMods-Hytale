# Quêtes existantes

Remise à zéro le 2026-11-14 (demande de l'utilisateur) : tout le reste a été supprimé (100 secondaires de PNJ et leurs
100 rôles `Quest_Npc_Secondaire_*`, 111 journalières, Explorateur, Duelliste, et les quêtes de test Loups / Bois / Gobelins).
Les nouvelles quêtes se créent avec l'utilisateur. **Mettre ce fichier à jour à chaque quête ajoutée, modifiée ou retirée.**

## Cartes (ChapterQuests)

| Quête | Type | Donneur | Étapes (rang, illustration) |
|---|---|---|---|
| L'Épopée d'un Roi | Principale (niveau 250) | un civil, ajouté par un admin (carte « + ») | 1. Les Rumeurs du Roi Fou (B, RUMEURS) : rapporter les 3 indices des témoins. Étapes suivantes à concevoir |
| Le Vieux Garde | Exclusive, si la principale est commencée | civil posé à > 5 000 blocs (carte « + ») | C, HAMEAU : 25 zombies + 3 cerveaux flétris → indice « Journal d'un garde du palais » |
| La Servante de la Reine | Exclusive, si le Vieux Garde est commencé | civil (carte « + ») | C, AZUR : 12 fruits d'azur, 8 sèves, 20 bûches d'azur → indice « Lettre de la reine » |
| L'Ermite des Livres | Exclusive, si la Servante est commencée | civil (carte « + ») | B, MINE : 15 minerais de cobalt + 10 de thorium → indice « Page arrachée du grimoire » |
| Les Épreuves du Dojo | Exclusive | Sensei | 1. Les Disciples du Dojo (F, DOJO) · 2. La Chasse du Vent (E, VENT) · 3. La Bande des Collines (D, BRIGANDS) · 4. Le Grand Maître et le Trork (C, TRORK) · 5. L'Épreuve du Maître (B, SENSEI) |
| Un Œuf venu d'ailleurs | Secondaire (3 mois, 5 min en test) | Dresseur de bêtes | 1 étape (B, AUBE : illustration propre à faire) |
| Les Crocs de la Lisière | Secondaire (24 h, 5 min en test) | Éleveur | F, LISIERE : 8 peaux + 4 crocs de loup |
| Remèdes du Marais | Secondaire | Alchimiste | E, MARAIS : 5 glandes de crapaud + 3 graines maudites |
| Braises pour la Forge | Secondaire | Forgeron | D, FORGE : 10 os calcinés + 3 cœurs de braise |
| Les Morts du Désert | Secondaire | Marchand Luo | C, DUNES : abattre 15 zombies des sables |
| Trophées pour un Collectionneur | Secondaire | Marchand ambulant | B, OUTLANDERS : 5 trophées de raid + 1 talisman d'os |
| Le Droit d'Entrée | Exclusive | Maître de guilde | D, ARENE : gagner 3 duels **contre des joueurs** → Épée du Gardien (rare) |
| L'Œil du Vide | Exclusive | Alchimiste | B, VIDE : 5 pupilles du Vide + 2 prismes instables → Parchemin de Rune : Présage (épique) |
| L'Acier de l'Ombre | Exclusive | Forgeron légendaire | A, OMBRE : 3 noyaux de firesteel + heaume de l'Ombre → Croc du Néant (unique) |

Code de la quête du Roi : `quest/chapitres/QueteRoi.java` (niveau 250, `Chain.minLevel`, `Chain.requires`). Les textes
disent près de quel lieu vit le témoin suivant (`{lieu:<chaîne>}` = lieu-dit du PNJ à qui la chaîne est ajoutée).

Code des quêtes de métier : `quest/chapitres/QuetesMetiers.java` (donneur = `NpcServices.chapterGiver` : métier du commerçant
en minuscules, `marchand_luo`, `ambulant`, `maitre_guilde`, `forgeron_legendaire`). La garde de ville ne parle pas : elle ne
peut pas donner de quête.

## Ancien système (dialogues de PNJ, jamais vérifié en jeu)

- « La Naissance d'un Roi » (principale, S) ← « L'Épopée d'un Roi » de l'Ancien Conseiller (`Quest_Npc_Secondaire_Multiple_N1`)
  ← indices du Tavernier Alex Silford (`Quest_Npc_Secondaire_Indice_N1`, `InfoQuest_Tavernier`). Gardée pour tester le
  système « parler à un PNJ / indices ». Doublon de nom avec la principale en cartes : à décider avec l'utilisateur.

## Journalières (tirage du jour : les 5 sont affichées chaque jour)

Les 5 affiches du tableau le 2026-11-13 : Guerrier Trork (B, 6 guerriers trork) · Cendres et Os (C, 10 squelettes
brûlés) · Investisseur Avisé (C, gagner 7 000 or) · Cartographe (B, découvrir 18 coffres) · Légende de Guerre (A,
25 monstres). Plus aucune journalière PK : un PK n'a pas d'affiche. Illustration commune AUBE (à remplacer).
