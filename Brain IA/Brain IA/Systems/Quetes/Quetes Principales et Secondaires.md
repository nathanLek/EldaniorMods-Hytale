# Quetes Principales et Secondaires

#quetes #principal #secondaire #histoire

## Quetes Principales
Quetes d'histoire du monde, chainees les unes aux autres.

### Naissance du Roi
- **Fichier** : `quest/definitions/main/MainQuest_NaissanceRoi.java`
- Premiere quete principale du jeu
- Liee a l'histoire du Royaume d'Eldanior
- Declenche par un PNJ specifique via dialogue

### Caracteristiques
- Non repetables (une seule fois par joueur)
- Chainees via `nextQuestId` → la completion deverrouille la suivante
- Le joueur ne peut pas sauter de chapitre
- Recompenses elevees (XP + Or + Titre)

## Quetes Secondaires
Quetes independantes donnees par des PNJ.

### Exemples definis
| Quete | Fichier | Type |
|-------|---------|------|
| Duelliste | `SideQuest_Duelliste.java` | Combat/Duel |
| Explorateur | `SideQuest_Explorateur.java` | Exploration |
| AncienConseiller | `SecQuest_AncienConseiller.java` | Dialogue/Lore |

### Caracteristiques
- Independantes (pas de chaine)
- Non repetables
- Donnees par des PNJ via le systeme de dialogue
- Recompenses variables selon difficulte

## Quetes d'Information
| Quete | Fichier | Role |
|-------|---------|------|
| Tavernier | `InfoQuest_Tavernier.java` | Tutorial/lore |

- Quetes qui donnent des informations au joueur
- Pas d'objectif de combat, seulement du dialogue
- Servent de tutoriel ou d'enrichissement du lore

## Structure d'une quete (QuestModel)
```java
id, name, description, type, category, difficulty
targetId, targetAmount          // Objectif
rewardXP, rewardGold, rewardTitleId  // Recompenses
nextQuestId                     // Chainage
npcGiverId                      // PNJ qui donne la quete
cooldownMinutes                 // 0 pour non-repetable
```

## Fichiers cles
- `quest/definitions/principal/` — quetes principales
- `quest/definitions/secondaire/` — quetes secondaires + info
- `quest/QuestModel.java` — modele de quete

## Liens
- [[Types de Quetes]] - Categories et types
- [[Progression et Recompenses]] - Flow et rewards
- [[../NPC et Dialogues/Systeme de Dialogue]] - Dialogues PNJ
- [[../Quetes]] - Vue d'ensemble