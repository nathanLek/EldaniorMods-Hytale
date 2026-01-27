package com.eldanior.system.items.Interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.utils.NotificationHelper;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SkillInteractions extends SimpleInteraction {

    public static final BuilderCodec<SkillInteractions> CODEC = BuilderCodec.builder(SkillInteractions.class, SkillInteractions::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {

        if (!firstRun) return;
        // Cette méthode est appelée nativement au clic

        Ref<EntityStore> entityRef = context.getOwningEntity();
        Store<EntityStore> store = entityRef.getStore();

        // 1. Récupérer le joueur
        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        // 2. Récupérer l'item en main
        ItemStack heldItem = context.getHeldItem();
        if (heldItem == null) return;

        // 3. Lire le Skill ID
        String skillId = heldItem.getFromMetadataOrNull(EldaniorSystem.getSkillIdKey());
        if (skillId == null) return;

        // 4. Logique Eldanior
        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data != null) {
            boolean isActive = data.toggleSkill(skillId);
            store.putComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType(), data);

            // On passe simplement 'player' (l'objet Player récupéré à l'étape 1)
            if (isActive) {
                NotificationHelper.sendSuccess(player, "§aACTIVÉ : " + skillId);
            } else {
                NotificationHelper.sendWarning(player, "§cDÉSACTIVÉ : " + skillId);
            }
        }
    }
}