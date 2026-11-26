package com.eldanior.system.territory.pancarte;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.gui.tabs.ProprietesTab;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelEconomyManager;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelRole;
import com.eldanior.system.territory.ParcelType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Pancarte de propriete (demande du 2026-11-12) : un panneau pose pres d'un bien et lie a sa zone. F dessus ouvre une fenetre
 * dans le style du recrutement d'aventurier (PancartePage) : informations, achat, location ; une seconde carte de gestion
 * s'affiche pour le proprietaire (et les admins). Remplace l'onglet Proprietes du menu systeme.
 *
 * Lien : garde dans la zone (ParcelData.pancartes, "monde|x|y|z" separes par ';'), sauvegarde avec les parcelles ; une zone peut
 * avoir plusieurs pancartes, supprimer la zone efface le lien. Pose : admins (toute zone de bien proche) ou proprietaires (leurs
 * zones). Casser : admins ou proprietaire de la zone liee.
 * Les reglements reprennent ceux de l'ancien onglet (ProprietesTab) : taxe de ParcelEconomyManager, net au proprietaire ou a la
 * ville, bail de 7 jours.
 */
public final class Pancartes {

    private Pancartes() {}

    /** Ancienne pancarte (panneau du village), retiree le 2026-11-12 : seule l'affiche reste. */
    public static final String ITEM = "Eldanior_Pancarte_Propriete";
    /** Affiche murale, seul objet depuis le 2026-11-12 ; ses etats suivent le bien (refreshPoster). */
    public static final String POSTER = "Eldanior_Affiche_Propriete";
    /** Etats de l'affiche (gen_pancarte.py, POSTER_STATES) ; "default" = affiche neutre (non liee, ou bien non propose). */
    static final String STATE_SALE = "Vendre", STATE_RENT = "Louer", STATE_BOTH = "VendreLouer", STATE_TAKEN = "Occupe",
            STATE_NONE = "default";
    private static final int REFRESH_SECONDS = 15;
    /** Comptage des coffres : sections (32 x 32 x 32) parcourues au plus, duree du cache (courte : un coffre pose se voit vite). */
    private static final int CHEST_SCAN_MAX_SECTIONS = 20_000;
    private static final long CHEST_CACHE_MS = 5_000;
    private static final java.util.Map<String, long[]> chestCache = new java.util.concurrent.ConcurrentHashMap<>();
    /** Index de type de bloc -> coffre ou non (calcule une fois par type). */
    private static final java.util.Map<Integer, Boolean> chestTypes = new java.util.concurrent.ConcurrentHashMap<>();
    /** Zones qu'une pancarte peut presenter : les biens (pas les royaumes ni les territoires, geres par l'intendant). */
    static final List<ParcelType> LINKABLE = List.of(ParcelType.PLOT, ParcelType.HOUSING, ParcelType.ROOM, ParcelType.FARM,
            ParcelType.FOREST, ParcelType.MINE, ParcelType.CITY);
    /** Distance maximale entre la pancarte et le bord de la zone proposee au lien. */
    static final int LINK_RANGE = 48;
    static final long DAY_MS = 24L * 60 * 60 * 1000;

    public static String key(String world, int x, int y, int z) { return world + "|" + x + "|" + y + "|" + z; }

    static String world(String key) { return key.substring(0, key.indexOf('|')); }

    static int[] coords(String key) {
        String[] k = key.split("\\|");
        return new int[]{Integer.parseInt(k[1]), Integer.parseInt(k[2]), Integer.parseInt(k[3])};
    }

    public static boolean isPancarte(BlockType type) {
        String id = type != null ? type.getId() : null;
        return id != null && (is(id, ITEM) || is(id, POSTER));
    }

    static boolean isItem(String id) { return POSTER.equals(id); }

    /**
     * Les etats d'un bloc ont pour id « *Eldanior_Affiche_Propriete_State_Definitions_Vendre » (etoile en tete) : sans la retirer,
     * une affiche passee a l'etat Vendre / Louer / Occupe n'etait plus reconnue (F, casse et rafraichissement ignores).
     */
    private static boolean is(String id, String item) {
        if (id.startsWith("*")) id = id.substring(1);
        return id.equals(item) || id.startsWith(item + "_State");
    }

    // ===== Affiche dynamique (2026-11-12) =====

    /** Lance le suivi : toutes les REFRESH_SECONDS, chaque affiche liee prend l'etat de son bien. */
    public static void init() {
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                checkNotices();
                for (ParcelData p : ParcelManager.getAll()) if (!p.getPancartes().isEmpty()) refreshPosters(p);
            }
            catch (Exception e) { EldaniorLogger.error("[Pancarte] etat des affiches", e); }
        }, REFRESH_SECONDS, REFRESH_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        // remises en etat interrompues par un arret du serveur (mondes charges entre-temps)
        EldaniorLogger.SCHEDULER.schedule(com.eldanior.system.territory.ZoneSnapshots::resumePending, 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    /** Etat attendu : a vendre (prioritaire), a louer (sans locataire), occupe (proprietaire ou locataire), sinon neutre. */
    public static String stateOf(ParcelData p) {
        if (p == null) return STATE_NONE;
        boolean rented = p.isRented() && p.getRenterUUID() != null;
        if (p.isForSale() && p.isForRent() && !rented) return STATE_BOTH;
        if (p.isForSale() && !rented) return STATE_SALE;
        if (p.isForRent() && !rented) return STATE_RENT;
        if (rented || p.getOwnerUUID() != null) return STATE_TAKEN;
        return STATE_NONE;
    }

    /** Met chaque affiche liee a ce bien dans l'etat du bien (thread de son monde). */
    public static void refreshPosters(ParcelData p) {
        String state = stateOf(p);
        for (String key : p.getPancartes().split(";")) {
            if (key.isEmpty()) continue;
            var world = Universe.get().getWorld(world(key));
            if (world == null) continue;
            int[] c = coords(key);
            world.execute(() -> setState(world, c[0], c[1], c[2], state));
        }
    }

    /** Affiche deliee : retour a l'etat neutre. */
    static void resetPoster(String key) {
        var world = Universe.get().getWorld(world(key));
        if (world == null) return;
        int[] c = coords(key);
        world.execute(() -> setState(world, c[0], c[1], c[2], STATE_NONE));
    }

    private static void setState(com.hypixel.hytale.server.core.universe.world.World world, int x, int y, int z, String state) {
        try {
            BlockType type = com.eldanior.system.util.WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null || !isPancarte(type)) return;
            BlockType base = BlockType.getAssetMap().getAsset(POSTER);
            if (base == null) return;
            BlockType want = STATE_NONE.equals(state) ? base : base.getBlockForState(state);
            if (want == null || want.getId().equals(type.getId())) return;
            var chunks = world.getChunkStore();
            var section = chunks.getChunkSectionReferenceAtBlock(x, y, z);
            if (section == null || !section.isValid()) return;
            com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations.setBlockInteractionState(chunks, section, x, y, z, type, state, false);
        } catch (Exception e) {
            EldaniorLogger.error("[Pancarte] etat de l'affiche", e);
        }
    }

    // ===== Coffres du bien (2026-11-12 : « informer l'acheteur du nombre de coffres qu'il possedera ») =====

    /**
     * Nombre de coffres dans la zone (blocs dont l'id contient « chest » ou « coffer », hors coffres au tresor), -1 si la zone est
     * trop grande pour etre parcourue. Parcours par section de chunk : les sections sans coffre sont sautees d'un coup, un grand
     * coffre (2 cases) n'est compte qu'une fois (cases de remplissage ignorees). Les sections non chargees ne sont pas comptees
     * (et le resultat n'est alors pas garde en cache). Thread du monde de la zone.
     */
    public static int chests(ParcelData p, com.hypixel.hytale.server.core.universe.world.World world) {
        long now = System.currentTimeMillis();
        long[] cached = chestCache.get(p.getId());
        if (cached != null && now - cached[1] < CHEST_CACHE_MS) return (int) cached[0];
        if (world == null) return 0;
        int y0 = Math.max(0, p.getMinY()), y1 = Math.min(319, p.getMaxY());
        if (y1 < y0) return 0;
        int sx0 = p.getMinX() >> 5, sx1 = p.getMaxX() >> 5, sy0 = y0 >> 5, sy1 = y1 >> 5, sz0 = p.getMinZ() >> 5, sz1 = p.getMaxZ() >> 5;
        long sections = (long) (sx1 - sx0 + 1) * (sy1 - sy0 + 1) * (sz1 - sz0 + 1);
        if (sections > CHEST_SCAN_MAX_SECTIONS) { chestCache.put(p.getId(), new long[]{-1, now}); return -1; }
        var chunks = world.getChunkStore();
        var cstore = chunks.getStore();
        var bsType = com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection.getComponentType();
        int count = 0;
        boolean partial = false;
        for (int sx = sx0; sx <= sx1; sx++)
            for (int sz = sz0; sz <= sz1; sz++)
                for (int sy = sy0; sy <= sy1; sy++) {
                    var ref = chunks.getChunkSectionReferenceAtBlock(sx << 5, sy << 5, sz << 5);
                    var bs = ref != null && ref.isValid() ? cstore.getComponent(ref, bsType) : null;
                    if (bs == null) { partial = true; continue; }
                    if (bs.isSolidAir()) continue;
                    boolean any = false;
                    for (int id : bs.values()) if (isChestType(id)) { any = true; break; }
                    if (!any) continue;
                    int ax = Math.max(p.getMinX(), sx << 5), bx = Math.min(p.getMaxX(), (sx << 5) + 31);
                    int ay = Math.max(y0, sy << 5), by = Math.min(y1, (sy << 5) + 31);
                    int az = Math.max(p.getMinZ(), sz << 5), bz = Math.min(p.getMaxZ(), (sz << 5) + 31);
                    for (int x = ax; x <= bx; x++)
                        for (int z = az; z <= bz; z++) {
                            if (p.isCylinder() && !p.contains(x, y0, z)) continue;
                            for (int y = ay; y <= by; y++)
                                if (isChestType(bs.get(x, y, z)) && bs.getFiller(x, y, z) == 0) count++;
                        }
                }
        if (!partial) chestCache.put(p.getId(), new long[]{count, now});
        return count;
    }

    private static boolean isChestType(int id) {
        if (id <= 0) return false;
        return chestTypes.computeIfAbsent(id, i -> {
            BlockType t = BlockType.getAssetMap().getAsset(i);
            String n = t != null && t.getId() != null ? t.getId().toLowerCase() : "";
            return (n.contains("chest") || n.contains("coffer")) && !n.contains("treasure");
        });
    }

    /** Zone liee a cette pancarte, null sinon. */
    public static ParcelData parcelOf(String key) {
        if (key == null) return null;
        for (ParcelData p : ParcelManager.getAll()) if (p.hasPancarte(key)) return p;
        return null;
    }

    static boolean admin(PlayerRef pr) {
        return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
    }

    static UUID uuid(PlayerRef pr) {
        try { return pr != null ? UUIDExtractor.getUUID(pr) : null; } catch (Exception e) { return null; }
    }

    /** Proprietaire (joueur) de la zone, ou admin : voit la carte de gestion. */
    public static boolean manages(ParcelData p, PlayerRef pr) {
        if (p == null || pr == null) return false;
        UUID u = uuid(pr);
        return admin(pr) || (u != null && p.isOwner(u));
    }

    /** Distance horizontale de (x, z) au rectangle de la zone (0 dedans). */
    static double distance(ParcelData p, int x, int z) {
        double dx = Math.max(0, Math.max(p.getMinX() - x, x - p.getMaxX()));
        double dz = Math.max(0, Math.max(p.getMinZ() - z, z - p.getMaxZ()));
        return Math.hypot(dx, dz);
    }

    /** Zones proches que ce joueur peut lier (admins : tous les biens ; sinon ceux dont il est proprietaire), les plus proches d'abord. */
    static List<ParcelData> candidates(String key, PlayerRef pr) {
        int[] c = coords(key);
        String world = world(key);
        boolean admin = admin(pr);
        UUID u = uuid(pr);
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData p : ParcelManager.getAll()) {
            if (!world.equals(p.getWorld()) || !LINKABLE.contains(p.getType())) continue;
            if (!admin && (u == null || !p.isOwner(u))) continue;
            if (distance(p, c[0], c[2]) > LINK_RANGE) continue;
            out.add(p);
        }
        out.sort(Comparator.comparingDouble((ParcelData p) -> distance(p, c[0], c[2]))
                .thenComparingLong(p -> (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1)));
        return out;
    }

    static void link(String key, ParcelData p) {
        ParcelData old = parcelOf(key);
        if (old != null) old.removePancarte(key);
        p.addPancarte(key);
        ParcelManager.save();
        refreshPosters(p);
    }

    static void unlink(String key) {
        ParcelData p = parcelOf(key);
        if (p == null) return;
        p.removePancarte(key);
        ParcelManager.save();
        resetPoster(key);
    }

    // ============================================ achat et location ============================================

    private static PlayerLevelData data(Ref<EntityStore> ref, Store<EntityStore> store) {
        return store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
    }

    /** Preleve le montant, verse le net au proprietaire (ou a la ville) et la taxe a la hierarchie. Message d'erreur ou null. */
    private static String pay(Ref<EntityStore> ref, Store<EntityStore> store, ParcelData p, long amount, String reason) {
        PlayerLevelData d = data(ref, store);
        if (d == null) return "Données du joueur introuvables.";
        if (d.getMoney() < amount) return "Pas assez d'or : il faut " + money(amount) + " (vous avez " + money(d.getMoney()) + ").";
        if (amount > 0) {
            d.removeMoney(amount);
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), d);
            long[] t = ParcelEconomyManager.calculateTax(amount);
            ProprietesTab.payOwnerOrCity(p, t[0], reason);
            ParcelEconomyManager.distributeTax(p.getId(), t[1]);
        }
        return null;
    }

    public static String buy(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef pr, ParcelData p) {
        UUID u = uuid(pr);
        if (u == null) return "Joueur introuvable.";
        if (!p.isForSale()) return "Ce bien n'est pas à vendre.";
        if (p.isOwner(u)) return "Ce bien est déjà à vous.";
        if (p.isRented() && p.getRenterUUID() != null && !p.getRenterUUID().equals(u)) return "Ce bien est loué : il ne peut pas être vendu.";
        if (p.getType() == ParcelType.CITY) {
            PlayerLevelData d = data(ref, store);
            var rank = d != null ? com.eldanior.system.titles.nobility.NobilityRank.fromString(d.getNobilityRank()) : null;
            if (rank == null || rank.ordinal() < com.eldanior.system.titles.nobility.NobilityRank.COMTE.ordinal()
                    || !com.eldanior.system.territory.LandOffice.cityLeader(u, d))
                return com.eldanior.system.territory.LandOffice.CITY_RULE;
        }
        String err = pay(ref, store, p, p.getPrice(), "vente de " + p.getName());
        if (err != null) return err;
        if (!ParcelManager.buyParcel(p.getId(), u, pr.getUsername())) return "L'achat a échoué.";
        if (p.getType() == ParcelType.CITY) {
            PlayerLevelData d = data(ref, store);
            if (d != null) { p.setGuildId(d.getGuildId()); ParcelManager.save(); }
        }
        return null;
    }

    public static String rent(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef pr, ParcelData p) {
        UUID u = uuid(pr);
        if (u == null) return "Joueur introuvable.";
        if (!p.isForRent() || p.getRenterUUID() != null) return "Ce bien n'est pas à louer.";
        if (p.isOwner(u)) return "Vous êtes le propriétaire de ce bien.";
        String err = pay(ref, store, p, p.getRentPrice(), "location de " + p.getName());
        if (err != null) return err;
        return ParcelManager.rentParcel(p.getId(), u, pr.getUsername()) ? null : "La location a échoué.";
    }

    public static String renew(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef pr, ParcelData p) {
        UUID u = uuid(pr);
        if (u == null || !p.isRented() || !u.equals(p.getRenterUUID())) return "Vous ne louez pas ce bien.";
        if (p.getRentPrice() <= 0) return "Ce bien n'a pas de loyer.";
        if (p.getEvictionAt() > 0) return "Le propriétaire vous a donné congé : le bail ne peut plus être prolongé.";
        String err = pay(ref, store, p, p.getRentPrice(), "prolongation de location");
        if (err != null) return err;
        ParcelManager.renewRent(p.getId());
        return null;
    }

    public static String quit(PlayerRef pr, ParcelData p) {
        UUID u = uuid(pr);
        if (u == null || !p.isRented() || !u.equals(p.getRenterUUID())) return "Vous ne louez pas ce bien.";
        ParcelManager.quitRental(p.getId());
        return null;
    }

    // ============================================ preavis d'expulsion (2026-11-17) ============================================

    /** Delai du preavis donne par le proprietaire a son locataire. */
    static final long NOTICE_MS = 2 * DAY_MS;

    public static boolean rented(ParcelData p) { return p != null && p.isRented() && p.getRenterUUID() != null; }

    /** Le joueur loue-t-il ce bien ? */
    public static boolean isRenter(ParcelData p, UUID u) { return u != null && rented(p) && u.equals(p.getRenterUUID()); }

    /** Proprietaire : donne congé au locataire, qui doit partir dans 2 jours (avant la fin du bail si elle vient plus tot). */
    public static String notice(ParcelData p) {
        if (!rented(p)) return "Ce bien n'a pas de locataire.";
        if (p.getEvictionAt() > 0) return "Un préavis court déjà : départ dans " + remaining(p.getEvictionAt()) + ".";
        p.setEvictionAt(System.currentTimeMillis() + NOTICE_MS);
        ParcelManager.save();
        PlayerRef renter = Universe.get().getPlayer(p.getRenterUUID());
        if (renter != null) renter.sendMessage(com.hypixel.hytale.server.core.Message.raw("Le propriétaire de « " + p.getName()
                + " » vous donne congé : vous devez partir dans 2 jours. Videz vos coffres : ce que vous avez posé sera retiré."));
        return null;
    }

    public static String cancelNotice(ParcelData p) {
        if (p.getEvictionAt() <= 0) return "Aucun préavis en cours.";
        p.setEvictionAt(0);
        ParcelManager.save();
        PlayerRef renter = rented(p) ? Universe.get().getPlayer(p.getRenterUUID()) : null;
        if (renter != null) renter.sendMessage(com.hypixel.hytale.server.core.Message.raw("Le préavis pour « " + p.getName() + " » est annulé."));
        return null;
    }

    /** Preavis echus : fin de la location (remise en etat comprise). */
    private static void checkNotices() {
        long now = System.currentTimeMillis();
        for (ParcelData p : ParcelManager.getAll()) {
            if (p.getEvictionAt() <= 0 || now < p.getEvictionAt()) continue;
            if (!rented(p)) { p.setEvictionAt(0); ParcelManager.save(); continue; }
            PlayerRef renter = Universe.get().getPlayer(p.getRenterUUID());
            ParcelManager.quitRental(p.getId());
            if (renter != null) renter.sendMessage(com.hypixel.hytale.server.core.Message.raw("Votre préavis est échu : vous ne louez plus « " + p.getName() + " »."));
            EldaniorLogger.info("[Pancarte] preavis echu : " + p.getName());
        }
    }

    // ============================================ gestion (proprietaire) ============================================

    /**
     * Prix et loyer, mise en vente et/ou en location, protection. Vente et location peuvent aller ensemble (2026-11-17) : tant
     * qu'un locataire occupe le bien, il ne peut pas etre achete (buy).
     */
    public static String saveSettings(ParcelData p, long price, long rent, boolean sale, boolean forRent, boolean protect) {
        if (price < 0 || rent < 0) return "Les montants doivent être positifs.";
        if (sale && price <= 0) return "Fixez un prix de vente avant de mettre le bien en vente.";
        if (forRent && rent <= 0) return "Fixez un loyer avant de mettre le bien en location.";
        if (forRent && p.isRented()) forRent = false;                         // deja loue : la remise en location viendra au depart
        p.setPrice(price);
        p.setRentPrice(rent);
        p.setForSale(sale);
        p.setForRent(forRent);
        p.setProtectedByDefault(protect);
        if (p.getOwnerUUID() != null && p.getRole(p.getOwnerUUID()) == null) p.addMember(p.getOwnerUUID(), ParcelRole.OWNER);
        ParcelManager.save();
        return null;
    }

    /**
     * Qui gere les acces (2026-11-17) : le locataire pendant la location, le proprietaire sinon (un proprietaire dont le bien
     * est loue n'invite personne : il peut seulement donner congé). Les admins toujours.
     */
    public static boolean managesAccess(ParcelData p, PlayerRef pr) {
        UUID u = uuid(pr);
        if (isRenter(p, u)) return true;
        if (admin(pr)) return true;
        return u != null && p.isOwner(u) && !rented(p);
    }

    static String invite(ParcelData p, String name) {
        if (name == null || name.isBlank()) return "Indiquez le nom d'un joueur connecté.";
        PlayerRef target = Universe.get().getPlayerByUsername(name.trim(), com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
        UUID t = uuid(target);
        if (t == null) return "Aucun joueur connecté ne s'appelle « " + name.trim() + " ».";
        if (p.isMember(t)) return target.getUsername() + " a déjà accès à ce bien.";
        p.addMember(t, ParcelRole.MEMBER);
        ParcelManager.save();
        target.sendMessage(com.hypixel.hytale.server.core.Message.raw("Vous avez accès à « " + p.getName() + " »."));
        return null;
    }

    static String kick(ParcelData p, UUID member) {
        if (member == null || !p.isMember(member)) return "Ce joueur n'a pas accès à ce bien.";
        if (p.isOwner(member)) return "Le propriétaire ne peut pas être retiré.";
        if (member.equals(p.getRenterUUID())) return "C'est le locataire : utilisez « Expulser le locataire ».";
        p.removeMember(member);
        ParcelManager.save();
        return null;
    }

    static String evict(ParcelData p) {
        if (!p.isRented() || p.getRenterUUID() == null) return "Ce bien n'a pas de locataire.";
        ParcelManager.quitRental(p.getId());
        return null;
    }

    // ============================================ textes ============================================

    public static String money(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ') + " or";
    }

    public static String name(UUID u, String fallback) {
        if (u == null) return fallback;
        PlayerRef r = Universe.get().getPlayer(u);
        return r != null ? r.getUsername() : fallback;
    }

    public static String remaining(long end) {
        long left = end - System.currentTimeMillis();
        if (left <= 0) return "échu";
        long d = left / DAY_MS, h = (left % DAY_MS) / (60L * 60 * 1000);
        return d > 0 ? d + " j " + h + " h" : h + " h";
    }
}
