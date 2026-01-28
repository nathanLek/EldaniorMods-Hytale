package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.awt.Color;

public class ConsumableItemSkillInteraction extends SimpleInteraction {

    public static final BuilderCodec<ConsumableItemSkillInteraction> CODEC =
            BuilderCodec.builder(ConsumableItemSkillInteraction.class, ConsumableItemSkillInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context,
                         @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;

        Ref<EntityStore> playerRef = context.getOwningEntity();
        Store<EntityStore> store = playerRef.getStore();
        Player player = store.getComponent(playerRef, Player.getComponentType());

        // --- CORRECTION ICI ---
        // On récupère l'instance du type via ta classe principale
        PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        // ----------------------

        if (data == null) return;

        String skillToLearn = "fireball";

        if (!data.getUnlockedSkills().contains(skillToLearn)) {
            data.learnSkill(skillToLearn);

            assert player != null;
            player.getInventory().getHotbar().removeItemStackFromSlot(context.getHeldItemSlot(), 1, true, false);

            player.sendMessage(Message.raw("Nouveau Skill débloqué : " + skillToLearn)
                    .color(Color.CYAN).bold(true));
        } else {
            assert player != null;
            player.sendMessage(Message.raw("Vous connaissez déjà cette compétence !")
                    .color(Color.RED));
        }
    }
}