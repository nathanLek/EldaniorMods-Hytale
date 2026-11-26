package com.eldanior.system.poche;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Clic (gauche, droit ou F) avec la Poche dimensionnelle en main : l'equipe dans sa case du menu Systeme si le joueur n'en a
 * pas (l'objet est consomme), puis ouvre le menu sur la poche (PocheDimensionnelle.useItem).
 * Interaction Eldanior_OuvrirPoche (Server/Item/Interactions, Root_Eldanior_OuvrirPoche ; scripts/poche/gen_poche.py).
 */
public class OuvrirPocheInteraction extends SimpleInteraction {

    public OuvrirPocheInteraction() { super(); }

    public static final BuilderCodec<OuvrirPocheInteraction> CODEC =
            BuilderCodec.builder(OuvrirPocheInteraction.class, OuvrirPocheInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        try {
            var ref = context.getOwningEntity();
            if (ref == null || !ref.isValid()) return;
            PocheDimensionnelle.useItem(ref, ref.getStore(), context.getHeldItemSlot());   // equipe (1re fois) puis ouvre le menu
        } catch (Exception e) {
            EldaniorLogger.error("OuvrirPocheInteraction", e);
        }
    }
}
