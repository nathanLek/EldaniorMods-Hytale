package com.eldanior.system.items.Interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.skills.Skills.InstantSkill;
import com.eldanior.system.rpg.classes.skills.Skills.SkillManager;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.eldanior.system.rpg.classes.skills.Skills.ToggledSkill;
import com.eldanior.system.utils.NotificationHelper;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction; // IMPORTANT
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger; // Pour les logs

import javax.annotation.Nonnull;

public class SkillInteractions extends SimpleInstantInteraction { // On hérite de SimpleInstantInteraction

    // CODEC adapté pour SimpleInstantInteraction
    public static final BuilderCodec<SkillInteractions> CODEC = BuilderCodec.builder(
            SkillInteractions.class, SkillInteractions::new, SimpleInstantInteraction.CODEC
    ).build();

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {

        LOGGER.atInfo().log("[DEBUG] SkillInteractions.firstRun() DÉCLENCHÉ !"); // Log visible dans la console

        Ref<EntityStore> entityRef = context.getEntity(); // Ou getOwningEntity()
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

        // Sécurité CommandBuffer (Comme dans la doc)
        if (commandBuffer == null) {
            LOGGER.atInfo().log("[DEBUG] CommandBuffer est null !");
            return;
        }

        // On récupère le Store via le buffer
        Store<EntityStore> store = commandBuffer.getExternalData().getStore();

        // 1. Récupération du joueur
        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        // 2. Récupération de l'item
        ItemStack heldItem = context.getHeldItem();
        if (heldItem == null) return;

        // 3. ID du Skill
        String skillId = heldItem.getFromMetadataOrNull(EldaniorSystem.getSkillIdKey());
        if (skillId == null) {
            LOGGER.atInfo().log("[DEBUG] Pas de Skill ID sur cet item.");
            return;
        }

        SkillModel model = SkillManager.get(skillId);
        if (model == null) {
            NotificationHelper.sendWarning(player, "§cSkill inconnu : " + skillId);
            return;
        }

        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        // --- LOGIQUE METIER ---

        if (model instanceof InstantSkill instantSkill) {
            if (!data.canCast(instantSkill.getId())) {
                long remaining = data.getRemainingCooldown(instantSkill.getId()) / 1000;
                NotificationHelper.sendWarning(player, "§cCooldown : " + remaining + "s");
                return;
            }

            // On passe le CommandBuffer valide !
            boolean success = instantSkill.cast(entityRef, store, commandBuffer);

            if (success) {
                data.applyCooldown(instantSkill.getId(), instantSkill.getCooldown());
                store.putComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType(), data);
                NotificationHelper.sendSuccess(player, "§bSort lancé : " + model.getName());
            }
        }
        else if (model instanceof ToggledSkill) {
            boolean isActive = data.toggleSkill(skillId);
            store.putComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType(), data);

            if (isActive) NotificationHelper.sendSuccess(player, "§aAURA ACTIVÉE");
            else NotificationHelper.sendWarning(player, "§cAURA DÉSACTIVÉE");
        }
    }
}