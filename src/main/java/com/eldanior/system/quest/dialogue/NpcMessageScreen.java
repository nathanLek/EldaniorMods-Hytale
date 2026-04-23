package com.eldanior.system.quest.dialogue;

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

import javax.annotation.Nonnull;

/**
 * Page simple pour afficher un message de PNJ (quand il n'a rien de nouveau a dire).
 */
public class NpcMessageScreen extends InteractiveCustomUIPage<NpcMessageScreen.MsgEventData> {

    private final String speakerName;
    private final String message;

    public NpcMessageScreen(@Nonnull PlayerRef playerRef, String speakerName, String message) {
        super(playerRef, CustomPageLifetime.CanDismiss, MsgEventData.CODEC);
        this.speakerName = speakerName;
        this.message = message;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Quest/NpcMessage.ui");

        ui.set("#NpcMsgSpeaker.Text", speakerName);
        ui.set("#NpcMsgText.Text", message);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#NpcMsgBtnOk", EventData.of("Action", "ok"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull MsgEventData data) {
        this.close();
    }

    public static class MsgEventData {
        public static final BuilderCodec<MsgEventData> CODEC = BuilderCodec.builder(MsgEventData.class, MsgEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
