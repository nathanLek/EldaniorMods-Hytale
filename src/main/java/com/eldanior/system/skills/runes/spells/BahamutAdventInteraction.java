package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Interaction "BahamutAdvent" : declenche l'Avenement de Bahamut (dragon spectral, Megaflare) (animation, cout et recharge geres en JSON). */
public class BahamutAdventInteraction extends SimpleInteraction {

    public BahamutAdventInteraction() { super(); }

    public static final BuilderCodec<BahamutAdventInteraction> CODEC =
            BuilderCodec.builder(BahamutAdventInteraction.class, BahamutAdventInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        var caster = context.getOwningEntity();
        if (caster == null || !caster.isValid()) return;
        BahamutAdvent.cast(caster, caster.getStore());
    }
}
