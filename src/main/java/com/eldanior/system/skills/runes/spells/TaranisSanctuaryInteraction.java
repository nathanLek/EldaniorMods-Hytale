package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Interaction "TaranisSanctuary" : declenche le Sanctuaire de Taranis (tempete de foudre) (animation, cout et recharge geres en JSON). */
public class TaranisSanctuaryInteraction extends SimpleInteraction {

    public TaranisSanctuaryInteraction() { super(); }

    public static final BuilderCodec<TaranisSanctuaryInteraction> CODEC =
            BuilderCodec.builder(TaranisSanctuaryInteraction.class, TaranisSanctuaryInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        var caster = context.getOwningEntity();
        if (caster == null || !caster.isValid()) return;
        TaranisSanctuary.cast(caster, caster.getStore());
    }
}
