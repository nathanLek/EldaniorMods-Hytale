package com.eldanior.system.territory.teleport;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.chapitres.ChapterQuests;
import com.eldanior.system.shop.CoinWallet;
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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fenetre du Teleporteur Continental, refaite le 2026-11-24 a la demande de l'utilisateur : « la meme vue que pour les quetes,
 * avec une carte par teleporteur ». Elle reprend la fenetre Chapitres des quetes (Quest/Chapitres.ui) : a droite le carrousel,
 * une carte illustree par Marquisat relie (contour cyan, distance) ; a gauche la fiche de la destination choisie (distance,
 * prix, bourse du joueur) et le bouton Voyager. Le prix est paye au depart (pieces puis compte) et verse au tresor du Marquisat
 * de depart ; le Gardien ouvre alors la porte (ContinentalGates).
 */
public class ContinentalPage extends InteractiveCustomUIPage<ContinentalPage.Data> {

    /** Doit correspondre a scripts/quetes/gen_chapitres_ui.py (CARDS, NOTES, OBJECTIVES, TINTS). */
    private static final int CARDS = 3, NOTES = 3, OBJECTIVES = 4;
    private static final String[] TINTS = {"Or", "Bleu", "Vert", "Violet", "Rouge", "Orange", "Cyan", "Rose"};
    private static final String TINT = "Cyan", CYAN = "#7fd4ff", GOLD = "#f2c45e", RED = "#e0574a";
    /** Illustrations de paysage des cartes, tirees du Marquisat (toujours la meme pour une destination). */
    private static final ChapterQuests.Art[] ARTS = {ChapterQuests.Art.HAMEAU, ChapterQuests.Art.AZUR, ChapterQuests.Art.DUNES,
            ChapterQuests.Art.LISIERE, ChapterQuests.Art.MARAIS, ChapterQuests.Art.MINE, ChapterQuests.Art.AUBE,
            ChapterQuests.Art.VENT, ChapterQuests.Art.NUIT, ChapterQuests.Art.TRONE, ChapterQuests.Art.RUMEURS};

    private final String fromId;
    private List<ParcelData> dest = List.of();
    private int selected, offset;

    public ContinentalPage(@Nonnull PlayerRef playerRef, String fromId) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.fromId = fromId;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Quest/Chapitres.ui");
        for (int i = 0; i < CARDS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapCard" + i, EventData.of("Action", "card").append("Index", String.valueOf(i)), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapPrev", EventData.of("Action", "prev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapNext", EventData.of("Action", "next"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapAction", EventData.of("Action", "go"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapClose", EventData.of("Action", "close"), false);
        fill(ui, ref, store, null);
    }

    private static String artId(ChapterQuests.Art art) {
        String n = art.name().toLowerCase();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    private static ChapterQuests.Art artOf(ParcelData m) {
        return ARTS[Math.floorMod(m.getId().hashCode(), ARTS.length)];
    }

    private static long distance(ContinentalTeleporters.Spot a, ContinentalTeleporters.Spot b) {
        return a != null && b != null ? Math.round(Math.hypot(b.x() - a.x(), b.z() - a.z())) : -1;
    }

    private static String blocs(long n) {
        return n < 0 ? "?" : String.format("%,d", n).replace(',', ' ') + " blocs";
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, String hint) {
        ParcelData from = ParcelManager.get(fromId);
        dest = from != null ? ContinentalTeleporters.destinations(from) : List.of();
        ContinentalTeleporters.Spot here = from != null ? ContinentalTeleporters.spot(from) : null;
        selected = Math.clamp(selected, 0, Math.max(0, dest.size() - 1));
        offset = Math.clamp(offset, 0, Math.max(0, dest.size() - CARDS));
        if (selected < offset) offset = selected;
        if (selected >= offset + CARDS) offset = selected - CARDS + 1;

        // ---- carrousel : une carte par teleporteur ----
        for (int i = 0; i < CARDS; i++) {
            int n = offset + i;
            boolean shown = n < dest.size();
            ui.set("#ChapCard" + i + ".Visible", shown);
            if (!shown) continue;
            ParcelData m = dest.get(n);
            boolean sel = n == selected;
            ChapterQuests.Art art = artOf(m);
            for (ChapterQuests.Art a : ChapterQuests.Art.values()) {
                ui.set("#ChapArt" + artId(a) + "C" + i + ".Visible", sel && a == art);
                ui.set("#ChapArt" + artId(a) + "G" + i + ".Visible", !sel && a == art);
            }
            ui.set("#ChapFrameD" + i + ".Visible", false);
            ui.set("#ChapFrameS" + i + ".Visible", false);
            for (String t : TINTS) {
                ui.set("#ChapFrameD" + i + t + ".Visible", !sel && t.equals(TINT));
                ui.set("#ChapFrameS" + i + t + ".Visible", sel && t.equals(TINT));
                ui.set("#ChapCardShards" + i + t + ".Visible", sel && t.equals(TINT));
            }
            ui.set("#ChapFrameL" + i + ".Visible", false);
            ui.set("#ChapFrameLS" + i + ".Visible", false);
            ui.set("#ChapCardLock" + i + ".Visible", false);
            ui.set("#ChapCardPlus" + i + ".Visible", false);
            ui.set("#ChapCardShade" + i + ".Visible", true);
            ui.set("#ChapCardShards" + i + ".Visible", false);
            ui.set("#ChapCardBadge" + i + ".Visible", false);
            ui.set("#ChapCardRank" + i + ".Visible", false);
            ui.set("#ChapCardTitle" + i + ".Text", m.getName());
            ui.set("#ChapCardTitle" + i + ".Style.TextColor", GOLD);
            ui.set("#ChapCardState" + i + ".Text", blocs(distance(here, ContinentalTeleporters.spot(m))));
            ui.set("#ChapCardState" + i + ".Style.TextColor", CYAN);
        }
        ui.set("#ChapPrev.Visible", offset > 0);
        ui.set("#ChapNext.Visible", offset + CARDS < dest.size());

        // ---- fiche de la destination ----
        boolean any = !dest.isEmpty();
        ui.set("#ChapDetail.Visible", any);
        ui.set("#ChapGlows.Visible", any);
        ui.set("#ChapEmpty.Visible", !any);
        ui.set("#ChapEmpty.Text", from == null ? "Ce téléporteur n'appartient à aucun Marquisat."
                : "Aucun autre Téléporteur Continental n'est actif.");
        ui.set("#ChapAlt.Visible", false);
        ui.set("#ChapFilter.Visible", false);
        ui.set("#ChapRewards.Visible", false);
        if (!any) {
            ui.set("#ChapActionBox.Visible", false);
            ui.set("#ChapHint.Text", hint != null ? hint : "");
            return;
        }
        ParcelData to = dest.get(selected);
        long dist = distance(here, ContinentalTeleporters.spot(to));
        long price = ContinentalTeleporters.price(from);
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerLevelData pd = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        long purse = player != null ? CoinWallet.value(player) : 0, bank = pd != null ? pd.getMoney() : 0;
        boolean canPay = price <= 0 || purse + bank >= price;

        ui.set("#ChapRank.Visible", true);
        ui.set("#ChapRank.Text", "TP");
        ui.set("#ChapRank.Style.TextColor", CYAN);
        ui.set("#ChapMission.Visible", true);
        ui.set("#ChapMission.Text", "Téléporteur Continental");
        ui.set("#ChapMission.Style.TextColor", CYAN);
        ui.set("#ChapChain.Text", "Départ : " + from.getName());
        ui.set("#ChapTitle.Text", to.getName());
        ui.set("#ChapTitle.Style.TextColor", GOLD);
        String owner = to.getOwnerName();
        ui.set("#ChapTagline.Text", owner != null && !owner.isBlank() ? "Marquisat de " + owner : "Marquisat sans seigneur");
        ui.set("#ChapStory.Text", "Le Gardien de " + from.getName() + " ouvre la porte vers " + to.getName() + ", à "
                + blocs(dist) + " d'ici. " + (price > 0 ? "Le voyage se paie au départ, au trésor de " + from.getName() + "."
                : "Le Marquis offre le voyage."));
        String[] notes = {
                "Payé d'abord avec vos pièces, puis avec votre compte en banque.",
                "Une fois payé, traversez la porte dans les " + ContinentalGates.PASS_MS / 1000 + " secondes.",
                "Les autres joueurs paient aussi leur propre passage."};
        for (int i = 0; i < NOTES; i++) {
            ui.set("#ChapNote" + i + ".Visible", i < notes.length);
            if (i < notes.length) ui.set("#ChapNoteText" + i + ".Text", notes[i]);
        }
        String[][] rows = {
                {"Distance", blocs(dist), "1"},
                {"Prix du voyage", price > 0 ? CoinWallet.format(price) : "Offert", "1"},
                {"Vos pièces et votre compte", CoinWallet.format(purse + bank), canPay ? "1" : "0"},
                {"Porte d'arrivée", ContinentalTeleporters.active(to) ? "Active" : "Éteinte", ContinentalTeleporters.active(to) ? "1" : "0"}};
        ui.set("#ChapObjHead.Visible", true);
        for (int i = 0; i < OBJECTIVES; i++) {
            boolean shown = i < rows.length;
            ui.set("#ChapObj" + i + ".Visible", shown);
            if (!shown) continue;
            boolean ok = "1".equals(rows[i][2]);
            ui.set("#ChapObjOn" + i + ".Visible", ok);
            ui.set("#ChapObjOff" + i + ".Visible", !ok);
            ui.set("#ChapObjText" + i + ".Text", rows[i][0]);
            ui.set("#ChapObjText" + i + ".Style.TextColor", ok ? "#d9c58a" : "#b8c2d0");
            ui.set("#ChapObjCount" + i + ".Text", rows[i][1]);
            ui.set("#ChapObjCount" + i + ".Style.TextColor", ok ? "#f0c868" : RED);
        }
        ui.set("#ChapActionBox.Visible", true);
        ui.set("#ChapAction.Visible", true);
        ui.set("#ChapAction.Text", price > 0 ? "Voyager  ·  " + CoinWallet.format(price) : "Voyager");
        ui.set("#ChapHint.Text", hint != null ? hint : canPay ? "Choisissez une carte, puis « Voyager »."
                : "Il vous manque " + CoinWallet.format(price - purse - bank) + " pour ce voyage.");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        String action = data.action != null ? data.action : "";
        String hint = null;
        switch (action) {
            case "close" -> { close(); return; }
            case "card" -> {
                try { selected = offset + Integer.parseInt(data.index); } catch (Exception ignored) { }
            }
            case "prev" -> { offset = Math.max(0, offset - 1); selected = Math.min(selected, offset + CARDS - 1); }
            case "next" -> { offset++; selected = Math.max(selected, offset); }
            case "go" -> {
                try {
                    hint = travel(ref, store);
                } catch (Exception e) {
                    EldaniorLogger.error("[Teleporteur] voyage", e);
                    hint = "Le téléporteur crépite sans rien faire (voir le journal du serveur).";
                }
                if (hint == null) { close(); return; }
            }
            default -> { }
        }
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, ref, store, hint);
        sendUpdate(ui);
    }

    /** null : parti. Sinon le refus a afficher. */
    private String travel(Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData from = ParcelManager.get(fromId);
        if (from == null || !ContinentalTeleporters.active(from)) return "Ce téléporteur vient d'être éteint.";
        if (selected < 0 || selected >= dest.size()) return "Choisissez une destination.";
        ParcelData to = ParcelManager.get(dest.get(selected).getId());
        ContinentalTeleporters.Spot s = to != null ? ContinentalTeleporters.spot(to) : null;
        if (to == null || s == null || !ContinentalTeleporters.active(to)) return "Ce téléporteur ne répond plus.";
        Player player = store.getComponent(ref, Player.getComponentType());
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData pd = store.getComponent(ref, type);
        if (player == null || pd == null) return "";
        long price = ContinentalTeleporters.price(from);
        if (!com.eldanior.system.quest.chapitres.Primes.payWithBank(player, pd, price))
            return "Le voyage coûte " + CoinWallet.format(price) + " : ni vos pièces ni votre compte n'y suffisent.";
        store.putComponent(ref, type, pd);
        if (price > 0) {
            from.addTreasury(price);
            ParcelManager.save();
        }
        ContinentalGates.grant(playerRef.getUuid(), from, to);                          // on paie, puis on traverse la porte
        playerRef.sendMessage(Message.raw("Passage vers " + to.getName() + " payé" + (price > 0 ? " (" + CoinWallet.format(price) + ")" : "")
                + ". La porte s'illumine : traversez-la dans les " + ContinentalGates.PASS_MS / 1000 + " secondes.").color("#7fd4ff"));
        return null;
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .build();
        public String action, index;
    }
}
