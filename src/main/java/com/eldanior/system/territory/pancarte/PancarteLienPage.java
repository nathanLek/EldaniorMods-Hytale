package com.eldanior.system.territory.pancarte;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/** Choix du bien presente par une pancarte (Territoire/PancarteLien.ui, scripts/territoire/gen_pancarte.py). */
public class PancarteLienPage extends InteractiveCustomUIPage<PancarteLienPage.Data> {

    private final String key;

    public PancarteLienPage(@Nonnull PlayerRef playerRef, String key) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.key = key;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Territoire/PancarteLien.ui");
        int[] c = Pancartes.coords(key);
        List<ParcelData> list = Pancartes.candidates(key, playerRef);
        List<DropdownEntryInfo> entries = new ArrayList<>();
        for (ParcelData p : list) {
            int d = (int) Math.round(Pancartes.distance(p, c[0], c[2]));
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(p.getName() + " — " + p.getType().getLabel()
                    + (d == 0 ? " (ici)" : " (" + d + " blocs)")), p.getId()));
        }
        ParcelData current = Pancartes.parcelOf(key);
        ui.set("#PlTitle.Text", "Pancarte de propriété");
        ui.set("#PlSubtitle.Text", "Lier à un bien");
        ui.set("#PlFoot.Text", list.size() + (list.size() > 1 ? " biens proches" : " bien proche"));
        ui.set("#PlState.Text", current != null ? "Elle présente aujourd'hui « " + current.getName() + " »." :
                "Choisissez le bien que cette pancarte présentera aux passants.");
        ui.set("#PlZone.Entries", entries);
        if (!list.isEmpty()) ui.set("#PlZone.Value", current != null && list.contains(current) ? current.getId() : list.get(0).getId());
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PlBtnLink",
                EventData.of("Action", "link").append("@Zone", "#PlZone.Value"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            if (!"link".equals(data.action)) return;
            ParcelData p = data.zone != null ? ParcelManager.get(data.zone) : null;
            if (p == null || !Pancartes.candidates(key, playerRef).contains(p)) {
                playerRef.sendMessage(Message.raw("Ce bien ne peut pas être lié à cette pancarte."));
                return;
            }
            Pancartes.link(key, p);
            playerRef.sendMessage(Message.raw("La pancarte présente maintenant « " + p.getName() + " »."));
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPageManager().openCustomPage(ref, store, new PancartePage(playerRef, key));   // remplace cette page
        } catch (Exception e) {
            EldaniorLogger.error("[Pancarte] lien", e);
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("@Zone", Codec.STRING), (d, v) -> d.zone = v, d -> d.zone).add()
                .build();
        public String action;
        public String zone;
    }
}
