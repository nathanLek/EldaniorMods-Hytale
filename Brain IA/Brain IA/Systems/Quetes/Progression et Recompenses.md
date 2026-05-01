# Progression et Recompenses des Quetes

#quetes #progression #recompenses #flow

## Flow de progression
1. Le joueur s'approche d'un PNJ → `NpcQuestDetectionSystem` detecte
2. Le dialogue s'ouvre → `DialoguePage.ui`
3. La quete apparait dans l'onglet **Quetes** du SystemScreen
4. Le joueur peut :
   - **Accepter** → la quete passe en cours
   - **Activer** → met la quete dans le HUD (tracking visible)
   - **Abandonner** → supprime la progression

5. Quand les objectifs sont remplis → bouton **RECLAMER**
6. Grand titre "QUETE TERMINEE" + recompenses distribuees

## Recompenses
| Type | Description |
|------|------------|
| XP | Points d'experience (×multiplicateur difficulte) |
| Or | Pieces de monnaie (×multiplicateur difficulte) |
| Titre | Titre deblocable (optionnel, `rewardTitleId`) |

### Formule de recompense
```
XP final = rewardXP × difficultyMultiplier
Or final = rewardGold × difficultyMultiplier
```

## Quetes chainees
- `nextQuestId` → deverrouille la quete suivante apres completion
- Permet des histoires en plusieurs chapitres
- Le joueur ne peut pas sauter de chapitre

## HUD de quete active
- `CombinedHud` affiche la quete active avec les objectifs
- Format "boxes style" avec progression (X/Y)
- Se met a jour en temps reel
- 1 seule quete active a la fois dans le HUD

## Onglet Quetes (SystemScreen)
- **En cours** : quetes acceptees avec progression
- **Disponibles** : quetes decouvertes non acceptees
- **Completees** : historique des quetes terminees
- Boutons : Accepter, Activer, Reclamer, Abandonner

## Fichiers cles
- `quest/QuestManager.java` — gestion des recompenses
- `quest/PlayerQuest.java` — etat par joueur
- `hud/CombinedHud.java` — HUD quete active
- `gui/tabs/QuestTab.java` — onglet GUI

## Liens
- [[Types de Quetes]] - Les differents types
- [[Quetes Journalieres]] - Systeme daily
- [[../Quetes]] - Vue d'ensemble
