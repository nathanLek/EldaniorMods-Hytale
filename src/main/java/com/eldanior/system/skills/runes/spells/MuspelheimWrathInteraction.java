package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Interaction "MuspelheimWrath" : declenche la pluie de meteorites du Courroux de Muspelheim.
 * Appelee par Ability_CourrouxMuspelheim_Cast apres l'incantation (animation, cout et recharge geres en JSON).
 */
public class MuspelheimWrathInteraction extends SimpleInteraction {

    public MuspelheimWrathInteraction() { super(); }

    public static final BuilderCodec<MuspelheimWrathInteraction> CODEC =
            BuilderCodec.builder(MuspelheimWrathInteraction.class, MuspelheimWrathInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        var caster = context.getOwningEntity();
        if (caster == null || !caster.isValid()) return;
        MuspelheimWrath.cast(caster, caster.getStore());
    }
}
