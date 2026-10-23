package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Interaction JSON generique { "Type": "RuneSpell", "Spell": "<id>" } : lance le sort de rune enregistre sous cet id.
 * Le cout, la recharge et l'animation restent declares en JSON (StatsCondition / ChangeStat / TriggerCooldown).
 */
public class RuneSpellInteraction extends SimpleInteraction {

    private String spell;

    public RuneSpellInteraction() { super(); }

    public static final BuilderCodec<RuneSpellInteraction> CODEC =
            BuilderCodec.builder(RuneSpellInteraction.class, RuneSpellInteraction::new, SimpleInteraction.CODEC)
                    .append(new KeyedCodec<>("Spell", Codec.STRING), (obj, val) -> obj.spell = val, obj -> obj.spell)
                    .add()
                    .build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        var caster = context.getOwningEntity();
        if (caster == null || !caster.isValid()) return;
        RuneSpellRuntime.queue(caster, spell);
    }
}
