package com.eldanior.system.guards;

import com.eldanior.system.territory.ParcelData;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.luo.LuoPlaques;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRole;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Random;

/**
 * Marchand du Clan Luo (GuardType.MARCHAND) : une boutique et un rang par poste (Post.shop = "BOUTIQUE:RANG"), boutiques
 * Luo_<Boutique>_<Rang> et Luo_Comptoir generees par scripts/guards/gen_marchand_luo.py.
 *
 * A l'interaction (NpcInteractSystem, service Commercer de NpcServices), la boutique ouverte depend du client :
 *   - membre du clan : celle du marchand, si son rang le permet (Apprenti : tout le clan ; Negociant : Nobles et au-dessus ;
 *     Grand Marchand : Seigneur, Doyens, Patriarche) ;
 *   - marchand agree (plaque, FamilyManager.merchants) : le Comptoir de la Porte du Dragon (marchandises neutres) ;
 *   - etranger (2026-11-16) : le laissez-passer, PASS_SHOP (plaque Visiteur valable 2 jours) ;
 *   - admin : celle du marchand ; tout autre : refus.
 */
public final class LuoMerchants {

    private LuoMerchants() {}

    public enum Shop {
        ARMURERIE("armurerie", "Armurerie", "Armurier"),
        TAILLEUR("tailleur", "Tailleur", "Tailleur"),
        HERBORISTE("herboriste", "Herboriste", "Herboriste"),
        MAISON_THE("maison_the", "Maison_The", "Maître de thé");

        final String arg, asset, trade;
        Shop(String arg, String asset, String trade) { this.arg = arg; this.asset = asset; this.trade = trade; }
        public String getLabel() { return trade; }
    }

    public enum Rank {
        APPRENTI("apprenti", "Apprenti", "Apprenti", FamilyRole.MEMBER, "tout le clan"),
        NEGOCIANT("negociant", "Negociant", "Négociant", FamilyRole.NOBLE, "les Nobles du clan et leurs aînés"),
        MAITRE("grand", "Maitre", "Grand Marchand", FamilyRole.VICE, "le Seigneur, les Doyens et le Patriarche");

        final String arg, asset, label, audience;
        final FamilyRole minRole;
        Rank(String arg, String asset, String label, FamilyRole minRole, String audience) {
            this.arg = arg; this.asset = asset; this.label = label; this.minRole = minRole; this.audience = audience;
        }
        public String getLabel() { return label; }
    }

    public static final String COMPTOIR = "Luo_Comptoir";
    /** Laissez-passer (2026-11-16) : ce qu'un marchand Luo vend aux etrangers (plaque Visiteur, 2 jours ; luo.LuoPlaques). */
    public static final String PASS_SHOP = "Luo_Laissez_Passer";
    /** Marchand ambulant : un assortiment par poste parmi Eldanior_Ambulant_1..N (scripts/guards/gen_marchand_luo.py). */
    static final int WANDERER_SHOPS = 3;
    /** Niveaux de joueur a partir desquels le marchand ambulant propose l'assortiment B1, B2... (scripts/guards/gen_marchand_luo.py). */
    static final int[] WANDERER_BRACKETS = {0, 30, 100, 250, 450};

    public static Shop shopArg(String s) {
        if (s == null) return null;
        for (Shop v : Shop.values()) if (v.arg.equalsIgnoreCase(s) || v.name().equalsIgnoreCase(s)) return v;
        return null;
    }

    public static Rank rankArg(String s) {
        if (s == null) return null;
        if ("maitre".equalsIgnoreCase(s) || "grand_marchand".equalsIgnoreCase(s)) return Rank.MAITRE;
        for (Rank v : Rank.values()) if (v.arg.equalsIgnoreCase(s) || v.name().equalsIgnoreCase(s)) return v;
        return null;
    }

    /** Valeur de Post.shop : boutique et rang imposes, sinon tires au hasard (Grand Marchand plus rare). */
    static String draw(Shop shop, Rank rank, Random rng) {
        Shop s = shop != null ? shop : Shop.values()[rng.nextInt(Shop.values().length)];
        Rank r = rank != null ? rank : rng.nextInt(10) < 5 ? Rank.APPRENTI : rng.nextInt(5) < 4 ? Rank.NEGOCIANT : Rank.MAITRE;
        return s.name() + ":" + r.name();
    }

    static Shop shopOf(GuardManager.Post p) {
        try { return Shop.valueOf(p.shop.split(":")[0]); } catch (Exception e) { return Shop.ARMURERIE; }
    }

    static Rank rankOf(GuardManager.Post p) {
        try { return Rank.valueOf(p.shop.split(":")[1]); } catch (Exception e) { return Rank.APPRENTI; }
    }

    static String assetOf(GuardManager.Post p) {
        return "Luo_" + shopOf(p).asset + "_" + rankOf(p).asset;
    }

    /** « Négociant Haruki - Tailleur » ; ambulant : « Marchand ambulant Aldric » */
    static String nameplate(GuardManager.Post p) {
        if (p.type == GuardType.AMBULANT) return "Marchand ambulant " + (p.luo ? LuoNpcs.givenName(p) : GuardManager.firstName(p));
        return rankOf(p).label + " " + LuoNpcs.givenName(p) + " - " + shopOf(p).trade;
    }

    /** Un joueur parle au marchand : ouvre la boutique qui lui revient, ou explique le refus. Thread du monde. */
    static void open(GuardManager.Post p, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        Player player = store.getComponent(playerRef, Player.getComponentType());
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (LawGuards.closesAtCurfew(p)) {                                                       // couvre-feu (2026-11-18) : boutique fermee
            String town = com.eldanior.system.territory.laws.DomainLawSystem.curfewAt(p.getWorld(), p.position(), store);
            if (town != null) {
                pr.sendMessage(Message.raw(GuardManager.givenName(p) + " : « C'est le couvre-feu à " + town + ". Ma boutique est fermée, "
                        + "revenez au matin. »"));
                return;
            }
        }
        if (p.type == GuardType.SENSEI) {                                                       // techniques de la famille
            Sensei.openShop(p, playerRef, store);
            return;
        }
        if (p.type == GuardType.MAITRE_GUILDE) {                                                // comptoir de la guilde (2026-11-25)
            try {
                player.getPageManager().openCustomPage(playerRef, store, new ShopPage(pr, GUILD_SHOP,
                        GuardManager.nameplate(p) + " — comptoir de la guilde", GUILD_THEME, GUILD_BUYS).allLevels()
                        .pricing(id -> id.startsWith("Butin_") ? ShopPage.buyback(id) * GUILD_LOOT_RATE : ShopPage.buyback(id)));
            } catch (Exception e) {
                EldaniorLogger.error("[Guilde] comptoir " + p.id, e);
            }
            return;
        }
        if (p.type == GuardType.LEGENDAIRE) {                                                   // forge du fer divin : materiaux + pieces
            LegendarySmiths.Smith smith = LegendarySmiths.of(p);
            try {
                player.getPageManager().openCustomPage(playerRef, store, new ShopPage(pr, smith.shop(),
                        "Forge légendaire — " + smith.getLabel(), smith.theme, LegendarySmiths.BUYS_NOTHING).allLevels());
            } catch (Exception e) {
                EldaniorLogger.error("[Forge] " + p.id, e);
            }
            return;
        }
        NpcTrades.Metier metier = NpcTrades.metier(p);
        if (metier == NpcTrades.Metier.BANQUIER) {                                               // banque : depots, retraits, change
            try {
                player.getPageManager().openCustomPage(playerRef, store, new BankPage(pr, GuardManager.nameplate(p),
                        "Dépôts, retraits et change des pièces", metier.theme));
            } catch (Exception e) {
                EldaniorLogger.error("[Banque] " + p.id, e);
            }
            return;
        }
        if (metier == NpcTrades.Metier.IMMOBILIER) {                                             // bureau des terres (2026-11-13)
            try {
                player.getPageManager().openCustomPage(playerRef, store, new LandOfficeScreen(pr, GuardManager.nameplate(p), placeOf(p),
                        p.getWorld(), null, 0));
            } catch (Exception e) {
                EldaniorLogger.error("[Immobilier] " + p.id, e);
            }
            return;
        }
        if (metier == NpcTrades.Metier.INTENDANT) {                                              // registre de la maison (refait le 2026-11-18)
            // tout membre de la maison l'ouvre (gestes selon son rang) ; un etranger y voit les quetes de la maison et, Comte ou plus,
            // les terres a acquerir ; un noble sans maison peut y prendre la tete d'une maison libre
            if (!IntendantPage.hasAccess(stewardFamily(p, data), data, pr)) {
                pr.sendMessage(Message.raw(GuardManager.givenName(p) + " : « Je tiens le registre de la maison. Ses quêtes sont affichées "
                        + "au tableau des quêtes. »"));
                return;
            }
            try {
                player.getPageManager().openCustomPage(playerRef, store, new IntendantPage(pr, stewardFamily(p, data), GuardManager.nameplate(p)));
            } catch (Exception e) {
                EldaniorLogger.error("[Intendant] " + p.id, e);
            }
            return;
        }
        if (metier == NpcTrades.Metier.BAILLI) {                                                 // registre de la ville (2026-11-19)
            ParcelData city = CityStewards.cityOf(p);
            if (city != null && IntendantPage.canOpenCity(city, pr.getUuid(), pr))
                player.getPageManager().openCustomPage(playerRef, store, IntendantPage.forCity(pr, city, GuardManager.nameplate(p)));
            else pr.sendMessage(Message.raw(GuardManager.givenName(p) + " : « " + CityStewards.greeting(city) + " »"));
            return;
        }
        if (metier == NpcTrades.Metier.PASSEUR) {                                                // Portail Continental (2026-11-19)
            com.eldanior.system.territory.teleport.ContinentalGates.talk(p.getWorld(), p.position(), playerRef, store, pr, GuardManager.givenName(p));
            return;
        }
        if (metier == NpcTrades.Metier.MAROQUINIER) {                                            // tout son stock, quel que soit le niveau
            try {
                player.getPageManager().openCustomPage(playerRef, store, new ShopPage(pr, NpcTrades.shopOf(p),
                        GuardManager.nameplate(p) + " — " + metier.pitch, metier.theme, metier.buys()).allLevels());
            } catch (Exception e) {
                EldaniorLogger.error("[Maroquinier] " + p.id, e);
            }
            return;
        }
        if (metier != null) {                                                                    // commercant : ouvert a tous
            show(player, pr, playerRef, store, NpcTrades.shopOf(p), GuardManager.nameplate(p) + " — " + metier.pitch, metier.theme, metier.buys());
            return;
        }
        if (wanderer(p)) {                                                                       // ouvert a tous, assortiment de son niveau
            int level = data != null ? data.getLevel() : 1;
            int bracket = 1;
            for (int i = 1; i < WANDERER_BRACKETS.length; i++) if (level >= WANDERER_BRACKETS[i]) bracket = i + 1;
            show(player, pr, playerRef, store, "Eldanior_Ambulant_B" + bracket + "_" + (1 + (int) Math.floorMod(p.skinSeed, (long) WANDERER_SHOPS)),
                    GuardManager.nameplate(p) + " — un peu de tout", "#a8743a", ShopPage.ALL);
            return;
        }
        boolean admin = pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        boolean member = data != null && com.eldanior.system.luo.LuoClans.isEmpire(data.getNobleFamilyId());   // Luo et ses clans
        Rank rank = rankOf(p);
        String speaker = LuoNpcs.givenName(p) + " : ";
        String shopId;
        if (member && !admin) {
            FamilyRole role = FamilyRole.of(data.getStatus());
            if (role == null || role.ordinal() < rank.minRole.ordinal()) {
                pr.sendMessage(Message.raw(speaker + "« Pardonnez-moi, ma boutique ne sert que " + rank.audience + ". »"));
                return;
            }
            shopId = assetOf(p);
        } else if (admin) {
            shopId = assetOf(p);
        } else if (FamilyManager.isMerchant(LuoPlaques.FAMILY, UUIDExtractor.getUUID(pr))) {
            shopId = COMPTOIR;
        } else {                                                                                 // etranger : le laissez-passer seulement
            pr.sendMessage(Message.raw(speaker + "« Le Clan Luo ne commerce pas avec les étrangers. Je peux seulement vous vendre un "
                    + "laissez-passer : deux jours sur nos terres sans que la garde ne vous chasse. »"));
            shopId = PASS_SHOP;
        }
        boolean comptoir = COMPTOIR.equals(shopId);
        if (PASS_SHOP.equals(shopId)) {
            show(player, pr, playerRef, store, shopId, GuardManager.nameplate(p) + " — laissez-passer du Clan Luo", "#c43c2c", null);
            return;
        }
        show(player, pr, playerRef, store, shopId, comptoir ? "Comptoir de la Porte du Dragon"
                : GuardManager.nameplate(p) + " — " + shopOf(p).getLabel() + " du Clan Luo (" + rank.getLabel() + ")",
                comptoir ? "#3f9f7f" : rank == Rank.MAITRE ? "#f1c40f" : "#c9a24a", null);
    }

    /** Assortiment du marchand ambulant : son type, ou un autre PNJ dont le poste vend « AMBULANT ». */
    static boolean wanderer(GuardManager.Post p) {
        return p.type == GuardType.AMBULANT || AMBULANT.equals(p.shop);
    }

    /** Ce poste fait-il commerce ? (marchands, ou tout PNJ civil a qui l'on a donne une boutique) */
    static boolean sells(GuardManager.Post p) {
        return p.type == GuardType.MARCHAND || p.type == GuardType.MAITRE_GUILDE || p.type == GuardType.SENSEI || wanderer(p) || p.shop != null && !p.shop.isEmpty();
    }

    static final String AMBULANT = "AMBULANT";

    /**
     * Comptoir du maitre de guilde (2026-11-25) : BarterShop scripts/guards/gen_maitre_guilde.py (parchemins de relance, failles
     * scellees, cle de donjon, pilules, tomes). Il rachete tout ce que les marchands rachetent (ShopPage.sellable) et paie les
     * butins de monstres GUILD_LOOT_RATE fois le bareme.
     */
    static final String GUILD_SHOP = "Eldanior_Maitre_Guilde", GUILD_THEME = "#c9a24a";
    static final long GUILD_LOOT_RATE = 2;
    static final ShopPage.Buys GUILD_BUYS = new ShopPage.Buys("tout, et les butins de monstres au double", id -> true);

    /**
     * Intendant (a venir : gestion de la famille et reserve a stock limite, payee par la tresorerie) : seuls le Patriarche,
     * les Doyens et le Seigneur de la famille qui possede le territoire du poste s'en servent ; les admins aussi.
     * Hors des terres d'une famille, personne (sauf admin).
     */
    /** Maison du domaine de l'intendant : proprietaire de la parcelle, sinon famille du poste, sinon celle du joueur. */
    /** Maison sans Patriarche, et visiteur sans famille d'un rang suffisant (Duc, Marquis, Roi selon la maison). */
    /**
     * Terres que l'intendant de ce poste peut vendre (2026-11-13) : bureau des terres limite aux domaines de sa maison (tout le
     * royaume pour la maison royale), ouvert sur l'onglet Terres.
     */
    static void openLands(GuardManager.Post p, Player player, PlayerRef pr, Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {
        try {
            java.util.List<String> scope = new java.util.ArrayList<>();
            for (var d : IntendantPage.familyDomains(stewardFamily(p, data))) scope.add(d.getId());
            player.getPageManager().openCustomPage(playerRef, store, new LandOfficeScreen(pr, GuardManager.nameplate(p), placeOf(p),
                    p.getWorld(), scope, LandOfficeScreen.TAB_LANDS));
        } catch (Exception e) {
            EldaniorLogger.error("[Intendant] terres " + p.id, e);
        }
    }

    private static String placeOf(GuardManager.Post p) {
        try {
            var parcel = com.eldanior.system.territory.ParcelManager.getParcelAt(p.getWorld(), p.x, p.y, p.z);
            return parcel != null ? com.eldanior.system.territory.LandOffice.name(parcel) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Maison libre que ce noble peut prendre chez son intendant : depuis le 2026-11-13, seulement un Duche (les Marquisats
     * s'achetent depuis l'onglet Famille, le Roi tient la maison royale d'office).
     */
    static boolean claimable(String familyId, PlayerLevelData data) {
        if (familyId == null || data == null || (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty())) return false;
        var family = FamilyManager.get(familyId);
        if (family == null || FamilyManager.isFamilyTaken(familyId)) return false;
        if (family.getMinimumRank() != com.eldanior.system.titles.nobility.NobilityRank.DUC) return false;
        var rank = com.eldanior.system.titles.nobility.NobilityRank.fromString(data.getNobilityRank());
        return rank != null && rank.ordinal() >= family.getMinimumRank().ordinal();
    }

    static String stewardFamily(GuardManager.Post p, PlayerLevelData data) {
        try {
            var parcel = com.eldanior.system.territory.ParcelManager.getParcelAt(p.getWorld(), p.x, p.y, p.z);
            String owner = parcel != null ? parcel.getFamilyId() : null;
            if (owner != null && !owner.isEmpty()) return FamilyManager.migrateId(owner);
        } catch (Exception ignored) { }
        String post = GuardManager.familyIdOf(p);
        if (post != null) return post;
        return data != null && data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty() ? FamilyManager.migrateId(data.getNobleFamilyId()) : null;
    }

    private static void show(Player player, PlayerRef pr, Ref<EntityStore> playerRef, Store<EntityStore> store, String shopId, String title,
                             String theme, ShopPage.Buys buys) {
        try {
            player.getPageManager().openCustomPage(playerRef, store, new ShopPage(pr, shopId, title, theme, buys));
        } catch (Exception e) {
            EldaniorLogger.error("[Luo] boutique " + shopId, e);
        }
    }
}
