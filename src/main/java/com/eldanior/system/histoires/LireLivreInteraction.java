package com.eldanior.system.histoires;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Lire le livre tenu en main (Eldanior_LireLivre, 2026-11-14) : fin de la chaine Eldanior_Livre_Secondaire, quand le joueur
 * n'est pas accroupi (sinon il pose le livre) et que le bloc vise n'a pas d'usage (ou qu'il vise le vide).
 */
public class LireLivreInteraction extends SimpleInteraction {

    public LireLivreInteraction() { super(); }

    public static final BuilderCodec<LireLivreInteraction> CODEC =
            BuilderCodec.builder(LireLivreInteraction.class, LireLivreInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;
        try {
            var ref = context.getOwningEntity();
            if (ref == null || !ref.isValid()) return;
            var store = ref.getStore();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            if (player == null || pr == null) return;
            ItemStack held = player.getInventory().getHotbar().getItemStack(context.getHeldItemSlot());
            if (held == null || held.isEmpty() || !Livres.isLivre(held.getItemId())) return;
            Livres.open(ref, store, player, pr, held.getItemId());
        } catch (Exception e) {
            EldaniorLogger.error("LireLivreInteraction", e);
        }
    }
}
