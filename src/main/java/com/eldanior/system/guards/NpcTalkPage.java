package com.eldanior.system.guards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fenetre de discussion d'un PNJ (Npc/Talk.ui) : son salut, ses services (NpcServices), « Discuter » pour la replique
 * suivante, « Au revoir ».
 */
public class NpcTalkPage extends InteractiveCustomUIPage<NpcTalkPage.TalkEventData> {

    private static final int OPTIONS = 5;
    private final String postId;
    private String text;
    private int line = -1;

    public NpcTalkPage(@Nonnull PlayerRef playerRef, String postId, String greeting) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalkEventData.CODEC);
        this.postId = postId;
        this.text = greeting;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Npc/Talk.ui");
        for (int i = 0; i < OPTIONS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#TalkOpt" + i, EventData.of("Action", "opt").append("Index", String.valueOf(i)), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TalkBye", EventData.of("Action", "bye"), false);
        fill(ui, ref, store);
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        GuardManager.Post p = GuardManager.post(postId);
        ui.set("#TalkName.Text", p != null ? GuardManager.nameplate(p) : "");
        ui.set("#TalkText.Text", "« " + text + " »");
        List<NpcServices.Service> list = p != null ? NpcServices.services(p, ref, store) : List.of();
        for (int i = 0; i < OPTIONS; i++) {
            boolean shown = i < list.size();
            ui.set("#TalkOpt" + i + ".Visible", shown);
            if (!shown) continue;
            ui.set("#TalkOptTitle" + i + ".Text", list.get(i).label);
            ui.set("#TalkOptDesc" + i + ".Text", list.get(i).description);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull TalkEventData data) {
        try {
            handle(ref, store, data);
        } catch (Throwable e) {
            com.eldanior.system.config.EldaniorLogger.error("[PNJ] discussion " + postId + " action " + data.action, e);
            close();
        }
    }

    private void handle(Ref<EntityStore> ref, Store<EntityStore> store, TalkEventData data) {
        GuardManager.Post p = GuardManager.post(postId);
        if (p == null || "bye".equals(data.action)) { close(); return; }
        List<NpcServices.Service> list = NpcServices.services(p, ref, store);
        int i;
        try { i = Integer.parseInt(data.index); } catch (Exception e) { return; }
        if (i < 0 || i >= list.size()) return;
        NpcServices.Service s = list.get(i);
        if (s == NpcServices.Service.DISCUTER) {                                       // replique suivante
            List<String> lines = NpcServices.lines(p);
            if (lines.isEmpty()) return;
            line = (line + 1) % lines.size();
            text = lines.get(line);
            UICommandBuilder ui = new UICommandBuilder();
            fill(ui, ref, store);
            sendUpdate(ui);
            return;
        }
        // Ne pas fermer AVANT d'ouvrir la page suivante : close() attend un accuse du client que celui-ci
        // n'envoie pas si une page le remplace aussitot ; le PageManager ignore alors tous les clics de la
        // nouvelle page (boutique figee). openCustomPage remplace deja la page : on ne ferme qu'apres, si rien ne l'a remplacee.
        World world = Universe.get().getWorld(p.getWorld());
        Ref<EntityStore> npc = world != null && p.guard != null ? world.getEntityStore().getRefFromUUID(p.guard) : null;
        NpcServices.run(s, p, npc, ref, store);
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null || player.getPageManager().getCustomPage() == this) close();
    }

    public static class TalkEventData {
        public static final BuilderCodec<TalkEventData> CODEC = BuilderCodec.builder(TalkEventData.class, TalkEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .build();
        public String action, index;
    }
}
