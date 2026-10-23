package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Interaction "GilgameshBabylon" : declenche l'Gilgamesh Babylon (invocation des epees de lumiere) (animation, cout et recharge geres en JSON). */
public class GilgameshBabylonInteraction extends SimpleInteraction {

    public GilgameshBabylonInteraction() { super(); }

    public static final BuilderCodec<GilgameshBabylonInteraction> CODEC =
            BuilderCodec.builder(GilgameshBabylonInteraction.class, GilgameshBabylonInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        var caster = context.getOwningEntity();
        if (caster == null || !caster.isValid()) return;
        GilgameshBabylon.cast(caster, caster.getStore());
    }
}
