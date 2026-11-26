package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Garde de la Rune de Transformation d'Arme : { "Type": "Eldanior_ArmeLegendaireTenue", "Next": ..., "Failed": ... }.
 * Next seulement si le joueur tient un mousquet legendaire (Black Bess, Green Colt, Blue Tiger) ; sinon message et Failed,
 * AVANT le cout en mana et la recharge (rune_tool.py, champ "gate" de la fiche).
 */
public class ArmeLegendaireTenueInteraction extends SimpleInstantInteraction {

    public ArmeLegendaireTenueInteraction() { super(); }

    public static final BuilderCodec<ArmeLegendaireTenueInteraction> CODEC =
            BuilderCodec.builder(ArmeLegendaireTenueInteraction.class, ArmeLegendaireTenueInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    public WaitForDataFrom getWaitForDataFrom() { return WaitForDataFrom.Server; }

    @Override
    protected void firstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context,
                            @NonNullDecl CooldownHandler cooldownHandler) {
        boolean ok = false;
        try {
            var ref = context.getOwningEntity();
            if (ref != null && ref.isValid()) {
                var cb = context.getCommandBuffer();
                ok = ArmeTransfo.canTransform(ref, cb);
                if (!ok) Mousquet.tell(cb.getComponent(ref, PlayerRef.getComponentType()),
                        "Tenez Black Bess, Green Colt ou Blue Tiger pour la transformer.", false);
            }
        } catch (Exception e) {
            EldaniorLogger.error("ArmeLegendaireTenueInteraction", e);
        }
        if (!ok) context.getState().state = InteractionState.Failed;
    }
}
