package com.eldanior.system.classes.gui;

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

    // --- CORRECTION : Variables d'instance (personnelles au joueur) au lieu de statiques ---
    private final List<String> availableClasses;
    private final boolean isEvolution;

    // Le constructeur demande maintenant directement la liste lors de la création de l'écran
    public ClassIntroScreen(@Nonnull PlayerRef playerRef, List<String> availableClasses, boolean isEvolution) {
        super(playerRef, CustomPageLifetime.CanDismiss, IntroEventData.CODEC);
        this.availableClasses = availableClasses;
        this.isEvolution = isEvolution;
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

        // On transmet la liste personnelle du joueur à l'écran de sélection !
        player.getPageManager().openCustomPage(ref, store,
                new ClassSelectionScreen(playerRef, this.availableClasses, this.isEvolution));
    }

    public static class IntroEventData {
        public String action;

        public static final BuilderCodec<IntroEventData> CODEC =
                BuilderCodec.builder(IntroEventData.class, IntroEventData::new)
                        .addField(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                        .build();
    }
}