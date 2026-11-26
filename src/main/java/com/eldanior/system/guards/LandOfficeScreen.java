package com.eldanior.system.guards;

import com.eldanior.system.boards.BoardScreen;
import com.eldanior.system.boards.BoardType;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.territory.LandOffice;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.territory.pancarte.Pancartes;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Bureau des terres (2026-11-13, demandes de l'utilisateur), fenetre facon tableau (BoardType.IMMOBILIER) :
 *  - Commercant Immobilier (metier IMMOBILIER, ouvert a tous) : « le joueur qui possede une propriete mais n'a pas acces a
 *    l'intendant peut gerer ses proprietes et en voir la liste » ;
 *  - intendant, pour les Ducs et Comtes (et depuis l'onglet Domaines des membres) : acheter un Territoire ou une ville de ses
 *    domaines, choisi dans la liste (TerritoryOffice : LandOffice).
 * Onglets : Mes biens (vendre, louer, retirer du marche ; prolonger ou quitter une location), Marche (biens a vendre ou a louer :
 * acheter, louer), Terres (Duches libres, territoires et villes sans maitre). scope : zones de l'intendant (null : tout le monde).
 * Achats de terre : second clic.
 */
public final class LandOfficeScreen extends BoardScreen {

    private static final int T_MINE = 0, T_MARKET = 1, T_LANDS = 2;
    private static final String C_GREEN = "#00e7b6", C_BLUE = "#7a9cc6", C_MUTED = "#6f8296";
    private static final long ARM_MS = 6_000;
    /** Biens geres ici : parcelles, maisons, chambres, fermes, forets, mines, villes (pas les territoires des maisons). */
    private static final List<ParcelType> ESTATES = List.of(ParcelType.PLOT, ParcelType.HOUSING, ParcelType.ROOM, ParcelType.FARM,
            ParcelType.FOREST, ParcelType.MINE, ParcelType.CITY, ParcelType.TERRITORY);

    private final String speaker, world;
    private final Collection<String> scope;
    private int selected = -1;
    private long amount;
    private String armed;
    private long armedAt;
    private List<ParcelData> rows = List.of();

    /**
     * speaker : nom du PNJ ; place : lieu affiche ; world : monde du PNJ ; scope : zones dont l'intendant vend les terres (null :
     * tout le monde) ; startTab : onglet ouvert (Terres depuis l'intendant).
     */
    public LandOfficeScreen(@Nonnull PlayerRef playerRef, String speaker, String place, String world, Collection<String> scope, int startTab) {
        super(playerRef, BoardType.IMMOBILIER, place);
        this.speaker = speaker;
        this.world = world;
        this.scope = scope;
        this.tab = Math.max(0, Math.min(scope == null ? T_MARKET : T_LANDS, startTab));
    }

    public static final int TAB_LANDS = T_LANDS;

    /**
     * Commercant immobilier (scope null) : plus d'onglet Terres (2026-11-18, demande de l'utilisateur : les terres s'achetent
     * chez l'intendant, selon le titre de noblesse) ; il garde Mes biens et Marche.
     */
    @Override
    protected List<String> tabs() {
        List<String> all = super.tabs();
        return scope == null ? all.subList(0, Math.min(2, all.size())) : all;
    }

    @Override
    protected String title() { return scope == null ? "Bureau des terres" : "Terres du domaine"; }

    @Override
    protected String purseLabel() { return "Votre compte"; }

    private UUID me() {
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }

    // ===== Listes =====

    private List<ParcelData> mine() {
        UUID me = me();
        List<ParcelData> out = new ArrayList<>();
        if (me == null) return out;
        for (ParcelData p : ParcelManager.getAll())
            if (ESTATES.contains(p.getType()) && (p.isOwner(me) || (p.isRented() && me.equals(p.getRenterUUID())))) out.add(p);
        out.sort(java.util.Comparator.comparing((ParcelData p) -> p.getType().ordinal()).thenComparing(LandOffice::name, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private List<ParcelData> market() {
        UUID me = me();
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData p : ParcelManager.getAll()) {
            if (!ESTATES.contains(p.getType()) || p.getType() == ParcelType.TERRITORY || (world != null && !world.equals(p.getWorld()))) continue;
            if (me != null && p.isOwner(me)) continue;
            boolean rented = p.isRented() && p.getRenterUUID() != null;
            if ((p.isForSale() && !rented || p.isForRent() && !rented) && LandOffice.within(p, scope)) out.add(p);
        }
        out.sort(java.util.Comparator.comparing(LandOffice::name, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private void reload() {
        rows = switch (tab) {
            case T_MINE -> mine();
            case T_MARKET -> market();
            default -> LandOffice.lands(scope, world);
        };
        if (selected >= rows.size()) selected = -1;
    }

    private ParcelData sel() { return selected >= 0 && selected < rows.size() ? rows.get(selected) : null; }

    // ===== Replique et tuiles =====

    @Override
    protected String quote(Ref<EntityStore> ref, Store<EntityStore> store) {
        String who = speaker.split(" ")[0];
        return switch (tab) {
            case T_MINE -> rows.isEmpty() ? "Je n'ai aucun acte à votre nom. Le marché vous attend, si votre bourse le permet."
                    : "Vos actes sont en ordre : " + rows.size() + (rows.size() > 1 ? " biens" : " bien") + ". Que dois-je y changer ?";
            case T_MARKET -> rows.isEmpty() ? "Rien à vendre ni à louer en ce moment. Repassez dans quelques jours."
                    : rows.size() + (rows.size() > 1 ? " biens cherchent" : " bien cherche") + " preneur. Désignez-en un, je vous dirai ce qu'il vaut.";
            default -> rows.isEmpty() ? "Aucune terre n'est à vendre " + (scope == null ? "dans le royaume" : "dans ces domaines") + ". Les titres se méritent."
                    : "Duchés, territoires et villes sans maître : choisissez. " + who + " consignera l'acte.";
        };
    }

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        reload();
        PlayerLevelData d = data(ref, store);
        NobilityRank rank = d != null ? NobilityRank.fromString(d.getNobilityRank()) : null;
        List<ParcelData> mine = tab == T_MINE ? rows : mine();
        return List.of(new Stat(String.valueOf(mine.size()), "Mes biens"),
                new Stat(String.valueOf(mine.stream().filter(p -> p.isForSale() || p.isForRent()).count()), "Proposés"),
                new Stat(String.valueOf(tab == T_MARKET ? rows.size() : market().size()), "Au marché"),
                new Stat(rank != null ? rank.getDisplayName() : "—", "Votre rang"));
    }

    // ===== Onglets =====

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        reload();
        UUID me = me();
        List<Row> out = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ParcelData p = rows.get(i);
            ParcelData parent = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
            String where = p.getType().getLabel() + (parent != null ? " · " + LandOffice.name(parent) : "");
            switch (index) {
                case T_MINE -> {
                    boolean renter = me != null && !p.isOwner(me);
                    String state = renter ? "loué par vous, encore " + Pancartes.remaining(p.getRentEndTime()) : state(p);
                    out.add(new Row(renter ? "L" : "P", LandOffice.name(p), where + "  ·  " + state,
                            p.isForSale() ? gold(p.getPrice()) + " or" : p.isForRent() || p.isRented() ? gold(p.getRentPrice()) + " or / 7 j" : "—",
                            p.isForSale() ? C_GREEN : p.isForRent() ? C_BLUE : null, true, true, i == selected));
                }
                case T_MARKET -> {
                    boolean sale = p.isForSale();
                    out.add(new Row(sale ? "V" : "L", LandOffice.name(p), where + "  ·  " + (sale ? "à vendre" : "à louer") + "  ·  "
                            + Pancartes.name(p.getOwnerUUID(), p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "la ville"),
                            sale ? gold(p.getPrice()) + " or" : gold(p.getRentPrice()) + " or / 7 j", sale ? C_GREEN : C_BLUE, true, false, i == selected));
                }
                default -> {
                    var house = LandOffice.freeHouse(p);
                    out.add(new Row(p.getType() == ParcelType.CITY ? "V" : "T", LandOffice.name(p), where
                            + (house != null ? "  ·  maison " + house.getDisplayName() + " sans Patriarche" : "  ·  sans maître")
                            + "  ·  " + LandOffice.required(p).getDisplayName(), gold(LandOffice.price(p)) + " or", null, true, false, i == selected));
                }
            }
        }
        return switch (index) {
            case T_MINE -> new Tab("Mes biens", out, "Aucun bien à votre nom.", rows.size() + " bien(s)  ·  l'affiche d'un bien suit son état.",
                    sel() != null ? "Gérer le bien" : null,
                    "Vos parcelles, maisons, chambres, fermes, forêts, mines et villes, et les biens que vous louez.\n\n"
                            + "Désignez un bien (œil) : tapez un montant dans la fiche, puis mettez-le en vente ou en location "
                            + "(sept jours). Un locataire prolonge son bail ou le quitte ici.\n\n"
                            + "« Gérer le bien » : la fenêtre de l'affiche — vente, location, invités (le locataire invite chez lui), "
                            + "préavis d'expulsion de 2 jours pour le propriétaire d'un bien loué.\n\n"
                            + "Pas besoin de l'intendant : je tiens les actes de tout le royaume.");
            case T_MARKET -> new Tab("Biens à vendre ou à louer", out, "Le marché est vide.", rows.size() + " bien(s) proposé(s).", null,
                    "Tous les biens proposés " + (scope == null ? "dans le royaume" : "dans ces domaines") + ".\n\n"
                            + "Désignez un bien (œil) : prix, loyer, surface, coffres. L'achat et la location se paient depuis votre "
                            + "compte, moins la taxe de la ville qui revient à ses dirigeants.");
            default -> new Tab("Terres à acquérir", out, "Aucune terre à vendre.", "Territoire : Duc ou plus  ·  ville : Comte, chef de guilde ou Capitaine d'Ordre.", null,
                    "Les Duchés dont la maison attend un Patriarche, et les territoires et villes sans maître.\n\n"
                            + "Duc (ou plus) : un Territoire. Acheter un Duché libre fait de vous le Patriarche de sa maison (il faut "
                            + "n'appartenir à aucune).\nComte (ou plus), chef de guilde ou Capitaine d'un Ordre : une ville.\n\n"
                            + "Le prix est pris sur votre compte et versé au trésor de la terre qui la vendait. Les Marquisats "
                            + "s'achètent depuis l'onglet Famille du menu ; le Roi tient la maison royale.");
        };
    }

    private static String state(ParcelData p) {
        boolean rented = p.isRented() && p.getRenterUUID() != null;
        if (rented) return "loué à " + Pancartes.name(p.getRenterUUID(), "un locataire");
        if (p.isForSale()) return "à vendre";
        if (p.isForRent()) return "à louer";
        return "à vous";
    }

    // ===== Fiche =====

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = sel();
        if (p == null) return null;
        UUID me = me();
        ParcelData parent = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
        int w = p.getMaxX() - p.getMinX() + 1, l = p.getMaxZ() - p.getMinZ() + 1;
        StringBuilder t = new StringBuilder();
        t.append(p.getType().getLabel()).append(parent != null ? " de " + LandOffice.name(parent) : "").append("  ·  ")
                .append(p.isCylinder() ? "Ø " + w : w + " × " + l).append("\n");
        t.append("Lieu : ").append(com.eldanior.system.guards.MartialDuels.placeName(p.getWorld(),
                new org.joml.Vector3d((p.getMinX() + p.getMaxX()) / 2.0, p.getMinY(), (p.getMinZ() + p.getMaxZ()) / 2.0))).append("\n");
        String subtitle = speaker;
        switch (tab) {
            case T_MINE -> {
                boolean owner = me != null && p.isOwner(me);
                t.append("État : ").append(owner ? state(p) : "loué par vous").append("\n");
                t.append("Prix : ").append(p.getPrice() > 0 ? gold(p.getPrice()) + " or" : "—").append("  ·  loyer : ")
                        .append(p.getRentPrice() > 0 ? gold(p.getRentPrice()) + " or / 7 j" : "—").append("\n");
                if (p.getType() != ParcelType.TERRITORY) {
                    int chests = Pancartes.chests(p, store.getExternalData().getWorld());
                    t.append("Coffres : ").append(chests < 0 ? "trop grande zone pour les compter" : chests).append("\n");
                }
                if (owner) {
                    boolean rented = p.isRented() && p.getRenterUUID() != null;
                    if (rented) t.append("Bail : encore ").append(Pancartes.remaining(p.getRentEndTime())).append("\n");
                    t.append("\n« Tapez un montant : il sert de prix de vente, ou de loyer pour sept jours. »");
                    return new Side(LandOffice.name(p), "Votre bien", t.toString(), "Prix ou loyer (or)",
                            amount > 0 ? "Vente : " + gold(amount) + " or  ·  loyer : " + gold(amount) + " or / 7 j" : "", null,
                            rented ? null : "Vendre à " + gold(Math.max(0, amount)) + " or", "Fermer");
                }
                t.append("Bail : encore ").append(Pancartes.remaining(p.getRentEndTime())).append("\n");
                return new Side(LandOffice.name(p), "Vous louez ce bien", t.toString(), null, null, null,
                        "Prolonger : " + gold(p.getRentPrice()) + " or", "Fermer");
            }
            case T_MARKET -> {
                t.append("Maître : ").append(Pancartes.name(p.getOwnerUUID(), p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "la ville")).append("\n");
                t.append("Prix : ").append(p.isForSale() ? gold(p.getPrice()) + " or" : "—").append("  ·  loyer : ")
                        .append(p.isForRent() ? gold(p.getRentPrice()) + " or / 7 j" : "—").append("\n");
                int chests = Pancartes.chests(p, store.getExternalData().getWorld());
                t.append("Coffres : ").append(chests < 0 ? "trop grande zone pour les compter" : chests).append("\n");
                if (p.getType() == ParcelType.CITY) t.append("\n").append(LandOffice.CITY_RULE);
                return new Side(LandOffice.name(p), subtitle, t.toString(), null, null, null,
                        p.isForSale() ? (armed("buy:" + p.getId()) ? "Confirmer l'achat" : "Acheter : " + gold(p.getPrice()) + " or")
                                : armed("rent:" + p.getId()) ? "Confirmer la location" : "Louer : " + gold(p.getRentPrice()) + " or",
                        "Fermer");
            }
            default -> {
                var house = LandOffice.freeHouse(p);
                if (house != null) t.append("Maison : ").append(house.getDisplayName()).append(", sans Patriarche — « ").append(house.getMotto()).append(" »\n");
                t.append("Zones vassales : ").append(p.getChildIds().size()).append("  ·  trésor : ").append(gold(p.getTreasury())).append(" or\n");
                t.append("Prix : ").append(gold(LandOffice.price(p))).append(" or  ·  rang requis : ").append(LandOffice.required(p).getDisplayName()).append("\n");
                String refused = LandOffice.refusal(p, data(ref, store), me);
                if (refused != null) t.append("\n« ").append(refused).append(" »");
                else if (house != null) t.append("\n« Vous en deviendrez le Patriarche : ses terres, ses coffres et son intendant seront à vous. »");
                return new Side(LandOffice.name(p), subtitle, t.toString(), null, null, null,
                        refused != null ? null : armed("land:" + p.getId()) ? "Confirmer l'achat" : "Acheter : " + gold(LandOffice.price(p)) + " or",
                        "Fermer");
            }
        }
    }

    @Override
    protected String altButton(Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = sel();
        UUID me = me();
        if (p == null || tab != T_MINE) return null;
        if (me != null && p.isOwner(me)) return p.isRented() && p.getRenterUUID() != null ? null : "Louer à " + gold(Math.max(0, amount)) + " or";
        return "Quitter le logement";
    }

    // ===== Evenements =====

    @Override
    protected void onTab() { selected = -1; amount = 0; armed = null; }

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        reload();
        if (index < 0 || index >= rows.size()) return;
        selected = index == selected ? -1 : index;
        armed = null;
        ParcelData p = sel();
        if (p != null && tab == T_MINE) {
            amount = p.isForRent() ? p.getRentPrice() : p.getPrice();
            setAmount(amount);
        }
    }

    @Override
    protected void onAmount(long value) { amount = Math.max(0, value); }

    @Override
    protected void onCancel() { selected = -1; armed = null; }

    @Override
    protected void onAction(Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = sel();
        UUID me = me();
        if (tab != T_MINE || p == null || me == null) return;
        if (!p.isOwner(me) && !Pancartes.isRenter(p, me)) return;
        // 2026-11-17 : « gerer son bien depuis le PNJ Immobilier » = la fenetre de l'affiche, sans affiche
        var player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player != null) player.getPageManager().openCustomPage(ref, store,
                com.eldanior.system.territory.pancarte.PancartePage.forParcel(playerRef, p.getId()));
    }

    @Override
    protected void onConfirm(long value, Ref<EntityStore> ref, Store<EntityStore> store) {
        amount = Math.max(0, value);
        ParcelData p = sel();
        if (p == null) return;
        UUID me = me();
        switch (tab) {
            case T_MINE -> {
                if (me != null && p.isOwner(me)) { market(p, true); return; }
                report(Pancartes.renew(ref, store, playerRef, p), "Bail prolongé de 7 jours.", p);
            }
            case T_MARKET -> {
                boolean sale = p.isForSale();
                if (!arm((sale ? "buy:" : "rent:") + p.getId())) {
                    error("Cliquez encore pour " + (sale ? "acheter " : "louer ") + LandOffice.name(p) + ".");
                    return;
                }
                report(sale ? Pancartes.buy(ref, store, playerRef, p) : Pancartes.rent(ref, store, playerRef, p),
                        sale ? "« " + LandOffice.name(p) + " » est à vous." : "Vous louez « " + LandOffice.name(p) + " » pour 7 jours.", p);
                selected = -1;
            }
            default -> {
                if (!arm("land:" + p.getId())) { error("Cliquez encore pour acheter " + LandOffice.name(p) + "."); return; }
                var house = LandOffice.freeHouse(p);
                String err = LandOffice.buy(ref, store, p);
                if (err != null) { error(err); return; }
                ok(house != null ? "Vous êtes le Patriarche de la maison " + house.getDisplayName() + " et le maître de " + LandOffice.name(p) + "."
                        : LandOffice.name(p) + " est à vous.");
                selected = -1;
            }
        }
    }

    @Override
    protected void onAlt(Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = sel();
        UUID me = me();
        if (p == null || tab != T_MINE) return;
        if (me != null && p.isOwner(me)) { market(p, false); return; }
        if (!arm("quit:" + p.getId())) { error("Cliquez encore pour quitter " + LandOffice.name(p) + "."); return; }
        report(Pancartes.quit(playerRef, p), "Vous avez quitté « " + LandOffice.name(p) + " ».", p);
        selected = -1;
    }

    /** sale : mettre en vente au montant de la fiche ; sinon en location (7 jours). */
    private void market(ParcelData p, boolean sale) {
        long value = Math.max(0, amount);
        String err = Pancartes.saveSettings(p, sale ? value : p.getPrice(), sale ? p.getRentPrice() : value, sale, !sale, p.isProtectedByDefault());
        if (err != null) { error(err); return; }
        Pancartes.refreshPosters(p);
        ok(LandOffice.name(p) + (sale ? " est à vendre : " + gold(value) + " or." : " est à louer : " + gold(value) + " or pour 7 jours."));
    }

    private void report(String err, String success, ParcelData p) {
        if (err != null) { error(err); return; }
        Pancartes.refreshPosters(p);
        ok(success);
    }

    private boolean arm(String key) {
        long now = System.currentTimeMillis();
        if (key.equals(armed) && now - armedAt <= ARM_MS) { armed = null; return true; }
        armed = key;
        armedAt = now;
        return false;
    }

    private boolean armed(String key) { return key.equals(armed) && System.currentTimeMillis() - armedAt <= ARM_MS; }
}
