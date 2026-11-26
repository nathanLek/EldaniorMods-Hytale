package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Palier d'endurance : { "Type": "Eldanior_SeuilEndurance", "Endurance": 1200, "Next": ..., "Failed": ... }. Next si
 * l'endurance du joueur (x1,5 avec Black Bess) atteint le seuil, sinon Failed. Sert a choisir le grossissement de la
 * lunette des armes a feu (scripts/artefacts/gen_mousquets.py).
 */
public class SeuilEnduranceInteraction extends SimpleInstantInteraction {

    private int threshold;

    public SeuilEnduranceInteraction() { super(); }

    public static final BuilderCodec<SeuilEnduranceInteraction> CODEC =
            BuilderCodec.builder(SeuilEnduranceInteraction.class, SeuilEnduranceInteraction::new, SimpleInstantInteraction.CODEC)
                    .append(new KeyedCodec<>("Endurance", Codec.INTEGER), (o, v) -> o.threshold = v == null ? 0 : v, o -> o.threshold)
                    .add()
                    .build();

    @Override
    public WaitForDataFrom getWaitForDataFrom() { return WaitForDataFrom.Server; }

    @Override
    protected void firstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context,
                            @NonNullDecl CooldownHandler cooldownHandler) {
        boolean ok = false;
        try {
            var ref = context.getOwningEntity();
            if (ref != null && ref.isValid()) ok = Mousquet.scopeEndurance(ref, context.getCommandBuffer()) >= threshold;
        } catch (Exception e) {
            EldaniorLogger.error("SeuilEnduranceInteraction", e);
        }
        if (!ok) context.getState().state = InteractionState.Failed;
    }
}
