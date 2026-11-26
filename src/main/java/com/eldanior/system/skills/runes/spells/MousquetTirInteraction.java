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
 * Detente des armes a feu : { "Type": "Eldanior_MousquetTir", "Next": ..., "Failed": ... }. Le serveur decide (arme
 * chargee, munitions) : Next = le coup part (effets JSON), Failed = cliquetis a vide. Voir Mousquet.
 *  - "Concentration": true -> signature des mousquets (balle a precision et degats doubles) ;
 *  - "Colt": n -> Colts de cavalerie, n balles prises dans les chargeurs (5 + 5 Balles Divines).
 * Sans champ : selon l'arme tenue (mousquets, SARS, canon de char).
 */
public class MousquetTirInteraction extends SimpleInstantInteraction {

    private boolean concentration;
    private int colt;

    public MousquetTirInteraction() { super(); }

    public static final BuilderCodec<MousquetTirInteraction> CODEC =
            BuilderCodec.builder(MousquetTirInteraction.class, MousquetTirInteraction::new, SimpleInstantInteraction.CODEC)
                    .append(new KeyedCodec<>("Concentration", Codec.BOOLEAN), (o, v) -> o.concentration = v != null && v, o -> o.concentration)
                    .add()
                    .append(new KeyedCodec<>("Colt", Codec.INTEGER), (o, v) -> o.colt = v == null ? 0 : v, o -> o.colt)
                    .add()
                    .build();

    @Override
    public WaitForDataFrom getWaitForDataFrom() { return WaitForDataFrom.Server; }

    @Override
    protected void firstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context,
                            @NonNullDecl CooldownHandler cooldownHandler) {
        boolean fired = false;
        try {
            var shooter = context.getOwningEntity();
            if (shooter != null && shooter.isValid()) {
                fired = colt > 0 ? Mousquet.coltTrigger(shooter, context.getCommandBuffer(), colt)
                        : Mousquet.pullTrigger(shooter, context.getCommandBuffer(), concentration);
            }
        } catch (Exception e) {
            EldaniorLogger.error("MousquetTirInteraction", e);
        }
        if (!fired) context.getState().state = InteractionState.Failed;
    }
}
