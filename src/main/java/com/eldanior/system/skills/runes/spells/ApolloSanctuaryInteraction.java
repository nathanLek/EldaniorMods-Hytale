package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Interaction "ApolloSanctuary" : declenche le Sanctuaire d'Apollon (animation, cout et recharge geres en JSON). */
public class ApolloSanctuaryInteraction extends SimpleInteraction {

    public ApolloSanctuaryInteraction() { super(); }

    public static final BuilderCodec<ApolloSanctuaryInteraction> CODEC =
            BuilderCodec.builder(ApolloSanctuaryInteraction.class, ApolloSanctuaryInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        var caster = context.getOwningEntity();
        if (caster == null || !caster.isValid()) return;
        ApolloSanctuary.cast(caster, caster.getStore());
    }
}
