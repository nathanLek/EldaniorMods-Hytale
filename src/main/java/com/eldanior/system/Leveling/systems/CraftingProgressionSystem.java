package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.CraftTier;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.CraftingConfig;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Progression des compétences de craft + double craft par palier.
 * Ecoute CraftRecipeEvent.Post via le système ECS.
 */
public class CraftingProgressionSystem extends EntityEventSystem<EntityStore, CraftRecipeEvent.Post> {

    public CraftingProgressionSystem() {
        super(CraftRecipeEvent.Post.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl CraftRecipeEvent.Post event) {
        EldaniorLogger.info("[CraftProg] EVENT POST RECU !");
        try {
            var recipe = event.getCraftedRecipe();
            if (recipe == null) return;

            EldaniorLogger.info("[CraftProg] EVENT RECU ! Recipe: " + recipe.getId());

            var benchReqs = recipe.getBenchRequirement();
            if (benchReqs == null || benchReqs.length == 0) {
                EldaniorLogger.info("[CraftProg] Pas de bench requirement");
                return;
            }

            String benchId = benchReqs[0].id != null ? benchReqs[0].id.toLowerCase() : "";
            EldaniorLogger.info("[CraftProg] Bench ID: '" + benchId + "'");

            PassiveSkill matchedSkill = null;
            for (Map.Entry<String, PassiveSkill> entry : CraftingConfig.BENCH_SKILLS.entrySet()) {
                if (benchId.contains(entry.getKey())) {
                    matchedSkill = entry.getValue();
                    break;
                }
            }

            if (matchedSkill == null) {
                EldaniorLogger.info("[CraftProg] Aucun skill pour bench: '" + benchId + "'");
                return;
            }

            Ref<EntityStore> playerEntityRef = archetypeChunk.getReferenceTo(index);
            Player player = store.getComponent(playerEntityRef, Player.getComponentType());
            PlayerRef netRef = store.getComponent(playerEntityRef, PlayerRef.getComponentType());

            if (player == null) return;

            PlayerLevelData data = store.getComponent(playerEntityRef, EldaniorSystem.get().getPlayerLevelDataType());
            if (data == null) return;

            // Vérifier que le joueur a ce skill actif
            boolean hasSkill = false;
            if (data.getActivePassives() != null) {
                for (PassiveSkill skill : data.getActivePassives()) {
                    if (skill == matchedSkill) {
                        hasSkill = true;
                        break;
                    }
                }
            }
            if (!hasSkill) {
                hasSkill = data.isSkillEnabled(matchedSkill.name());
            }
            if (!hasSkill) return;

            // Ajouter les procs
            int quantity = Math.max(1, event.getQuantity());
            for (int i = 0; i < quantity; i++) {
                data.addSkillProc(matchedSkill.name());
            }

            // Notification progression
            if (netRef != null) {
                float progress = (data.getSkillProcs(matchedSkill.name()) / 10000f) * 100f;
                String progressStr = String.format("%.2f", Math.min(100f, progress));
                NotificationHelper.sendNotification(netRef,
                        "<color:gold>" + matchedSkill.getDisplayName() + " : " + progressStr + "%</color>",
                        NotificationStyle.Success);
            }

            // --- DOUBLE CRAFT (Compagnon+) ---
            CraftTier tier = data.getCraftTier(matchedSkill.name());
            double chance = tier.getDoubleCraftChance();
            if (chance > 0.0 && ThreadLocalRandom.current().nextDouble() < chance) {
                List<ItemStack> outputs = CraftingManager.getOutputItemStacks(recipe);
                for (ItemStack output : outputs) {
                    if (!ItemStack.isEmpty(output)) {
                        player.getInventory().getHotbar().addItemStack(output);
                    }
                }
                if (netRef != null) {
                    NotificationHelper.sendNotification(netRef,
                            "<color:gold>Double craft !</color> <color:white>(" + matchedSkill.getDisplayName() + ")</color>",
                            NotificationStyle.Success);
                }
                EldaniorLogger.info("[CraftProg] DOUBLE CRAFT proc pour " + matchedSkill.name());
            }

            EldaniorLogger.info("[CraftProg] +1 proc " + matchedSkill.name() + " pour joueur");
        } catch (Exception e) {
            EldaniorLogger.error("CraftingProgressionSystem", e);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
