package com.eldanior.system.failles;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

/**
 * Un joueur marche dans une faille (CollisionEnter des blocs Eldanior_Faille_<Rang>, scripts/failles/gen_failles.py) :
 * FailleManager decide s'il entre (une fois par joueur, place libre, groupe du chef) et cree le donjon au premier.
 */
public class EntrerFailleInteraction extends SimpleInteraction {

    public EntrerFailleInteraction() { super(); }

    public static final BuilderCodec<EntrerFailleInteraction> CODEC =
            BuilderCodec.builder(EntrerFailleInteraction.class, EntrerFailleInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        try {
            var ref = context.getOwningEntity();
            if (ref == null || !ref.isValid()) return;
            BlockPosition b = context.getTargetBlock();
            Vector3i at = b != null ? new Vector3i(b.x, b.y, b.z) : null;
            ref.getStore().getExternalData().getWorld().execute(() -> { if (ref.isValid()) FailleManager.touch(ref, at); });
        } catch (Exception e) {
            EldaniorLogger.error("EntrerFailleInteraction", e);
        }
    }
}
