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

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/** Fenetre de recrutement d'un aventurier (touche d'interaction) : presentation, prix, duree, boutons Recruter / Partir. */
public class RecruitScreen extends InteractiveCustomUIPage<RecruitScreen.RecruitEventData> {

    private final String postId;
    private final int level;
    private final long price, money;
    private final String title;

    public RecruitScreen(@Nonnull PlayerRef playerRef, String postId, int level, long price, long money, String title) {
        super(playerRef, CustomPageLifetime.CanDismiss, RecruitEventData.CODEC);
        this.postId = postId;
        this.level = level;
        this.price = price;
        this.money = money;
        this.title = title;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events,
                      @Nonnull Store<EntityStore> store) {
        ui.append("Npc/Recruit.ui");
        GuardManager.Post post = GuardManager.post(postId);
        var cls = post != null ? AdventurerTier.classOf(post) : null;
        var tier = post != null ? AdventurerTier.of(post) : AdventurerTier.ORDINAIRE;
        ui.set("#RecruitName.Text", cls != null ? cls.getDisplayName() : title);      // classe (sinon « Aventurier Archer »)
        ui.set("#RecruitLevel.Text", "•  Rang " + getAdventureRank(level) + "  ·  Mission de " + GuardManager.CONTRACT_MS / 60_000 + " min");
        ui.set("#RecruitFootnote.Text", (tier == AdventurerTier.ORDINAIRE ? "" : tier.getLabel() + "  ·  ")
                + title + (cls != null ? "  ·  Classe " + cls.getRarity().getDisplayName().toLowerCase() : ""));
        ui.set("#RecruitQuote.Text", "« Ma lame est à louer, voyageur. Payez-moi et je vous suivrai jusqu'au bout de la mission. »");
        String skills = post != null ? AdventurerPassives.names(post) : "";
        String spells = post != null ? com.eldanior.system.skills.runes.spells.AdventurerMagic.spells(AdventurerType.of(post.getSkinSeed())) : "";
        ui.set("#RecruitInfo.Text", (skills.isEmpty() ? "" : "Compétences : " + skills + ".  ")
                + (spells.isEmpty() ? "" : "Sorts : " + spells + ".  ")
                + "Il vous suit et combat à vos côtés : ce que vous attaquez, ceux qui vous attaquent et les monstres "
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
