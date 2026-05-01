# Systeme de NPC et Dialogues

#npc #dialogue #interaction #quete

## Detection des NPC
- `NpcQuestDetectionSystem` (ECS) detecte les joueurs proches des NPC
- Quand le joueur interagit (touche F) → ouvre le dialogue

## Types de NPC
| NPC | Role | Fichier |
|-----|------|---------|
| Tavernier | Donne des quetes | TavernierQuest_Npc.json |
| Ancien Conseiller | Quete principale | AncienConseiller_Npc.json |

## Systeme de dialogue
- `DialoguePage.ui` - Layout de la fenetre de dialogue
- `NpcMessage.ui` - Style des messages NPC
- Le dialogue peut proposer des quetes a accepter

## Interaction NPC-Quete
- `QuestNpcInteraction` - Interaction enregistree
- Quand un joueur interagit avec un NPC → verifie les quetes disponibles
- `QuestManager.getFirstAvailableQuest(uuid, npcId)` → retourne la quete

## NPC Roles
Les NPC sont definis dans `Server/NPC/Roles/Intelligent/Neutral/Kweebec/`

## Fichiers cles
- `quest/interaction/NpcQuestDetectionSystem.java` - Detection ECS
- `quest/interaction/NpcQuestInteraction.java` - Interaction
- `quest/interaction/QuestNpcInteraction.java` - Lien NPC-Quete
- `quest/dialogue/` - Systeme de dialogue
- `Common/UI/Custom/Quest/DialoguePage.ui` - UI dialogue
- `Common/UI/Custom/Quest/NpcMessage.ui` - Style message

## Pages detaillees
- [[NPC et Dialogues/Systeme de Dialogue]] - Architecture dialogue, composants, flow

## Liens
- [[Systems/Quetes]] - Systeme de quetes
- [[Mobs/Configuration Mobs]] - NPC (Kweebecs invincibles)
