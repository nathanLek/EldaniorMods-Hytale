package com.eldanior.system.guards;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Statut d'un aventurier (ordinaire, exceptionnel, heros), tire de la graine de son poste (toujours le meme). Un exceptionnel
 * ou un heros a une classe de son type : ses passifs s'ajoutent aux siens (AdventurerPassives), ses bonus de stats le
 * renforcent. Plus fort, plus resistant, plus cher.
 */
public enum AdventurerTier {
    ORDINAIRE("Aventurier", 1f, 1f, 1f, List.of(Rarity.COMMON, Rarity.RARE)),
    EXCEPTIONNEL("Exceptionnel", 1.3f, 1.6f, 2.5f, List.of(Rarity.EPIC, Rarity.UNIQUE)),
    HEROS("Héros", 1.7f, 2.5f, 6f, List.of(Rarity.UNIQUE, Rarity.LEGENDARY));

    private final String label;
    private final float power, toughness, priceFactor;
    private final List<Rarity> classRarities;

    AdventurerTier(String label, float power, float toughness, float priceFactor, List<Rarity> classRarities) {
        this.label = label; this.power = power; this.toughness = toughness;
        this.priceFactor = priceFactor; this.classRarities = classRarities;
    }

    public String getLabel() { return label; }
    public float getPriceFactor() { return priceFactor; }

    /** Chances (sur 100) d'etre exceptionnel / heros selon le rang : E, D, C, B, A, S. */
    private static final int[] EXCEPTIONAL = {6, 7, 9, 11, 13, 15};
    private static final int[] HERO = {0, 1, 2, 3, 5, 8};

    private record Status(AdventurerTier tier, ClassModel cls) {}

    private static final Map<String, Status> CACHE = new ConcurrentHashMap<>();

    private static Status status(GuardManager.Post p) {
        return CACHE.computeIfAbsent(p.id + ":" + p.level, k -> {
            int rank = Math.max(0, "EDCBAS".indexOf(getAdventureRank(p.level)));
            Random rng = new Random(p.skinSeed * 17 + 3);
            int roll = rng.nextInt(100);
            AdventurerTier tier = roll < HERO[rank] ? HEROS : roll < HERO[rank] + EXCEPTIONAL[rank] ? EXCEPTIONNEL : ORDINAIRE;
            List<ClassModel> stage = classesForLevel(classType(AdventurerType.of(p.skinSeed)), p.level);
            if (stage.isEmpty()) return new Status(tier, null);
            List<ClassModel> pool = new ArrayList<>();
            for (ClassModel c : stage) if (tier.classRarities.contains(c.getRarity())) pool.add(c);
            if (pool.isEmpty()) pool = stage;                    // aucune classe de cette rarete a ce palier : une du palier
            return new Status(tier, pool.get(rng.nextInt(pool.size())));
        });
    }

    /**
     * Classes qu'un joueur de ce type et de ce niveau pourrait avoir : classe de base avant 180, une de ses evolutions de 180
     * a 399, une evolution de celles-ci a partir de 400 (chaines nextClassId du ClassManager).
     */
    private static List<ClassModel> classesForLevel(ClassType type, int level) {
        Map<String, ClassModel> byId = new HashMap<>();
        for (ClassModel c : ClassManager.getAll()) if (!c.isAdminAccess()) byId.put(c.getId(), c);
        List<ClassModel> stage = new ArrayList<>();
        for (ClassModel c : byId.values()) if (c.getType() == type && c.getPromotionLevel() == 180) stage.add(c);   // classe de base
        for (int step : new int[]{180, 400}) {
            if (level < step) break;
            List<ClassModel> next = new ArrayList<>();
            for (ClassModel c : stage)
                for (String id : c.getNextClassId()) {
                    ClassModel n = byId.get(id);
                    if (n != null && n.getType() == type && !next.contains(n)) next.add(n);
                }
            if (next.isEmpty()) break;
            stage = next;
        }
        stage.sort(Comparator.comparing(ClassModel::getId));                               // meme tirage a chaque demarrage
        return stage;
    }

    private static ClassType classType(AdventurerType t) {
        return switch (t) {
            case GUERRIER -> ClassType.WARRIOR;
            case MAGE -> ClassType.MAGE;
            case ARCHER -> ClassType.ARCHER;
            case ASSASSIN -> ClassType.ASSASSIN;
        };
    }

    /** Statut de l'aventurier de ce poste. */
    public static AdventurerTier of(GuardManager.Post p) { return status(p).tier(); }

    /** Classe de l'aventurier (null : ordinaire). */
    public static ClassModel classOf(GuardManager.Post p) { return status(p).cls(); }

    /** Multiplicateur de ses degats : statut, et stat principale de sa classe. */
    public static float power(GuardManager.Post p) {
        Status s = status(p);
        if (s.cls() == null) return s.tier().power;
        int main = switch (AdventurerType.of(p.skinSeed)) {
            case GUERRIER -> s.cls().getBonusStr();
            case MAGE -> s.cls().getBonusInt();
            case ARCHER, ASSASSIN -> s.cls().getBonusAgl();
        };
        return s.tier().power * (1f + Math.max(0, main) / 200f);
    }

    /** Diviseur des degats qu'il subit : statut, vitalite et endurance de sa classe. */
    public static float toughness(GuardManager.Post p) {
        Status s = status(p);
        if (s.cls() == null) return s.tier().toughness;
        return s.tier().toughness * (1f + Math.max(0, s.cls().getBonusVit() + s.cls().getBonusEnd()) / 200f);
    }
}