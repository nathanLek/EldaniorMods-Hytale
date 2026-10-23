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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/** Fenetre de recrutement d'un aventurier (touche d'interaction) : presentation, prix, duree, boutons Recruter / Partir. */
public class RecruitScreen extends InteractiveCustomUIPage<RecruitScreen.RecruitEventData> {

    private final String postId;
    private final int level;
    private final long price, money;

    public RecruitScreen(@Nonnull PlayerRef playerRef, String postId, int level, long price, long money) {
        super(playerRef, CustomPageLifetime.CanDismiss, RecruitEventData.CODEC);
        this.postId = postId;
        this.level = level;
        this.price = price;
        this.money = money;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events,
                      @Nonnull Store<EntityStore> store) {
        ui.append("Npc/Recruit.ui");
        ui.set("#RecruitName.Text", "Aventurier");
        ui.set("#RecruitLevel.Text", "•  Niveau " + level + "  ·  Mission de " + GuardManager.CONTRACT_MS / 60_000 + " min");
        ui.set("#RecruitFootnote.Text", "Deux aventuriers au plus par joueur");
        ui.set("#RecruitQuote.Text", "« Ma lame est à louer, voyageur. Payez-moi et je vous suivrai jusqu'au bout de la mission. »");
        ui.set("#RecruitInfo.Text", "Il vous suit et combat à vos côtés : ce que vous attaquez, ceux qui vous attaquent et les monstres "
                + "qui approchent. Il rejoint votre groupe. Il retourne à son poste à la fin de la mission, si vous partez ou s'il tombe.");
        ui.set("#RecruitSlotValue.Text", String.valueOf(price));
        ui.set("#RecruitCount.Text", money + " / " + price);
        ui.set("#RecruitCount.Style.TextColor", money >= price ? "#00e7b6" : "#cc3361");
        ui.set("#RecruitCost.Text", "Consomme " + price + " pièces d'or");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RecruitBtnOk", EventData.of("Action", "recruit"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull RecruitEventData data) {
        this.close();
        if ("recruit".equals(data.action)) AdventurerSystem.confirm(postId, ref, store);
    }

    public static class RecruitEventData {
        public static final BuilderCodec<RecruitEventData> CODEC = BuilderCodec.builder(RecruitEventData.class, RecruitEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
