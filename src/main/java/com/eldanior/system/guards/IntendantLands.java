package com.eldanior.system.guards;

import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

/**
 * Terres sous la tutelle d'une maison, vues par son intendant (2026-11-18, 3e passe : « une vision d'ensemble et le detail de
 * chaque territoire sous notre tutelle, Grand Territoire > Territoire > Villes > etc., avec la population ») :
 *  - racines : les domaines de la maison qui n'ont pas de parent dans la maison (Royaume Eldanior, Grand Territoire Luo…) ;
 *  - tout ce qui est dessous est sous tutelle, meme ce qui porte les couleurs d'une autre maison (en lecture) ;
 *  - population d'une terre (sous-arbre compris) : habitants (proprietaires, locataires, membres inscrits sur une zone),
 *    PNJ (postes places dans ses limites, par genre) et joueurs presents ;
 *  - vassaux : clans de l'Empire Luo (Taxe Imperiale), Marquisats du Royaume d'Eldanior (Taxe Royale).
 */
final class IntendantLands {

    private IntendantLands() {}

    static final List<ParcelType> DOMAIN_TYPES = List.of(ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY, ParcelType.CITY);

    /** Maison d'une zone (id migre, "" sans maison). */
    static String house(ParcelData p) {
        return p == null || p.getFamilyId().isEmpty() ? "" : FamilyManager.migrateId(p.getFamilyId());
    }

    /** Domaines de la maison sans parent dans la maison : sommets de l'arbre. */
    static List<ParcelData> roots(String familyId) {
        List<ParcelData> own = new ArrayList<>();
        for (ParcelType t : DOMAIN_TYPES)
            for (ParcelData p : ParcelManager.getByType(t)) if (familyId.equals(house(p))) own.add(p);
        Set<String> ids = new HashSet<>();
        for (ParcelData p : own) ids.add(p.getId());
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData p : own) if (!underAny(p, ids)) out.add(p);
        out.sort(BY_RANK_THEN_NAME);
        return out;
    }

    private static boolean underAny(ParcelData p, Set<String> ids) {
        Set<String> seen = new HashSet<>();
        for (ParcelData a = parentOf(p); a != null && seen.add(a.getId()); a = parentOf(a))
            if (ids.contains(a.getId())) return true;
        return false;
    }

    /**
     * Parent dans l'arbre des domaines (2026-11-18, retour de l'utilisateur : « une ville du clan Sama hors de son territoire
     * apparait dans le Marquisat Luo, il manque un liant ») : un domaine d'une maison qui n'est sous aucune terre de sa maison
     * (ville batie hors des limites du territoire du clan) se range sous la terre principale de sa maison, si elle est d'un rang
     * plus eleve. Sinon : le parent geographique. Une ville sans territoire de sa maison reste une ville du Marquisat.
     */
    static ParcelData parentOf(ParcelData p) {
        if (p == null) return null;
        ParcelData geo = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
        if (!isDomain(p)) return geo;
        String h = house(p);
        if (h.isEmpty()) return geo;
        Set<String> seen = new HashSet<>();
        for (ParcelData a = geo; a != null && seen.add(a.getId()); a = a.getParentId() != null ? ParcelManager.get(a.getParentId()) : null)
            if (h.equals(house(a))) return geo;                                       // deja dans les terres de sa maison
        ParcelData main = ParcelManager.getFamilyParcel(h);
        if (main == null || main.getId().equals(p.getId()) || !isDomain(main) || main.getType().ordinal() >= p.getType().ordinal()) return geo;
        return main;
    }

    private static Map<String, List<ParcelData>> index = Map.of();
    private static long indexAt;

    /** Enfants de chaque zone selon parentOf (recalcule au plus une fois par seconde). */
    private static synchronized Map<String, List<ParcelData>> index() {
        long now = System.currentTimeMillis();
        if (now - indexAt < 1000) return index;
        Map<String, List<ParcelData>> m = new HashMap<>();
        for (ParcelData q : ParcelManager.getAll()) {
            ParcelData par = parentOf(q);
            if (par != null) m.computeIfAbsent(par.getId(), k -> new ArrayList<>()).add(q);
        }
        index = m;
        indexAt = now;
        return m;
    }

    static final Comparator<ParcelData> BY_RANK_THEN_NAME = Comparator.comparingInt((ParcelData p) -> p.getType().ordinal())
            .thenComparing(p -> p.getName() != null ? p.getName() : p.getId(), String.CASE_INSENSITIVE_ORDER);

    /** Zones directement sous celle-ci (domaines d'abord, puis biens). */
    static List<ParcelData> children(ParcelData p) {
        List<ParcelData> out = new ArrayList<>();
        if (p == null) return out;
        out.addAll(index().getOrDefault(p.getId(), List.of()));
        out.sort(BY_RANK_THEN_NAME);
        return out;
    }

    /** Domaines directement sous celle-ci, sans les biens (2026-11-18 : les biens restent dans l'onglet Biens). */
    static List<ParcelData> domainChildren(ParcelData p) {
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData c : children(p)) if (isDomain(c)) out.add(c);
        return out;
    }

    /** La zone et tout ce qui est dessous. */
    static List<ParcelData> subtree(ParcelData p) {
        List<ParcelData> out = new ArrayList<>();
        Deque<ParcelData> todo = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        if (p != null) todo.add(p);
        while (!todo.isEmpty()) {
            ParcelData c = todo.poll();
            if (!seen.add(c.getId())) continue;
            out.add(c);
            todo.addAll(index().getOrDefault(c.getId(), List.of()));
        }
        return out;
    }

    /** La zone est-elle sous l'une des racines (ou l'une d'elles) ? */
    static boolean inTutelle(ParcelData p, List<ParcelData> roots) {
        if (p == null) return false;
        Set<String> ids = new HashSet<>();
        for (ParcelData r : roots) ids.add(r.getId());
        return ids.contains(p.getId()) || underAny(p, ids);
    }

    /** Chemin depuis la racine de la tutelle jusqu'a p (racine en tete). */
    static List<ParcelData> path(ParcelData p, List<ParcelData> roots) {
        LinkedList<ParcelData> out = new LinkedList<>();
        Set<String> rootIds = new HashSet<>();
        for (ParcelData r : roots) rootIds.add(r.getId());
        Set<String> seen = new HashSet<>();
        for (ParcelData c = p; c != null && seen.add(c.getId()); c = parentOf(c)) {
            out.addFirst(c);
            if (rootIds.contains(c.getId())) break;
        }
        return out;
    }

    static boolean isDomain(ParcelData p) { return p != null && DOMAIN_TYPES.contains(p.getType()); }

    // ==================== POPULATION ====================

    record Pop(int residents, int owners, int renters, int npcs, Map<String, Integer> npcByKind, int present,
               int domains, int estates, int forSale, int forRent, int occupied, long treasury, long tradeWeek, int taxReady) {
        int total() { return residents + npcs; }
    }

    /** Population et bilan de la zone et de tout ce qui est dessous. present : joueurs du monde de store qui s'y trouvent. */
    static Pop population(ParcelData root, Store<EntityStore> store) {
        Set<UUID> residents = new HashSet<>(), owners = new HashSet<>(), renters = new HashSet<>();
        int domains = 0, estates = 0, forSale = 0, forRent = 0, occupied = 0, taxReady = 0;
        long treasury = 0, trade = 0;
        for (ParcelData p : subtree(root)) {
            if (p.getOwnerUUID() != null) { owners.add(p.getOwnerUUID()); residents.add(p.getOwnerUUID()); }
            if (p.isRented() && p.getRenterUUID() != null) { renters.add(p.getRenterUUID()); residents.add(p.getRenterUUID()); }
            residents.addAll(p.getMembers().keySet());
            if (isDomain(p)) {
                domains++;
                treasury += p.getTreasury();
                trade += p.getTradeTaxWeek();
                if (com.eldanior.system.gui.tabs.TerritoiresTab.collectionRate(p) > 0 && !p.getChildIds().isEmpty() && p.canCollectTax()) taxReady++;
            } else if (p.getType() != ParcelType.ARENA && p.getType() != ParcelType.DUNGEON) {
                estates++;
                boolean rented = p.isRented() && p.getRenterUUID() != null;
                if (p.isForSale() && !rented) forSale++;
                else if (p.isForRent() && !rented) forRent++;
                else if (rented || p.getOwnerUUID() != null) occupied++;
            }
        }
        Map<String, Integer> kinds = new LinkedHashMap<>();
        int npcs = 0;
        if (root != null)
            for (GuardManager.Post post : GuardManager.getPosts()) {
                if (post.world == null || !post.world.equals(root.getWorld()) || !root.contains(post.x, post.y, post.z)) continue;
                npcs++;
                kinds.merge(kindLabel(post.type), 1, Integer::sum);
            }
        int present = 0;
        if (root != null && store != null)
            for (PlayerRef pr : Universe.get().getPlayers()) {
                var r = pr.getReference();
                if (r == null || !r.isValid() || r.getStore() != store) continue;
                TransformComponent t = store.getComponent(r, TransformComponent.getComponentType());
                if (t != null && root.contains(t.getPosition().x, t.getPosition().y, t.getPosition().z)) present++;
            }
        return new Pop(residents.size(), owners.size(), renters.size(), npcs, kinds, present, domains, estates, forSale, forRent,
                occupied, treasury, trade, taxReady);
    }

    private static String kindLabel(GuardType t) {
        if (t == null) return "Autres";
        return switch (t.getKind()) {
            case GUARD -> "Gardes";
            case VILLAGER -> "Habitants";
            case SERVICE -> "Marchands et maîtres";
            case ADVENTURER -> "Aventuriers";
            case HUNTER -> "Chasseurs de prime";
            case CRIMINAL -> "Criminels";
            case ROAMER -> "Errants";
        };
    }

    // ==================== VASSAUX ====================

    /** Taux du tribut des vassaux (Taxe Imperiale de l'Empire Luo, Taxe Royale du Royaume d'Eldanior) : 10 % par semaine. */
    static final double TRIBUTE_RATE = 0.10;

    static boolean hasVassals(String familyId) { return "luo".equals(familyId) || "eldanior".equals(familyId); }

    static String tributeName(String familyId) { return "eldanior".equals(familyId) ? "Taxe Royale" : "Taxe Impériale"; }

    /**
     * Domaines vassaux qui doivent le tribut : pour Luo, ceux des quatre clans ; pour Eldanior, les Grands Territoires
     * (Marquisats) des autres maisons dans ses royaumes.
     */
    static List<ParcelData> vassals(String familyId) {
        List<ParcelData> out = new ArrayList<>();
        if ("luo".equals(familyId)) {
            Set<String> clans = new HashSet<>();
            for (var c : com.eldanior.system.luo.LuoClans.Clan.values()) clans.add(c.id);
            for (ParcelType t : DOMAIN_TYPES)
                for (ParcelData p : ParcelManager.getByType(t)) if (clans.contains(house(p))) out.add(p);
        } else if ("eldanior".equals(familyId)) {
            List<ParcelData> kingdoms = new ArrayList<>();
            for (ParcelData k : ParcelManager.getByType(ParcelType.KINGDOM)) if (familyId.equals(house(k))) kingdoms.add(k);
            for (ParcelData p : ParcelManager.getByType(ParcelType.GRAND_TERRITORY))
                if (!familyId.equals(house(p)) && inTutelle(p, kingdoms)) out.add(p);
        }
        out.sort(BY_RANK_THEN_NAME);
        return out;
    }

    static String houseName(ParcelData p) {
        String h = house(p);
        if (h.isEmpty()) return "sans maison";
        var f = FamilyManager.get(h);
        return f != null ? f.getDisplayName() : h;
    }
}
