# Systeme de Dialogue

#npc #dialogue #quetes #interaction

## Architecture
Le systeme de dialogue permet aux PNJ de donner des quetes via des conversations interactives.

## Composants
| Composant | Fichier | Role |
|-----------|---------|------|
| DialoguePage | `quest/dialogue/DialoguePage.java` | Page de dialogue avec texte + choix |
| DialogueScreen | `quest/dialogue/DialogueScreen.java` | Ecran complet de dialogue |
| NpcMessageScreen | `quest/dialogue/NpcMessageScreen.java` | Message simple du PNJ |
| DialogueSupplier | `quest/dialogue/DialogueSupplier.java` | Interface pour fournir des dialogues |
| NpcDialogueQuest | `quest/dialogue/NpcDialogueQuest.java` | Quete liee a un dialogue |

## Flow de dialogue
1. Le joueur s'approche d'un PNJ → `NpcQuestDetectionSystem` detecte la proximite
2. Le systeme appelle `getNextDialogueForNpc(playerRef, npcId)`
3. Si une quete est disponible → ouvre `DialogueScreen`
4. Le joueur lit le texte et clique sur les choix
5. A la fin du dialogue → la quete est ajoutee au journal

## UI
- `DialoguePage.ui` — Layout du dialogue (texte + boutons choix)
- `NpcMessage.ui` — Message simple du PNJ

## Quetes via dialogue
Les `NpcDialogueQuest` associent un PNJ a une quete :
- `npcGiverId` — ID du PNJ qui donne la quete
- Le dialogue se declenche uniquement si le joueur n'a pas deja la quete
- Les conditions de prerequis sont verifiees avant d'afficher le dialogue

## Fichiers cles
- `quest/dialogue/` — tout le systeme de dialogue (8+ fichiers)
- `quest/interaction/NpcQuestDetectionSystem.java` — detection PNJ
- `Common/UI/Custom/Quest/DialoguePage.ui` — layout
- `Common/UI/Custom/Quest/NpcMessage.ui` — message PNJ

## Liens
- [[../NPC et Dialogues]] - Vue d'ensemble
- [[../Quetes/Types de Quetes]] - Types de quetes
- [[../Quetes/Progression et Recompenses]] - Recompenses