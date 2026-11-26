package com.eldanior.system.anchors;

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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Porte des Mondes : choix de l'ancre de destination (Ancre/AncreChoix.ui). Le choix part dans AnchorManager, que la porte en
 * attente lit a son tick. Fermer la fenetre sans choisir = choix vide (la porte prend la derniere destination).
 */
public class AnchorChoicePage extends InteractiveCustomUIPage<AnchorChoicePage.Data> {

    public static final int ROWS = 8;

    private final List<AnchorManager.Anchor> anchors;
    private final Vector3d from;
    private boolean answered;

    public AnchorChoicePage(@Nonnull PlayerRef playerRef, List<AnchorManager.Anchor> anchors, Vector3d from) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.anchors = List.copyOf(anchors.subList(0, Math.min(ROWS, anchors.size())));
        this.from = from;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Ancre/AncreChoix.ui");
        for (int i = 0; i < ROWS; i++) {
            boolean shown = i < anchors.size();
            ui.set("#AncreRow" + i + ".Visible", shown);
            if (!shown) continue;
            AnchorManager.Anchor a = anchors.get(i);
            long dist = from != null ? Math.round(from.distance(a.arrival())) : -1;
            ui.set("#AncrePick" + i + ".Text", a.name + (dist >= 0 ? "   •   " + dist + " blocs" : ""));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AncrePick" + i, EventData.of("Pick", String.valueOf(i)));
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        int i;
        try { i = Integer.parseInt(data.pick); } catch (Exception e) { return; }
        if (i < 0 || i >= anchors.size()) return;
        answered = true;
        AnchorManager.choose(playerRef.getUuid(), anchors.get(i).key());
        close();
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (!answered) AnchorManager.choose(playerRef.getUuid(), "");
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Pick", Codec.STRING), (d, v) -> d.pick = v, d -> d.pick).add()
                .build();
        public String pick;
    }
}
