package com.eldanior.system.guards;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.runes.spells.AdventurerMagic;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
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
import java.util.List;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Fenetre de recrutement d'un aventurier (touche d'interaction) : presentation (classe a la couleur de sa rarete), puissance
 * et robustesse, competences passives et runes de sa classe (AdventurerMagic.runes), prix, duree, boutons Recruter / Partir.
 */
public class RecruitScreen extends InteractiveCustomUIPage<RecruitScreen.RecruitEventData> {

    /** Lignes prevues dans Npc/Recruit.ui. */
    private static final int PASSIVE_ROWS = 3, RUNE_ROWS = 4;

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
        if (cls != null) ui.set("#RecruitName.Style.TextColor", rarityColor(cls.getRarity()));
        ui.set("#RecruitLevel.Text", "•  Rang " + getAdventureRank(level) + "  ·  Mission de " + GuardManager.CONTRACT_MS / 60_000 + " min");
        ui.set("#RecruitFootnote.Text", (tier == AdventurerTier.ORDINAIRE ? "" : tier.getLabel() + "  ·  ")
                + title + (cls != null ? "  ·  Classe " + cls.getRarity().getDisplayName().toLowerCase() : ""));
        if (tier != AdventurerTier.ORDINAIRE) ui.set("#RecruitFootnote.Style.TextColor", tier == AdventurerTier.HEROS ? "#F1C40F" : "#b58cff");
        ui.set("#RecruitQuote.Text", "« " + quote(post) + " »");
        List<AdventurerMagic.Rune> runes = post != null ? AdventurerMagic.runes(post) : List.of();
        ui.set("#RecruitPower.Text", post != null ? multiplier(AdventurerTier.power(post)) : "×1");
        ui.set("#RecruitTough.Text", post != null ? multiplier(AdventurerTier.toughness(post)) : "×1");
        ui.set("#RecruitRuneCount.Text", String.valueOf(runes.size()));
        passives(ui, post != null ? AdventurerPassives.of(post) : List.of());
        runes(ui, runes);
        ui.set("#RecruitSlotValue.Text", String.valueOf(price));
        ui.set("#RecruitCount.Text", money + " / " + price);
        ui.set("#RecruitCount.Style.TextColor", money >= price ? "#00e7b6" : "#cc3361");
        ui.set("#RecruitCost.Text", "Il vous suit, combat à vos côtés et rejoint votre groupe");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RecruitBtnOk", EventData.of("Action", "recruit"));
    }

    /** Sa replique, selon son type. */
    private static String quote(GuardManager.Post post) {
        if (post == null) return "Ma lame est à louer, voyageur.";
        return switch (AdventurerType.of(post.getSkinSeed())) {
            case GUERRIER -> "Mon épée et mon bouclier sont à louer. Je tiendrai la première ligne.";
            case MAGE -> "Mes runes sont prêtes. Payez-moi, et vos ennemis connaîtront ma magie.";
            case ARCHER -> "Je couvrirai vos arrières, à distance. Aucune cible ne m'échappe.";
            case ASSASSIN -> "Je frappe depuis l'ombre. Payez, et vos ennemis ne me verront pas venir.";
        };
    }

    /** Competences passives de sa classe : nom et effet (un ordinaire n'en a pas). */
    private static void passives(UICommandBuilder ui, List<PassiveSkill> list) {
        ui.set("#RecruitNoPassive.Visible", list.isEmpty());
        if (list.isEmpty()) ui.set("#RecruitNoPassive.Text", "Aucune : seuls les exceptionnels et les héros ont une classe.");
        for (int i = 0; i < PASSIVE_ROWS; i++) {
            boolean shown = i < list.size();
            ui.set("#RecruitPassive" + i + ".Visible", shown);
            if (!shown) continue;
            ui.set("#RecruitPassiveName" + i + ".Text", list.get(i).getDisplayName());
            ui.set("#RecruitPassiveDesc" + i + ".Text", list.get(i).getDescription());
        }
    }

    /** Sorts de rune : icone de la rune, nom, rarete et recharge, effet. */
    private static void runes(UICommandBuilder ui, List<AdventurerMagic.Rune> list) {
        ui.set("#RecruitNoRune.Visible", list.isEmpty());
        if (list.isEmpty()) ui.set("#RecruitNoRune.Text", "Aucune rune.");
        for (int i = 0; i < RUNE_ROWS; i++) {
            boolean shown = i < list.size();
            ui.set("#RecruitRune" + i + ".Visible", shown);
            if (!shown) continue;
            AdventurerMagic.Rune r = list.get(i);
            ui.set("#RecruitRuneIcon" + i + ".ItemId", r.itemId());
            ui.set("#RecruitRuneName" + i + ".Text", r.name());
            ui.set("#RecruitRuneMeta" + i + ".Text", r.rarity().getDisplayName() + "  ·  " + r.cooldownMs() / 1000 + " s");
            ui.set("#RecruitRuneMeta" + i + ".Style.TextColor", rarityColor(r.rarity()));
            ui.set("#RecruitRuneFrame" + i + ".Background", rarityColor(r.rarity()));
            ui.set("#RecruitRuneDesc" + i + ".Text", r.description());
        }
    }

    /** Multiplicateur lisible : ×1,7. */
    private static String multiplier(float value) {
        return "×" + String.format(java.util.Locale.FRANCE, "%.1f", value).replace(",0", "");
    }

    /** Couleurs de rarete de l'ecran des classes (ClassSelectionScreen). */
    private static String rarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "#E0E0E0";
            case RARE -> "#3498DB";
            case EPIC -> "#9B59B6";
            case UNIQUE -> "#E74C3C";
            case LEGENDARY -> "#F1C40F";
            case DIVINE -> "#00FFFF";
        };
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
