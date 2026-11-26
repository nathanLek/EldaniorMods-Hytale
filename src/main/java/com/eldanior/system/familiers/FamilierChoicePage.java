package com.eldanior.system.familiers;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.familiers.Espece.Age;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Touche d'interaction sur son familier (2026-11-13, demande de l'utilisateur) : choisir entre le chevaucher (s'il se monte a
 * son age) et la fenetre de gestion (FamilierPage). Fenetre de discussion des PNJ (Npc/Talk.ui) : options cliquables, Au revoir.
 */
public class FamilierChoicePage extends InteractiveCustomUIPage<FamilierChoicePage.Data> {

    private static final int OPTIONS = 5;                                   // = Npc/Talk.ui

    private final UUID owner;
    private final Ref<EntityStore> pet;
    private final boolean admin;
    private final List<String> options = new ArrayList<>();

    public FamilierChoicePage(@Nonnull PlayerRef playerRef, UUID owner, Ref<EntityStore> pet, boolean admin) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.owner = owner;
        this.pet = pet;
        this.admin = admin;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Npc/Talk.ui");
        for (int i = 0; i < OPTIONS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#TalkOpt" + i, EventData.of("Action", "opt").append("Index", String.valueOf(i)), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TalkBye", EventData.of("Action", "bye"), false);
        Familier f = FamilierManager.of(owner);
        long now = System.currentTimeMillis();
        options.clear();
        String[][] rows = new String[OPTIONS][];
        int n = 0;
        if (f != null && f.espece.mountable(f.age(now)) && owner.equals(playerRef.getUuid())) {
            options.add("ride");
            rows[n++] = new String[]{"Chevaucher", "Monter sur son dos (s'accroupir pour descendre)"};
        }
        options.add("manage");
        rows[n++] = new String[]{"Gestion", "Fiche, évolution, nourriture, suivre ou rester"};
        ui.set("#TalkName.Text", f != null ? f.name + " · " + f.espece.label : "Familier");
        ui.set("#TalkText.Text", f == null ? "" : f.espece.mountable(f.age(now)) ? "Il vous regarde, prêt à partir."
                : f.espece.mountFrom != null ? "Encore trop jeune pour vous porter (âge " + f.espece.mountFrom.label.toLowerCase() + ")."
                : "Il remue la queue en vous voyant.");
        for (int i = 0; i < OPTIONS; i++) {
            boolean shown = i < n;
            ui.set("#TalkOpt" + i + ".Visible", shown);
            if (!shown) continue;
            ui.set("#TalkOptTitle" + i + ".Text", rows[i][0]);
            ui.set("#TalkOptDesc" + i + ".Text", rows[i][1]);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            if (!"opt".equals(data.action)) { close(); return; }
            int i;
            try { i = Integer.parseInt(data.index); } catch (Exception e) { return; }
            if (i < 0 || i >= options.size()) return;
            close();
            Player player = store.getComponent(ref, Player.getComponentType());
            switch (options.get(i)) {
                case "ride" -> {
                    String no = FamilierManager.mount(owner, pet, ref, store);
                    if (no != null) FamilierManager.say(playerRef, no, "#e8c060");
                }
                case "manage" -> {
                    if (player != null) player.getPageManager().openCustomPage(ref, store, new FamilierPage(playerRef, owner, admin));
                }
                default -> { }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] choix", e);
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .build();
        public String action, index;
    }
}
