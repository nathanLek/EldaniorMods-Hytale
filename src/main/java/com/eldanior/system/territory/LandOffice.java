package com.eldanior.system.territory;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRegistry;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Terres a acquerir (2026-11-13, demande de l'utilisateur : « les Ducs et Comtes peuvent acheter un Territoire ou une ville depuis
 * l'intendant, il ne faut pas que ce soit aleatoire ») : le joueur choisit lui-meme dans la liste.
 *  - Territoire (Duc ou plus) : un Duche dont la maison n'a pas de Patriarche (l'acheteur, sans maison, en devient le Patriarche,
 *    FamilleTab.claim), ou un territoire sans maison ni maitre (l'acheteur en devient le maitre).
 *  - Ville (Comte ou plus) : une ville sans maison ni maitre.
 * Le prix va au tresor de la zone qui contient la terre vendue (le Marquisat d'un Duche, par exemple).
 * Marquisats : onglet Famille du menu systeme (Marquis) ; Royaume : le Roi (RoyalHouse).
 * Revente par une maison (2026-11-18) : le Patriarche met en vente une ville ou un territoire de ses domaines (pas sa terre
 * principale) depuis l'intendant ; l'acheteur en devient le maitre (il rejoint sa maison s'il en a une) et le prix va aux
 * coffres de la maison qui vendait.
 */
public final class LandOffice {

    private LandOffice() {}

    /** Prix d'un territoire sans maison et d'une ville, si la zone n'a pas de prix fixe (ParcelData.price). */
    public static final long TERRITORY_PRICE = 30_000_000L;

    /** Maison sans Patriarche a qui appartient ce territoire (rang Duc), null sinon. */
    public static NobleFamilyModel freeHouse(ParcelData p) {
        String fid = p.getFamilyId();
        if (fid == null || fid.isEmpty()) return null;
        NobleFamilyModel f = FamilyManager.get(FamilyManager.migrateId(fid));
        if (f == null || f.getMinimumRank() != NobilityRank.DUC || FamilyManager.isFamilyTaken(f.getId())) return null;
        ParcelData main = ParcelManager.getFamilyParcel(f.getId());
        return main != null && main.getId().equals(p.getId()) ? f : null;   // la terre principale de la maison
    }

    /**
     * Ville mise en vente par son maitre (2026-11-19, Tresor du registre de la ville) : le prix va au vendeur, pas a une maison
     * ni au tresor d'une zone. Passe avant les ventes par une maison.
     */
    public static boolean ownerSale(ParcelData p) {
        return p != null && p.getType() == ParcelType.CITY && p.getOwnerUUID() != null && p.isForSale() && p.getPrice() > 0;
    }

    /** Ville ou territoire mis en vente par sa maison (2026-11-18), jamais sa terre principale. */
    public static boolean houseSale(ParcelData p) {
        if (p == null || !p.isForSale() || p.getPrice() <= 0 || ownerSale(p)) return false;
        if (p.getType() != ParcelType.CITY && p.getType() != ParcelType.TERRITORY) return false;
        String fid = p.getFamilyId();
        if (fid == null || fid.isEmpty()) return false;
        ParcelData main = ParcelManager.getFamilyParcel(FamilyManager.migrateId(fid));
        return main == null || !main.getId().equals(p.getId());
    }

    /**
     * Ville d'une maison sans maitre (2026-11-18, retour de l'utilisateur : « dans le clan Luo j'ai cree des villes et je ne les
     * vois pas dans Terres a acquerir ») : a vendre d'office, sauf si le Patriarche l'a retiree du marche (ParcelData.reserved).
     * Prix aux coffres de la maison ; un acheteur de la maison la garde dans la maison.
     */
    public static boolean houseCity(ParcelData p) {
        if (p == null || p.getType() != ParcelType.CITY || p.getOwnerUUID() != null || p.isReserved() || p.isForSale()) return false;
        String fid = p.getFamilyId();
        if (fid == null || fid.isEmpty()) return false;
        ParcelData main = ParcelManager.getFamilyParcel(FamilyManager.migrateId(fid));
        return main == null || !main.getId().equals(p.getId());
    }

    /** Au marche des terres par sa maison : mise en vente, ou ville sans maitre non retiree. */
    public static boolean onMarket(ParcelData p) { return houseSale(p) || houseCity(p); }

    /** Peut-on mettre ce domaine en vente (ville ou territoire de la maison, pas sa terre principale) ? */
    public static boolean sellable(ParcelData p) {
        if (p == null || (p.getType() != ParcelType.CITY && p.getType() != ParcelType.TERRITORY)) return false;
        String fid = p.getFamilyId();
        if (fid == null || fid.isEmpty()) return false;
        ParcelData main = ParcelManager.getFamilyParcel(FamilyManager.migrateId(fid));
        return main == null || !main.getId().equals(p.getId());
    }

    /** Terre a vendre : Territoire (Duche libre ou sans maison) ou Ville sans maison, sans maitre ; ou mise en vente par sa maison. */
    public static boolean available(ParcelData p) {
        if (ownerSale(p) || houseSale(p) || houseCity(p)) return true;
        if (p == null || p.getOwnerUUID() != null) return false;
        boolean noHouse = p.getFamilyId() == null || p.getFamilyId().isEmpty();
        return switch (p.getType()) {
            case TERRITORY -> noHouse || freeHouse(p) != null;
            case CITY -> noHouse;
            default -> false;
        };
    }

    public static long price(ParcelData p) {
        if (freeHouse(p) != null) return ParcelManager.getFamilyTerritoryPrice(freeHouse(p).getId());
        if (p.getPrice() > 0) return p.getPrice();
        return p.getType() == ParcelType.CITY ? ParcelManager.CITY_PRICE : TERRITORY_PRICE;
    }

    /** Rang requis : Duc pour un territoire, Comte pour une ville. */
    public static NobilityRank required(ParcelData p) {
        return p.getType() == ParcelType.TERRITORY ? NobilityRank.DUC : NobilityRank.COMTE;
    }

    /** Terres a vendre dans le monde world, dans les zones scope ou sous elles (scope null : toutes). */
    public static List<ParcelData> lands(Collection<String> scope, String world) {
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData p : ParcelManager.getAll()) {
            if (!available(p) || (world != null && !world.equals(p.getWorld())) || !within(p, scope)) continue;
            out.add(p);
        }
        out.sort(java.util.Comparator.comparing((ParcelData p) -> p.getType().ordinal())
                .thenComparing(p -> p.getName() != null ? p.getName() : p.getId(), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    /** La zone est-elle l'une de scope, ou dedans (scope null : oui) ? */
    public static boolean within(ParcelData p, Collection<String> scope) {
        if (scope == null) return true;
        Set<String> seen = new HashSet<>();
        for (String id = p.getId(); id != null && seen.add(id); ) {
            if (scope.contains(id)) return true;
            ParcelData parent = ParcelManager.get(id);
            id = parent != null ? parent.getParentId() : null;
        }
        return false;
    }

    /**
     * Ville (2026-11-13, regle de l'utilisateur) : Comte au moins, et chef de guilde ou Capitaine d'un Ordre de chevalerie d'une
     * famille. Aussi pour l'achat par l'affiche (Pancartes.buy).
     */
    public static boolean cityLeader(UUID me, PlayerLevelData d) {
        if (d == null) return false;
        if (d.hasGuild() && "CHEF".equalsIgnoreCase(d.getGuildRole())) return true;
        var order = me != null ? com.eldanior.system.titles.nobility.family.KnightOrderManager.getPlayerOrder(me) : null;
        return order != null && me.equals(order.getCapitaineUUID());
    }

    public static final String CITY_RULE = "Une ville ne s'achète que par un Comte (ou plus), chef de guilde ou Capitaine d'un Ordre de chevalerie.";

    /** Pourquoi ce joueur ne peut pas acheter cette terre (null : il peut). */
    public static String refusal(ParcelData p, PlayerLevelData d, UUID me) {
        if (p == null || !available(p)) return "Cette terre n'est plus à vendre.";
        if (d == null) return "Données introuvables.";
        if (me != null && me.equals(p.getOwnerUUID())) return "Cette ville est déjà à vous.";
        NobilityRank rank = NobilityRank.fromString(d.getNobilityRank());
        NobilityRank need = required(p);
        if (rank == null || rank.ordinal() < need.ordinal())
            return "Il faut être au moins " + need.getDisplayName() + " pour acheter " + (p.getType() == ParcelType.CITY ? "une ville." : "un territoire.");
        if (p.getType() == ParcelType.CITY && !cityLeader(me, d) && !admin(me)) return CITY_RULE;
        boolean house = d.getNobleFamilyId() != null && !d.getNobleFamilyId().isEmpty();
        if (freeHouse(p) != null && house) return "Vous appartenez déjà à une maison : un Duché libre se prend sans maison.";
        if (houseSale(p) && !houseCity(p) && house && FamilyManager.migrateId(d.getNobleFamilyId()).equals(FamilyManager.migrateId(p.getFamilyId())))
            return "Ce domaine appartient déjà à votre maison.";
        long cost = price(p), purse = purse(me);
        if (d.getMoney() + purse < cost)
            return "Il faut " + gold(cost) + " or (pièces et compte réunis ; vous avez " + gold(d.getMoney() + purse) + ").";
        return null;
    }

    /** Pieces portees par ce joueur s'il est connecte (2026-11-19 : on paie aussi avec ses pieces, pas seulement le compte). */
    static long purse(UUID me) {
        var pl = online(me);
        return pl != null ? com.eldanior.system.shop.CoinWallet.value(pl) : 0;
    }

    private static com.hypixel.hytale.server.core.entity.entities.Player online(UUID me) {
        var pr = me != null ? com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(me) : null;
        var r = pr != null ? pr.getReference() : null;
        return r != null && r.isValid() ? r.getStore().getComponent(r, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()) : null;
    }

    private static boolean admin(UUID me) {
        var pr = me != null ? com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(me) : null;
        return pr != null && pr.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION);
    }

    /** Achat : null si c'est fait, sinon le refus. Thread du monde du joueur. */
    public static String buy(Ref<EntityStore> ref, Store<EntityStore> store, ParcelData p) {
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData d = store.getComponent(ref, type);
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        UUID me;
        try { me = pr != null ? UUIDExtractor.getUUID(pr) : null; } catch (Exception e) { me = null; }
        if (me == null) return "Joueur introuvable.";
        String refused = refusal(p, d, me);
        if (refused != null) return refused;
        long cost = price(p);
        NobleFamilyModel house = freeHouse(p);
        if (ownerSale(p)) {                                                       // vendue par son maitre (2026-11-19)
            UUID seller = p.getOwnerUUID();
            String sellerName = p.getOwnerName();
            PlayerLevelData copy = (PlayerLevelData) d.clone();
            if (copy == null) return "Données introuvables.";
            if (!com.eldanior.system.quest.chapitres.Primes.payWithBank(store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()), copy, cost))
                return "Il faut " + gold(cost) + " or (pièces et compte réunis).";
            store.putComponent(ref, type, copy);
            p.getMembers().clear();
            p.setOwnerUUID(me);
            p.setOwnerName(pr.getUsername());
            p.setPurchaseType("BOUGHT");
            p.setForSale(false);
            p.setForRent(false);
            p.setGuildId(copy.hasGuild() && "CHEF".equalsIgnoreCase(copy.getGuildRole()) ? copy.getGuildId() : "");
            p.addMember(me, ParcelRole.OWNER);
            credit(seller, cost, "vente de la ville " + name(p) + " à " + pr.getUsername());
            String fam = p.getFamilyId();
            if (fam != null && !fam.isEmpty()) FamilyRegistry.log(FamilyManager.migrateId(fam), sellerName + " vend la ville " + name(p)
                    + " à " + pr.getUsername() + " : " + gold(cost) + " or");
            giveCityBorne(store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()), pr);
            ParcelManager.save();
            return null;
        }
        if (houseSale(p) || houseCity(p)) {                                      // vendue par une maison (2026-11-18)
            String sellerFam = FamilyManager.migrateId(p.getFamilyId());
            boolean keepHouse = houseCity(p);
            PlayerLevelData copy = (PlayerLevelData) d.clone();
            if (copy == null) return "Données introuvables.";
            if (!com.eldanior.system.quest.chapitres.Primes.payWithBank(store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()), copy, cost))
                return "Il faut " + gold(cost) + " or (pièces et compte réunis).";
            store.putComponent(ref, type, copy);
            String buyerFam = copy.getNobleFamilyId() != null ? FamilyManager.migrateId(copy.getNobleFamilyId()) : "";
            p.getMembers().clear();
            p.setOwnerUUID(me);
            p.setOwnerName(pr.getUsername());
            p.setPurchaseType("BOUGHT");
            p.setForSale(false);
            p.setForRent(false);
            if (!(keepHouse && sellerFam.equals(buyerFam))) p.setFamilyId(buyerFam);   // un membre de la maison la garde dans la maison
            p.addMember(me, ParcelRole.OWNER);
            FamilyManager.getRuntimeData(sellerFam).addTreasury(cost);
            FamilyRegistry.log(sellerFam, pr.getUsername() + " achète " + name(p) + " à la maison : " + gold(cost) + " or aux coffres");
            if (p.getType() == ParcelType.CITY) giveCityBorne(store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()), pr);
            ParcelManager.save();
            return null;
        }
        if (house != null) {                                                    // Duche libre : sa maison avec
            String err = com.eldanior.system.gui.tabs.FamilleTab.claim(house.getId(), ref, store);
            if (err != null) return err;
            FamilyRegistry.log(house.getId(), pr.getUsername() + " achète " + name(p) + " et prend la tête de la maison");
        } else {
            PlayerLevelData copy = (PlayerLevelData) d.clone();
            if (copy == null) return "Données introuvables.";
            if (!com.eldanior.system.quest.chapitres.Primes.payWithBank(store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()), copy, cost))
                return "Il faut " + gold(cost) + " or (pièces et compte réunis).";
            store.putComponent(ref, type, copy);
            p.getMembers().clear();
            p.setOwnerUUID(me);
            p.setOwnerName(pr.getUsername());
            p.setPurchaseType("BOUGHT");
            p.setForSale(false);
            p.setForRent(false);
            p.addMember(me, ParcelRole.OWNER);
            if (p.getType() == ParcelType.CITY && copy.hasGuild() && "CHEF".equalsIgnoreCase(copy.getGuildRole()))
                p.setGuildId(copy.getGuildId());                                  // ville de sa guilde, comme avant
        }
        ParcelData seller = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
        if (seller != null) seller.addTreasury(cost);                           // le prix au tresor de la zone qui la vendait
        if (p.getType() == ParcelType.CITY) giveCityBorne(store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()), pr);
        ParcelManager.save();
        return null;
    }

    /** Verse amount sur le compte du joueur u : tout de suite s'il est connecte, sinon a sa prochaine connexion. */
    public static void credit(UUID u, long amount, String reason) {
        if (u == null || amount <= 0) return;
        PlayerRef pr = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(u);
        Ref<EntityStore> r = pr != null ? pr.getReference() : null;
        if (r == null || !r.isValid()) { ParcelManager.addPendingEarnings(u, amount); return; }
        r.getStore().getExternalData().getWorld().execute(() -> {
            if (!r.isValid()) { ParcelManager.addPendingEarnings(u, amount); return; }
            var type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = r.getStore().getComponent(r, type);
            PlayerLevelData copy = data != null ? (PlayerLevelData) data.clone() : null;
            if (copy == null) { ParcelManager.addPendingEarnings(u, amount); return; }
            copy.addMoney(amount);
            r.getStore().putComponent(r, type, copy);
            pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("+" + gold(amount) + " or sur votre compte (" + reason + ").").color("#00e7b6"));
        });
    }

    /** A l'achat d'une ville : sa borne (2026-11-19), a poser dans la ville. */
    private static void giveCityBorne(com.hypixel.hytale.server.core.entity.entities.Player player, PlayerRef pr) {
        if (player == null || com.eldanior.system.guards.CityBornes.carries(player)) return;
        if (com.eldanior.system.guards.CityBornes.give(player))
            pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("Vous recevez la borne de votre ville : posez-la dans la ville, elle ouvre "
                    + "son registre (aussi : /es ville).").color("#7fd4ff"));
    }

    public static String name(ParcelData p) {
        return p.getName() != null ? p.getName().replace('_', ' ') : p.getId();
    }

    static String gold(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }
}
