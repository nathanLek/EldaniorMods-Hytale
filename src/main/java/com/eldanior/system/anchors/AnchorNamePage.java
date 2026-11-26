package com.eldanior.system.anchors;

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

/** Fenetre du nom d'une ancre (Ancre/AncreNom.ui) : a la pose, puis touche d'interaction sur son cercle. Annuler garde le nom actuel. */
public class AnchorNamePage extends InteractiveCustomUIPage<AnchorNamePage.Data> {

    private final AnchorManager.Anchor anchor;
    private final boolean fresh;

    public AnchorNamePage(@Nonnull PlayerRef playerRef, AnchorManager.Anchor anchor, boolean fresh) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.anchor = anchor;
        this.fresh = fresh;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Ancre/AncreNom.ui");
        int count = AnchorManager.countOf(anchor.owner);
        int max = AnchorManager.maxAnchors(ref, store);
        ui.set("#AncreInfo.Text", (fresh ? "Votre cercle est tracé. Donnez un nom à cette ancre : la Porte des Mondes vous y mènera."
                : "Renommer cette ancre.") + "\nPosition : " + anchor.coords() + "   •   Ancres : " + count + " / " + max);
        ui.set("#AncreName.Value", anchor.name);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AncreOk",
                EventData.of("Action", "ok").append("@Name", "#AncreName.Value"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AncreCancel", EventData.of("Action", "cancel"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            if ("cancel".equals(data.action)) {
                close();
                if (fresh) playerRef.sendMessage(Message.raw("Ancre « " + anchor.name + " » posée.").color("#c8a8ff"));
                return;
            }
            if (!"ok".equals(data.action)) return;
            if (AnchorManager.find(anchor.key()) != anchor) { close(); return; }               // effacee entre-temps
            String name = AnchorManager.cleanName(data.name);
            String error = name.equals(anchor.name) ? null : AnchorManager.checkName(anchor.owner, name, anchor);
            if (error != null) {
                UICommandBuilder ui = new UICommandBuilder();
                ui.set("#AncreError.Visible", true);
                ui.set("#AncreError.Text", error);
                sendUpdate(ui);
                return;
            }
            AnchorManager.rename(anchor, name);
            close();
            playerRef.sendMessage(Message.raw("Ancre « " + name + " » " + (fresh ? "posée." : "renommée.")).color("#c8a8ff"));
        } catch (Exception e) {
            EldaniorLogger.error("[Ancres] nom", e);
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (d, v) -> d.name = v, d -> d.name).add()
                .build();
        public String action, name;
    }
}
