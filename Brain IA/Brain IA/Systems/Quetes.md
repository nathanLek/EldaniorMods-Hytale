# Systeme de Quetes

#quetes #npc #dialogue #progression

## Types de quetes
- **Principales** : histoire du monde (ex: Naissance du Roi)
- **Secondaires** : quetes de PNJ
- **Journalieres** : repetables avec cooldown
  - Chasse (tuer des mobs)
  - Collection (collecter de l'or)
  - Exploration (decouvrir des coffres)
  - Duel (gagner des duels)
  - Massacre (tuer en masse)
  - PK (kills PvP)

## Flow
1. Le joueur s'approche d'un PNJ (NpcQuestDetectionSystem)
2. Le dialogue s'ouvre (DialoguePage.ui)
3. La quete apparait dans l'onglet Quetes du SystemScreen
4. Le joueur peut **accepter**, **activer** (met dans le HUD), **abandonner**
5. Quand les objectifs sont remplis → bouton **RECLAMER**
6. Grand titre "QUETE TERMINEE" + recompenses

## Recompenses
- XP
- Or
- Titre (deblocable)

## HUD
- CombinedHud affiche la quete active avec les objectifs en boxes style objectifs
- Se met a jour en temps reel

## Onglet Quetes (SystemScreen)
- Liste des quetes disponibles/en cours/completees
- Boutons : Accepter, Activer, Reclamer, Abandonner
- Affichage des objectifs et recompenses

## Fichiers cles
- `quest/QuestManager.java` - Registre et gestion
- `quest/definitions/` - Definitions des quetes
- `quest/interaction/NpcQuestDetectionSystem.java` - Detection NPC
- `quest/dialogue/` - Systeme de dialogue
- `gui/tabs/QuestTab.java` - Onglet GUI
- `hud/CombinedHud.java` - HUD quete active

## Pages detaillees
- [[Quetes/Types de Quetes]] - 3 categories, 6 types, difficultes
- [[Quetes/Progression et Recompenses]] - Flow, rewards, HUD
- [[Quetes/Quetes Journalieres]] - 102+ quetes daily, selection, cooldowns
- [[Quetes/Quetes Principales et Secondaires]] - Naissance du Roi, side quests, info quests

## Liens
- [[Systems/Consommables]] - Recompenses (XP, Or)
- [[Systems/Noblesse]] - Titres deblocables
