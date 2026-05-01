package com.eldanior.system.quest.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.dialogue.DialogueScreen;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.eldanior.system.quest.dialogue.NpcMessageScreen;
import com.eldanior.system.quest.dialogue.QuestCompletionScreen;
import com.eldanior.system.quest.dialogue.QuestCondition;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Systeme ECS qui detecte les interactions joueur-NPC de quete.
 */
public class NpcQuestDetectionSystem extends EntityTickingSystem<EntityStore> {

    private static final Set<String> QUEST_NPC_IDS = Set.of(
            "AncienConseiller_Npc",
            "TavernierQuest_Npc"
    );

    // Cooldown par joueur+NPC (nettoyé périodiquement)
    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 1500;

    // Track les NPC en $Interaction déjà traités
    private static final Map<Integer, Long> activeInteractions = new ConcurrentHashMap<>();

    // Reflection cache
    private float cleanupTimer = 0;

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(NPCEntity.getComponentType());
    }

    @Override
    public void tick(float dt, int index,
                     @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // Nettoyage mémoire toutes les 60 secondes
        cleanupTimer += dt;
        if (cleanupTimer > 60f && index == 0) {
            cleanupTimer = 0;
            long now = System.currentTimeMillis();
            cooldowns.entrySet().removeIf(e -> (now - e.getValue()) > 30000);
            activeInteractions.entrySet().removeIf(e -> (now - e.getValue()) > 30000);
        }

        Ref<EntityStore> npcRef = chunk.getReferenceTo(index);
        if (!npcRef.isValid()) return;

        NPCEntity npc = store.getComponent(npcRef, NPCEntity.getComponentType());
        if (npc == null) return;

        String typeId = npc.getNPCTypeId();
        if (typeId == null || !QUEST_NPC_IDS.contains(typeId)) return;

        Role role = npc.getRole();
        if (role == null) return;

        StateSupport stateSupport = role.getStateSupport();
        if (stateSupport == null) return;

        String stateName = stateSupport.getStateName();
        int npcHash = System.identityHashCode(npc);

        boolean inInteraction = stateName != null && stateName.contains("$Interaction");

        if (inInteraction) {
            // Déjà traité pour cette session → skip
            if (activeInteractions.containsKey(npcHash)) return;
            activeInteractions.put(npcHash, System.currentTimeMillis());

            MarkedEntitySupport markedSupport = role.getMarkedEntitySupport();
            Ref<EntityStore>[] targets = markedSupport.getEntityTargets();

            if (targets != null) {
                for (Ref<EntityStore> targetRef : targets) {
                    if (targetRef == null || !targetRef.isValid()) continue;

                    Player player = store.getComponent(targetRef, Player.getComponentType());
                    if (player == null) continue;

                    PlayerRef playerRef = store.getComponent(targetRef, PlayerRef.getComponentType());
                    if (playerRef == null) continue;

                    UUID playerUUID = extractUUID(playerRef);
                    String cooldownKey = npcHash + ":" + playerUUID;
                    long now = System.currentTimeMillis();
                    Long last = cooldowns.get(cooldownKey);
                    if (last != null && (now - last) < COOLDOWN_MS) continue;
                    cooldowns.put(cooldownKey, now);

                    QuestModel quest = QuestManager.getNextDialogueForNpc(playerRef, typeId);

                    if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
                        // Info quest déjà complétée ?
                        if (dialogueQuest.isInfoOnly() && playerUUID != null) {
                            boolean alreadyDone = false;
                            for (PlayerQuest pq : QuestManager.getPlayerQuests(playerUUID)) {
                                if (pq.getQuestId().equals(quest.getId()) && pq.isCompleted()) {
                                    alreadyDone = true;
                                    break;
                                }
                            }
                            if (alreadyDone) {
                                String npcName = dialogueQuest.getPages().get(0).getSpeakerName();
                                player.getPageManager().openCustomPage(targetRef, store,
                                        new NpcMessageScreen(playerRef, npcName,
                                                "Je vous ai deja dit tout ce que je savais.\n\nRetournez voir l'Ancien Conseiller si vous avez rempli vos objectifs."));
                                break;
                            }
                        }

                        // Quête déjà en cours ?
                        if (!dialogueQuest.isInfoOnly() && playerUUID != null) {
                            boolean alreadyAccepted = false;
                            for (PlayerQuest pq : QuestManager.getPlayerQuests(playerUUID)) {
                                if (pq.getQuestId().equals(quest.getId()) && !pq.isCompleted()) {
                                    alreadyAccepted = true;
                                    break;
                                }
                            }
                            if (alreadyAccepted) {
                                String npcName = dialogueQuest.getPages().get(0).getSpeakerName();
                                QuestCondition cond = dialogueQuest.getCompletionCondition();

                                // Conditions remplies ? → Écran de complétion
                                if (cond != null) {
                                    var pData = store.getComponent(targetRef, EldaniorSystem.get().getPlayerLevelDataType());
                                    if (pData != null && cond.check(pData, player).isEmpty()) {
                                        player.getPageManager().openCustomPage(targetRef, store,
                                                new QuestCompletionScreen(playerRef, dialogueQuest));
                                        break;
                                    }
                                }

                                // Pas encore rempli
                                String msg = "Vous n'avez pas encore rempli tous les objectifs.\n\nRevenez me voir quand ce sera fait.";
                                if (cond != null) {
                                    msg += "\n\nObjectifs :\n" + String.join("\n", cond.getRequirementText().split("  \\|  "));
                                }
                                player.getPageManager().openCustomPage(targetRef, store,
                                        new NpcMessageScreen(playerRef, npcName, msg));
                                break;
                            }
                        }

                        // Nouvelle quête → Dialogue
                        player.getPageManager().openCustomPage(targetRef, store,
                                new DialogueScreen(playerRef, dialogueQuest));
                    } else {
                        player.getPageManager().openCustomPage(targetRef, store,
                                new NpcMessageScreen(playerRef, typeId,
                                        "Je n'ai rien pour vous pour le moment.\n\nRevenez plus tard."));
                    }
                    break;
                }
            }
        } else {
            activeInteractions.remove(npcHash);
        }
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        try { return UUIDExtractor.getUUID(playerRef); }
        catch (Exception e) { return null; }
    }
}
