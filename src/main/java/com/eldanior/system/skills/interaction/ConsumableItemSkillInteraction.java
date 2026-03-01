package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.SkillManager;
// 🌟 NOUVEAUX IMPORTS POUR LIRE LA CLASSE PARENTE
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;

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
import java.util.Timer;
import java.util.TimerTask;

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

            // 1. On récupère la classe actuelle du joueur
            String playerClass = data.getPlayerClassId().toLowerCase();

            // 2. 🌟 On récupère la classe parente via le ClassModel
            ClassModel classModel = ClassManager.get(data.getPlayerClassId());
            // Remplace "getParentId()" par la méthode exacte de ton ClassModel (ex: getParentClass(), getParent(), etc.)
            String parentClass = (classModel != null && classModel.getType().name().toLowerCase() != null)
                    ? classModel.getType().name().toLowerCase()
                    : "";

            String requiredClass = skill.requiredClass();

            // 3. On vérifie les permissions (Parent OU Classe Actuelle)
            if (requiredClass != null && !requiredClass.equalsIgnoreCase("all")) {
                String reqLower = requiredClass.toLowerCase();

                // Si la classe requise ne contient NI la classe actuelle, NI la classe parente
                if (!reqLower.contains(playerClass) && !reqLower.contains(parentClass)) {
                    player.sendMessage(Message.raw("Votre classe n'est pas apte à déchiffrer ce savoir !")
                            .color(Color.ORANGE));
                    return;
                }
            }

            if (!data.getUnlockedSkills().contains(skill.skillId())) {
                data.learnSkill(skill.skillId());

                player.sendMessage(Message.raw("Savoir acquis : " + skill.displayName())
                        .color(Color.CYAN).bold(true));

                int slot = context.getHeldItemSlot();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            player.getInventory().getHotbar().removeItemStackFromSlot((short) slot, 1, true, false);
                        } catch (Exception ignored) {}
                    }
                }, 50);

            } else {
                player.sendMessage(Message.raw("Vous maîtrisez déjà ce savoir !").color(Color.RED));
            }
        });
    }
}