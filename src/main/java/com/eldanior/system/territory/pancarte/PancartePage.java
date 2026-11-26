package com.eldanior.system.territory.pancarte;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelRole;
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
import java.util.Map;
import java.util.UUID;

/**
 * Fenetre d'une pancarte de propriete (Territoire/Pancarte.ui, scripts/territoire/gen_pancarte.py), style du recrutement
 * d'aventurier. Carte du bien (#PcCard) : statut, surface, prix, loyer, proprietaire, bail, acces ; Acheter / Louer, ou
 * Prolonger / Quitter pour le locataire. Carte de gestion (#PgCard), proprietaire et admins : prix, loyer, vente / location,
 * protection, acces (inviter, retirer), expulsion du locataire, lien de la pancarte.
 * Les commandes .Text recoivent des String (jamais de Message compose : deconnexion du client).
 */
public class PancartePage extends InteractiveCustomUIPage<PancartePage.Data> {

    private static final String YES = "oui", NO = "non";
    /** Choix « Proposer le bien » (2026-11-17 : une liste au lieu de deux oui / non). */
    private static final String M_NONE = "aucun", M_SALE = "vente", M_RENT = "location", M_BOTH = "les_deux";
    private static final int MEMBERS = 4;                       // = gen_pancarte.MEMBERS
    /** Affiche ouverte ("monde|x|y|z"), null si la fenetre vient du PNJ Immobilier (parcelId). */
    private final String key;
    private final String parcelId;
    /** Joueurs affiches dans les lignes d'acces (index du bouton Retirer -> joueur). */
    private final List<UUID> shownMembers = new ArrayList<>();

    public PancartePage(@Nonnull PlayerRef playerRef, String key) {
        this(playerRef, key, null);
    }

    private PancartePage(@Nonnull PlayerRef playerRef, String key, String parcelId) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.key = key;
        this.parcelId = parcelId;
    }

    /** Meme fenetre sans affiche (2026-11-17 : « gerer son bien depuis le PNJ Immobilier »). */
    public static PancartePage forParcel(@Nonnull PlayerRef playerRef, String parcelId) {
        return new PancartePage(playerRef, null, parcelId);
    }

    private ParcelData parcel() {
        return key != null ? Pancartes.parcelOf(key) : parcelId != null ? ParcelManager.get(parcelId) : null;
    }

    private static DropdownEntryInfo entry(String label, String value) {
        return new DropdownEntryInfo(LocalizableString.fromString(label), value);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Territoire/Pancarte.ui");
        ParcelData p = parcel();
        if (p == null) {                                                 // lien perdu (zone supprimee)
            ui.set("#PcName.Text", "Pancarte vierge");
            ui.set("#PcType.Text", "");
            ui.set("#PcStatus.Text", "Elle ne présente plus aucun bien.");
            ui.set("#PcRowDeal.Visible", false);
            ui.set("#PcRowLease.Visible", false);
            ui.set("#PgCard.Visible", false);
            return;
        }
        fill(ui, ref, store, p);
        bind(events);
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, ParcelData p) {
        UUID me = Pancartes.uuid(playerRef);
        boolean owner = me != null && p.isOwner(me);
        boolean renter = me != null && p.isRented() && me.equals(p.getRenterUUID());
        boolean rented = p.isRented() && p.getRenterUUID() != null;
        PlayerLevelData d = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        long purse = d != null ? d.getMoney() : 0;

        // ----- carte du bien -----
        ParcelData parent = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
        ui.set("#PcName.Text", p.getName() != null ? p.getName() : p.getId());
        ui.set("#PcType.Text", p.getType().getLabel() + (parent != null ? " · " + parent.getName() : ""));
        String status = owner ? "Votre bien" : renter ? "Vous louez ce bien" : rented ? "Loué"
                : p.isForSale() && p.isForRent() ? "À vendre ou à louer"
                : p.isForSale() ? "À vendre" : p.isForRent() ? "À louer"
                : p.getOwnerUUID() != null ? "Occupé" : "Pas à vendre";
        ui.set("#PcStatus.Text", status);
        String ownerName = p.getOwnerUUID() != null ? Pancartes.name(p.getOwnerUUID(), p.getOwnerName())
                : p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "La ville";
        ui.set("#PcQuote.Text", quote(p, owner, renter, rented));
        int w = p.getMaxX() - p.getMinX() + 1, l = p.getMaxZ() - p.getMinZ() + 1;
        ui.set("#PcSurface.Text", p.isCylinder() ? "Ø " + w : w + " x " + l);
        ui.set("#PcPrice.Text", p.isForSale() && p.getPrice() > 0 ? short_(p.getPrice()) : "—");
        ui.set("#PcRent.Text", p.isForRent() && p.getRentPrice() > 0 || rented ? short_(p.getRentPrice()) : "—");
        ui.set("#PcOwner.Text", ownerName);
        ui.set("#PcRenter.Text", rented ? Pancartes.name(p.getRenterUUID(), "un locataire") : "Aucun");
        ui.set("#PcLease.Text", rented && p.getEvictionAt() > 0 ? "congé dans " + Pancartes.remaining(p.getEvictionAt())
                : rented ? "encore " + Pancartes.remaining(p.getRentEndTime()) : p.getRentPrice() > 0 ? "7 jours" : "—");
        ui.set("#PcParent.Text", parent != null ? parent.getType().getLabel() + " " + parent.getName() : "Terres sauvages");
        int members = 0;
        for (var e : p.getMembers().entrySet()) if (e.getKey() != null && !e.getKey().equals(p.getOwnerUUID())) members++;
        ui.set("#PcAccess.Text", (p.isProtectedByDefault() ? "Protégé" : "Ouvert à tous") + " · " + members + (members > 1 ? " invités" : " invité"));
        ui.set("#PcPurse.Text", Pancartes.money(purse));
        int chests = Pancartes.chests(p, store.getExternalData().getWorld());
        ui.set("#PcChests.Text", chests < 0 ? "zone trop grande pour les compter" : chests == 0 ? "aucun"
                : chests + (chests > 1 ? " coffres" : " coffre"));

        boolean canBuy = !owner && p.isForSale() && !rented;
        boolean canRent = !owner && p.isForRent() && !rented;
        ui.set("#PcBtnBuy.Visible", canBuy);
        ui.set("#PcBtnRent.Visible", canRent);
        ui.set("#PcRowDeal.Visible", canBuy || canRent);
        ui.set("#PcRowLease.Visible", renter);
        ui.set("#PcBtnRenew.Visible", renter && p.getEvictionAt() <= 0);       // conge donne : plus de prolongation
        long cost = renter ? p.getRentPrice() : canBuy ? p.getPrice() : canRent ? p.getRentPrice() : 0;
        ui.set("#PcSlotValue.Text", cost > 0 ? short_(cost) : "—");
        ui.set("#PcCost.Text", renter ? "Prolonger le bail : " + Pancartes.money(p.getRentPrice()) + " pour 7 jours"
                : canBuy && canRent ? "Achat " + Pancartes.money(p.getPrice()) + " · loyer " + Pancartes.money(p.getRentPrice())
                : canBuy ? "Prix : " + Pancartes.money(p.getPrice())
                : canRent ? "Loyer : " + Pancartes.money(p.getRentPrice()) + " pour 7 jours"
                : owner ? "Réglez la vente et la location à droite." : "Ce bien n'est pas proposé pour le moment.");
        ui.set("#PcNote.Text", cost > purse && (canBuy || canRent || renter) ? "Votre bourse ne suffit pas." : "");

        // ----- carte de gestion (2026-11-17) -----
        // proprietaire (ou admin) : vente, location, protection ; acces : le locataire pendant la location, le proprietaire
        // sinon ; proprietaire d'un bien loue : seulement le preavis d'expulsion (2 jours).
        boolean admin = Pancartes.admin(playerRef);
        boolean settings = owner || admin;
        boolean access = Pancartes.managesAccess(p, playerRef);
        boolean manage = settings || renter;
        ui.set("#PgCard.Visible", manage);
        ui.set("#PgGap.Visible", manage);
        if (!manage) return;
        ui.set("#PgTitle.Text", renter ? "Votre location" : "Gestion du bien");
        ui.set("#PgSubtitle.Text", renter ? "Bail de " + ownerName : owner ? "Vous en êtes propriétaire" : "Admin · " + ownerName);
        ui.set("#PgTreasury.Text", renter ? "Vous décidez qui entre chez vous" : "Taxe sur chaque paiement : vers la ville");

        for (String id : new String[]{"#PgSecSale", "#PgRowMode", "#PgRowProtect", "#PgRowSave"}) ui.set(id + ".Visible", settings);
        if (settings) {
            ui.set("#PgMode.Entries", List.of(entry("Ne pas le proposer", M_NONE), entry("À vendre", M_SALE),
                    entry("À louer", M_RENT), entry("À vendre et à louer", M_BOTH)));
            ui.set("#PgProtect.Entries", List.of(entry("Protégé", YES), entry("Ouvert", NO)));
            ui.set("#PgPrice.Value", (int) Math.min(Integer.MAX_VALUE, p.getPrice()));
            ui.set("#PgRent.Value", (int) Math.min(Integer.MAX_VALUE, p.getRentPrice()));
            // deja loue : forRent a ete coupe a la signature, la location reprendra au depart du locataire
            boolean offerRent = p.isForRent() || rented && p.getRentPrice() > 0;
            String mode = p.isForSale() && offerRent ? M_BOTH : p.isForSale() ? M_SALE : offerRent ? M_RENT : M_NONE;
            ui.set("#PgMode.Value", mode);
            showModeRows(ui, mode);
            ui.set("#PgProtect.Value", p.isProtectedByDefault() ? YES : NO);
        } else {
            showModeRows(ui, M_NONE);
        }

        shownMembers.clear();
        if (access) for (Map.Entry<UUID, ParcelRole> e : p.getMembers().entrySet()) {
            if (e.getKey() == null || e.getKey().equals(p.getOwnerUUID()) || e.getKey().equals(p.getRenterUUID())) continue;
            if (shownMembers.size() >= MEMBERS) break;
            shownMembers.add(e.getKey());
        }
        for (int i = 0; i < MEMBERS; i++) {
            boolean on = i < shownMembers.size();
            ui.set("#PgMem" + i + ".Visible", on);
            if (on) ui.set("#PgMemName" + i + ".Text", Pancartes.name(shownMembers.get(i), "Joueur hors ligne"));
        }
        ui.set("#PgNoMember.Visible", access && shownMembers.isEmpty());
        ui.set("#PgRowInvite.Visible", access);
        ui.set("#PgAccessLocked.Visible", !access);
        if (!access) ui.set("#PgAccessLocked.Text", "Votre bien est loué : son locataire décide qui entre. Vous pouvez lui donner congé ci-dessous.");

        long notice = p.getEvictionAt();
        String noticeText = notice > 0 ? "préavis : départ dans " + Pancartes.remaining(notice) : null;
        if (renter) {
            ui.set("#PgRenter.Text", "Bail : encore " + Pancartes.remaining(p.getRentEndTime()) + (noticeText != null ? "\n" + noticeText : ""));
            ui.set("#PgBtnEvict.Visible", false);
        } else {
            ui.set("#PgRenter.Text", rented ? "Locataire : " + Pancartes.name(p.getRenterUUID(), "hors ligne") + " ("
                    + (noticeText != null ? noticeText : "bail : " + Pancartes.remaining(p.getRentEndTime())) + ")" : "Pas de locataire.");
            ui.set("#PgBtnEvict.Visible", rented);
            if (rented) ui.set("#PgBtnEvict.Text", owner ? (notice > 0 ? "Annuler le préavis" : "Préavis (2 j)") : "Expulser");
        }
        ui.set("#PgRowUnlink.Visible", key != null && settings);
        ui.set("#PgHint.Text", renter ? (notice > 0 ? "À votre départ, ce que vous avez posé sera retiré." : "")
                : rented && p.isForSale() ? "Loué : la vente attend le départ du locataire." : "");
    }

    /** Prix visible si le bien est a vendre, loyer s'il est a louer (les deux pour « vendre et louer »). */
    private static void showModeRows(UICommandBuilder ui, String mode) {
        ui.set("#PgRowPrice.Visible", M_SALE.equals(mode) || M_BOTH.equals(mode));
        ui.set("#PgRowRent.Visible", M_RENT.equals(mode) || M_BOTH.equals(mode));
    }

    private static String quote(ParcelData p, boolean owner, boolean renter, boolean rented) {
        if (owner) return rented ? "Votre bien est loué. Le loyer vous revient, moins la taxe de la ville." :
                p.isForSale() && p.isForRent() ? "Votre bien est proposé à la vente et à la location : le premier venu choisit." :
                p.isForSale() ? "Votre bien est en vente : le premier acheteur le prend." :
                p.isForRent() ? "Votre bien attend un locataire." : "Votre bien n'est proposé à personne.";
        if (renter && p.getEvictionAt() > 0) return "Le propriétaire vous a donné congé : partez dans " + Pancartes.remaining(p.getEvictionAt())
                + ". Videz vos coffres, ce que vous avez posé sera retiré.";
        if (renter) return "Votre bail court encore " + Pancartes.remaining(p.getRentEndTime()) + ". Prolongez-le avant son terme.";
        if (rented) return "Ce bien est occupé par un locataire.";
        if (p.isForSale() && p.isForRent()) return "À acheter pour de bon, ou à louer sept jours renouvelables : à vous de choisir.";
        if (p.isForSale()) return "Un bien à saisir : il sera à vous dès le paiement.";
        if (p.isForRent() && !rented) return "Un logement à louer pour sept jours, renouvelables.";
        return "Ce bien n'est pas proposé pour le moment.";
    }

    /** Montant court pour les indicateurs : 1 200, 15 k, 2,5 M. */
    private static String short_(long v) {
        if (v >= 1_000_000) return String.format(java.util.Locale.FRANCE, "%.1f M", v / 1_000_000.0).replace(",0 ", " ");
        if (v >= 100_000) return (v / 1000) + " k";
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }

    private void bind(UIEventBuilder events) {
        for (String[] b : new String[][]{{"#PcBtnBuy", "buy"}, {"#PcBtnRent", "rent"}, {"#PcBtnRenew", "renew"}, {"#PcBtnQuit", "quit"},
                {"#PgBtnEvict", "evict"}, {"#PgBtnUnlink", "unlink"}})
            events.addEventBinding(CustomUIEventBindingType.Activating, b[0], EventData.of("Action", b[1]), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PgBtnSave", EventData.of("Action", "save")
                .append("@Price", "#PgPrice.Value").append("@Rent", "#PgRent.Value").append("@Mode", "#PgMode.Value")
                .append("@Protect", "#PgProtect.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PgMode",
                EventData.of("Action", "mode").append("@Mode", "#PgMode.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PgBtnInvite",
                EventData.of("Action", "invite").append("@Name", "#PgInvite.Value"), false);
        for (int i = 0; i < MEMBERS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PgMemKick" + i,
                    EventData.of("Action", "kick").append("Param", String.valueOf(i)), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            ParcelData p = parcel();
            if (p == null || data.action == null) return;
            boolean manage = Pancartes.manages(p, playerRef);              // proprietaire ou admin : reglages
            boolean access = Pancartes.managesAccess(p, playerRef);         // invitations
            String err;
            String ok;
            switch (data.action) {
                case "buy" -> { err = Pancartes.buy(ref, store, playerRef, p); ok = "Vous avez acheté « " + p.getName() + " » !"; }
                case "rent" -> { err = Pancartes.rent(ref, store, playerRef, p); ok = "Vous louez « " + p.getName() + " » pour 7 jours."; }
                case "renew" -> { err = Pancartes.renew(ref, store, playerRef, p); ok = "Bail prolongé de 7 jours."; }
                case "quit" -> { err = Pancartes.quit(playerRef, p); ok = "Vous avez quitté « " + p.getName() + " » : ce que vous y aviez posé est retiré."; }
                case "mode" -> {                                                  // seules les lignes changent : la saisie reste
                    if (!manage) return;                                          // (reglages : proprietaire ou admin)
                    UICommandBuilder ui = new UICommandBuilder();
                    showModeRows(ui, data.mode != null ? data.mode : M_NONE);
                    sendUpdate(ui);
                    return;
                }
                case "save" -> {
                    if (!manage) return;
                    String m = data.mode != null ? data.mode : M_NONE;
                    err = Pancartes.saveSettings(p, Math.max(0, data.price), Math.max(0, data.rent),
                            M_SALE.equals(m) || M_BOTH.equals(m), M_RENT.equals(m) || M_BOTH.equals(m), !NO.equals(data.protect));
                    ok = "Réglages de « " + p.getName() + " » enregistrés.";
                }
                case "invite" -> {
                    if (!access) { err = "Votre bien est loué : c'est le locataire qui invite."; ok = null; }
                    else { err = Pancartes.invite(p, data.name); ok = "Accès accordé."; }
                }
                case "kick" -> {
                    if (!access) return;
                    int i = data.param != null ? EldaniorLogger.parseSafeInt(data.param, -1) : -1;
                    err = i >= 0 && i < shownMembers.size() ? Pancartes.kick(p, shownMembers.get(i)) : "Joueur introuvable.";
                    ok = "Accès retiré.";
                }
                case "evict" -> {
                    if (!manage) return;
                    UUID me = Pancartes.uuid(playerRef);
                    if (me != null && p.isOwner(me)) {                         // proprietaire : preavis de 2 jours
                        boolean cancel = p.getEvictionAt() > 0;
                        err = cancel ? Pancartes.cancelNotice(p) : Pancartes.notice(p);
                        ok = cancel ? "Préavis annulé." : "Préavis donné : le locataire doit partir dans 2 jours.";
                    } else {                                                    // admin : expulsion immediate
                        err = Pancartes.evict(p);
                        ok = "Locataire expulsé ; la zone est remise en état.";
                    }
                }
                case "unlink" -> {
                    if (!manage || key == null) return;
                    Pancartes.unlink(key);
                    playerRef.sendMessage(Message.raw("La pancarte ne présente plus « " + p.getName() + " »."));
                    Player player = store.getComponent(ref, Player.getComponentType());
                    if (player != null) player.getPageManager().openCustomPage(ref, store, new PancarteLienPage(playerRef, key));
                    return;
                }
                default -> { return; }
            }
            playerRef.sendMessage(Message.raw(err != null ? err : ok));
            if (err == null) Pancartes.refreshPosters(p);                         // l'affiche suit le nouvel etat du bien
            UICommandBuilder ui = new UICommandBuilder();
            fill(ui, ref, store, p);
            sendUpdate(ui);
        } catch (Exception e) {
            EldaniorLogger.error("[Pancarte] action " + data.action, e);
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Param", Codec.STRING), (d, v) -> d.param = v, d -> d.param).add()
                .append(new KeyedCodec<>("@Price", Codec.INTEGER), (d, v) -> d.price = v, d -> d.price).add()
                .append(new KeyedCodec<>("@Rent", Codec.INTEGER), (d, v) -> d.rent = v, d -> d.rent).add()
                .append(new KeyedCodec<>("@Mode", Codec.STRING), (d, v) -> d.mode = v, d -> d.mode).add()
                .append(new KeyedCodec<>("@Protect", Codec.STRING), (d, v) -> d.protect = v, d -> d.protect).add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (d, v) -> d.name = v, d -> d.name).add()
                .build();
        public String action, param, mode, protect, name;
        public int price, rent;
    }
}
