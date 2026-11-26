package com.eldanior.system.histoires;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Fenetre de lecture d'un livre (Histoires/Livre.ui) : titre, surtitre, une page de texte qui defile, page precedente / suivante.
 * Le texte passe en Message.translation dans #LivreTexte.TextSpans, comme la page BlockLore du jeu (<i>, <b> interpretes).
 */
public class LivrePage extends InteractiveCustomUIPage<LivrePage.Data> {

    private final String itemId;
    private final int pages;
    private int page = 1;

    public LivrePage(@Nonnull PlayerRef playerRef, String itemId) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.itemId = itemId;
        this.pages = Livres.pageCount(itemId);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Histoires/Livre.ui");
        ui.set("#LivreTitre.Text", Message.translation(Livres.key(itemId, "name")));
        boolean sur = Livres.exists(Livres.key(itemId, "surtitre"));
        ui.set("#LivreSurtitre.Visible", sur);
        if (sur) ui.set("#LivreSurtitre.Text", Message.translation(Livres.key(itemId, "surtitre")));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#LivrePrec", EventData.of("Action", "Prec"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#LivreSuiv", EventData.of("Action", "Suiv"));
        fill(ui);
    }

    private void fill(UICommandBuilder ui) {
        String text = pages > 0 ? Livres.key(itemId, "page" + page) : Livres.key(itemId, "description");
        ui.set("#LivreTexte.TextSpans", Message.translation(text));
        ui.set("#LivreNumero.Text", pages > 1 ? "Page " + page + " / " + pages : "");
        ui.set("#LivrePrec.Visible", page > 1);
        ui.set("#LivreSuiv.Visible", page < pages);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            int next = page;
            if ("Prec".equals(data.action)) next = Math.max(1, page - 1);
            else if ("Suiv".equals(data.action)) next = Math.min(Math.max(1, pages), page + 1);
            if (next == page) return;
            page = next;
            UICommandBuilder ui = new UICommandBuilder();
            fill(ui);
            sendUpdate(ui);
        } catch (Exception e) {
            EldaniorLogger.error("LivrePage " + itemId, e);
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
