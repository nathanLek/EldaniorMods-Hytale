package com.eldanior.system.classes.gui;

import com.eldanior.system.classes.models.ClassModel;
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
import java.util.List;

public class ClassIntroScreen extends InteractiveCustomUIPage<ClassIntroScreen.IntroEventData> {

    public static List<ClassModel> pendingClasses;
    public static boolean pendingIsEvolution;

    public static void setPending(List<ClassModel> classes, boolean isEvolution) {
        pendingClasses = classes;
        pendingIsEvolution = isEvolution;
    }

    public ClassIntroScreen(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, IntroEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Classes/ClassIntro.ui");
        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#DiscoverClassesButton",
                EventData.of("Action", "openSelection")
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull IntroEventData data) {
        if (!"openSelection".equals(data.action)) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        player.getPageManager().openCustomPage(ref, store,
                new ClassSelectionScreen(playerRef, pendingClasses, pendingIsEvolution));
    }

    public static class IntroEventData {
        public String action;

        public static final BuilderCodec<IntroEventData> CODEC =
                BuilderCodec.builder(IntroEventData.class, IntroEventData::new)
                        .addField(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                        .build();
    }
}