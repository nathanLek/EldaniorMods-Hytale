package com.eldanior.system.classes.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenClassSelectionInteraction extends SimpleInteraction {

    private static final int REQUIRED_LEVEL = 20;

    public OpenClassSelectionInteraction() { super(); }

    public static final BuilderCodec<OpenClassSelectionInteraction> CODEC =
            BuilderCodec.builder(OpenClassSelectionInteraction.class, OpenClassSelectionInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {

        if (!firstRun || type != InteractionType.Use) return;

        var entityRef = context.getOwningEntity();
        var store = entityRef.getStore();

        Player player = store.getComponent(entityRef, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        ComponentType<EntityStore, PlayerLevelData> dataType = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(entityRef, dataType);
        if (playerData == null) return;

        int level = playerData.getLevel();
        String currentClassId = playerData.getPlayerClassId();
        boolean isNovice = currentClassId == null || currentClassId.equalsIgnoreCase("novice");

        if (level < REQUIRED_LEVEL) {
            NotificationHelper.sendNotification(playerRef,
                    "<color:red>Niveau insuffisant !</color> <color:gray>Niveau " + REQUIRED_LEVEL + " requis (vous êtes niveau " + level + ").</color>",
                    NotificationStyle.Warning);
            return;
        }

        if (isNovice) {
            List<ClassModel> baseClasses = new ArrayList<>();
            for (ClassModel m : ClassManager.getAll()) {
                if (!m.isAdminAccess()
                        && !m.getId().equalsIgnoreCase("novice")
                        && m.getRarity() == Rarity.COMMON
                        && m.getType() != ClassType.NOVICE) {
                    baseClasses.add(m);
                }
            }
            ClassIntroScreen.setPending(baseClasses, false);
            player.getPageManager().openCustomPage(entityRef, store, new ClassIntroScreen(playerRef));
            return;
        }

        ClassModel currentClass = ClassManager.get(currentClassId);
        if (currentClass == null) return;

        if (level < currentClass.getPromotionLevel()) {
            int remaining = currentClass.getPromotionLevel() - level;
            NotificationHelper.sendNotification(playerRef,
                    "<color:gold>Prochaine évolution au niveau " + currentClass.getPromotionLevel() + "</color> <color:gray>(" + remaining + " niveaux restants)</color>",
                    NotificationStyle.Warning);
            return;
        }

        List<ClassModel> evolutions = new ArrayList<>();
        for (ClassModel m : ClassManager.getAll()) {
            if (!m.isAdminAccess()
                    && m.getType() == currentClass.getType()
                    && m.getPromotionLevel() > currentClass.getPromotionLevel()) {
                evolutions.add(m);
            }
        }

        if (evolutions.isEmpty()) {
            NotificationHelper.sendNotification(playerRef,
                    "<color:gray>Aucune évolution disponible pour votre classe.</color>",
                    NotificationStyle.Warning);
            return;
        }

        Collections.shuffle(evolutions);
        List<ClassModel> proposed = evolutions.subList(0, Math.min(3, evolutions.size()));

        ClassIntroScreen.setPending(proposed, true);
        player.getPageManager().openCustomPage(entityRef, store, new ClassEvolutionIntroScreen(playerRef));
    }
}