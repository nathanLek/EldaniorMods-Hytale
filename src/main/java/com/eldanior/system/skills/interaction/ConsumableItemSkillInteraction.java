package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.eldanior.system.skills.SkillManager;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.awt.Color;

public class ConsumableItemSkillInteraction extends SimpleInteraction {

    public ConsumableItemSkillInteraction() { super(); }

    public static final BuilderCodec<ConsumableItemSkillInteraction> CODEC =
            BuilderCodec.builder(ConsumableItemSkillInteraction.class, ConsumableItemSkillInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun || type != InteractionType.Use) return;

        var playerRef = context.getOwningEntity();
        Player player = playerRef.getStore().getComponent(playerRef, Player.getComponentType());
        PlayerLevelData data = playerRef.getStore().getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (player == null || data == null) return;

        ItemStack heldItem = player.getInventory().getHotbar().getItemStack(context.getHeldItemSlot());
        if (heldItem == null) return;

        SkillManager.getSkillFromItem(heldItem.getItemId()).ifPresent(skill -> {

            // --- NOUVELLE VÉRIFICATION DE CLASSE ---
            // On suppose que data.getPlayerClass() renvoie "mage", "warrior", etc.
            String playerClass = data.getPlayerClass().toLowerCase();

            if (!playerClass.equals(skill.requiredClass().toLowerCase())) {
                player.sendMessage(Message.raw("Votre classe (" + playerClass + ") n'est pas apte à déchiffrer ce savoir !")
                        .color(Color.ORANGE));
                return; // On arrête tout ici, l'item n'est pas consommé
            }
            // ----------------------------------------

            if (!data.getUnlockedSkills().contains(skill.skillId())) {
                data.learnSkill(skill.skillId());

                player.getInventory().getHotbar().removeItemStackFromSlot(context.getHeldItemSlot(), 1, true, false);
                player.getInventory().getHotbar().addItemStack(new ItemStack(skill.catalystId(), 1));

                player.sendMessage(Message.raw("✨ Savoir acquis : " + skill.displayName())
                        .color(Color.CYAN).bold(true));
            } else {
                player.sendMessage(Message.raw("Vous maîtrisez déjà ce sort !").color(Color.RED));
            }
        });
    }
}