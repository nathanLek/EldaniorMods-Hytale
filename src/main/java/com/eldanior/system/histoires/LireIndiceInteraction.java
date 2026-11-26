package com.eldanior.system.histoires;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BlockLorePage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Lire un indice tenu en main (2026-11-14, demande de l'utilisateur) : meme fenetre que les notes du jeu
 * (Lore_Note_Enkindled_Journal_Vermin, page BlockLore), mais l'indice ne se pose pas. La page du jeu lit le bloc vise ; ici on
 * lit l'objet tenu : titre = son nom, texte = sa description (cles du server.lang ecrites par scripts/histoires/gen_histoires.py).
 */
public class LireIndiceInteraction extends SimpleInteraction {

    public LireIndiceInteraction() { super(); }

    public static final BuilderCodec<LireIndiceInteraction> CODEC =
            BuilderCodec.builder(LireIndiceInteraction.class, LireIndiceInteraction::new, SimpleInteraction.CODEC).build();

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
            if (held == null || held.isEmpty()) return;
            Item item = Item.getAssetMap().getAsset(held.getItemId());
            if (item == null || item.getTranslationProperties() == null) return;
            var tp = item.getTranslationProperties();
            Message title = Message.translation(tp.getName());
            Message text = Message.translation(tp.getDescription() != null ? tp.getDescription() : tp.getName());
            player.getPageManager().openCustomPage(ref, store, new BlockLorePage(pr, title, text, BlockLorePage.TextAlignment.Left));
        } catch (Exception e) {
            EldaniorLogger.error("LireIndiceInteraction", e);
        }
    }
}
